package com.liveroom.audience.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import common.bean.Recharge;
import common.constant.ErrorConstants;
import common.exception.BusinessException;
import common.exception.ValidationException;
import common.logger.TraceLogger;
import common.response.BaseResponse;
import common.service.DataAccessFacade;
import common.util.BeanUtil;
import common.util.DateTimeUtil;
import common.util.IdGeneratorUtil;
import common.util.TraceIdGenerator;
import com.liveroom.audience.dto.RechargeDTO;
import com.liveroom.audience.feign.AnchorServiceClient;
import com.liveroom.audience.vo.Top10AudienceVO;

/**
 * 打赏业务逻辑服务（Reward/Tip Service）
 * 
 * 重要说明：在本项目中，"Recharge" 表示 "打赏/礼物"，不是 "充值"
 * - createRecharge() = 创建打赏记录（观众给主播打赏）
 * 
 * 处理打赏请求、数据同步等核心业务
 *
 * 重构说明：已改为通过 DataAccessFacade 统一访问数据库
 */
@Service
@Slf4j
@Transactional
public class RechargeService {

    @Autowired
    private DataAccessFacade dataAccessFacade;

    @Autowired
    private AudienceService audienceService;

    @Autowired
    private AnchorServiceClient anchorServiceClient;

    // 内存同步包队列（待同步到财务服务的打赏记录）
    private final ConcurrentLinkedQueue<Recharge> syncQueue = new ConcurrentLinkedQueue<>();

    /**
     * 创建打赏记录（Reward/Tip）- 观众给主播打赏
     * 
     * 流程：
     * 1. 参数验证 + 幂等性检查
     * 2. 保存打赏记录到DB
     * 3. 异步通知主播服务和更新观众消费统计
     * 4. 加入同步队列（待同步到财务服务）
     * 5. 返回success（<200ms）
     *
     * 注意：此方法直接接受打赏金额，不进行余额检查
     * 实际业务中的支付验证应该在前端或网关层完成
     * 
     * @param rechargeDTO 打赏信息（包含主播ID、观众ID、打赏金额等）
     * @return 打赏记录
     */
    public RechargeDTO createRecharge(RechargeDTO rechargeDTO) {
        long startTime = System.currentTimeMillis();
        TraceLogger.info("RechargeService", "createRecharge", 
                "开始处理打赏请求: audienceId=" + rechargeDTO.getAudienceId() +
                ", amount=" + rechargeDTO.getRechargeAmount());

        try {
            // 1. 参数验证
            validateRechargeDTO(rechargeDTO);

            // 2. 获取或生成traceId
            String traceId = rechargeDTO.getTraceId();
            if (traceId == null || traceId.isEmpty()) {
                traceId = TraceIdGenerator.generate("audience-service");
                rechargeDTO.setTraceId(traceId);
            }

            // 3. 检查幂等性：traceId是否已存在
            Optional<Recharge> existing = dataAccessFacade.recharge().findByTraceId(traceId);
            if (existing.isPresent()) {
                TraceLogger.warn("RechargeService", "createRecharge", "重复的打赏请求: " + traceId);
                throw new BusinessException(ErrorConstants.DUPLICATE_RECHARGE, "该打赏请求已处理，请勿重复提交");
            }

            // 4. 创建Recharge实体并保存到数据库
            Recharge recharge = new Recharge();
            recharge.setRechargeId(IdGeneratorUtil.nextId());
            recharge.setLiveRoomId(rechargeDTO.getLiveRoomId());
            recharge.setAnchorId(rechargeDTO.getAnchorId());
            recharge.setAnchorName(rechargeDTO.getAnchorName());
            recharge.setAudienceId(rechargeDTO.getAudienceId());
            recharge.setAudienceNickname(rechargeDTO.getAudienceNickname());
            recharge.setRechargeAmount(rechargeDTO.getRechargeAmount());
            recharge.setRechargeTime(DateTimeUtil.now());
            recharge.setTraceId(traceId);
            recharge.setRechargeType(rechargeDTO.getRechargeType());
            recharge.setMessage(rechargeDTO.getMessage());
            recharge.setStatus(0);  // 0 = 已入账
            recharge.setCreateTime(DateTimeUtil.now());
            recharge.setUpdateTime(DateTimeUtil.now());

            Recharge savedRecharge = dataAccessFacade.recharge().createRecharge(recharge);
            TraceLogger.info("RechargeService", "createRecharge",
                    "打赏记录已保存到数据库: rechargeId=" + savedRecharge.getRechargeId());

            // 5. 异步通知主播服务更新直播间实时数据（降级处理）
            if (anchorServiceClient != null) {
                try {
                    TraceLogger.info("RechargeService", "createRecharge",
                            String.format("通知主播服务更新直播间数据: liveRoomId=%d, amount=%s",
                                    savedRecharge.getLiveRoomId(), savedRecharge.getRechargeAmount()));

                    BaseResponse<Void> response = anchorServiceClient.notifyReward(
                            savedRecharge.getLiveRoomId(),
                            savedRecharge.getAudienceId(),
                            savedRecharge.getRechargeAmount());

                    if (response != null && response.getCode() == 0) {
                        TraceLogger.info("RechargeService", "createRecharge",
                                "主播服务更新成功: liveRoomId=" + savedRecharge.getLiveRoomId());
                    } else {
                        TraceLogger.warn("RechargeService", "createRecharge",
                                "主播服务更新失败: " + (response != null ? response.getMessage() : "无响应"));
                    }
                } catch (Exception e) {
                    // 降级处理：主播服务调用失败不影响打赏记录保存
                    TraceLogger.error("RechargeService", "createRecharge",
                            "调用主播服务失败，打赏已记录但未实时更新直播间", e);
                }
            }

            // 6. 异步更新观众消费统计（降级处理）
            try {
                audienceService.updateConsumptionStats(rechargeDTO.getAudienceId(), rechargeDTO.getRechargeAmount());
                TraceLogger.debug("RechargeService", "createRecharge",
                        "观众消费统计已更新: audienceId=" + rechargeDTO.getAudienceId());
            } catch (Exception e) {
                TraceLogger.error("RechargeService", "createRecharge",
                    "更新观众消费统计失败: " + rechargeDTO.getAudienceId(), e);
                // 不影响打赏记录保存，继续处理
            }

            // 7. 添加到同步队列（待同步到财务服务）
            syncQueue.offer(savedRecharge);
            TraceLogger.debug("RechargeService", "createRecharge",
                    "打赏记录已加入同步队列，当前队列大小: " + syncQueue.size());

            long endTime = System.currentTimeMillis();
            TraceLogger.info("RechargeService", "createRecharge",
                String.format("打赏记录创建成功: rechargeId=%d, traceId=%s, 耗时=%dms",
                    savedRecharge.getRechargeId(), traceId, (endTime - startTime)));

            return BeanUtil.convert(savedRecharge, RechargeDTO.class);
        } catch (ValidationException | BusinessException e) {
            TraceLogger.warn("RechargeService", "createRecharge", "业务异常: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            TraceLogger.error("RechargeService", "createRecharge", "系统异常: 创建打赏记录失败", e);
            throw new BusinessException(ErrorConstants.SYSTEM_ERROR, "创建打赏记录失败", e);
        }
    }

    /**
     * 获取同步队列中的打赏记录（供同步任务调用）
     */
    public List<Recharge> pollSyncQueue(int batchSize) {
        List<Recharge> batch = new ArrayList<>();
        for (int i = 0; i < batchSize && !syncQueue.isEmpty(); i++) {
            Recharge recharge = syncQueue.poll();
            if (recharge != null) {
                batch.add(recharge);
            }
        }
        return batch;
    }

    /**
     * 获取同步队列大小
     */
    public int getSyncQueueSize() {
        return syncQueue.size();
    }

    /**
     * 获取打赏记录详情
     */
    public RechargeDTO getRecharge(Long rechargeId) {
        try {
            if (rechargeId == null || rechargeId <= 0) {
                throw new ValidationException("打赏ID不合法");
            }

            Optional<Recharge> optional = dataAccessFacade.recharge().findById(rechargeId);
            if (optional.isEmpty()) {
                throw new BusinessException(ErrorConstants.RECHARGE_NOT_FOUND, "打赏记录不存在");
            }
            return BeanUtil.convert(optional.get(), RechargeDTO.class);
        } catch (ValidationException | BusinessException e) {
            throw e;
        } catch (Exception e) {
            TraceLogger.error("RechargeService", "getRecharge", "系统异常: 查询打赏失败, id=" + rechargeId, e);
            throw new BusinessException(ErrorConstants.SYSTEM_ERROR, "查询打赏记录失败", e);
        }
    }

    /**
     * 按traceId查询打赏记录（用于查询重复）
     */
    public RechargeDTO getRechargeByTraceId(String traceId) {
        try {
            if (traceId == null || traceId.trim().isEmpty()) {
                throw new ValidationException("traceId不能为空");
            }

            return dataAccessFacade.recharge().findByTraceId(traceId)
                .map(r -> BeanUtil.convert(r, RechargeDTO.class))
                .orElse(null);
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            TraceLogger.error("RechargeService", "getRechargeByTraceId", "系统异常: 查询打赏失败, traceId=" + traceId, e);
            throw new BusinessException(ErrorConstants.SYSTEM_ERROR, "查询打赏记录失败", e);
        }
    }

    /**
     * 查询主播的打赏列表
     */
    public Page<RechargeDTO> listAnchorRecharges(Long anchorId, Integer page, Integer size) {
        try {
            if (anchorId == null || anchorId <= 0) {
                throw new ValidationException("主播ID不合法");
            }
            if (page == null || page < 1) {
                throw new ValidationException("页码必须从1开始");
            }
            if (size == null || size < 1 || size > 100) {
                throw new ValidationException("每页大小必须在1-100之间");
            }

            Pageable pageable = PageRequest.of(page - 1, size);
            Page<Recharge> recharges = dataAccessFacade.recharge().findByAnchorId(anchorId, pageable);
            return recharges.map(r -> BeanUtil.convert(r, RechargeDTO.class));
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            TraceLogger.error("RechargeService", "listAnchorRecharges", "系统异常: 查询主播打赏列表失败, anchorId=" + anchorId, e);
            throw new BusinessException(ErrorConstants.SYSTEM_ERROR, "查询打赏列表失败", e);
        }
    }

    /**
     * 查询观众的打赏历史
     */
    public Page<RechargeDTO> listAudienceRecharges(Long audienceId, Integer page, Integer size) {
        try {
            if (audienceId == null || audienceId <= 0) {
                throw new ValidationException("观众ID不合法");
            }
            if (page == null || page < 1) {
                throw new ValidationException("页码必须从1开始");
            }
            if (size == null || size < 1 || size > 100) {
                throw new ValidationException("每页大小必须在1-100之间");
            }

            Pageable pageable = PageRequest.of(page - 1, size);
            Page<Recharge> recharges = dataAccessFacade.recharge().findByAudienceId(audienceId, pageable);
            return recharges.map(r -> BeanUtil.convert(r, RechargeDTO.class));
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            TraceLogger.error("RechargeService", "listAudienceRecharges", "系统异常: 查询观众打赏历史失败, audienceId=" + audienceId, e);
            throw new BusinessException(ErrorConstants.SYSTEM_ERROR, "查询打赏列表失败", e);
        }
    }

    /**
     * 查询直播间的打赏列表
     */
    public Page<RechargeDTO> listLiveRoomRecharges(Long liveRoomId, Integer page, Integer size) {
        try {
            if (liveRoomId == null || liveRoomId <= 0) {
                throw new ValidationException("直播间ID不合法");
            }
            if (page == null || page < 1) {
                throw new ValidationException("页码必须从1开始");
            }
            if (size == null || size < 1 || size > 100) {
                throw new ValidationException("每页大小必须在1-100之间");
            }

            Pageable pageable = PageRequest.of(page - 1, size);
            Page<Recharge> recharges = dataAccessFacade.recharge().findByLiveRoomId(liveRoomId, pageable);
            return recharges.map(r -> BeanUtil.convert(r, RechargeDTO.class));
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            TraceLogger.error("RechargeService", "listLiveRoomRecharges", "系统异常: 查询直播间打赏列表失败, liveRoomId=" + liveRoomId, e);
            throw new BusinessException(ErrorConstants.SYSTEM_ERROR, "查询打赏列表失败", e);
        }
    }

    /**
     * 查询主播的TOP10打赏观众
     */
    public List<Top10AudienceVO> getTop10Audiences(Long anchorId, String period) {
        try {
            if (anchorId == null || anchorId <= 0) {
                throw new ValidationException("主播ID不合法");
            }
            if (period == null || period.trim().isEmpty()) {
                period = "all";
            }

            LocalDateTime startTime = getStartTimeByPeriod(period);
            LocalDateTime endTime = DateTimeUtil.now();

            // 查询时间范围内的所有打赏记录
            List<Recharge> recharges = dataAccessFacade.recharge().findTop10ByAnchorAndTimeRange(anchorId, startTime, endTime, PageRequest.of(0, 100))
                .stream()
                .collect(Collectors.toList());

            // 按观众ID分组统计
            return recharges.stream()
                .collect(Collectors.groupingBy(
                    Recharge::getAudienceId,
                    Collectors.reducing(
                        BigDecimal.ZERO,
                        Recharge::getRechargeAmount,
                        BigDecimal::add
                    )
                ))
                .entrySet()
                .stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(10)
                .map(entry -> Top10AudienceVO.builder()
                    .rank((int)(entry.getKey() % 10 + 1))
                    .audienceId(entry.getKey())
                    .totalRechargeAmount(entry.getValue())
                    .build()
                )
                .collect(Collectors.toList());
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            TraceLogger.error("RechargeService", "getTop10Audiences", "系统异常: 查询TOP10观众失败, anchorId=" + anchorId, e);
            throw new BusinessException(ErrorConstants.SYSTEM_ERROR, "查询TOP10观众失败", e);
        }
    }

    /**
     * 查询未同步的打赏记录
     */
    public List<RechargeDTO> listUnsyncedRecharges(Integer limit) {
        try {
            if (limit != null && (limit < 1 || limit > 1000)) {
                throw new ValidationException("查询数量必须在1-1000之间");
            }

            Pageable pageable = PageRequest.of(0, limit != null ? limit : 100);
            List<Recharge> recharges = dataAccessFacade.recharge().findUnsyncedRecharges(pageable);
            return recharges.stream()
                .map(r -> BeanUtil.convert(r, RechargeDTO.class))
                .collect(Collectors.toList());
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            TraceLogger.error("RechargeService", "listUnsyncedRecharges", "系统异常: 查询未同步打赏失败", e);
            throw new BusinessException(ErrorConstants.SYSTEM_ERROR, "查询未同步打赏失败", e);
        }
    }

    /**
     * 标记打赏为已同步
     */
    public void markRechargeAsSynced(Long rechargeId, Long settlementId) {
        try {
            if (rechargeId == null || rechargeId <= 0) {
                throw new ValidationException("打赏ID不合法");
            }
            if (settlementId == null || settlementId <= 0) {
                throw new ValidationException("结算ID不合法");
            }

            Recharge recharge = dataAccessFacade.recharge().findById(rechargeId)
                .orElseThrow(() -> new BusinessException(
                    ErrorConstants.RECHARGE_NOT_FOUND, "打赏记录不存在"));

            recharge.setStatus(1);  // 1 = 待结算
            recharge.setSettlementId(settlementId);
            recharge.setUpdateTime(DateTimeUtil.now());
            dataAccessFacade.recharge().updateRecharge(recharge);

            TraceLogger.info("RechargeService", "markRechargeAsSynced",
                "打赏记录标记为同步: " + rechargeId + ", settlementId: " + settlementId);
        } catch (ValidationException | BusinessException e) {
            throw e;
        } catch (Exception e) {
            TraceLogger.error("RechargeService", "markRechargeAsSynced", "系统异常: 标记打赏同步失败, id=" + rechargeId, e);
            throw new BusinessException(ErrorConstants.SYSTEM_ERROR, "标记打赏同步失败", e);
        }
    }

    /**
     * 参数验证
     */
    private void validateRechargeDTO(RechargeDTO dto) {
        if (dto == null) {
            throw new ValidationException("打赏信息不能为空");
        }

        if (dto.getLiveRoomId() == null || dto.getLiveRoomId() <= 0) {
            throw new ValidationException("直播间ID不合法");
        }

        if (dto.getAnchorId() == null || dto.getAnchorId() <= 0) {
            throw new ValidationException("主播ID不合法");
        }

        if (dto.getAudienceId() == null || dto.getAudienceId() <= 0) {
            throw new ValidationException("观众ID不合法");
        }

        if (dto.getRechargeAmount() == null || dto.getRechargeAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("打赏金额必须大于0");
        }

        if (dto.getRechargeAmount().compareTo(new BigDecimal("999999.99")) > 0) {
            throw new ValidationException("打赏金额超过限额");
        }

        if (dto.getRechargeType() == null) {
            throw new ValidationException("打赏类型不能为空");
        }
    }

    /**
     * 根据period获取开始时间
     */
    private LocalDateTime getStartTimeByPeriod(String period) {
        LocalDateTime now = DateTimeUtil.now();
        if ("day".equalsIgnoreCase(period)) {
            return now.toLocalDate().atStartOfDay();
        } else if ("week".equalsIgnoreCase(period)) {
            return now.minusWeeks(1);
        } else if ("month".equalsIgnoreCase(period)) {
            return now.minusMonths(1);
        } else {
            return LocalDateTime.of(2000, 1, 1, 0, 0, 0);  // all = 所有时间
        }
    }
}
