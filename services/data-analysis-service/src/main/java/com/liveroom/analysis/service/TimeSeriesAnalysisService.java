package com.liveroom.analysis.service;

import com.liveroom.analysis.dto.TimeSeriesDataDTO;
import common.exception.AnalysisException;
import common.logger.TraceLogger;
import common.service.DataAccessFacade;
import common.service.AnalysisQueryService.RechargeStats;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 时间序列分析Service
 */
@Service
@RequiredArgsConstructor
public class TimeSeriesAnalysisService {

    private final DataAccessFacade dataAccessFacade;

    /**
     * 获取每日时间序列数据
     */
    @Cacheable(value = "analysis:timeseries", key = "'daily:' + #startTime + ':' + #endTime")
    public TimeSeriesDataDTO getDailyTimeSeries(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            TraceLogger.info("TimeSeriesAnalysisService", "getDailyTimeSeries", 
                null, "startTime", startTime, "endTime", endTime);

            Map<String, RechargeStats> statsByDay = dataAccessFacade.analysisQuery()
                .getRechargeStatsByDay(startTime, endTime);

            List<String> timePoints = new ArrayList<>(statsByDay.keySet());
            timePoints.sort(String::compareTo);

            List<BigDecimal> values = new ArrayList<>();
            BigDecimal total = BigDecimal.ZERO;
            BigDecimal maxValue = BigDecimal.ZERO;
            BigDecimal minValue = new BigDecimal(Long.MAX_VALUE);

            for (String timePoint : timePoints) {
                BigDecimal value = statsByDay.get(timePoint).getTotalAmount();
                values.add(value);
                total = total.add(value);
                
                if (value.compareTo(maxValue) > 0) {
                    maxValue = value;
                }
                if (value.compareTo(minValue) < 0) {
                    minValue = value;
                }
            }

            BigDecimal average = timePoints.isEmpty() ? BigDecimal.ZERO :
                total.divide(BigDecimal.valueOf(timePoints.size()), 2, BigDecimal.ROUND_HALF_UP);

            return TimeSeriesDataDTO.builder()
                .timePoints(timePoints)
                .values(values)
                .dataType("daily")
                .startTime(startTime)
                .endTime(endTime)
                .total(total)
                .average(average)
                .maxValue(maxValue)
                .minValue(minValue.equals(new BigDecimal(Long.MAX_VALUE)) ? BigDecimal.ZERO : minValue)
                .build();

        } catch (Exception e) {
            TraceLogger.error("TimeSeriesAnalysisService", "getDailyTimeSeries", 
                null, e, "startTime", startTime, "endTime", endTime);
            throw new AnalysisException(5001, "获取每日时间序列数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取每小时时间序列数据
     */
    @Cacheable(value = "analysis:timeseries", key = "'hourly:' + #startTime + ':' + #endTime")
    public TimeSeriesDataDTO getHourlyTimeSeries(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            TraceLogger.info("TimeSeriesAnalysisService", "getHourlyTimeSeries", 
                null, "startTime", startTime, "endTime", endTime);

            Map<String, RechargeStats> statsByHour = dataAccessFacade.analysisQuery()
                .getRechargeStatsByHour(startTime, endTime);

            List<String> timePoints = new ArrayList<>(statsByHour.keySet());
            timePoints.sort(String::compareTo);

            List<BigDecimal> values = new ArrayList<>();
            BigDecimal total = BigDecimal.ZERO;

            for (String timePoint : timePoints) {
                BigDecimal value = statsByHour.get(timePoint).getTotalAmount();
                values.add(value);
                total = total.add(value);
            }

            return TimeSeriesDataDTO.builder()
                .timePoints(timePoints)
                .values(values)
                .dataType("hourly")
                .startTime(startTime)
                .endTime(endTime)
                .total(total)
                .build();

        } catch (Exception e) {
            TraceLogger.error("TimeSeriesAnalysisService", "getHourlyTimeSeries", 
                null, e, "startTime", startTime, "endTime", endTime);
            throw new AnalysisException(5002, "获取每小时时间序列数据失败: " + e.getMessage(), e);
        }
    }
}
