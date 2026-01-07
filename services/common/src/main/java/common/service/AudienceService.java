package common.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import common.bean.user.Audience;
import common.logger.TraceLogger;
import common.repository.AudienceRepository;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 观众Service - 统一的观众数据访问接口
 * 所有其他模块必须通过此Service来操作观众数据
 * 提供缓存和批量操作支持
 */
@Slf4j
@Service
public class AudienceService extends BaseService<Audience, Long, AudienceRepository> {

    public AudienceService(AudienceRepository repository) {
        super(repository);
    }

    @Override
    protected String getCachePrefix() {
        return "audience::";
    }

    @Override
    protected String getEntityName() {
        return "Audience";
    }


    /**
     * 按观众用户ID查询（带缓存）
     */
    @Cacheable(value = "audience::userId", key = "#userId", unless = "#result == null")
    @Transactional(readOnly = true)
    public Optional<Audience> findByUserId(Long userId) {
        if (userId == null) {
            TraceLogger.warn("Audience", "findByUserId", "用户ID为空");
            return Optional.empty();
        }
        TraceLogger.info("Audience", "findByUserId", "查询观众用户ID: " + userId);
        return repository.findByUserId(userId);
    }

    /**
     * 按昵称查询观众（带缓存）
     */
    @Cacheable(value = "audience::nickname", key = "#nickname", unless = "#result == null")
    @Transactional(readOnly = true)
    public Optional<Audience> findByNickname(String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            TraceLogger.warn("Audience", "findByNickname", "昵称为空");
            return Optional.empty();
        }
        TraceLogger.info("Audience", "findByNickname", "查询观众昵称: " + nickname);
        return repository.findByNickname(nickname);
    }

    /**
     * 按消费等级查询观众
     */
    @Cacheable(value = "audience::level", key = "#level")
    @Transactional(readOnly = true)
    public List<Audience> findByConsumptionLevel(Integer level) {
        TraceLogger.info("Audience", "findByConsumptionLevel", "查询消费等级: " + level);
        return repository.findByConsumptionLevel(level);
    }

    /**
     * 按消费等级分页查询观众
     */
    @Cacheable(value = "audience::level:page", key = "#level + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public Page<Audience> findByConsumptionLevel(Integer level, Pageable pageable) {
        TraceLogger.info("Audience", "findByConsumptionLevel", 
            String.format("分页查询消费等级: level=%d, page=%d, size=%d", 
                level, pageable.getPageNumber(), pageable.getPageSize()));
        return repository.findByConsumptionLevel(level, pageable);
    }

    /**
     * 分页查询所有观众
     */
    @Transactional(readOnly = true)
    public Page<Audience> findAll(Pageable pageable) {
        TraceLogger.info("Audience", "findAll", 
            String.format("分页查询所有观众: page=%d, size=%d", 
                pageable.getPageNumber(), pageable.getPageSize()));
        return repository.findAll(pageable);
    }

    /**
     * 按关键词搜索观众（分页）
     */
    @Transactional(readOnly = true)
    public Page<Audience> searchByKeyword(String keyword, Pageable pageable) {
        TraceLogger.info("Audience", "searchByKeyword", 
            String.format("搜索观众: keyword=%s, page=%d, size=%d", 
                keyword, pageable.getPageNumber(), pageable.getPageSize()));
        return repository.searchByKeyword(keyword, pageable);
    }

    /**
     * 按粉丝等级查询观众
     */
    @Cacheable(value = "audience::vipLevel", key = "#level")
    @Transactional(readOnly = true)
    public List<Audience> findByVipLevel(Integer level) {
        TraceLogger.info("Audience", "findByVipLevel", "查询粉丝等级: " + level);
        return repository.findByVipLevel(level);
    }

    /**
     * 查询高价值观众（前20%消费用户）
     */
    @Cacheable(value = "audience::highValue")
    @Transactional(readOnly = true)
    public List<Audience> findHighValueAudience() {
        TraceLogger.info("Audience", "findHighValueAudience", "查询高价值观众");
        return repository.findHighValueAudience();
    }

    /**
     * 查询消费金额大于指定值的观众
     */
    @Cacheable(value = "audience::amountGt", key = "#amount")
    @Transactional(readOnly = true)
    public List<Audience> findAudienceByMinAmount(BigDecimal amount) {
        TraceLogger.info("Audience", "findAudienceByMinAmount", "查询消费金额大于: " + amount);
        return repository.findByTotalRechargeAmountGreaterThan(amount);
    }

    /**
     * 查询消费次数大于指定值的观众
     */
    @Cacheable(value = "audience::countGt", key = "#count")
    @Transactional(readOnly = true)
    public List<Audience> findAudienceByMinCount(Long count) {
        TraceLogger.info("Audience", "findAudienceByMinCount", "查询消费次数大于: " + count);
        return repository.findByTotalRechargeCountGreaterThan(count);
    }

    /**
     * 查询消费金额最多的N个观众
     */
    @Cacheable(value = "audience::topBySpending", key = "#limit")
    @Transactional(readOnly = true)
    public List<Audience> findTopAudienceBySpending(int limit) {
        TraceLogger.info("Audience", "findTopAudienceBySpending", "查询top " + limit + " 观众(按消费)");
        return repository.findTopAudienceBySpending(limit);
    }

    /**
     * 检查用户是否是观众
     */
    @Transactional(readOnly = true)
    public boolean isAudience(Long userId) {
        return repository.existsByUserId(userId);
    }

    /**
     * 创建观众信息
     */
    @CacheEvict(value = {"audience::userId", "audience::nickname", "audience::level", "audience::vipLevel", 
                         "audience::highValue", "audience::amountGt", "audience::countGt", 
                         "audience::topBySpending"}, allEntries = true)
    @Transactional
    public Audience createAudience(Audience audience) {
        if (audience == null || audience.getUserId() == null) {
            throw new IllegalArgumentException("观众信息不完整");
        }
        TraceLogger.info("Audience", "createAudience", "创建观众用户ID: " + audience.getUserId());
        return repository.save(audience);
    }

    /**
     * 更新观众信息
     */
    @CacheEvict(value = {"audience::userId", "audience::nickname", "audience::level", "audience::vipLevel", 
                         "audience::highValue", "audience::amountGt", "audience::countGt", 
                         "audience::topBySpending"}, allEntries = true)
    @Transactional
    public Audience updateAudience(Audience audience) {
        if (audience == null || audience.getUserId() == null) {
            throw new IllegalArgumentException("观众信息不完整");
        }
        TraceLogger.info("Audience", "updateAudience", "更新观众用户ID: " + audience.getUserId());
        return repository.save(audience);
    }

    /**
     * 增加观众消费金额和次数
     */
    @CacheEvict(value = {"audience::userId", "audience::nickname", "audience::level", 
                         "audience::highValue", "audience::amountGt", "audience::countGt", "audience::topBySpending"}, 
                allEntries = true)
    @Transactional
    public void incrementRecharge(Long userId, BigDecimal amount, Long count) {
        if (userId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0 || count == null || count <= 0) {
            throw new IllegalArgumentException("参数不合法");
        }
        repository.findByUserId(userId).ifPresent(audience -> {
            audience.setTotalRechargeAmount(audience.getTotalRechargeAmount().add(amount));
            audience.setTotalRechargeCount(audience.getTotalRechargeCount() + count);
            audience.setLastRechargeTime(java.time.LocalDateTime.now());
            repository.save(audience);
            TraceLogger.info("Audience", "incrementRecharge", 
                String.format("观众%d增加消费，金额: %s, 次数: %d", userId, amount, count));
        });
    }

    /**
     * 批量增加观众消费金额（用于结算统计）
     */
    @CacheEvict(value = {"audience::userId", "audience::nickname", "audience::level", 
                         "audience::highValue", "audience::amountGt", "audience::countGt", "audience::topBySpending"}, 
                allEntries = true)
    @Transactional
    public void batchIncrementRecharge(List<Long> userIds, BigDecimal amount, Long count) {
        if (userIds == null || userIds.isEmpty() || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("参数不合法");
        }
        userIds.forEach(userId -> incrementRecharge(userId, amount, count != null ? count : 1L));
        TraceLogger.info("Audience", "batchIncrementRecharge", 
            String.format("批量增加观众消费，观众数: %d, 金额: %s", userIds.size(), amount));
    }

    /**
     * 删除观众信息
     */
    @CacheEvict(value = {"audience::userId", "audience::nickname", "audience::level", "audience::vipLevel", 
                         "audience::highValue", "audience::amountGt", "audience::countGt", 
                         "audience::topBySpending"}, allEntries = true)
    @Transactional
    public void deleteAudience(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("观众ID不能为空");
        }
        TraceLogger.info("Audience", "deleteAudience", "删除观众用户ID: " + userId);
        repository.deleteById(userId);
    }

    /**
     * 批量保存或更新观众信息
     */
    @CacheEvict(value = {"audience::userId", "audience::nickname", "audience::level", "audience::vipLevel", 
                         "audience::highValue", "audience::amountGt", "audience::countGt", 
                         "audience::topBySpending"}, allEntries = true)
    @Transactional
    public List<Audience> batchSaveAudience(List<Audience> audiences) {
        if (audiences == null || audiences.isEmpty()) {
            TraceLogger.warn("Audience", "batchSaveAudience", "保存列表为空");
            return List.of();
        }
        TraceLogger.info("Audience", "batchSaveAudience", "批量保存观众，条数: " + audiences.size());
        return repository.saveAll(audiences);
    }

    /**
     * 按创建日期查询观众
     */
    @Transactional(readOnly = true)
    public List<Audience> findByCreatedDate(LocalDate date) {
        TraceLogger.info("Audience", "findByCreatedDate", "按创建日期查询观众: " + date);
        return repository.findByCreatedDate(date);
    }

    /**
     * 按创建日期范围查询观众
     */
    @Transactional(readOnly = true)
    public List<Audience> findByCreatedDateBetween(LocalDate startDate, LocalDate endDate) {
        TraceLogger.info("Audience", "findByCreatedDateBetween", 
            String.format("按创建日期范围查询观众: %s - %s", startDate, endDate));
        return repository.findByCreatedDateBetween(startDate, endDate);
    }
}
