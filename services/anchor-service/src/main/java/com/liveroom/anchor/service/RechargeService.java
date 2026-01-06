package com.liveroom.anchor.service;

import com.liveroom.anchor.feign.AudienceServiceClient;
import com.liveroom.anchor.vo.RechargeVO;
import common.constant.ErrorConstants;
import common.exception.BusinessException;
import common.logger.TraceLogger;
import common.response.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 打赏记录服务
 * 通过Feign调用audience-service查询打赏记录
 * 
 * @author Team
 * @version 1.0.0
 */
@Service
public class RechargeService {

    @Autowired
    private AudienceServiceClient audienceServiceClient;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String TOP10_CACHE_PREFIX = "anchor:top10:";

    /**
     * 查询主播的打赏记录（分页）
     */
    public BaseResponse<Object> getRechargesByAnchor(
            Long anchorId, LocalDateTime startTime, LocalDateTime endTime,
            Integer page, Integer size) {

        TraceLogger.info("RechargeService", "getRechargesByAnchor",
                String.format("查询主播打赏记录: anchorId=%d, page=%d, size=%d",
                        anchorId, page, size));

        try {
            BaseResponse<Object> response = audienceServiceClient.getRechargesByAnchor(
                    anchorId, startTime, endTime, page, size);

            if (response == null || response.getCode() != 0) {
                throw new BusinessException(ErrorConstants.SERVICE_ERROR,
                        "查询打赏记录失败: " + (response != null ? response.getMessage() : "服务无响应"));
            }

            return response;

        } catch (Exception e) {
            TraceLogger.error("RechargeService", "getRechargesByAnchor",
                    "查询主播打赏记录失败", e);
            throw new BusinessException(ErrorConstants.SERVICE_ERROR,
                    "查询打赏记录失败: " + e.getMessage());
        }
    }

    /**
     * 查询直播间的打赏记录（分页）
     */
    public BaseResponse<Object> getRechargesByLiveRoom(
            Long liveRoomId, LocalDateTime startTime, LocalDateTime endTime,
            Integer page, Integer size) {

        TraceLogger.info("RechargeService", "getRechargesByLiveRoom",
                String.format("查询直播间打赏记录: liveRoomId=%d", liveRoomId));

        try {
            BaseResponse<Object> response = audienceServiceClient.getRechargesByLiveRoom(
                    liveRoomId, startTime, endTime, page, size);

            if (response == null || response.getCode() != 0) {
                throw new BusinessException(ErrorConstants.SERVICE_ERROR,
                        "查询打赏记录失败: " + (response != null ? response.getMessage() : "服务无响应"));
            }

            return response;

        } catch (Exception e) {
            TraceLogger.error("RechargeService", "getRechargesByLiveRoom",
                    "查询直播间打赏记录失败", e);
            throw new BusinessException(ErrorConstants.SERVICE_ERROR,
                    "查询打赏记录失败: " + e.getMessage());
        }
    }

    /**
     * 按traceId查询打赏记录
     */
    public RechargeVO getRechargeByTraceId(String traceId) {
        TraceLogger.info("RechargeService", "getRechargeByTraceId",
                "查询打赏记录: traceId=" + traceId);

        try {
            RechargeVO recharge = audienceServiceClient.getRechargeByTraceId(traceId);

            if (recharge == null) {
                throw new BusinessException(ErrorConstants.RESOURCE_NOT_FOUND,
                        "打赏记录不存在");
            }

            return recharge;

        } catch (Exception e) {
            TraceLogger.error("RechargeService", "getRechargeByTraceId",
                    "查询打赏记录失败", e);
            throw new BusinessException(ErrorConstants.SERVICE_ERROR,
                    "查询打赏记录失败: " + e.getMessage());
        }
    }

    /**
     * 统计主播的打赏总额
     */
    public Object getTotalRechargeByAnchor(
            Long anchorId, LocalDateTime startTime, LocalDateTime endTime) {

        TraceLogger.info("RechargeService", "getTotalRechargeByAnchor",
                String.format("统计主播打赏总额: anchorId=%d", anchorId));

        try {
            Object result = audienceServiceClient.getTotalRechargeByAnchor(
                    anchorId, startTime, endTime);

            return result;

        } catch (Exception e) {
            TraceLogger.error("RechargeService", "getTotalRechargeByAnchor",
                    "统计打赏总额失败", e);
            throw new BusinessException(ErrorConstants.SERVICE_ERROR,
                    "统计打赏总额失败: " + e.getMessage());
        }
    }

    /**
     * 查询TOP10打赏观众（带缓存）
     */
    @Cacheable(value = "top10Audiences", key = "#anchorId + ':' + #period")
    public List<RechargeVO.Top10AudienceVO> getTop10Audiences(Long anchorId, String period) {
        TraceLogger.info("RechargeService", "getTop10Audiences",
                String.format("查询TOP10打赏观众: anchorId=%d, period=%s", anchorId, period));

        // 先尝试从Redis获取缓存
        String cacheKey = TOP10_CACHE_PREFIX + anchorId + ":" + period;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            TraceLogger.debug("RechargeService", "getTop10Audiences",
                    "从缓存中获取TOP10数据");
            return (List<RechargeVO.Top10AudienceVO>) cached;
        }

        // 计算时间范围
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = calculateStartTime(period);

        try {
            List<RechargeVO.Top10AudienceVO> result = 
                    audienceServiceClient.getTop10Audiences(anchorId, startTime, endTime);

            // 缓存结果
            if (result != null && !result.isEmpty()) {
                long ttl = getCacheTTL(period);
                redisTemplate.opsForValue().set(cacheKey, result, ttl, TimeUnit.HOURS);
                TraceLogger.debug("RechargeService", "getTop10Audiences",
                        "TOP10数据已缓存，TTL=" + ttl + "小时");
            }

            return result;

        } catch (Exception e) {
            TraceLogger.error("RechargeService", "getTop10Audiences",
                    "查询TOP10打赏观众失败", e);
            throw new BusinessException(ErrorConstants.SERVICE_ERROR,
                    "查询TOP10失败: " + e.getMessage());
        }
    }

    /**
     * 计算统计开始时间
     */
    private LocalDateTime calculateStartTime(String period) {
        LocalDateTime now = LocalDateTime.now();
        switch (period) {
            case "day":
                return now.truncatedTo(ChronoUnit.DAYS);
            case "week":
                return now.minusWeeks(1).truncatedTo(ChronoUnit.DAYS);
            case "month":
                return now.minusMonths(1).truncatedTo(ChronoUnit.DAYS);
            case "all":
            default:
                return LocalDateTime.of(2020, 1, 1, 0, 0); // 系统开始时间
        }
    }

    /**
     * 获取缓存TTL（小时）
     */
    private long getCacheTTL(String period) {
        switch (period) {
            case "day":
                return 2;  // 2小时
            case "week":
                return 12; // 12小时
            case "month":
                return 24; // 24小时
            case "all":
            default:
                return 48; // 48小时
        }
    }

    /**
     * 清除TOP10缓存
     */
    public void clearTop10Cache(Long anchorId) {
        String[] periods = {"day", "week", "month", "all"};
        for (String period : periods) {
            String cacheKey = TOP10_CACHE_PREFIX + anchorId + ":" + period;
            redisTemplate.delete(cacheKey);
        }
        TraceLogger.info("RechargeService", "clearTop10Cache",
                "已清除主播TOP10缓存: anchorId=" + anchorId);
    }
}
