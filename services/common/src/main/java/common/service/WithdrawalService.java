package common.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import common.bean.Withdrawal;
import common.logger.TraceLogger;
import common.repository.WithdrawalRepository;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 提现Service - 统一的提现数据访问接口
 * 所有其他模块必须通过此Service来操作提现数据
 * 支持幂等性控制、防重复提交等
 */
@Slf4j
@Service
public class WithdrawalService extends BaseService<Withdrawal, Long, WithdrawalRepository> {

    public WithdrawalService(WithdrawalRepository repository) {
        super(repository);
    }

    @Override
    protected String getCachePrefix() {
        return "withdrawal::";
    }

    @Override
    protected String getEntityName() {
        return "Withdrawal";
    }

    /**
     * 按traceId查询（幂等性控制，防重复提交）
     */
    @Cacheable(value = "withdrawal::traceId", key = "#traceId", unless = "#result == null")
    @Transactional(readOnly = true)
    public Optional<Withdrawal> findByTraceId(String traceId) {
        if (traceId == null || traceId.isEmpty()) {
            TraceLogger.warn("Withdrawal", "findByTraceId", "TraceId为空");
            return Optional.empty();
        }
        TraceLogger.info("Withdrawal", "findByTraceId", "查询traceId: " + traceId);
        return repository.findByTraceId(traceId);
    }

    /**
     * 按主播ID查询所有提现记录
     */
    @Cacheable(value = "withdrawal::anchorId", key = "#anchorId")
    @Transactional(readOnly = true)
    public List<Withdrawal> findByAnchorId(Long anchorId) {
        if (anchorId == null) {
            return List.of();
        }
        TraceLogger.info("Withdrawal", "findByAnchorId", "查询主播ID: " + anchorId);
        return repository.findByAnchorId(anchorId);
    }

    /**
     * 按提现状态查询
     */
    @Cacheable(value = "withdrawal::status", key = "#status")
    @Transactional(readOnly = true)
    public List<Withdrawal> findByStatus(Integer status) {
        TraceLogger.info("Withdrawal", "findByStatus", "查询状态: " + status);
        return repository.findByStatus(status);
    }

    /**
     * 查询待处理的提现记录（申请中、处理中）
     */
    @Cacheable(value = "withdrawal::pending")
    @Transactional(readOnly = true)
    public List<Withdrawal> findPendingWithdrawals() {
        TraceLogger.info("Withdrawal", "findPendingWithdrawals", "查询待处理提现");
        return repository.findPendingWithdrawals();
    }

    /**
     * 查询时间范围内的提现记录
     */
    @Transactional(readOnly = true)
    public List<Withdrawal> findByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("时间范围不能为空");
        }
        TraceLogger.info("Withdrawal", "findByTimeRange", 
            String.format("查询时间范围: %s - %s", startTime, endTime));
        return repository.findByTimeRange(startTime, endTime);
    }

    /**
     * 计算主播的总成功提现金额
     */
    @Transactional(readOnly = true)
    public BigDecimal sumSuccessfulWithdrawalsByAnchor(Long anchorId) {
        if (anchorId == null) {
            throw new IllegalArgumentException("主播ID不能为空");
        }
        TraceLogger.info("Withdrawal", "sumSuccessfulWithdrawalsByAnchor", 
            "计算主播成功提现: " + anchorId);
        return repository.sumSuccessfulWithdrawalsByAnchor(anchorId);
    }

    /**
     * 计算主播申请中的总提现金额
     */
    @Transactional(readOnly = true)
    public BigDecimal sumPendingWithdrawalsByAnchor(Long anchorId) {
        if (anchorId == null) {
            throw new IllegalArgumentException("主播ID不能为空");
        }
        TraceLogger.info("Withdrawal", "sumPendingWithdrawalsByAnchor", 
            "计算主播待提现: " + anchorId);
        return repository.sumPendingWithdrawalsByAnchor(anchorId);
    }

    /**
     * 检查traceId是否存在（防重复）
     */
    @Transactional(readOnly = true)
    public boolean existsByTraceId(String traceId) {
        return repository.existsByTraceId(traceId);
    }

    /**
     * 批量查询主播的提现记录
     */
    @Transactional(readOnly = true)
    public List<Withdrawal> findByAnchorIds(List<Long> anchorIds) {
        if (anchorIds == null || anchorIds.isEmpty()) {
            return List.of();
        }
        TraceLogger.info("Withdrawal", "findByAnchorIds", "批量查询主播提现，数量: " + anchorIds.size());
        return repository.findByAnchorIds(anchorIds);
    }

    /**
     * 创建提现申请（带幂等性控制）
     */
    @CacheEvict(value = {"withdrawal::traceId", "withdrawal::anchorId", "withdrawal::status", 
                         "withdrawal::pending"}, allEntries = true)
    @Transactional
    public Withdrawal createWithdrawal(Withdrawal withdrawal) {
        if (withdrawal == null || withdrawal.getTraceId() == null) {
            throw new IllegalArgumentException("提现信息不完整");
        }
        
        // 检查幂等性
        if (existsByTraceId(withdrawal.getTraceId())) {
            TraceLogger.warn("Withdrawal", "createWithdrawal", "重复的traceId: " + withdrawal.getTraceId());
            return repository.findByTraceId(withdrawal.getTraceId()).orElse(null);
        }
        
        TraceLogger.info("Withdrawal", "createWithdrawal", 
            String.format("创建提现申请: 主播%d, 金额%s", 
                withdrawal.getAnchorId(), withdrawal.getWithdrawalAmount()));
        return repository.save(withdrawal);
    }

    /**
     * 更新提现申请（如状态变更：申请->处理->已打款）
     */
    @CacheEvict(value = {"withdrawal::traceId", "withdrawal::anchorId", "withdrawal::status", 
                         "withdrawal::pending"}, allEntries = true)
    @Transactional
    public Withdrawal updateWithdrawal(Withdrawal withdrawal) {
        if (withdrawal == null || withdrawal.getWithdrawalId() == null) {
            throw new IllegalArgumentException("提现信息不完整");
        }
        TraceLogger.info("Withdrawal", "updateWithdrawal", "更新提现ID: " + withdrawal.getWithdrawalId());
        return repository.save(withdrawal);
    }

    /**
     * 批量保存或更新提现记录
     */
    @CacheEvict(value = {"withdrawal::traceId", "withdrawal::anchorId", "withdrawal::status", 
                         "withdrawal::pending"}, allEntries = true)
    @Transactional
    public List<Withdrawal> batchSaveWithdrawals(List<Withdrawal> withdrawals) {
        if (withdrawals == null || withdrawals.isEmpty()) {
            TraceLogger.warn("Withdrawal", "batchSaveWithdrawals", "保存列表为空");
            return List.of();
        }
        TraceLogger.info("Withdrawal", "batchSaveWithdrawals", "批量保存提现，条数: " + withdrawals.size());
        return repository.saveAll(withdrawals);
    }

    /**
     * 删除提现记录
     */
    @CacheEvict(value = {"withdrawal::traceId", "withdrawal::anchorId", "withdrawal::status", 
                         "withdrawal::pending"}, allEntries = true)
    @Transactional
    public void deleteWithdrawal(Long withdrawalId) {
        if (withdrawalId == null) {
            throw new IllegalArgumentException("提现ID不能为空");
        }
        TraceLogger.info("Withdrawal", "deleteWithdrawal", "删除提现ID: " + withdrawalId);
        repository.deleteById(withdrawalId);
    }
}
