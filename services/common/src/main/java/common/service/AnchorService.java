package common.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import common.bean.user.Anchor;
import common.logger.TraceLogger;
import common.repository.AnchorRepository;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 主播Service - 统一的主播数据访问接口
 */
@Slf4j
@Service
public class AnchorService extends BaseService<Anchor, Long, AnchorRepository> {

    public AnchorService(AnchorRepository repository) {
        super(repository);
    }

    @Override
    protected String getCachePrefix() {
        return "anchor::";
    }

    @Override
    protected String getEntityName() {
        return "Anchor";
    }

    /**
     * 按主播用户ID查询（带缓存）
     */
    @Cacheable(value = "anchor::userId", key = "#userId", unless = "#result == null")
    @Transactional(readOnly = true)
    public Optional<Anchor> findByUserId(Long userId) {
        if (userId == null) {
            TraceLogger.warn("Anchor", "findByUserId", "用户ID为空");
            return Optional.empty();
        }
        TraceLogger.info("Anchor", "findByUserId", "查询主播用户ID: " + userId);
        return repository.findByUserId(userId);
    }

    /**
     * 按直播间ID查询主播
     */
    @Cacheable(value = "anchor::liveRoomId", key = "#liveRoomId", unless = "#result == null")
    @Transactional(readOnly = true)
    public Optional<Anchor> findByLiveRoomId(Long liveRoomId) {
        if (liveRoomId == null) {
            return Optional.empty();
        }
        TraceLogger.info("Anchor", "findByLiveRoomId", "查询直播间ID: " + liveRoomId);
        return repository.findByLiveRoomId(liveRoomId);
    }

    /**
     * 查询粉丝数最多的N个主播
     */
    @Cacheable(value = "anchor::topByFans", key = "#limit")
    @Transactional(readOnly = true)
    public List<Anchor> findTopAnchorsByFans(int limit) {
        TraceLogger.info("Anchor", "findTopAnchorsByFans", "查询top " + limit + " 主播(按粉丝数)");
        return repository.findTopAnchorsByFans(limit);
    }

    /**
     * 查询收益最多的N个主播
     */
    @Cacheable(value = "anchor::topByEarnings", key = "#limit")
    @Transactional(readOnly = true)
    public List<Anchor> findTopAnchorsByEarnings(int limit) {
        TraceLogger.info("Anchor", "findTopAnchorsByEarnings", "查询top " + limit + " 主播(按收益)");
        return repository.findTopAnchorsByEarnings(limit);
    }

    /**
     * 查询粉丝数大于指定值的主播
     */
    @Cacheable(value = "anchor::fanCountGt", key = "#minFans")
    @Transactional(readOnly = true)
    public List<Anchor> findAnchorsByMinFans(Long minFans) {
        TraceLogger.info("Anchor", "findAnchorsByMinFans", "查询粉丝数大于: " + minFans);
        return repository.findByFanCountGreaterThan(minFans);
    }

    /**
     * 查询收益大于指定值的主播
     */
    @Cacheable(value = "anchor::earningsGt", key = "#minEarnings")
    @Transactional(readOnly = true)
    public List<Anchor> findAnchorsByMinEarnings(BigDecimal minEarnings) {
        TraceLogger.info("Anchor", "findAnchorsByMinEarnings", "查询收益大于: " + minEarnings);
        return repository.findByTotalEarningsGreaterThan(minEarnings);
    }

    /**
     * 检查用户是否是主播
     */
    @Transactional(readOnly = true)
    public boolean isAnchor(Long userId) {
        return repository.existsByUserId(userId);
    }

    /**
     * 创建主播信息
     */
    @CacheEvict(value = {"anchor::userId", "anchor::liveRoomId", "anchor::topByFans", 
                         "anchor::topByEarnings", "anchor::fanCountGt", "anchor::earningsGt"}, 
                allEntries = true)
    @Transactional
    public Anchor createAnchor(Anchor anchor) {
        if (anchor == null || anchor.getUserId() == null) {
            throw new IllegalArgumentException("主播信息不完整");
        }
        TraceLogger.info("Anchor", "createAnchor", "创建主播用户ID: " + anchor.getUserId());
        return repository.save(anchor);
    }

    /**
     * 更新主播信息
     */
    @CacheEvict(value = {"anchor::userId", "anchor::liveRoomId", "anchor::topByFans", 
                         "anchor::topByEarnings", "anchor::fanCountGt", "anchor::earningsGt"}, 
                allEntries = true)
    @Transactional
    public Anchor updateAnchor(Anchor anchor) {
        if (anchor == null || anchor.getUserId() == null) {
            throw new IllegalArgumentException("主播信息不完整");
        }
        TraceLogger.info("Anchor", "updateAnchor", "更新主播用户ID: " + anchor.getUserId());
        return repository.save(anchor);
    }

    /**
     * 增加主播粉丝数
     */
    @CacheEvict(value = {"anchor::userId", "anchor::topByFans", "anchor::fanCountGt"}, allEntries = true)
    @Transactional
    public void incrementFanCount(Long userId, Long increment) {
        if (userId == null || increment == null || increment <= 0) {
            throw new IllegalArgumentException("参数不合法");
        }
        repository.findByUserId(userId).ifPresent(anchor -> {
            anchor.setFanCount(anchor.getFanCount() + increment);
            repository.save(anchor);
            TraceLogger.info("Anchor", "incrementFanCount", 
                String.format("主播%d粉丝数增加%d", userId, increment));
        });
    }

    /**
     * 增加主播收益
     */
    @CacheEvict(value = {"anchor::userId", "anchor::topByEarnings", "anchor::earningsGt"}, allEntries = true)
    @Transactional
    public void incrementEarnings(Long userId, BigDecimal amount) {
        if (userId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("参数不合法");
        }
        repository.findByUserId(userId).ifPresent(anchor -> {
            anchor.setTotalEarnings(anchor.getTotalEarnings().add(amount));
            repository.save(anchor);
            TraceLogger.info("Anchor", "incrementEarnings", 
                String.format("主播%d收益增加%s", userId, amount));
        });
    }

    /**
     * 批量增加主播收益（用于结算）
     */
    @CacheEvict(value = {"anchor::userId", "anchor::topByEarnings", "anchor::earningsGt"}, allEntries = true)
    @Transactional
    public void batchIncrementEarnings(List<Long> userIds, BigDecimal amount) {
        if (userIds == null || userIds.isEmpty() || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("参数不合法");
        }
        userIds.forEach(userId -> incrementEarnings(userId, amount));
        TraceLogger.info("Anchor", "batchIncrementEarnings", 
            String.format("批量增加主播收益，主播数: %d, 金额: %s", userIds.size(), amount));
    }

    /**
     * 删除主播信息
     */
    @CacheEvict(value = {"anchor::userId", "anchor::liveRoomId", "anchor::topByFans", 
                         "anchor::topByEarnings", "anchor::fanCountGt", "anchor::earningsGt"}, 
                allEntries = true)
    @Transactional
    public void deleteAnchor(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("主播ID不能为空");
        }
        repository.findByUserId(userId).ifPresent(anchor -> {
            repository.deleteById(anchor.getUserId());
            TraceLogger.info("Anchor", "deleteAnchor", "删除主播用户ID: " + userId);
        });
    }

    /**
     * 批量保存或更新主播信息
     */
    @CacheEvict(value = {"anchor::userId", "anchor::liveRoomId", "anchor::topByFans", 
                         "anchor::topByEarnings", "anchor::fanCountGt", "anchor::earningsGt"}, 
                allEntries = true)
    @Transactional
    public List<Anchor> batchSaveAnchors(List<Anchor> anchors) {
        if (anchors == null || anchors.isEmpty()) {
            TraceLogger.warn("Anchor", "batchSaveAnchors", "保存列表为空");
            return List.of();
        }
        TraceLogger.info("Anchor", "batchSaveAnchors", "批量保存主播，条数: " + anchors.size());
        return repository.saveAll(anchors);
    }
}
