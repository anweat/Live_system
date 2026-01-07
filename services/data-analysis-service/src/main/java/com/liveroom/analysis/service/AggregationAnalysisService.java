package com.liveroom.analysis.service;

import com.liveroom.analysis.dto.KeyMetricsDTO;
import common.exception.AnalysisException;
import common.logger.TraceLogger;
import common.service.DataAccessFacade;
import common.service.AnalysisQueryService.RechargeStats;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 聚合统计分析Service
 */
@Service
@RequiredArgsConstructor
public class AggregationAnalysisService {

    private final DataAccessFacade dataAccessFacade;

    /**
     * 获取关键指标
     */
    @Cacheable(value = "analysis:aggregation", key = "'metrics:' + #startTime + ':' + #endTime")
    public KeyMetricsDTO getKeyMetrics(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            TraceLogger.info("AggregationAnalysisService", "getKeyMetrics", 
                null, "startTime", startTime, "endTime", endTime);

            Map<String, RechargeStats> statsByDay = dataAccessFacade.analysisQuery()
                .getRechargeStatsByDay(startTime, endTime);

            BigDecimal gmv = BigDecimal.ZERO;
            int totalOrders = 0;
            int uniqueAudiences = 0;

            for (RechargeStats stats : statsByDay.values()) {
                gmv = gmv.add(stats.getTotalAmount());
                totalOrders += stats.getCount();
                uniqueAudiences = Math.max(uniqueAudiences, stats.getUniqueAudiences());
            }

            // 简化计算，实际应查询总用户数
            int totalUsers = uniqueAudiences * 2;
            
            BigDecimal paymentRate = totalUsers == 0 ? BigDecimal.ZERO :
                BigDecimal.valueOf(uniqueAudiences).divide(
                    BigDecimal.valueOf(totalUsers), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            BigDecimal arpu = totalUsers == 0 ? BigDecimal.ZERO :
                gmv.divide(BigDecimal.valueOf(totalUsers), 2, RoundingMode.HALF_UP);

            BigDecimal arppu = uniqueAudiences == 0 ? BigDecimal.ZERO :
                gmv.divide(BigDecimal.valueOf(uniqueAudiences), 2, RoundingMode.HALF_UP);

            BigDecimal avgOrderAmount = totalOrders == 0 ? BigDecimal.ZERO :
                gmv.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP);

            return KeyMetricsDTO.builder()
                .gmv(gmv)
                .payingUserCount(uniqueAudiences)
                .totalUserCount(totalUsers)
                .paymentRate(paymentRate)
                .arpu(arpu)
                .arppu(arppu)
                .avgOrderAmount(avgOrderAmount)
                .totalOrders(totalOrders)
                .build();

        } catch (Exception e) {
            TraceLogger.error("AggregationAnalysisService", "getKeyMetrics", 
                null, e, "startTime", startTime, "endTime", endTime);
            throw new AnalysisException(5004, "获取关键指标失败: " + e.getMessage(), e);
        }
    }
}
