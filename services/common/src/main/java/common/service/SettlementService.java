package common.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import common.bean.Settlement;
import common.logger.TraceLogger;
import common.repository.SettlementRepository;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 结算Service - 统一的结算数据访问接口
 * 所有其他模块必须通过此Service来操作结算数据
 */
@Slf4j
@Service
public class SettlementService extends BaseService<Settlement, Long, SettlementRepository> {

    public SettlementService(SettlementRepository repository) {
        super(repository);
    }

    @Override
    protected String getCachePrefix() {
        return "settlement::";
    }

    @Override
    protected String getEntityName() {
        return "Settlement";
    }

    /**
     * 按主播ID查询结算信息（一一对应）
     */
    @Cacheable(value = "settlement::anchorId", key = "#anchorId", unless = "#result == null")
    @Transactional(readOnly = true)
    public Optional<Settlement> findByAnchorId(Long anchorId) {
        if (anchorId == null) {
            TraceLogger.warn("Settlement", "findByAnchorId", "主播ID为空");
            return Optional.empty();
        }
        TraceLogger.info("Settlement", "findByAnchorId", "查询主播ID: " + anchorId);
        return repository.findByAnchorId(anchorId);
    }

    /**
     * 按结算状态查询
     */
    @Cacheable(value = "settlement::status", key = "#status")
    @Transactional(readOnly = true)
    public List<Settlement> findByStatus(Integer status) {
        TraceLogger.info("Settlement", "findByStatus", "查询状态: " + status);
        return repository.findByStatus(status);
    }

    /**
     * 查询可提取金额大于0的结算记录
     */
    @Cacheable(value = "settlement::hasAmount")
    @Transactional(readOnly = true)
    public List<Settlement> findSettlementsWithAvailableAmount() {
        TraceLogger.info("Settlement", "findSettlementsWithAvailableAmount", "查询可提取结算");
        return repository.findSettlementsWithAvailableAmount();
    }

    /**
     * 查询冻结状态的结算记录
     */
    @Cacheable(value = "settlement::frozen")
    @Transactional(readOnly = true)
    public List<Settlement> findFrozenSettlements() {
        TraceLogger.info("Settlement", "findFrozenSettlements", "查询冻结的结算");
        return repository.findFrozenSettlements();
    }

    /**
     * 查询待结算的记录
     */
    @Transactional(readOnly = true)
    public List<Settlement> findPendingSettlements(LocalDateTime now) {
        TraceLogger.info("Settlement", "findPendingSettlements", "查询待结算的记录");
        return repository.findPendingSettlements(now);
    }

    /**
     * 计算某个主播的总可提取金额
     */
    @Transactional(readOnly = true)
    public BigDecimal sumAvailableAmountByAnchor(Long anchorId) {
        if (anchorId == null) {
            throw new IllegalArgumentException("主播ID不能为空");
        }
        TraceLogger.info("Settlement", "sumAvailableAmountByAnchor", "计算主播可提取金额: " + anchorId);
        return repository.sumAvailableAmountByAnchor(anchorId);
    }

    /**
     * 查询可提取金额大于指定值的结算记录
     */
    @Cacheable(value = "settlement::amountGt", key = "#amount")
    @Transactional(readOnly = true)
    public List<Settlement> findByMinAvailableAmount(BigDecimal amount) {
        TraceLogger.info("Settlement", "findByMinAvailableAmount", "查询可提取金额大于: " + amount);
        return repository.findByAvailableAmountGreaterThan(amount);
    }

    /**
     * 检查主播是否存在结算记录
     */
    @Transactional(readOnly = true)
    public boolean existsByAnchorId(Long anchorId) {
        return repository.existsByAnchorId(anchorId);
    }

    /**
     * 批量查询主播的结算信息
     */
    @Transactional(readOnly = true)
    public List<Settlement> findByAnchorIds(List<Long> anchorIds) {
        if (anchorIds == null || anchorIds.isEmpty()) {
            return List.of();
        }
        TraceLogger.info("Settlement", "findByAnchorIds", "批量查询主播结算，数量: " + anchorIds.size());
        return repository.findByAnchorIds(anchorIds);
    }

    /**
     * 创建结算信息
     */
    @CacheEvict(value = {"settlement::anchorId", "settlement::status", "settlement::hasAmount", 
                         "settlement::frozen", "settlement::amountGt"}, allEntries = true)
    @Transactional
    public Settlement createSettlement(Settlement settlement) {
        if (settlement == null || settlement.getAnchorId() == null) {
            throw new IllegalArgumentException("结算信息不完整");
        }
        TraceLogger.info("Settlement", "createSettlement", "创建结算: 主播ID " + settlement.getAnchorId());
        return repository.save(settlement);
    }

    /**
     * 更新结算信息
     */
    @CacheEvict(value = {"settlement::anchorId", "settlement::status", "settlement::hasAmount", 
                         "settlement::frozen", "settlement::amountGt"}, allEntries = true)
    @Transactional
    public Settlement updateSettlement(Settlement settlement) {
        if (settlement == null || settlement.getSettlementId() == null) {
            throw new IllegalArgumentException("结算信息不完整");
        }
        TraceLogger.info("Settlement", "updateSettlement", "更新结算ID: " + settlement.getSettlementId());
        return repository.save(settlement);
    }

    /**
     * 增加主播的可提取金额
     */
    @CacheEvict(value = {"settlement::anchorId", "settlement::status", "settlement::hasAmount", 
                         "settlement::frozen", "settlement::amountGt"}, allEntries = true)
    @Transactional
    public void incrementAvailableAmount(Long anchorId, BigDecimal amount) {
        if (anchorId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("参数不合法");
        }
        repository.findByAnchorId(anchorId).ifPresent(settlement -> {
            settlement.setAvailableAmount(settlement.getAvailableAmount().add(amount));
            repository.save(settlement);
            TraceLogger.info("Settlement", "incrementAvailableAmount", 
                String.format("主播%d的可提取金额增加%s", anchorId, amount));
        });
    }

    /**
     * 批量保存或更新结算信息
     */
    @CacheEvict(value = {"settlement::anchorId", "settlement::status", "settlement::hasAmount", 
                         "settlement::frozen", "settlement::amountGt"}, allEntries = true)
    @Transactional
    public List<Settlement> batchSaveSettlements(List<Settlement> settlements) {
        if (settlements == null || settlements.isEmpty()) {
            TraceLogger.warn("Settlement", "batchSaveSettlements", "保存列表为空");
            return List.of();
        }
        TraceLogger.info("Settlement", "batchSaveSettlements", "批量保存结算，条数: " + settlements.size());
        return repository.saveAll(settlements);
    }

    /**
     * 删除结算信息
     */
    @CacheEvict(value = {"settlement::anchorId", "settlement::status", "settlement::hasAmount", 
                         "settlement::frozen", "settlement::amountGt"}, allEntries = true)
    @Transactional
    public void deleteSettlement(Long settlementId) {
        if (settlementId == null) {
            throw new IllegalArgumentException("结算ID不能为空");
        }
        TraceLogger.info("Settlement", "deleteSettlement", "删除结算ID: " + settlementId);
        repository.deleteById(settlementId);
    }
}
