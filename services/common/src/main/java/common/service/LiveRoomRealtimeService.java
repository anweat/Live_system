package common.service;

import common.bean.liveroom.LiveRoomRealtime;
import common.logger.TraceLogger;
import common.repository.LiveRoomRealtimeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 直播间实时数据业务服务层
 * 封装直播间实时数据的增量更新逻辑
 * 使用 Repository + Service 架构，支持自动缓存
 * 
 * @author Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class LiveRoomRealtimeService extends BaseService<LiveRoomRealtime, Long, LiveRoomRealtimeRepository> {

    public LiveRoomRealtimeService(LiveRoomRealtimeRepository repository) {
        super(repository);
    }

    @Override
    protected String getCachePrefix() {
        return "liveRoomRealtime::";
    }

    @Override
    protected String getEntityName() {
        return "LiveRoomRealtime";
    }

    /**
     * 根据直播间ID查询实时数据（带缓存）
     */
    @Cacheable(value = "liveRoomRealtime::liveRoom", key = "#liveRoomId", unless = "#result == null")
    @Transactional(readOnly = true)
    public Optional<LiveRoomRealtime> findByLiveRoomId(Long liveRoomId) {
        TraceLogger.debug("LiveRoomRealtime", "findByLiveRoomId", 
            "查询直播间实时数据: liveRoomId=" + liveRoomId);

        return repository.findByLiveRoomId(liveRoomId);
    }

    /**
     * 增加弹幕计数
     */
    @Transactional
    @CacheEvict(value = "liveRoomRealtime::liveRoom", key = "#liveRoomId")
    public void incrementMessageCount(Long liveRoomId, long delta) {
        TraceLogger.debug("LiveRoomRealtime", "incrementMessageCount", 
            String.format("增加弹幕计数: liveRoomId=%d, delta=%d", liveRoomId, delta));

        int affected = repository.incrementMessageCount(liveRoomId, delta);
        
        if (affected == 0) {
            TraceLogger.warn("LiveRoomRealtime", "incrementMessageCount", 
                "未找到直播间实时数据: liveRoomId=" + liveRoomId);
            // 如果不存在，创建一条新记录
            createIfNotExists(liveRoomId);
            repository.incrementMessageCount(liveRoomId, delta);
        }
    }

    /**
     * 增加打赏计数
     */
    @Transactional
    @CacheEvict(value = "liveRoomRealtime::liveRoom", key = "#liveRoomId")
    public void incrementRechargeCount(Long liveRoomId, long delta) {
        TraceLogger.debug("LiveRoomRealtime", "incrementRechargeCount", 
            String.format("增加打赏计数: liveRoomId=%d, delta=%d", liveRoomId, delta));

        int affected = repository.incrementRechargeCount(liveRoomId, delta);
        
        if (affected == 0) {
            TraceLogger.warn("LiveRoomRealtime", "incrementRechargeCount", 
                "未找到直播间实时数据: liveRoomId=" + liveRoomId);
            createIfNotExists(liveRoomId);
            repository.incrementRechargeCount(liveRoomId, delta);
        }
    }

    /**
     * 增加当前场次收益
     */
    @Transactional
    @CacheEvict(value = "liveRoomRealtime::liveRoom", key = "#liveRoomId")
    public void incrementCurrentRevenue(Long liveRoomId, BigDecimal delta) {
        TraceLogger.debug("LiveRoomRealtime", "incrementCurrentRevenue", 
            String.format("增加当前场次收益: liveRoomId=%d, delta=%s", liveRoomId, delta));

        int affected = repository.incrementCurrentRevenue(liveRoomId, delta);
        
        if (affected == 0) {
            TraceLogger.warn("LiveRoomRealtime", "incrementCurrentRevenue", 
                "未找到直播间实时数据: liveRoomId=" + liveRoomId);
            createIfNotExists(liveRoomId);
            repository.incrementCurrentRevenue(liveRoomId, delta);
        }
    }

    /**
     * 更新当前观众数
     */
    @Transactional
    @CacheEvict(value = "liveRoomRealtime::liveRoom", key = "#liveRoomId")
    public void updateCurrentViewerCount(Long liveRoomId, int count) {
        TraceLogger.debug("LiveRoomRealtime", "updateCurrentViewerCount", 
            String.format("更新当前观众数: liveRoomId=%d, count=%d", liveRoomId, count));

        int affected = repository.updateCurrentViewerCount(liveRoomId, count);
        
        if (affected == 0) {
            TraceLogger.warn("LiveRoomRealtime", "updateCurrentViewerCount", 
                "未找到直播间实时数据: liveRoomId=" + liveRoomId);
            createIfNotExists(liveRoomId);
            repository.updateCurrentViewerCount(liveRoomId, count);
        }
    }

    /**
     * 重置当前场次数据（开播时调用）
     */
    @Transactional
    @CacheEvict(value = "liveRoomRealtime::liveRoom", key = "#liveRoomId")
    public void resetRealtimeData(Long liveRoomId) {
        TraceLogger.info("LiveRoomRealtime", "resetRealtimeData", 
            "重置直播间实时数据: liveRoomId=" + liveRoomId);

        int affected = repository.resetRealtimeData(liveRoomId);
        
        if (affected == 0) {
            TraceLogger.warn("LiveRoomRealtime", "resetRealtimeData", 
                "未找到直播间实时数据，创建新记录: liveRoomId=" + liveRoomId);
            createIfNotExists(liveRoomId);
        }
    }

    /**
     * 创建直播间实时数据记录（如果不存在）
     */
    @Transactional
    public void createIfNotExists(Long liveRoomId) {
        Optional<LiveRoomRealtime> existing = repository.findByLiveRoomId(liveRoomId);
        
        if (existing.isEmpty()) {
            TraceLogger.info("LiveRoomRealtime", "createIfNotExists", 
                "创建直播间实时数据记录: liveRoomId=" + liveRoomId);

            LiveRoomRealtime realtime = LiveRoomRealtime.builder()
                .liveRoomId(liveRoomId)
                .currentViewerCount(0)
                .currentRevenueAmount(BigDecimal.ZERO)
                .messageCount(0L)
                .rechargeCount(0L)
                .onlineDuration(0L)
                .avgLatency(0)
                .maxLatency(0)
                .lastUpdateTime(LocalDateTime.now())
                .build();

            repository.save(realtime);
        }
    }
}
