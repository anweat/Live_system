package com.liveroom.anchor.service;

import com.liveroom.anchor.feign.FinanceServiceClient;
import common.logger.TraceLogger;
import common.response.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * 分成比例服务
 * 从财务服务动态查询主播分成比例
 * 
 * @author Team
 * @version 1.0.0
 */
@Service
@Slf4j
public class CommissionRateService {

    @Autowired
    private FinanceServiceClient financeServiceClient;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String COMMISSION_CACHE_KEY = "anchor:commission:";
    private static final long CACHE_EXPIRE_HOURS = 2;  // 2小时缓存，比财务服务的24小时短

    /**
     * 查询主播当前生效的分成比例（带缓存）
     * 从财务服务实时查询，确保数据一致性
     */
    @Cacheable(value = "anchorCommission", key = "#anchorId", unless = "#result == null")
    public BigDecimal getCurrentCommissionRate(Long anchorId) {
        TraceLogger.debug("CommissionRateService", "getCurrentCommissionRate",
                "查询主播分成比例: anchorId=" + anchorId);

        // 1. 先尝试从Redis缓存获取
        String cacheKey = COMMISSION_CACHE_KEY + anchorId;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof BigDecimal) {
            TraceLogger.debug("CommissionRateService", "getCurrentCommissionRate",
                    "从缓存获取分成比例");
            return (BigDecimal) cached;
        }

        // 2. 缓存未命中，调用财务服务查询
        BaseResponse<FinanceServiceClient.CommissionRateVO> response = 
                financeServiceClient.getCurrentCommissionRate(anchorId);

        if (response.getCode() != 0 || response.getData() == null) {
            TraceLogger.warn("CommissionRateService", "getCurrentCommissionRate",
                    "财务服务未返回分成比例，使用默认值50%，主播ID: " + anchorId);
            // 未设置分成比例时，使用默认50%
            return new BigDecimal("50.00");
        }

        // 3. 获取分成比例
        Double rate = response.getData().getCommissionRate();
        BigDecimal commissionRate = new BigDecimal(rate.toString());

        // 4. 写入缓存（2小时过期）
        redisTemplate.opsForValue().set(cacheKey, commissionRate, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);

        TraceLogger.info("CommissionRateService", "getCurrentCommissionRate",
                String.format("主播分成比例: anchorId=%d, rate=%s%%", anchorId, commissionRate));

        return commissionRate;
    }

    /**
     * 清除分成比例缓存
     * 当财务服务更新分成比例后，可以调用此方法清除缓存
     */
    public void clearCommissionCache(Long anchorId) {
        String cacheKey = COMMISSION_CACHE_KEY + anchorId;
        redisTemplate.delete(cacheKey);
        
        TraceLogger.info("CommissionRateService", "clearCommissionCache",
                "清除分成比例缓存: anchorId=" + anchorId);
    }

    /**
     * 批量查询主播分成比例（优化性能）
     */
    public BigDecimal getCurrentCommissionRateWithDefault(Long anchorId, BigDecimal defaultRate) {
        try {
            BigDecimal rate = getCurrentCommissionRate(anchorId);
            return rate != null ? rate : defaultRate;
        } catch (Exception e) {
            TraceLogger.error("CommissionRateService", "getCurrentCommissionRateWithDefault",
                    "查询分成比例失败，使用默认值: anchorId=" + anchorId, e);
            return defaultRate;
        }
    }
}
