package com.liveroom.finance.service;

import com.liveroom.finance.dto.BatchRechargeDTO;
import com.liveroom.finance.vo.BalanceVO;
import com.liveroom.finance.vo.SettlementDetailVO;
import common.bean.RechargeRecord;
import common.bean.Settlement;
import common.bean.SettlementDetail;
import common.constant.ErrorConstants;
import common.constant.StatusConstants;
import common.exception.BusinessException;
import common.logger.TraceLogger;
import common.repository.RechargeRecordRepository;
import common.repository.SettlementDetailRepository;
import common.repository.SettlementRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 结算服务
 * 处理打赏结算、余额查询等核心业务
 */
@Service
@Slf4j
public class SettlementService {

    @Autowired
    private SettlementRepository settlementRepository;

    @Autowired
    private SettlementDetailRepository settlementDetailRepository;

    @Autowired
    private RechargeRecordRepository rechargeRecordRepository;

    @Autowired
    private CommissionRateService commissionRateService;

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String BALANCE_CACHE_KEY = "finance:balance:";
    private static final int CACHE_EXPIRE_MINUTES = 10;

    /**
     * 调度结算任务（异步处理）
     */
    @Transactional(rollbackFor = Exception.class)
    public void scheduleSettlement(List<BatchRechargeDTO.RechargeItemDTO> recharges) {
        // 按主播ID分组
        Map<Long, List<BatchRechargeDTO.RechargeItemDTO>> anchorRecharges = recharges.stream()
                .collect(Collectors.groupingBy(BatchRechargeDTO.RechargeItemDTO::getAnchorId));

        for (Map.Entry<Long, List<BatchRechargeDTO.RechargeItemDTO>> entry : anchorRecharges.entrySet()) {
            Long anchorId = entry.getKey();
            List<BatchRechargeDTO.RechargeItemDTO> anchorRechargeList = entry.getValue();

            try {
                settleForAnchor(anchorId, anchorRechargeList);
            } catch (Exception e) {
                TraceLogger.error("SettlementService", "scheduleSettlement",
                         anchorId, e);
                     }
                 }
             }


    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "balance", key = "#anchorId")
    public void settleForAnchor(Long anchorId, List<BatchRechargeDTO.RechargeItemDTO> recharges) {
        if (recharges == null || recharges.isEmpty()) {
            return;
        }

        TraceLogger.info("SettlementService", "settleForAnchor",
                "开始结算主播，主播ID: " + anchorId + ", 记录数: " + recharges.size());

        // 1. 查询主播未结算的打赏记录
        List<RechargeRecord> unsettledRecords = rechargeRecordRepository.findUnsettledRecordsByAnchor(anchorId);
        if (unsettledRecords.isEmpty()) {
            TraceLogger.info("SettlementService", "settleForAnchor",
                    "主播无待结算记录，主播ID: " + anchorId);
            return;
        }

        // 2. 获取主播当前分成比例
        BigDecimal commissionRate = commissionRateService.getCurrentCommissionRate(anchorId) != null
                ? commissionRateService.getCurrentCommissionRate(anchorId).getCommissionRate()
                : new BigDecimal("70.0"); // 默认70%

        // 3. 计算总金额
        BigDecimal totalAmount = unsettledRecords.stream()
                .map(RechargeRecord::getRechargeAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. 计算结算金额
        BigDecimal settlementAmount = totalAmount
                .multiply(commissionRate)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        // 5. 更新或创建主播结算记录
        Settlement settlement = settlementRepository.findByAnchorId(anchorId)
                .orElse(Settlement.builder()
                        .anchorId(anchorId)
                        .anchorName(unsettledRecords.get(0).getAnchorName())
                        .settlementAmount(BigDecimal.ZERO)
                        .withdrawnAmount(BigDecimal.ZERO)
                        .availableAmount(BigDecimal.ZERO)
                        .settlementCycle(1)
                        .status(0)
                        .createTime(LocalDateTime.now())
                        .build());

        settlement.setSettlementAmount(settlement.getSettlementAmount().add(settlementAmount));
        settlement.setAvailableAmount(settlement.getSettlementAmount().subtract(settlement.getWithdrawnAmount()));
        settlement.setLastSettlementTime(LocalDateTime.now());
        settlement.setUpdateTime(LocalDateTime.now());
        settlementRepository.save(settlement);

        // 6. 创建结算明细
        SettlementDetail detail = SettlementDetail.builder()
                .settlementId(settlement.getSettlementId())
                .anchorId(anchorId)
                .totalRechargeAmount(totalAmount)
                .commissionRate(commissionRate)
                .settlementAmount(settlementAmount)
                .settlementStartTime(unsettledRecords.stream()
                        .map(RechargeRecord::getRechargeTime)
                        .min(LocalDateTime::compareTo)
                        .orElse(LocalDateTime.now()))
                .settlementEndTime(LocalDateTime.now())
                .rechargeCount(unsettledRecords.size())
                .status(0)
                .createTime(LocalDateTime.now())
                .build();
        settlementDetailRepository.save(detail);

        // 7. 更新RechargeRecord的结算状态
        List<Long> recordIds = unsettledRecords.stream()
                .map(RechargeRecord::getRecordId)
                .collect(Collectors.toList());
        LocalDateTime now = LocalDateTime.now();
        rechargeRecordRepository.batchUpdateSettlementStatus(
                recordIds, 1, now, commissionRate.doubleValue(), settlementAmount, now);

        // 8. 清除缓存
        String cacheKey = BALANCE_CACHE_KEY + anchorId;
        redisTemplate.delete(cacheKey);
        statisticsService.clearAnchorStatisticsCache(anchorId);

        TraceLogger.info("SettlementService", "settleForAnchor",
                String.format("主播结算完成，主播ID: %d, 打赏总额: %s, 结算金额: %s, 记录数: %d",
                        anchorId, totalAmount, settlementAmount, unsettledRecords.size()));
    }

    /**
     * 查询主播余额（Redis缓存）
     */
    @Cacheable(value = "balance", key = "#anchorId", unless = "#result == null")
    public BalanceVO getAnchorBalance(Long anchorId) {
        // 1. 先从Redis缓存查询
        String cacheKey = BALANCE_CACHE_KEY + anchorId;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof BalanceVO) {
            TraceLogger.debug("SettlementService", "getAnchorBalance",
                    "从缓存获取余额，主播ID: " + anchorId);
            return (BalanceVO) cached;
        }

        // 2. 从数据库查询
        Settlement settlement = settlementRepository.findByAnchorId(anchorId)
                .orElseThrow(() -> new BusinessException(ErrorConstants.SETTLEMENT_NOT_FOUND, "主播结算记录不存在"));

        // 3. 获取当前分成比例
        BigDecimal currentRate = commissionRateService.getCurrentCommissionRate(anchorId) != null
                ? commissionRateService.getCurrentCommissionRate(anchorId).getCommissionRate()
                : null;

        // 4. 构建VO
        BalanceVO balanceVO = BalanceVO.builder()
                .anchorId(settlement.getAnchorId())
                .anchorName(settlement.getAnchorName())
                .settlementAmount(settlement.getSettlementAmount())
                .withdrawnAmount(settlement.getWithdrawnAmount())
                .availableAmount(settlement.getAvailableAmount())
                .currentCommissionRate(currentRate)
                .status(settlement.getStatus())
                .statusDesc(getStatusDesc(settlement.getStatus()))
                .lastSettlementTime(settlement.getLastSettlementTime())
                .nextSettlementTime(settlement.getNextSettlementTime())
                .queryTime(LocalDateTime.now())
                .build();

        // 5. 更新Redis缓存
        redisTemplate.opsForValue().set(cacheKey, balanceVO, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        return balanceVO;
    }

    /**
     * 查询结算明细
     */
    public Page<SettlementDetailVO> getSettlementDetails(Long anchorId, LocalDateTime startDate, 
                                                          LocalDateTime endDate, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<SettlementDetail> detailPage;

        if (startDate != null && endDate != null) {
            detailPage = settlementDetailRepository
                    .findByAnchorIdAndTimeRange(anchorId, startDate, endDate, pageable);
        } else {
            detailPage = settlementDetailRepository
                    .findByAnchorIdOrderBySettlementStartTimeDesc(anchorId, pageable);
        }

        return detailPage.map(this::convertToVO);
    }

    /**
     * 扣减可提取金额（提现时调用）
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "balance", key = "#anchorId")
    public void deductAvailableAmount(Long anchorId, BigDecimal amount) {
        // 使用悲观锁查询
        Settlement settlement = settlementRepository.findByAnchorIdWithLock(anchorId)
                .orElseThrow(() -> new BusinessException(ErrorConstants.SETTLEMENT_NOT_FOUND, "主播结算记录不存在"));

        // 检查状态
        if (settlement.getStatus() == 1) { // FROZEN
            throw new BusinessException(ErrorConstants.INSUFFICIENT_WITHDRAWAL_BALANCE, "账户已冻结，无法提现");
        }
        if (settlement.getStatus() == 2) { // FORBIDDEN
            throw new BusinessException(ErrorConstants.OPERATION_NOT_ALLOWED, "账户已禁止提现");
        }

        // 检查余额
        if (settlement.getAvailableAmount().compareTo(amount) < 0) {
            throw new BusinessException(ErrorConstants.INSUFFICIENT_WITHDRAWAL_BALANCE, "可提取余额不足");
        }

        // 扣减余额
        settlement.setAvailableAmount(settlement.getAvailableAmount().subtract(amount));
        settlement.setWithdrawnAmount(settlement.getWithdrawnAmount().add(amount));
        settlement.setUpdateTime(LocalDateTime.now());
        settlementRepository.save(settlement);

        // 清除缓存
        String cacheKey = BALANCE_CACHE_KEY + anchorId;
        redisTemplate.delete(cacheKey);

        TraceLogger.info("SettlementService", "deductAvailableAmount",
                "扣减可提取金额成功，主播ID: " + anchorId + ", 金额: " + amount);
    }

    /**
     * 转换为VO
     */
    private SettlementDetailVO convertToVO(SettlementDetail detail) {
        return SettlementDetailVO.builder()
                .detailId(detail.getDetailId())
                .anchorId(detail.getAnchorId())
                .totalRechargeAmount(detail.getTotalRechargeAmount())
                .commissionRate(detail.getCommissionRate().doubleValue())
                .settlementAmount(detail.getSettlementAmount())
                .settlementStartTime(detail.getSettlementStartTime())
                .settlementEndTime(detail.getSettlementEndTime())
                .rechargeCount(detail.getRechargeCount())
                .status(detail.getStatus())
                .statusDesc(getStatusDesc(detail.getStatus()))
                .remark(detail.getRemark())
                .createTime(detail.getCreateTime())
                .build();
    }

    /**
     * 获取状态描述
     */
    private String getStatusDesc(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "正常";
            case 1: return "冻结";
            case 2: return "禁提";
            default: return "未知";
        }
    }
}
