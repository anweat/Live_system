package common.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import common.bean.Recharge;
import common.logger.TraceLogger;
import common.repository.RechargeRepository;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 打赏Service - 统一的打赏数据访问接口
 * 所有其他模块必须通过此Service来操作打赏数据
 * 支持幂等性控制、缓存和批量操作
 */
@Slf4j
@Service
public class RechargeService extends BaseService<Recharge, Long, RechargeRepository> {

    public RechargeService(RechargeRepository repository) {
        super(repository);
    }

    @Override
    protected String getCachePrefix() {
        return "recharge::";
    }

    @Override
    protected String getEntityName() {
        return "Recharge";
    }

    /**
     * 按traceId查询（幂等性控制，防重复）
     */
    @Cacheable(value = "recharge::traceId", key = "#traceId", unless = "#result == null")
    @Transactional(readOnly = true)
    public Optional<Recharge> findByTraceId(String traceId) {
        if (traceId == null || traceId.isEmpty()) {
            TraceLogger.warn("Recharge", "findByTraceId", "TraceId为空");
            return Optional.empty();
        }
        TraceLogger.info("Recharge", "findByTraceId", "查询traceId: " + traceId);
        return repository.findByTraceId(traceId);
    }

    /**
     * 按主播ID查询所有打赏（带缓存）
     */
    @Cacheable(value = "recharge::anchorId", key = "#anchorId")
    @Transactional(readOnly = true)
    public List<Recharge> findByAnchorId(Long anchorId) {
        if (anchorId == null) {
            return List.of();
        }
        TraceLogger.info("Recharge", "findByAnchorId", "查询主播ID: " + anchorId);
        return repository.findByAnchorId(anchorId);
    }

    /**
     * 按观众ID查询所有打赏
     */
    @Cacheable(value = "recharge::audienceId", key = "#audienceId")
    @Transactional(readOnly = true)
    public List<Recharge> findByAudienceId(Long audienceId) {
        TraceLogger.info("Recharge", "findByAudienceId", "查询观众ID: " + audienceId);
        return repository.findByAudienceId(audienceId);
    }

    /**
     * 按直播间ID查询所有打赏
     */
    @Cacheable(value = "recharge::liveRoomId", key = "#liveRoomId")
    @Transactional(readOnly = true)
    public List<Recharge> findByLiveRoomId(Long liveRoomId) {
        TraceLogger.info("Recharge", "findByLiveRoomId", "查询直播间ID: " + liveRoomId);
        return repository.findByLiveRoomId(liveRoomId);
    }

    /**
     * 按状态查询打赏（待结算、已结算等）
     */
    @Cacheable(value = "recharge::status", key = "#status")
    @Transactional(readOnly = true)
    public List<Recharge> findByStatus(Integer status) {
        TraceLogger.info("Recharge", "findByStatus", "查询状态: " + status);
        return repository.findByStatus(status);
    }

    /**
     * 查询时间范围内的打赏记录
     */
    @Transactional(readOnly = true)
    public List<Recharge> findByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("时间范围不能为空");
        }
        TraceLogger.info("Recharge", "findByTimeRange", 
            String.format("查询时间范围: %s - %s", startTime, endTime));
        return repository.findByTimeRange(startTime, endTime);
    }

    /**
     * 计算主播在时间范围内的打赏总额
     */
    @Transactional(readOnly = true)
    public BigDecimal sumRechargeByAnchorAndTime(Long anchorId, LocalDateTime startTime, LocalDateTime endTime) {
        if (anchorId == null || startTime == null || endTime == null) {
            throw new IllegalArgumentException("参数不完整");
        }
        TraceLogger.info("Recharge", "sumRechargeByAnchorAndTime", 
            String.format("计算主播%d的打赏总额: %s - %s", anchorId, startTime, endTime));
        return repository.sumRechargeAmountByAnchorAndTimeRange(anchorId, startTime, endTime);
    }

    /**
     * 计算观众的总打赏金额
     */
    @Transactional(readOnly = true)
    public BigDecimal sumRechargeByAudience(Long audienceId) {
        if (audienceId == null) {
            throw new IllegalArgumentException("观众ID不能为空");
        }
        TraceLogger.info("Recharge", "sumRechargeByAudience", "计算观众" + audienceId + "的打赏总额");
        return repository.sumRechargeAmountByAudience(audienceId);
    }

    /**
     * 计算主播的总打赏金额
     */
    @Transactional(readOnly = true)
    public BigDecimal sumRechargeByAnchor(Long anchorId) {
        if (anchorId == null) {
            throw new IllegalArgumentException("主播ID不能为空");
        }
        TraceLogger.info("Recharge", "sumRechargeByAnchor", "计算主播" + anchorId + "的打赏总额");
        return repository.sumRechargeAmountByAnchor(anchorId);
    }

    /**
     * 计算主播的打赏笔数
     */
    @Transactional(readOnly = true)
    public Long countRechargeByAnchor(Long anchorId) {
        if (anchorId == null) {
            throw new IllegalArgumentException("主播ID不能为空");
        }
        return repository.countRechargeByAnchor(anchorId);
    }

    /**
     * 检查traceId是否存在（防重复）
     */
    @Transactional(readOnly = true)
    public boolean existsByTraceId(String traceId) {
        return repository.existsByTraceId(traceId);
    }

    /**
     * 创建打赏记录（带幂等性控制）
     */
    @CacheEvict(value = {"recharge::traceId", "recharge::anchorId", "recharge::audienceId", 
                         "recharge::liveRoomId", "recharge::status"}, allEntries = true)
    @Transactional
    public Recharge createRecharge(Recharge recharge) {
        if (recharge == null || recharge.getTraceId() == null) {
            throw new IllegalArgumentException("打赏信息不完整");
        }
        
        // 检查幂等性
        if (existsByTraceId(recharge.getTraceId())) {
            TraceLogger.warn("Recharge", "createRecharge", "重复的traceId: " + recharge.getTraceId());
            return repository.findByTraceId(recharge.getTraceId()).orElse(null);
        }
        
        TraceLogger.info("Recharge", "createRecharge", 
            String.format("创建打赏: 主播%d, 观众%d, 金额%s", 
                recharge.getAnchorId(), recharge.getAudienceId(), recharge.getRechargeAmount()));
        return repository.save(recharge);
    }

    /**
     * 更新打赏记录（如结算状态变更）
     */
    @CacheEvict(value = {"recharge::traceId", "recharge::anchorId", "recharge::audienceId", 
                         "recharge::liveRoomId", "recharge::status"}, allEntries = true)
    @Transactional
    public Recharge updateRecharge(Recharge recharge) {
        if (recharge == null || recharge.getRechargeId() == null) {
            throw new IllegalArgumentException("打赏信息不完整");
        }
        TraceLogger.info("Recharge", "updateRecharge", "更新打赏ID: " + recharge.getRechargeId());
        return repository.save(recharge);
    }

    /**
     * 批量查询未结算的打赏
     */
    @Transactional(readOnly = true)
    public List<Recharge> findUnsettledRechargeBatch(List<Long> anchorIds) {
        if (anchorIds == null || anchorIds.isEmpty()) {
            return List.of();
        }
        TraceLogger.info("Recharge", "findUnsettledRechargeBatch", 
            String.format("查询未结算打赏，主播数: %d", anchorIds.size()));
        return repository.findUnsettledRechargesByAnchors(anchorIds);
    }

    /**
     * 批量保存打赏记录
     */
    @CacheEvict(value = {"recharge::traceId", "recharge::anchorId", "recharge::audienceId", 
                         "recharge::liveRoomId", "recharge::status"}, allEntries = true)
    @Transactional
    public List<Recharge> batchSaveRecharges(List<Recharge> recharges) {
        if (recharges == null || recharges.isEmpty()) {
            TraceLogger.warn("Recharge", "batchSaveRecharges", "保存列表为空");
            return List.of();
        }
        TraceLogger.info("Recharge", "batchSaveRecharges", "批量保存打赏，条数: " + recharges.size());
        return repository.saveAll(recharges);
    }

    /**
     * 删除打赏记录
     */
    @CacheEvict(value = {"recharge::traceId", "recharge::anchorId", "recharge::audienceId", 
                         "recharge::liveRoomId", "recharge::status"}, allEntries = true)
    @Transactional
    public void deleteRecharge(Long rechargeId) {
        if (rechargeId == null) {
            throw new IllegalArgumentException("打赏ID不能为空");
        }
        TraceLogger.info("Recharge", "deleteRecharge", "删除打赏ID: " + rechargeId);
        repository.deleteById(rechargeId);
    }
}
