package com.liveroom.anchor.service;

import com.liveroom.anchor.repository.LiveRoomRepository;
import com.liveroom.anchor.vo.LiveRoomRealtimeVO;
import common.bean.liveroom.LiveRoom;
import common.constant.ErrorConstants;
import common.exception.BusinessException;
import common.logger.TraceLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 直播间实时数据服务
 * 处理观众进入/离开、弹幕、打赏等实时消息
 * 使用Redis缓存热点数据，定量批量更新数据库
 * 
 * @author Team
 * @version 1.0.0
 */
@Service
@Slf4j
public class LiveRoomRealtimeService {

    @Autowired
    private LiveRoomRepository liveRoomRepository;

    // @Autowired
    // private LiveRoomRealtimeRepository liveRoomRealtimeRepository;

    // @Autowired
    // private MessageRepository messageRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // Redis key前缀
    private static final String LIVE_ROOM_VIEWERS_KEY = "live:room:viewers:";  // 当前在线观众数
    private static final String LIVE_ROOM_TOTAL_VIEWERS_KEY = "live:room:total_viewers:";  // 累计观看人次
    private static final String LIVE_ROOM_EARNINGS_KEY = "live:room:earnings:";  // 本次营收
    private static final String LIVE_ROOM_MESSAGE_COUNT_KEY = "live:room:message_count:";  // 弹幕数
    private static final String LIVE_ROOM_RECHARGE_COUNT_KEY = "live:room:recharge_count:";  // 打赏次数
    private static final String LIVE_ROOM_UPDATE_COUNTER_KEY = "live:room:update_counter:";  // 更新计数器

    // 配置参数
    private static final int UPDATE_THRESHOLD = 100;  // 累计100次操作后批量更新数据库
    private static final long REDIS_EXPIRE_HOURS = 24;  // Redis数据过期时间24小时

    /**
     * 观众进入直播间
     */
    public void viewerEnter(Long liveRoomId, Long audienceId) {
        TraceLogger.info("LiveRoomRealtimeService", "viewerEnter",
                String.format("观众进入直播间: liveRoomId=%d, audienceId=%d", liveRoomId, audienceId));

        // 1. 验证直播间存在且正在直播
        validateLiveRoom(liveRoomId);

        // 2. 增加当前在线观众数（Redis）
        String viewersKey = LIVE_ROOM_VIEWERS_KEY + liveRoomId;
        redisTemplate.opsForValue().increment(viewersKey, 1);
        redisTemplate.expire(viewersKey, REDIS_EXPIRE_HOURS, TimeUnit.HOURS);

        // 3. 增加累计观看人次（Redis）
        String totalViewersKey = LIVE_ROOM_TOTAL_VIEWERS_KEY + liveRoomId;
        redisTemplate.opsForValue().increment(totalViewersKey, 1);
        redisTemplate.expire(totalViewersKey, REDIS_EXPIRE_HOURS, TimeUnit.HOURS);

        // 4. 增加更新计数器
        incrementUpdateCounter(liveRoomId);

        TraceLogger.debug("LiveRoomRealtimeService", "viewerEnter",
                "观众进入成功，当前在线: " + getCurrentViewers(liveRoomId));
    }

    /**
     * 观众离开直播间
     */
    public void viewerLeave(Long liveRoomId, Long audienceId) {
        TraceLogger.info("LiveRoomRealtimeService", "viewerLeave",
                String.format("观众离开直播间: liveRoomId=%d, audienceId=%d", liveRoomId, audienceId));

        // 1. 减少当前在线观众数（Redis，不能小于0）
        String viewersKey = LIVE_ROOM_VIEWERS_KEY + liveRoomId;
        Long currentViewers = (Long) redisTemplate.opsForValue().get(viewersKey);
        
        if (currentViewers != null && currentViewers > 0) {
            redisTemplate.opsForValue().decrement(viewersKey, 1);
            redisTemplate.expire(viewersKey, REDIS_EXPIRE_HOURS, TimeUnit.HOURS);
        }

        // 2. 增加更新计数器
        incrementUpdateCounter(liveRoomId);

        TraceLogger.debug("LiveRoomRealtimeService", "viewerLeave",
                "观众离开成功，当前在线: " + getCurrentViewers(liveRoomId));
    }

    /**
     * 观众发送弹幕
     */
    @Transactional
    public void viewerDanmaku(Long liveRoomId, Long audienceId, String content) {
        TraceLogger.debug("LiveRoomRealtimeService", "viewerDanmaku",
                String.format("观众发送弹幕: liveRoomId=%d, audienceId=%d, content=%s",
                        liveRoomId, audienceId, content));

        // 1. 验证直播间存在且正在直播
        validateLiveRoom(liveRoomId);

        // 2. 保存弹幕到message表 (TODO: 需要实现Message实体和messageRepository)
        // Message message = new Message();
        // message.setLiveRoomId(liveRoomId);
        // message.setSenderId(audienceId);
        // message.setContent(content);
        // message.setCreateTime(LocalDateTime.now());
        // messageRepository.save(message);

        TraceLogger.debug("LiveRoomRealtimeService", "viewerDanmaku",
                "弹幕已保存到数据库");

        // 3. Redis递增弹幕计数（用于定量更新live_room_realtime表）
        String messageCountKey = LIVE_ROOM_MESSAGE_COUNT_KEY + liveRoomId;
        redisTemplate.opsForValue().increment(messageCountKey, 1);
        redisTemplate.expire(messageCountKey, REDIS_EXPIRE_HOURS, TimeUnit.HOURS);

        // 4. 增加更新计数器
        incrementUpdateCounter(liveRoomId);
    }

    /**
     * 观众打赏（只更新直播间数据，主播数据查询财务服务）
     */
    public void viewerReward(Long liveRoomId, Long audienceId, BigDecimal amount) {
        TraceLogger.info("LiveRoomRealtimeService", "viewerReward",
                String.format("观众打赏: liveRoomId=%d, audienceId=%d, amount=%s",
                        liveRoomId, audienceId, amount));

        // 1. 验证直播间存在且正在直播
        validateLiveRoom(liveRoomId);

        // 2. 增加本次直播总营收（Redis）
        String earningsKey = LIVE_ROOM_EARNINGS_KEY + liveRoomId;
        redisTemplate.opsForValue().increment(earningsKey, amount.doubleValue());
        redisTemplate.expire(earningsKey, REDIS_EXPIRE_HOURS, TimeUnit.HOURS);

        // 3. Redis递增打赏次数（用于定量更新live_room_realtime表）
        String rechargeCountKey = LIVE_ROOM_RECHARGE_COUNT_KEY + liveRoomId;
        redisTemplate.opsForValue().increment(rechargeCountKey, 1);
        redisTemplate.expire(rechargeCountKey, REDIS_EXPIRE_HOURS, TimeUnit.HOURS);

        // 4. 增加更新计数器
        incrementUpdateCounter(liveRoomId);

        TraceLogger.info("LiveRoomRealtimeService", "viewerReward",
                String.format("打赏成功，直播间总营收: %s", getTotalEarnings(liveRoomId)));

        // 注：主播的累计收益和可提现金额由财务服务管理，不在这里更新
    }

    /**
     * 获取直播间实时数据
     */
    public LiveRoomRealtimeVO getLiveRoomRealtimeData(Long liveRoomId) {
        TraceLogger.debug("LiveRoomRealtimeService", "getLiveRoomRealtimeData",
                "查询直播间实时数据: liveRoomId=" + liveRoomId);

        // 1. 查询直播间基础信息
        LiveRoom liveRoom = liveRoomRepository.findById(liveRoomId)
                .orElseThrow(() -> new BusinessException(ErrorConstants.RESOURCE_NOT_FOUND, "直播间不存在"));

        // 2. 从Redis获取实时数据
        Long currentViewers = getCurrentViewers(liveRoomId);
        Long totalViewers = getTotalViewers(liveRoomId);
        BigDecimal totalEarnings = getTotalEarnings(liveRoomId);

        // 3. 构建VO
        LiveRoomRealtimeVO vo = LiveRoomRealtimeVO.builder()
                .liveRoomId(liveRoomId)
                .anchorId(liveRoom.getAnchorId())
                .anchorName(liveRoom.getAnchorName())
                .currentViewers(currentViewers)
                .totalViewers(totalViewers != null ? totalViewers : liveRoom.getTotalViewers())
                .totalEarnings(totalEarnings != null ? totalEarnings : liveRoom.getTotalEarnings())
                .roomStatus(liveRoom.getRoomStatus())
                .startTime(liveRoom.getStartTime())
                .queryTime(LocalDateTime.now())
                .build();

        return vo;
    }

    /**
     * 定时任务：批量同步Redis数据到数据库
     * 每5分钟执行一次
     */
    @Scheduled(cron = "0 */5 * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void syncRealtimeDataToDB() {
        TraceLogger.info("LiveRoomRealtimeService", "syncRealtimeDataToDB",
                "开始同步直播间实时数据到数据库");

        // 查询所有正在直播的直播间
        var liveRooms = liveRoomRepository.findAllLiveRooms();

        int syncCount = 0;
        for (LiveRoom liveRoom : liveRooms) {
            try {
                syncSingleLiveRoom(liveRoom.getLiveRoomId());
                syncCount++;
            } catch (Exception e) {
                TraceLogger.error("LiveRoomRealtimeService", "syncRealtimeDataToDB",
                        "同步直播间数据失败: liveRoomId=" + liveRoom.getLiveRoomId(), e);
            }
        }

        TraceLogger.info("LiveRoomRealtimeService", "syncRealtimeDataToDB",
                String.format("同步完成，共同步%d个直播间", syncCount));
    }

    /**
     * 同步单个直播间的数据到数据库
     */
    private void syncSingleLiveRoom(Long liveRoomId) {
        // 1. 从Redis获取增量数据
        String totalViewersKey = LIVE_ROOM_TOTAL_VIEWERS_KEY + liveRoomId;
        String earningsKey = LIVE_ROOM_EARNINGS_KEY + liveRoomId;
        String messageCountKey = LIVE_ROOM_MESSAGE_COUNT_KEY + liveRoomId;
        String rechargeCountKey = LIVE_ROOM_RECHARGE_COUNT_KEY + liveRoomId;

        Object totalViewersObj = redisTemplate.opsForValue().get(totalViewersKey);
        Object earningsObj = redisTemplate.opsForValue().get(earningsKey);
        Object messageCountObj = redisTemplate.opsForValue().get(messageCountKey);
        Object rechargeCountObj = redisTemplate.opsForValue().get(rechargeCountKey);

        // 如果没有任何增量数据，直接返回
        if (totalViewersObj == null && earningsObj == null && 
            messageCountObj == null && rechargeCountObj == null) {
            return;
        }

        // 转换增量数据
        long totalViewersDelta = totalViewersObj != null ? 
            ((Number) totalViewersObj).longValue() : 0L;
        BigDecimal earningsDelta = earningsObj != null ? 
            new BigDecimal(earningsObj.toString()) : BigDecimal.ZERO;
        long messageCountDelta = messageCountObj != null ? 
            ((Number) messageCountObj).longValue() : 0L;
        long rechargeCountDelta = rechargeCountObj != null ? 
            ((Number) rechargeCountObj).longValue() : 0L;

        // 2. 更新live_room表（累计观看人次、累计收益）
        if (totalViewersDelta > 0) {
            liveRoomRepository.updateTotalViewers(liveRoomId, totalViewersDelta);
        }
        if (earningsDelta.compareTo(BigDecimal.ZERO) > 0) {
            liveRoomRepository.updateTotalEarnings(liveRoomId, earningsDelta);
        }

        // 3. 更新live_room_realtime表（弹幕数、打赏数、当前场次收益）(TODO: 需要实现liveRoomRealtimeRepository)
        // if (messageCountDelta > 0) {
        //     liveRoomRealtimeRepository.incrementMessageCount(liveRoomId, messageCountDelta);
        // }
        // if (rechargeCountDelta > 0) {
        //     liveRoomRealtimeRepository.incrementRechargeCount(liveRoomId, rechargeCountDelta);
        // }
        // if (earningsDelta.compareTo(BigDecimal.ZERO) > 0) {
        //     liveRoomRealtimeRepository.incrementCurrentRevenue(liveRoomId, earningsDelta);
        // }

        // 4. 清除Redis增量计数器
        redisTemplate.delete(totalViewersKey);
        redisTemplate.delete(earningsKey);
        redisTemplate.delete(messageCountKey);
        redisTemplate.delete(rechargeCountKey);
        redisTemplate.delete(LIVE_ROOM_UPDATE_COUNTER_KEY + liveRoomId);

        TraceLogger.debug("LiveRoomRealtimeService", "syncSingleLiveRoom",
                String.format("同步直播间数据成功: liveRoomId=%d, totalViewersDelta=%d, " +
                    "earningsDelta=%s, messageCountDelta=%d, rechargeCountDelta=%d",
                    liveRoomId, totalViewersDelta, earningsDelta, messageCountDelta, rechargeCountDelta));
    }

    /**
     * 验证直播间存在且正在直播
     */
    private void validateLiveRoom(Long liveRoomId) {
        Optional<LiveRoom> optional = liveRoomRepository.findById(liveRoomId);
        if (!optional.isPresent()) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_FOUND, "直播间不存在");
        }

        LiveRoom liveRoom = optional.get();
        if (liveRoom.getRoomStatus() != 1) {
            throw new BusinessException(ErrorConstants.BUSINESS_ERROR, "直播间未在直播中");
        }
    }

    /**
     * 获取当前在线观众数
     */
    private Long getCurrentViewers(Long liveRoomId) {
        String key = LIVE_ROOM_VIEWERS_KEY + liveRoomId;
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? ((Number) value).longValue() : 0L;
    }

    /**
     * 获取累计观看人次（增量）
     */
    private Long getTotalViewers(Long liveRoomId) {
        String key = LIVE_ROOM_TOTAL_VIEWERS_KEY + liveRoomId;
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? ((Number) value).longValue() : null;
    }

    /**
     * 获取本次总营收（增量）
     */
    private BigDecimal getTotalEarnings(Long liveRoomId) {
        String key = LIVE_ROOM_EARNINGS_KEY + liveRoomId;
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    /**
     * 增加更新计数器，达到阈值时触发数据库更新
     */
    private void incrementUpdateCounter(Long liveRoomId) {
        String counterKey = LIVE_ROOM_UPDATE_COUNTER_KEY + liveRoomId;
        Long counter = redisTemplate.opsForValue().increment(counterKey, 1);
        redisTemplate.expire(counterKey, REDIS_EXPIRE_HOURS, TimeUnit.HOURS);

        // 达到阈值，触发批量更新
        if (counter != null && counter >= UPDATE_THRESHOLD) {
            try {
                syncSingleLiveRoom(liveRoomId);
            } catch (Exception e) {
                TraceLogger.error("LiveRoomRealtimeService", "incrementUpdateCounter",
                        "触发批量更新失败: liveRoomId=" + liveRoomId, e);
            }
        }
    }
}
