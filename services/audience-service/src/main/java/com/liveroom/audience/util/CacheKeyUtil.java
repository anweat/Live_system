package com.liveroom.audience.util;

/**
 * 缓存键生成工具类
 * 统一管理Redis缓存键的生成规则
 */
public class CacheKeyUtil {

    /**
     * 观众基础信息缓存键
     */
    public static String getAudienceCacheKey(Long audienceId) {
        return "audience:" + audienceId;
    }

    /**
     * 观众消费统计缓存键
     */
    public static String getConsumptionStatsCacheKey(Long audienceId) {
        return "audience:" + audienceId + ":consumption_stats";
    }

    /**
     * 主播TOP10打赏观众缓存键
     */
    public static String getAnchorTop10CacheKey(Long anchorId, String period) {
        return "anchor:" + anchorId + ":top10_" + period;
    }

    /**
     * 打赏记录缓存键
     */
    public static String getRechargeCacheKey(Long rechargeId) {
        return "recharge:" + rechargeId;
    }

    /**
     * 打赏幂等性检查缓存键
     */
    public static String getRechargeIdempotencyCacheKey(String traceId) {
        return "recharge:trace:" + traceId;
    }

    /**
     * 同步进度缓存键
     */
    public static String getSyncProgressCacheKey(String serviceName) {
        return "sync:progress:" + serviceName;
    }
}
