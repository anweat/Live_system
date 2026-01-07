package com.liveroom.analysis.service;

import com.liveroom.analysis.dto.AnchorIncomeAnalysisDTO;
import common.bean.user.Anchor;
import common.bean.Recharge;
import common.exception.AnalysisException;
import common.logger.TraceLogger;
import common.service.AnalysisQueryService.RechargeStats;
import common.service.DataAccessFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 主播收入分析Service
 * 提供主播收入趋势、稳定性、增长率等分析
 */
@Service
@RequiredArgsConstructor
public class AnchorIncomeAnalysisService {

    private final DataAccessFacade dataAccessFacade;

    /**
     * 获取主播收入分析
     */
    @Cacheable(value = "analysis:anchor-income", key = "'analysis:' + #anchorId + ':' + #startTime + ':' + #endTime")
    public AnchorIncomeAnalysisDTO getAnchorIncomeAnalysis(Long anchorId, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            TraceLogger.info("AnchorIncomeAnalysisService", "getAnchorIncomeAnalysis",
                anchorId, "startTime", startTime, "endTime", endTime);

            // 获取主播基本信息
            Anchor anchor = dataAccessFacade.anchor()
                .findById(anchorId)
                .orElseThrow(() -> new AnalysisException(5040, "主播不存在"));

            // 获取主播的打赏数据
            List<Recharge> recharges = dataAccessFacade.analysisQuery()
                .getRechargesByAnchorAndTimeRange(anchorId, startTime, endTime);

            if (recharges.isEmpty()) {
                return buildEmptyAnalysis(anchorId, anchor.getNickname());
            }

            // 按日期分组统计收入
            Map<String, RechargeStats> dailyStats = dataAccessFacade.analysisQuery()
                .getAnchorRechargeStatsByDay(anchorId, startTime, endTime);

            // 计算总收入（基于打赏金额）
            BigDecimal totalRechargeAmount = recharges.stream()
                .map(Recharge::getRechargeAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 计算打赏次数和唯一付费用户数
            int rechargeCount = recharges.size();
            int uniquePayers = (int) recharges.stream()
                .map(Recharge::getAudienceId)
                .distinct()
                .count();

            // 计算每日收入趋势
            Map<String, BigDecimal> dailyTrend = dailyStats.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> entry.getValue().getTotalAmount()
                ));

            // 计算平均日收入
            BigDecimal avgDailyIncome = dailyTrend.isEmpty() ? BigDecimal.ZERO :
                totalRechargeAmount.divide(
                    BigDecimal.valueOf(dailyTrend.size()), 2, RoundingMode.HALF_UP);

            // 计算最高/最低日收入
            BigDecimal maxDailyIncome = dailyTrend.values().stream()
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

            BigDecimal minDailyIncome = dailyTrend.values().stream()
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

            // 计算收入稳定性（变异系数CV）
            BigDecimal stabilityScore = calculateStabilityScore(dailyTrend);
            String stabilityLevel = classifyStabilityLevel(stabilityScore);

            // 计算增长率（与上一时期对比）
            BigDecimal growthRate = calculateGrowthRate(dailyTrend);

            // 计算人均粉丝价值（需要获取粉丝数）
            int fansCount = anchor.getFanCount() != null ? anchor.getFanCount().intValue() : 1;
            BigDecimal fanValue = fansCount > 0 ? 
                totalRechargeAmount.divide(BigDecimal.valueOf(fansCount), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

            // 计算打赏转化率（需要直播间观看数据）
            BigDecimal conversionRate = BigDecimal.ZERO; // 简化实现

            // 计算移动平均
            Map<String, BigDecimal> ma7 = calculateMovingAverage(dailyTrend, 7);
            Map<String, BigDecimal> ma30 = calculateMovingAverage(dailyTrend, 30);

            return AnchorIncomeAnalysisDTO.builder()
                .anchorId(anchorId)
                .anchorName(anchor.getNickname())
                .totalIncome(totalRechargeAmount)
                .avgDailyIncome(avgDailyIncome)
                .maxDailyIncome(maxDailyIncome)
                .minDailyIncome(minDailyIncome)
                .stabilityScore(stabilityScore)
                .stabilityLevel(stabilityLevel)
                .growthRate(growthRate)
                .rechargeCount(rechargeCount)
                .uniquePayers(uniquePayers)
                .fanValue(fanValue)
                .conversionRate(conversionRate)
                .dailyTrend(dailyTrend)
                .ma7(ma7)
                .ma30(ma30)
                .build();

        } catch (AnalysisException e) {
            throw e;
        } catch (Exception e) {
            TraceLogger.error("AnchorIncomeAnalysisService", "getAnchorIncomeAnalysis", anchorId, e);
            throw new AnalysisException(5041, "获取主播收入分析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 计算收入稳定性（变异系数CV）
     * CV = 标准差 / 平均值
     * CV < 0.3: 收入稳定
     * 0.3 ≤ CV < 0.5: 收入一般
     * CV ≥ 0.5: 收入波动大
     */
    private BigDecimal calculateStabilityScore(Map<String, BigDecimal> dailyIncome) {
        if (dailyIncome.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // 计算平均值
        BigDecimal sum = dailyIncome.values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal mean = sum.divide(BigDecimal.valueOf(dailyIncome.size()), 10, RoundingMode.HALF_UP);

        if (mean.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        // 计算方差
        BigDecimal variance = dailyIncome.values().stream()
            .map(value -> value.subtract(mean).pow(2))
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(dailyIncome.size()), 10, RoundingMode.HALF_UP);

        // 计算标准差
        double stdDev = Math.sqrt(variance.doubleValue());
        double meanValue = mean.doubleValue();
        double cv = meanValue != 0 ? stdDev / meanValue : 0;

        return BigDecimal.valueOf(cv).setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * 分类稳定性等级
     */
    private String classifyStabilityLevel(BigDecimal cv) {
        if (cv.compareTo(new BigDecimal("0.3")) < 0) {
            return "稳定";
        } else if (cv.compareTo(new BigDecimal("0.5")) < 0) {
            return "一般";
        } else {
            return "波动大";
        }
    }

    /**
     * 计算增长率
     */
    private BigDecimal calculateGrowthRate(Map<String, BigDecimal> dailyTrend) {
        if (dailyTrend.size() < 2) {
            return BigDecimal.ZERO;
        }

        List<String> sortedDates = dailyTrend.keySet().stream()
            .sorted()
            .collect(Collectors.toList());

        BigDecimal lastValue = dailyTrend.get(sortedDates.get(sortedDates.size() - 1));
        BigDecimal prevValue = dailyTrend.get(sortedDates.get(sortedDates.size() - 2));

        if (prevValue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal growth = lastValue.subtract(prevValue)
            .divide(prevValue, 4, RoundingMode.HALF_UP)
            .multiply(new BigDecimal("100"));

        return growth.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 计算移动平均
     */
    private Map<String, BigDecimal> calculateMovingAverage(Map<String, BigDecimal> data, int windowSize) {
        TraceLogger.debug("AnchorIncomeAnalysisService", "calculateMovingAverage",
            String.format("计算%d周期移动平均", windowSize));

        List<String> keys = data.keySet().stream()
            .sorted()
            .collect(Collectors.toList());

        Map<String, BigDecimal> maMap = new LinkedHashMap<>();
        for (int i = 0; i < keys.size(); i++) {
            int start = Math.max(0, i - windowSize + 1);
            BigDecimal sum = BigDecimal.ZERO;
            for (int j = start; j <= i; j++) {
                sum = sum.add(data.get(keys.get(j)));
            }
            int count = i - start + 1;
            BigDecimal ma = sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
            maMap.put(keys.get(i), ma);
        }
        return maMap;
    }

    /**
     * 构建空分析（无数据）
     */
    private AnchorIncomeAnalysisDTO buildEmptyAnalysis(Long anchorId, String anchorName) {
        return AnchorIncomeAnalysisDTO.builder()
            .anchorId(anchorId)
            .anchorName(anchorName)
            .totalIncome(BigDecimal.ZERO)
            .avgDailyIncome(BigDecimal.ZERO)
            .maxDailyIncome(BigDecimal.ZERO)
            .minDailyIncome(BigDecimal.ZERO)
            .stabilityScore(BigDecimal.ZERO)
            .stabilityLevel("无数据")
            .growthRate(BigDecimal.ZERO)
            .rechargeCount(0)
            .uniquePayers(0)
            .fanValue(BigDecimal.ZERO)
            .conversionRate(BigDecimal.ZERO)
            .dailyTrend(new HashMap<>())
            .ma7(new HashMap<>())
            .ma30(new HashMap<>())
            .build();
    }

    /**
     * 获取主播收入排行榜
     */
    @Cacheable(value = "analysis:anchor-income", key = "'ranking:' + #startTime + ':' + #endTime + ':' + #limit")
    public List<AnchorIncomeAnalysisDTO> getTopAnchorsByIncome(LocalDateTime startTime, LocalDateTime endTime, int limit) {
        try {
            TraceLogger.info("AnchorIncomeAnalysisService", "getTopAnchorsByIncome",
                null, "startTime", startTime, "endTime", endTime, "limit", limit);

            // 获取所有主播的收入数据
            List<Recharge> allRecharges = dataAccessFacade.analysisQuery()
                .getRechargesByTimeRange(startTime, endTime);

            // 按主播分组统计收入
            Map<Long, BigDecimal> anchorIncome = allRecharges.stream()
                .collect(Collectors.groupingBy(
                    Recharge::getAnchorId,
                    Collectors.reducing(
                        BigDecimal.ZERO,
                        Recharge::getRechargeAmount,
                        BigDecimal::add
                    )
                ));

            // 获取主播信息并构建分析数据
            return anchorIncome.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue())) // 按收入降序
                .limit(limit)
                .map(entry -> {
                    Long anchorId = entry.getKey();
                    try {
                        return getAnchorIncomeAnalysis(anchorId, startTime, endTime);
                    } catch (Exception e) {
                        TraceLogger.warn("AnchorIncomeAnalysisService", "getTopAnchorsByIncome", 
                            anchorId, "获取单个主播分析失败", e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        } catch (Exception e) {
            TraceLogger.error("AnchorIncomeAnalysisService", "getTopAnchorsByIncome", null, e);
            throw new AnalysisException(5042, "获取主播收入排行榜失败: " + e.getMessage(), e);
        }
    }
}
