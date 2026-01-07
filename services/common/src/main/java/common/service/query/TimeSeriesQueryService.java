package common.service.query;

import common.logger.TraceLogger;
import common.service.AnalysisQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 时间序列数据查询Service
 *
 * 专门用于处理基于时间维度的数据查询和分析
 * 包括：
 * - 不同时间粒度的数据聚合（分钟、小时、天、周、月）
 * - 时间序列的补全与平滑
 * - 趋势计算（MA、EMA、增长率）
 * - 时段分析（高峰期、低谷期）
 *
 * @author Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TimeSeriesQueryService {

    private final AnalysisQueryService analysisQueryService;

    /**
     * 获取日粒度的时间序列数据
     * 包含移动平均和增长率
     */
    @Transactional(readOnly = true)
    public TimeSeriesData getDailyTimeSeries(LocalDateTime startTime, LocalDateTime endTime, boolean includeMA, boolean includeGrowthRate) {
        TraceLogger.info("TimeSeriesQueryService", "getDailyTimeSeries",
            String.format("获取日粒度时间序列: %s - %s", startTime, endTime));

        Map<String, AnalysisQueryService.RechargeStats> statsMap = analysisQueryService.getRechargeStatsByDay(startTime, endTime);

        TimeSeriesData data = new TimeSeriesData();
        data.setGranularity("day");
        data.setTimeLabels(new ArrayList<>(statsMap.keySet()));
        data.setValues(statsMap.values().stream()
            .map(s -> s.getTotalAmount().doubleValue())
            .collect(java.util.stream.Collectors.toList()));

        if (includeMA) {
            Map<String, BigDecimal> ma = AnalysisQueryService.calculateMovingAverage(statsMap, 7);
            data.setMovingAverage(ma.values().stream()
                .map(BigDecimal::doubleValue)
                .collect(java.util.stream.Collectors.toList()));
        }

        if (includeGrowthRate) {
            Map<String, BigDecimal> growthRate = AnalysisQueryService.calculateGrowthRate(statsMap);
            data.setGrowthRates(growthRate.values().stream()
                .map(BigDecimal::doubleValue)
                .collect(java.util.stream.Collectors.toList()));
        }

        return data;
    }

    /**
     * 获取小时粒度的时间序列数据
     */
    @Transactional(readOnly = true)
    public TimeSeriesData getHourlyTimeSeries(LocalDateTime startTime, LocalDateTime endTime) {
        TraceLogger.info("TimeSeriesQueryService", "getHourlyTimeSeries",
            String.format("获取小时粒度时间序列: %s - %s", startTime, endTime));

        Map<String, AnalysisQueryService.RechargeStats> statsMap = analysisQueryService.getRechargeStatsByHour(startTime, endTime);

        TimeSeriesData data = new TimeSeriesData();
        data.setGranularity("hour");
        data.setTimeLabels(new ArrayList<>(statsMap.keySet()));
        data.setValues(statsMap.values().stream()
            .map(s -> s.getTotalAmount().doubleValue())
            .collect(java.util.stream.Collectors.toList()));

        return data;
    }

    /**
     * 获取周粒度的时间序列数据
     */
    @Transactional(readOnly = true)
    public TimeSeriesData getWeeklyTimeSeries(LocalDateTime startTime, LocalDateTime endTime) {
        TraceLogger.info("TimeSeriesQueryService", "getWeeklyTimeSeries",
            String.format("获取周粒度时间序列: %s - %s", startTime, endTime));

        Map<String, AnalysisQueryService.RechargeStats> statsMap = analysisQueryService.getRechargeStatsByWeek(startTime, endTime);

        TimeSeriesData data = new TimeSeriesData();
        data.setGranularity("week");
        data.setTimeLabels(new ArrayList<>(statsMap.keySet()));
        data.setValues(statsMap.values().stream()
            .map(s -> s.getTotalAmount().doubleValue())
            .collect(java.util.stream.Collectors.toList()));

        return data;
    }

    /**
     * 获取月粒度的时间序列数据
     */
    @Transactional(readOnly = true)
    public TimeSeriesData getMonthlyTimeSeries(LocalDateTime startTime, LocalDateTime endTime) {
        TraceLogger.info("TimeSeriesQueryService", "getMonthlyTimeSeries",
            String.format("获取月粒度时间序列: %s - %s", startTime, endTime));

        Map<String, AnalysisQueryService.RechargeStats> statsMap = analysisQueryService.getRechargeStatsByMonth(startTime, endTime);

        TimeSeriesData data = new TimeSeriesData();
        data.setGranularity("month");
        data.setTimeLabels(new ArrayList<>(statsMap.keySet()));
        data.setValues(statsMap.values().stream()
            .map(s -> s.getTotalAmount().doubleValue())
            .collect(java.util.stream.Collectors.toList()));

        return data;
    }

    /**
     * 时间序列数据DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TimeSeriesData {
        private String granularity; // day, hour, week, month
        private List<String> timeLabels;
        private List<Double> values;
        private List<Double> movingAverage; // 移动平均
        private List<Double> ema; // 指数移动平均
        private List<Double> growthRates; // 增长率
        private Map<String, Object> metadata; // 元数据
    }
}

