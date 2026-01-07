package com.liveroom.analysis.service;

import com.liveroom.analysis.dto.HeatmapDataDTO;
import common.bean.Recharge;
import common.exception.AnalysisException;
import common.logger.TraceLogger;
import common.service.AnalysisQueryService.RechargeStats;
import common.service.DataAccessFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 热力图分析Service
 * 提供时段热力图、活跃度热力图等分析
 */
@Service
@RequiredArgsConstructor
public class HeatmapAnalysisService {

    private final DataAccessFacade dataAccessFacade;

    /**
     * 获取时段热力图数据（一周内每小时的打赏活跃度）
     */
    @Cacheable(value = "analysis:heatmap", key = "'hourly:' + #startTime + ':' + #endTime")
    public HeatmapDataDTO getHourlyHeatmap(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            TraceLogger.info("HeatmapAnalysisService", "getHourlyHeatmap",
                null, "startTime", startTime, "endTime", endTime);

            List<Recharge> recharges = dataAccessFacade.analysisQuery()
                .getRechargesByTimeRange(startTime, endTime);

            // 按星期和小时分组统计
            Map<DayOfWeek, Map<Integer, BigDecimal>> heatmapData = new HashMap<>();
            
            for (Recharge recharge : recharges) {
                DayOfWeek dayOfWeek = recharge.getRechargeTime().getDayOfWeek();
                int hour = recharge.getRechargeTime().getHour();
                
                heatmapData.computeIfAbsent(dayOfWeek, k -> new HashMap<>())
                    .merge(hour, recharge.getRechargeAmount(), BigDecimal::add);
            }

            // 构建热力图数据
            List<String> xAxis = new ArrayList<>(); // 小时
            for (int i = 0; i < 24; i++) {
                xAxis.add(String.format("%02d:00", i));
            }

            List<String> yAxis = Arrays.asList(
                "周一", "周二", "周三", "周四", "周五", "周六", "周日"
            );

            List<List<Object>> data = new ArrayList<>();
            double maxValue = 0.0;
            double minValue = Double.MAX_VALUE;

            // 构建数据矩阵
            DayOfWeek[] weekDays = {
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
            };

            for (int dayIndex = 0; dayIndex < weekDays.length; dayIndex++) {
                DayOfWeek day = weekDays[dayIndex];
                Map<Integer, BigDecimal> hourData = heatmapData.getOrDefault(day, new HashMap<>());
                
                for (int hour = 0; hour < 24; hour++) {
                    BigDecimal value = hourData.getOrDefault(hour, BigDecimal.ZERO);
                    double doubleValue = value.doubleValue();
                    
                    data.add(Arrays.asList(hour, dayIndex, doubleValue));
                    
                    if (doubleValue > maxValue) maxValue = doubleValue;
                    if (doubleValue < minValue && doubleValue > 0) minValue = doubleValue;
                }
            }

            if (minValue == Double.MAX_VALUE) minValue = 0.0;

            return HeatmapDataDTO.builder()
                .xAxis(xAxis)
                .yAxis(yAxis)
                .data(data)
                .maxValue(maxValue)
                .minValue(minValue)
                .dataType("hourly")
                .build();

        } catch (Exception e) {
            TraceLogger.error("HeatmapAnalysisService", "getHourlyHeatmap", null, e);
            throw new AnalysisException(5030, "获取时段热力图失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取主播时段热力图（某个主播的打赏活跃时段）
     */
    @Cacheable(value = "analysis:heatmap", key = "'anchor-hourly:' + #anchorId + ':' + #startTime + ':' + #endTime")
    public HeatmapDataDTO getAnchorHourlyHeatmap(Long anchorId, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            TraceLogger.info("HeatmapAnalysisService", "getAnchorHourlyHeatmap",
                anchorId, "startTime", startTime, "endTime", endTime);

            List<Recharge> recharges = dataAccessFacade.analysisQuery()
                .getRechargesByAnchorAndTimeRange(anchorId, startTime, endTime);

            // 按星期和小时分组统计
            Map<DayOfWeek, Map<Integer, Integer>> heatmapData = new HashMap<>();
            
            for (Recharge recharge : recharges) {
                DayOfWeek dayOfWeek = recharge.getRechargeTime().getDayOfWeek();
                int hour = recharge.getRechargeTime().getHour();
                
                heatmapData.computeIfAbsent(dayOfWeek, k -> new HashMap<>())
                    .merge(hour, 1, Integer::sum);
            }

            // 构建热力图数据
            List<String> xAxis = new ArrayList<>();
            for (int i = 0; i < 24; i++) {
                xAxis.add(String.format("%02d:00", i));
            }

            List<String> yAxis = Arrays.asList(
                "周一", "周二", "周三", "周四", "周五", "周六", "周日"
            );

            List<List<Object>> data = new ArrayList<>();
            double maxValue = 0.0;
            double minValue = Double.MAX_VALUE;

            DayOfWeek[] weekDays = {
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
            };

            for (int dayIndex = 0; dayIndex < weekDays.length; dayIndex++) {
                DayOfWeek day = weekDays[dayIndex];
                Map<Integer, Integer> hourData = heatmapData.getOrDefault(day, new HashMap<>());
                
                for (int hour = 0; hour < 24; hour++) {
                    int value = hourData.getOrDefault(hour, 0);
                    double doubleValue = (double) value;
                    
                    data.add(Arrays.asList(hour, dayIndex, doubleValue));
                    
                    if (doubleValue > maxValue) maxValue = doubleValue;
                    if (doubleValue < minValue && doubleValue > 0) minValue = doubleValue;
                }
            }

            if (minValue == Double.MAX_VALUE) minValue = 0.0;

            return HeatmapDataDTO.builder()
                .xAxis(xAxis)
                .yAxis(yAxis)
                .data(data)
                .maxValue(maxValue)
                .minValue(minValue)
                .dataType("anchor-hourly")
                .build();

        } catch (Exception e) {
            TraceLogger.error("HeatmapAnalysisService", "getAnchorHourlyHeatmap", anchorId, e);
            throw new AnalysisException(5031, "获取主播时段热力图失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取每日热力图（一个月内每天的活跃度）
     */
    @Cacheable(value = "analysis:heatmap", key = "'daily:' + #startTime + ':' + #endTime")
    public HeatmapDataDTO getDailyHeatmap(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            TraceLogger.info("HeatmapAnalysisService", "getDailyHeatmap",
                null, "startTime", startTime, "endTime", endTime);

            Map<String, RechargeStats> statsByDay = dataAccessFacade.analysisQuery()
                .getRechargeStatsByDay(startTime, endTime);

            List<String> dates = new ArrayList<>(statsByDay.keySet());
            dates.sort(String::compareTo);

            List<String> xAxis = dates;
            List<String> yAxis = Arrays.asList("打赏金额");

            List<List<Object>> data = new ArrayList<>();
            double maxValue = 0.0;
            double minValue = Double.MAX_VALUE;

            for (int i = 0; i < dates.size(); i++) {
                String date = dates.get(i);
                BigDecimal value = statsByDay.get(date).getTotalAmount();
                double doubleValue = value.doubleValue();
                
                data.add(Arrays.asList(i, 0, doubleValue));
                
                if (doubleValue > maxValue) maxValue = doubleValue;
                if (doubleValue < minValue && doubleValue > 0) minValue = doubleValue;
            }

            if (minValue == Double.MAX_VALUE) minValue = 0.0;

            return HeatmapDataDTO.builder()
                .xAxis(xAxis)
                .yAxis(yAxis)
                .data(data)
                .maxValue(maxValue)
                .minValue(minValue)
                .dataType("daily")
                .build();

        } catch (Exception e) {
            TraceLogger.error("HeatmapAnalysisService", "getDailyHeatmap", null, e);
            throw new AnalysisException(5032, "获取每日热力图失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取活跃用户热力图（按消费金额分段的用户分布）
     */
    @Cacheable(value = "analysis:heatmap", key = "'user-activity:' + #startTime + ':' + #endTime")
    public Map<String, Object> getUserActivityHeatmap(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            TraceLogger.info("HeatmapAnalysisService", "getUserActivityHeatmap",
                null, "startTime", startTime, "endTime", endTime);

            List<Recharge> recharges = dataAccessFacade.analysisQuery()
                .getRechargesByTimeRange(startTime, endTime);

            // 按用户统计消费
            Map<Long, BigDecimal> userConsumption = recharges.stream()
                .collect(Collectors.groupingBy(
                    Recharge::getAudienceId,
                    Collectors.reducing(
                        BigDecimal.ZERO,
                        Recharge::getRechargeAmount,
                        BigDecimal::add
                    )
                ));

            // 分段统计
            int[] ranges = {0, 50, 100, 200, 500, 1000, 2000, 5000, 10000};
            Map<String, Integer> distribution = new LinkedHashMap<>();
            
            for (int i = 0; i < ranges.length - 1; i++) {
                String key = ranges[i] + "-" + ranges[i + 1];
                distribution.put(key, 0);
            }
            distribution.put("10000+", 0);

            // 统计各区间用户数
            for (BigDecimal amount : userConsumption.values()) {
                double value = amount.doubleValue();
                boolean found = false;
                
                for (int i = 0; i < ranges.length - 1; i++) {
                    if (value >= ranges[i] && value < ranges[i + 1]) {
                        String key = ranges[i] + "-" + ranges[i + 1];
                        distribution.merge(key, 1, Integer::sum);
                        found = true;
                        break;
                    }
                }
                
                if (!found && value >= 10000) {
                    distribution.merge("10000+", 1, Integer::sum);
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("distribution", distribution);
            result.put("totalUsers", userConsumption.size());
            result.put("totalAmount", recharges.stream()
                .map(Recharge::getRechargeAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

            return result;

        } catch (Exception e) {
            TraceLogger.error("HeatmapAnalysisService", "getUserActivityHeatmap", null, e);
            throw new AnalysisException(5033, "获取用户活跃热力图失败: " + e.getMessage(), e);
        }
    }
}
