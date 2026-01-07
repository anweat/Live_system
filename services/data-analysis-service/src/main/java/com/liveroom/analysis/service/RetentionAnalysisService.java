package com.liveroom.analysis.service;

import com.liveroom.analysis.dto.RetentionAnalysisDTO;
import common.bean.user.Audience;
import common.bean.Recharge;
import common.exception.AnalysisException;
import common.logger.TraceLogger;
import common.service.DataAccessFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户留存分析Service
 * 提供次日/7日/30日留存率及流失预警分析
 */
@Service
@RequiredArgsConstructor
public class RetentionAnalysisService {

    private final DataAccessFacade dataAccessFacade;

    /**
     * 获取用户留存分析
     */
    @Cacheable(value = "analysis:retention", key = "'retention:' + #analysisDate")
    public RetentionAnalysisDTO getRetentionAnalysis(LocalDate analysisDate) {
        try {
            TraceLogger.info("RetentionAnalysisService", "getRetentionAnalysis", null, "analysisDate", analysisDate);

            LocalDateTime startTime = analysisDate.atStartOfDay();
            LocalDateTime endTime = analysisDate.plusDays(30).atStartOfDay(); // 分析30天内的留存

            // 获取分析日期当天新增的用户
            List<Audience> newUsers = dataAccessFacade.audience()
                .findByCreatedDate(analysisDate);

            int newUsersCount = newUsers.size();
            if (newUsersCount == 0) {
                return buildEmptyRetentionAnalysis(analysisDate);
            }

            // 获取这些用户在后续日期的活跃情况
            List<Recharge> recharges = dataAccessFacade.analysisQuery()
                .getRechargesByTimeRange(startTime, endTime);

            Map<Long, List<Recharge>> userRecharges = recharges.stream()
                .collect(Collectors.groupingBy(Recharge::getAudienceId));

            // 计算各日留存
            int day1Retained = 0;
            int day7Retained = 0;
            int day30Retained = 0;

            for (Audience newUser : newUsers) {
                List<Recharge> userActivity = userRecharges.getOrDefault(newUser.getUserId(), new ArrayList<>());
                
                if (userActivity.isEmpty()) continue;

                // 检查次日留存（第二天是否有活跃）
                if (hasActivityOnDay(userActivity, analysisDate.plusDays(1))) {
                    day1Retained++;
                }

                // 检查7日留存
                if (hasActivityInPeriod(userActivity, analysisDate.plusDays(1), analysisDate.plusDays(7))) {
                    day7Retained++;
                }

                // 检查30日留存
                if (hasActivityInPeriod(userActivity, analysisDate.plusDays(1), analysisDate.plusDays(30))) {
                    day30Retained++;
                }
            }

            // 计算留存率
            BigDecimal day1Retention = newUsersCount > 0 ?
                BigDecimal.valueOf(day1Retained)
                    .divide(BigDecimal.valueOf(newUsersCount), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")) :
                BigDecimal.ZERO;

            BigDecimal day7Retention = newUsersCount > 0 ?
                BigDecimal.valueOf(day7Retained)
                    .divide(BigDecimal.valueOf(newUsersCount), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")) :
                BigDecimal.ZERO;

            BigDecimal day30Retention = newUsersCount > 0 ?
                BigDecimal.valueOf(day30Retained)
                    .divide(BigDecimal.valueOf(newUsersCount), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")) :
                BigDecimal.ZERO;

            // 生成留存曲线
            Map<Integer, BigDecimal> retentionCurve = generateRetentionCurve(newUsers, userRecharges, analysisDate);

            // 流失预警分析
            var churnRiskAnalysis = analyzeChurnRisk(newUsers, userRecharges, analysisDate);

            return RetentionAnalysisDTO.builder()
                .day1Retention(day1Retention)
                .day7Retention(day7Retention)
                .day30Retention(day30Retention)
                .retentionCurve(retentionCurve)
                .highRiskUsers((Integer) churnRiskAnalysis.get("highRiskUsers"))
                .mediumRiskUsers((Integer) churnRiskAnalysis.get("mediumRiskUsers"))
                .lowRiskUsers((Integer) churnRiskAnalysis.get("lowRiskUsers"))
                .churnProbabilityDistribution((Map<String, Integer>) churnRiskAnalysis.get("churnDistribution"))
                .highRiskUserIds((List<Long>) churnRiskAnalysis.get("highRiskUserIds"))
                .analysisStartTime(startTime)
                .analysisEndTime(endTime)
                .newUsersCount(newUsersCount)
                .activeUsersCount(userRecharges.size())
                .build();

        } catch (Exception e) {
            TraceLogger.error("RetentionAnalysisService", "getRetentionAnalysis", null, e);
            throw new AnalysisException(5050, "获取留存分析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检查用户在指定日期是否有活跃
     */
    private boolean hasActivityOnDay(List<Recharge> recharges, LocalDate targetDate) {
        return recharges.stream()
            .anyMatch(r -> r.getRechargeTime().toLocalDate().equals(targetDate));
    }

    /**
     * 检查用户在指定时间段内是否有活跃
     */
    private boolean hasActivityInPeriod(List<Recharge> recharges, LocalDate startDate, LocalDate endDate) {
        return recharges.stream()
            .anyMatch(r -> {
                LocalDate activityDate = r.getRechargeTime().toLocalDate();
                return !activityDate.isBefore(startDate) && !activityDate.isAfter(endDate);
            });
    }

    /**
     * 生成留存曲线
     */
    private Map<Integer, BigDecimal> generateRetentionCurve(List<Audience> newUsers, 
                                                           Map<Long, List<Recharge>> userRecharges, 
                                                           LocalDate startDate) {
        Map<Integer, BigDecimal> curve = new HashMap<>();
        
        for (int day = 1; day <= 30; day++) {
            LocalDate targetDate = startDate.plusDays(day);
            int retained = 0;
            
            for (Audience newUser : newUsers) {
                List<Recharge> userActivity = userRecharges.getOrDefault(newUser.getUserId(), new ArrayList<>());
                if (hasActivityOnDay(userActivity, targetDate)) {
                    retained++;
                }
            }
            
            BigDecimal retentionRate = newUsers.size() > 0 ?
                BigDecimal.valueOf(retained)
                    .divide(BigDecimal.valueOf(newUsers.size()), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")) :
                BigDecimal.ZERO;
            
            curve.put(day, retentionRate);
        }
        
        return curve;
    }

    /**
     * 流失风险分析
     */
    private Map<String, Object> analyzeChurnRisk(List<Audience> newUsers, 
                                                Map<Long, List<Recharge>> userRecharges, 
                                                LocalDate analysisDate) {
        Map<String, Object> result = new HashMap<>();
        
        int highRiskUsers = 0;
        int mediumRiskUsers = 0;
        int lowRiskUsers = 0;
        
        List<Long> highRiskUserIds = new ArrayList<>();
        Map<String, Integer> churnDistribution = new HashMap<>();
        
        for (Audience user : newUsers) {
            List<Recharge> userActivity = userRecharges.getOrDefault(user.getUserId(), new ArrayList<>());
            double churnProbability = calculateChurnProbability(userActivity, analysisDate);
            
            if (churnProbability > 0.7) {
                highRiskUsers++;
                highRiskUserIds.add(user.getUserId());
                churnDistribution.merge("高风险", 1, Integer::sum);
            } else if (churnProbability > 0.4) {
                mediumRiskUsers++;
                churnDistribution.merge("中风险", 1, Integer::sum);
            } else {
                lowRiskUsers++;
                churnDistribution.merge("低风险", 1, Integer::sum);
            }
        }
        
        result.put("highRiskUsers", highRiskUsers);
        result.put("mediumRiskUsers", mediumRiskUsers);
        result.put("lowRiskUsers", lowRiskUsers);
        result.put("churnDistribution", churnDistribution);
        result.put("highRiskUserIds", highRiskUserIds);
        
        return result;
    }

    /**
     * 计算流失概率（简化版逻辑）
     * 基于最近活跃天数、消费频次等因素
     */
    private double calculateChurnProbability(List<Recharge> recharges, LocalDate analysisDate) {
        if (recharges.isEmpty()) {
            return 1.0; // 无活跃用户，流失概率100%
        }

        // 获取最后活跃时间
        Optional<LocalDateTime> lastActivity = recharges.stream()
            .map(Recharge::getRechargeTime)
            .max(LocalDateTime::compareTo);

        if (!lastActivity.isPresent()) {
            return 1.0;
        }

        long daysSinceLastActivity = ChronoUnit.DAYS.between(
            lastActivity.get().toLocalDate(), analysisDate);

        // 基于未活跃天数计算流失概率
        // 超过14天未活跃，流失概率较高
        if (daysSinceLastActivity > 14) {
            return Math.min(0.9, 0.5 + (daysSinceLastActivity - 14) * 0.05);
        } else if (daysSinceLastActivity > 7) {
            return Math.min(0.7, 0.3 + (daysSinceLastActivity - 7) * 0.08);
        } else {
            return Math.max(0.1, 0.3 - daysSinceLastActivity * 0.02);
        }
    }

    /**
     * 构建空留存分析
     */
    private RetentionAnalysisDTO buildEmptyRetentionAnalysis(LocalDate analysisDate) {
        return RetentionAnalysisDTO.builder()
            .day1Retention(BigDecimal.ZERO)
            .day7Retention(BigDecimal.ZERO)
            .day30Retention(BigDecimal.ZERO)
            .retentionCurve(new HashMap<>())
            .highRiskUsers(0)
            .mediumRiskUsers(0)
            .lowRiskUsers(0)
            .churnProbabilityDistribution(new HashMap<>())
            .highRiskUserIds(new ArrayList<>())
            .analysisStartTime(analysisDate.atStartOfDay())
            .analysisEndTime(analysisDate.plusDays(30).atStartOfDay())
            .newUsersCount(0)
            .activeUsersCount(0)
            .build();
    }

    /**
     * 获取指定时间段的留存分析
     */
    @Cacheable(value = "analysis:retention", key = "'period:' + #startTime + ':' + #endTime")
    public List<RetentionAnalysisDTO> getPeriodRetentionAnalysis(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            TraceLogger.info("RetentionAnalysisService", "getPeriodRetentionAnalysis",
                null, "startTime", startTime, "endTime", endTime);

            List<RetentionAnalysisDTO> results = new ArrayList<>();
            LocalDate start = startTime.toLocalDate();
            LocalDate end = endTime.toLocalDate();

            for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                results.add(getRetentionAnalysis(date));
            }

            return results;

        } catch (Exception e) {
            TraceLogger.error("RetentionAnalysisService", "getPeriodRetentionAnalysis", null, e);
            throw new AnalysisException(5051, "获取时间段留存分析失败: " + e.getMessage(), e);
        }
    }
}
