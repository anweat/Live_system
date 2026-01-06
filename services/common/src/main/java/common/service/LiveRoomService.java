package common.service;

import common.bean.liveroom.LiveRoom;
import common.repository.LiveRoomRepository;
import common.logger.TraceLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 直播间业务服务层
 * 封装直播间相关的业务逻辑，包括单条和批量操作
 * 使用 Repository + Service 架构，支持自动缓存和批量操作
 */
@Slf4j
@Service
public class LiveRoomService extends BaseService<LiveRoom, Long, LiveRoomRepository> {

    public LiveRoomService(LiveRoomRepository repository) {
        super(repository);
    }

    @Override
    protected String getCachePrefix() {
        return "liveRoom::";
    }

    @Override
    protected String getEntityName() {
        return "LiveRoom";
    }


    /**
     * 查询直播间信息（缓存）
     */
    @Cacheable(value = "liveRoom::id", key = "#liveRoomId")
    @Transactional(readOnly = true)
    public LiveRoom getLiveRoomInfo(Long liveRoomId) {
        TraceLogger.info("LiveRoom", "getLiveRoomInfo", "查询直播间: " + liveRoomId);
        return repository.findById(liveRoomId).orElse(null);
    }

    /**
     * 根据主播ID查询直播间（缓存）
     */
    @Cacheable(value = "liveRoom::anchor", key = "#anchorId")
    @Transactional(readOnly = true)
    public LiveRoom getLiveRoomByAnchor(Long anchorId) {
        TraceLogger.info("LiveRoom", "getLiveRoomByAnchor", "查询主播直播间: " + anchorId);
        return repository.findByAnchorId(anchorId).orElse(null);
    }

    /**
     * 查询所有直播间
     */
    @Transactional(readOnly = true)
    public List<LiveRoom> getAllLiveRooms() {
        TraceLogger.info("LiveRoom", "getAllLiveRooms", "查询所有直播间");
        return repository.findAll();
    }

    /**
     * 查询正在直播的直播间（缓存）
     */
    @Cacheable(value = "liveRoom::live")
    @Transactional(readOnly = true)
    public List<LiveRoom> getLiveRooms() {
        TraceLogger.info("LiveRoom", "getLiveRooms", "查询正在直播的直播间");
        return repository.findLiveRooms();
    }

    /**
     * 查询TOP直播间（按观众数，缓存）
     */
    @Cacheable(value = "liveRoom::topViewers", key = "#limit")
    @Transactional(readOnly = true)
    public List<LiveRoom> getTopLiveRoomsByViewers(int limit) {
        TraceLogger.info("LiveRoom", "getTopLiveRoomsByViewers", "查询TOP " + limit + " 直播间");
        return repository.findTopByViewers(limit);
    }

    /**
     * 查询按分类的直播间（缓存）
     */
    @Cacheable(value = "liveRoom::category", key = "#category")
    @Transactional(readOnly = true)
    public List<LiveRoom> getLiveRoomsByCategory(String category) {
        TraceLogger.info("LiveRoom", "getLiveRoomsByCategory", "查询分类: " + category);
        return repository.findByCategory(category);
    }

    /**
     * 单条创建直播间
     */
    @CacheEvict(value = {"liveRoom::id", "liveRoom::anchor", "liveRoom::live", 
                         "liveRoom::topViewers", "liveRoom::category"}, allEntries = true)
    @Transactional
    public void createLiveRoom(LiveRoom liveRoom) {
        if (liveRoom == null) {
            throw new IllegalArgumentException("直播间信息不完整");
        }
        liveRoom.setRoomStatus(0); // 默认未开播
        liveRoom.setTotalEarnings(BigDecimal.ZERO);
        liveRoom.setTotalViewers(0L);
        liveRoom.setCreateTime(LocalDateTime.now());
        
        repository.save(liveRoom);
        TraceLogger.info("LiveRoom", "createLiveRoom", 
            String.format("创建直播间成功: liveRoomId=%d, anchorId=%d", 
            liveRoom.getLiveRoomId(), liveRoom.getAnchorId()));
    }

    /**
     * 批量创建直播间（高效的批量写入）
     * 用于模拟服务初始化大量直播间
     */
    @CacheEvict(value = {"liveRoom::id", "liveRoom::anchor", "liveRoom::live", 
                         "liveRoom::topViewers", "liveRoom::category"}, allEntries = true)
    @Transactional
    public void batchCreateLiveRooms(List<LiveRoom> liveRooms) {
        if (liveRooms == null || liveRooms.isEmpty()) {
            return;
        }

        // 设置默认值
        liveRooms.forEach(room -> {
            room.setRoomStatus(0); // 默认未开播
            if (room.getTotalEarnings() == null) {
                room.setTotalEarnings(BigDecimal.ZERO);
            }
            if (room.getTotalViewers() <= 0) {
                room.setTotalViewers(0L);
            }
            if (room.getMaxViewers() <= 0) {
                room.setMaxViewers(10000);
            }
            if (room.getCreateTime() == null) {
                room.setCreateTime(LocalDateTime.now());
            }
        });

        // 使用 BaseService 的批量保存（自动分批 500 条）
        repository.saveAll(liveRooms);
        TraceLogger.info("LiveRoom", "batchCreateLiveRooms", 
            String.format("批量创建直播间完成，总数: %d", liveRooms.size()));
    }

    /**
     * 更新直播间信息
     */
    @CacheEvict(value = {"liveRoom::id", "liveRoom::anchor", "liveRoom::live", 
                         "liveRoom::topViewers", "liveRoom::category"}, allEntries = true)
    @Transactional
    public void updateLiveRoom(LiveRoom liveRoom) {
        if (liveRoom == null || liveRoom.getLiveRoomId() == null) {
            throw new IllegalArgumentException("直播间 ID 不能为空");
        }
        liveRoom.setUpdateTime(LocalDateTime.now());
        repository.save(liveRoom);
        TraceLogger.info("LiveRoom", "updateLiveRoom", "更新直播间信息: " + liveRoom.getLiveRoomId());
    }

    /**
     * 批量更新直播间信息
     */
    @CacheEvict(value = {"liveRoom::id", "liveRoom::anchor", "liveRoom::live", 
                         "liveRoom::topViewers", "liveRoom::category"}, allEntries = true)
    @Transactional
    public void batchUpdateLiveRooms(List<LiveRoom> liveRooms) {
        if (liveRooms == null || liveRooms.isEmpty()) {
            return;
        }

        liveRooms.forEach(room -> room.setUpdateTime(LocalDateTime.now()));
        repository.saveAll(liveRooms);
        TraceLogger.info("LiveRoom", "batchUpdateLiveRooms", "批量更新直播间: 共 " + liveRooms.size() + " 个");
    }

    /**
     * 开播
     */
    @CacheEvict(value = {"liveRoom::id", "liveRoom::live", "liveRoom::topViewers"}, allEntries = true)
    @Transactional
    public void startBroadcast(Long liveRoomId) {
        repository.findById(liveRoomId).ifPresent(room -> {
            room.setRoomStatus(1);  // 直播中
            room.setUpdateTime(LocalDateTime.now());
            repository.save(room);
            TraceLogger.info("LiveRoom", "startBroadcast", "开播: " + liveRoomId);
        });
    }

    /**
     * 批量开播
     */
    @CacheEvict(value = {"liveRoom::id", "liveRoom::live", "liveRoom::topViewers"}, allEntries = true)
    @Transactional
    public void batchStartBroadcast(List<Long> liveRoomIds) {
        if (liveRoomIds == null || liveRoomIds.isEmpty()) {
            return;
        }

        liveRoomIds.forEach(id -> {
            repository.findById(id).ifPresent(room -> {
                room.setRoomStatus(1);
                room.setUpdateTime(LocalDateTime.now());
                repository.save(room);
            });
        });
        TraceLogger.info("LiveRoom", "batchStartBroadcast", "批量开播: 共 " + liveRoomIds.size() + " 个直播间");
    }

    /**
     * 关播
     */
    @CacheEvict(value = {"liveRoom::id", "liveRoom::live", "liveRoom::topViewers"}, allEntries = true)
    @Transactional
    public void endBroadcast(Long liveRoomId) {
        repository.findById(liveRoomId).ifPresent(room -> {
            room.setRoomStatus(2);  // 直播结束
            room.setUpdateTime(LocalDateTime.now());
            repository.save(room);
            TraceLogger.info("LiveRoom", "endBroadcast", "关播: " + liveRoomId);
        });
    }

    /**
     * 封禁直播间
     */
    @CacheEvict(value = {"liveRoom::id", "liveRoom::live", "liveRoom::topViewers"}, allEntries = true)
    @Transactional
    public void banLiveRoom(Long liveRoomId) {
        repository.findById(liveRoomId).ifPresent(room -> {
            room.setRoomStatus(3);  // 被封禁
            room.setUpdateTime(LocalDateTime.now());
            repository.save(room);
            TraceLogger.info("LiveRoom", "banLiveRoom", "封禁直播间: " + liveRoomId);
        });
    }

    /**
     * 批量更新直播间状态
     */
    @CacheEvict(value = {"liveRoom::id", "liveRoom::live", "liveRoom::topViewers"}, allEntries = true)
    @Transactional
    public void batchUpdateStatus(List<Long> liveRoomIds, Integer status) {
        if (liveRoomIds == null || liveRoomIds.isEmpty()) {
            return;
        }

        liveRoomIds.forEach(id -> {
            repository.findById(id).ifPresent(room -> {
                room.setRoomStatus(status);
                room.setUpdateTime(LocalDateTime.now());
                repository.save(room);
            });
        });

        String statusDesc = getStatusDesc(status);
        TraceLogger.info("LiveRoom", "batchUpdateStatus", 
            String.format("批量更新直播间状态: 共 %d 个, 状态=%s", liveRoomIds.size(), statusDesc));
    }

    /**
     * 增加直播间收益（单笔更新）
     */
    @CacheEvict(value = "liveRoom::topViewers", allEntries = true)
    @Transactional
    public void addRoomEarnings(Long liveRoomId, BigDecimal earnings) {
        if (earnings == null || earnings.signum() <= 0) {
            throw new IllegalArgumentException("收益金额不能为空或为负数");
        }
        repository.findById(liveRoomId).ifPresent(room -> {
            room.setTotalEarnings(room.getTotalEarnings().add(earnings));
            room.setUpdateTime(LocalDateTime.now());
            repository.save(room);
            TraceLogger.debug("LiveRoom", "addRoomEarnings", 
                String.format("增加直播间收益: liveRoomId=%d, earnings=%s", liveRoomId, earnings));
        });
    }

    /**
     * 批量增加直播间收益
     * 用于批量结算或分析服务的批量更新
     */
    @CacheEvict(value = "liveRoom::topViewers", allEntries = true)
    @Transactional
    public void batchAddRoomEarnings(Map<Long, BigDecimal> roomEarningsMap) {
        if (roomEarningsMap == null || roomEarningsMap.isEmpty()) {
            return;
        }

        roomEarningsMap.forEach((roomId, earnings) -> {
            repository.findById(roomId).ifPresent(room -> {
                room.setTotalEarnings(room.getTotalEarnings().add(earnings));
                room.setUpdateTime(LocalDateTime.now());
                repository.save(room);
            });
        });
        TraceLogger.info("LiveRoom", "batchAddRoomEarnings", 
            String.format("批量增加直播间收益: 共 %d 个直播间", roomEarningsMap.size()));
    }

    /**
     * 增加直播间观看人次
     */
    @CacheEvict(value = "liveRoom::topViewers", allEntries = true)
    @Transactional
    public void addViewers(Long liveRoomId, long count) {
        if (count <= 0) {
            throw new IllegalArgumentException("观众数不能为 0 或负数");
        }
        repository.findById(liveRoomId).ifPresent(room -> {
            room.setTotalViewers(room.getTotalViewers() + count);
            room.setUpdateTime(LocalDateTime.now());
            repository.save(room);
            TraceLogger.debug("LiveRoom", "addViewers", 
                String.format("增加直播间观众: liveRoomId=%d, count=%d", liveRoomId, count));
        });
    }

    /**
     * 查询收益排行TOP N
     */
    @Transactional(readOnly = true)
    public List<Object[]> getTopRoomsByEarnings(int limit) {
        TraceLogger.info("LiveRoom", "getTopRoomsByEarnings", "查询收益排行TOP " + limit);
        return repository.findTopByEarnings(limit);
    }

    /**
     * 查询观众数排行TOP N
     */
    @Transactional(readOnly = true)
    public List<Object[]> getTopRoomsByViewersCount(int limit) {
        TraceLogger.info("LiveRoom", "getTopRoomsByViewersCount", "查询观众数排行TOP " + limit);
        return repository.findTopByViewersCount(limit);
    }

    /**
     * 获取所有分类列表
     */
    @Cacheable(value = "liveRoom::categories")
    @Transactional(readOnly = true)
    public List<String> getAllCategories() {
        TraceLogger.info("LiveRoom", "getAllCategories", "获取所有分类列表");
        return repository.findAllCategories();
    }

    /**
     * 按分类统计
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getCategoryDistribution() {
        List<Object[]> stats = repository.countByCategory();

        Map<String, Long> distribution = new HashMap<>();
        for (Object[] stat : stats) {
            String category = (String) stat[0];
            Long count = ((Number) stat[1]).longValue();
            distribution.put(category, count);
        }

        return distribution;
    }

    /**
     * 按状态统计
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getStatusDistribution() {
        List<Object[]> stats = repository.countByStatus();

        Map<String, Long> distribution = new HashMap<>();
        for (Object[] stat : stats) {
            Integer status = ((Number) stat[0]).intValue();
            Long count = ((Number) stat[1]).longValue();
            String statusName = getStatusDesc(status);
            distribution.put(statusName, count);
        }

        return distribution;
    }

    /**
     * 获取直播间总数
     */
    @Transactional(readOnly = true)
    public long getTotalCount() {
        TraceLogger.info("LiveRoom", "getTotalCount", "获取直播间总数");
        return repository.count();
    }

    /**
     * 获取直播间统计信息
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getLiveRoomStatistics() {
        long totalCount = repository.count();
        Map<String, Long> statusDistribution = getStatusDistribution();
        Map<String, Long> categoryDistribution = getCategoryDistribution();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRooms", totalCount);
        stats.put("statusDistribution", statusDistribution);
        stats.put("categoryDistribution", categoryDistribution);

        return stats;
    }

    /**
     * 辅助方法：获取状态描述
     */
    private String getStatusDesc(Integer status) {
        switch (status) {
            case 0:
                return "未开播";
            case 1:
                return "直播中";
            case 2:
                return "直播结束";
            case 3:
                return "被封禁";
            default:
                return "未知状态";
        }
    }
}
