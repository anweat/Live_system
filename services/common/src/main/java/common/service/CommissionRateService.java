package common.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import common.bean.CommissionRate;
import common.logger.TraceLogger;
import common.repository.CommissionRateRepository;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 分成比例Service - 统一的分成比例数据访问接口
 * 用于管理主播的分成比例版本和历史
 */
@Slf4j
@Service
public class CommissionRateService extends BaseService<CommissionRate, Long, CommissionRateRepository> {

    public CommissionRateService(CommissionRateRepository repository) {
        super(repository);
    }

    @Override
    protected String getCachePrefix() {
        return "commissionRate::";
    }

    @Override
    protected String getEntityName() {
        return "CommissionRate";
    }

    /**
     * 获取主播当前有效的分成比例
     */
    @Cacheable(value = "commissionRate::current", key = "#anchorId")
    @Transactional(readOnly = true)
    public Optional<CommissionRate> findCurrentRateByAnchor(Long anchorId) {
        if (anchorId == null) {
            TraceLogger.warn("CommissionRate", "findCurrentRateByAnchor", "主播ID为空");
            return Optional.empty();
        }
        TraceLogger.info("CommissionRate", "findCurrentRateByAnchor", "查询主播当前分成: " + anchorId);
        return repository.findCurrentRateByAnchor(anchorId, LocalDateTime.now());
    }

    /**
     * 查询主播的分成比例历史记录
     */
    @Cacheable(value = "commissionRate::history", key = "#anchorId")
    @Transactional(readOnly = true)
    public List<CommissionRate> findHistoryByAnchor(Long anchorId) {
        if (anchorId == null) {
            return List.of();
        }
        TraceLogger.info("CommissionRate", "findHistoryByAnchor", "查询主播分成历史: " + anchorId);
        return repository.findByAnchorIdOrderByEffectiveTimeDesc(anchorId);
    }

    /**
     * 按状态查询分成比例
     */
    @Cacheable(value = "commissionRate::status", key = "#status")
    @Transactional(readOnly = true)
    public List<CommissionRate> findByStatus(Integer status) {
        TraceLogger.info("CommissionRate", "findByStatus", "查询状态: " + status);
        return repository.findByStatus(status);
    }

    /**
     * 查询待启用的分成比例
     */
    @Cacheable(value = "commissionRate::pending")
    @Transactional(readOnly = true)
    public List<CommissionRate> findPendingRates() {
        TraceLogger.info("CommissionRate", "findPendingRates", "查询待启用的分成比例");
        return repository.findPendingRates();
    }

    /**
     * 查询已过期的分成比例
     */
    @Cacheable(value = "commissionRate::expired")
    @Transactional(readOnly = true)
    public List<CommissionRate> findExpiredRates() {
        TraceLogger.info("CommissionRate", "findExpiredRates", "查询已过期的分成比例");
        return repository.findExpiredRates();
    }

    /**
     * 检查主播是否有启用的分成比例
     */
    @Transactional(readOnly = true)
    public boolean hasActiveRate(Long anchorId) {
        return repository.hasActiveRate(anchorId);
    }

    /**
     * 批量查询多个主播的最新分成比例
     */
    @Transactional(readOnly = true)
    public List<CommissionRate> findLatestRatesByAnchors(List<Long> anchorIds) {
        if (anchorIds == null || anchorIds.isEmpty()) {
            return List.of();
        }
        TraceLogger.info("CommissionRate", "findLatestRatesByAnchors", 
            String.format("批量查询最新分成，主播数: %d", anchorIds.size()));
        return repository.findLatestRatesByAnchors(anchorIds);
    }

    /**
     * 创建新的分成比例记录
     */
    @CacheEvict(value = {"commissionRate::current", "commissionRate::history", 
                         "commissionRate::status", "commissionRate::pending", 
                         "commissionRate::expired"}, allEntries = true)
    @Transactional
    public CommissionRate createCommissionRate(CommissionRate rate) {
        if (rate == null || rate.getAnchorId() == null) {
            throw new IllegalArgumentException("分成比例信息不完整");
        }
        TraceLogger.info("CommissionRate", "createCommissionRate", 
            String.format("创建分成比例: 主播%d, 比例%s", rate.getAnchorId(), rate.getCommissionRate()));
        return repository.save(rate);
    }

    /**
     * 更新分成比例记录
     */
    @CacheEvict(value = {"commissionRate::current", "commissionRate::history", 
                         "commissionRate::status", "commissionRate::pending", 
                         "commissionRate::expired"}, allEntries = true)
    @Transactional
    public CommissionRate updateCommissionRate(CommissionRate rate) {
        if (rate == null || rate.getCommissionRateId() == null) {
            throw new IllegalArgumentException("分成比例信息不完整");
        }
        TraceLogger.info("CommissionRate", "updateCommissionRate", "更新分成比例ID: " + rate.getCommissionRateId());
        return repository.save(rate);
    }

    /**
     * 批量保存或更新分成比例
     */
    @CacheEvict(value = {"commissionRate::current", "commissionRate::history", 
                         "commissionRate::status", "commissionRate::pending", 
                         "commissionRate::expired"}, allEntries = true)
    @Transactional
    public List<CommissionRate> batchSaveRates(List<CommissionRate> rates) {
        if (rates == null || rates.isEmpty()) {
            TraceLogger.warn("CommissionRate", "batchSaveRates", "保存列表为空");
            return List.of();
        }
        TraceLogger.info("CommissionRate", "batchSaveRates", "批量保存分成比例，条数: " + rates.size());
        return repository.saveAll(rates);
    }

    /**
     * 删除分成比例记录
     */
    @CacheEvict(value = {"commissionRate::current", "commissionRate::history", 
                         "commissionRate::status", "commissionRate::pending", 
                         "commissionRate::expired"}, allEntries = true)
    @Transactional
    public void deleteCommissionRate(Long rateId) {
        if (rateId == null) {
            throw new IllegalArgumentException("分成比例ID不能为空");
        }
        TraceLogger.info("CommissionRate", "deleteCommissionRate", "删除分成比例ID: " + rateId);
        repository.deleteById(rateId);
    }
}
