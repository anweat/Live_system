package com.liveroom.analysis.service;

import com.liveroom.analysis.dto.UserPortraitDTO;
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
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户画像分析Service
 * 基于RFM模型进行用户分层和画像分析
 */
@Service
@RequiredArgsConstructor
public class UserPortraitService {

    private final DataAccessFacade dataAccessFacade;

    /**
     * 获取用户画像（基于RFM模型）
     */
    @Cacheable(value = "analysis:portrait", key = "'user:' + #audienceId")
    public UserPortraitDTO getUserPortrait(Long audienceId) {
        try {
            TraceLogger.info("UserPortraitService", "getUserPortrait", audienceId);

            // 获取用户基本信息
            Audience audience = dataAccessFacade.audience()
                .findById(audienceId)
                .orElseThrow(() -> new AnalysisException(5020, "用户不存在"));

            // 获取用户打赏记录
            List<Recharge> recharges = dataAccessFacade.recharge()
                .findByAudienceId(audienceId);

            if (recharges.isEmpty()) {
                return buildEmptyPortrait(audience);
            }

            // 计算RFM指标
            LocalDateTime now = LocalDateTime.now();
            
            // R - Recency（最近消费）
            LocalDateTime lastRechargeTime = recharges.stream()
                .map(Recharge::getRechargeTime)
                .max(LocalDateTime::compareTo)
                .orElse(null);
            
            int recencyDays = (int) ChronoUnit.DAYS.between(lastRechargeTime, now);
            int recencyScore = calculateRecencyScore(recencyDays);

            // F - Frequency（消费频次）
            int frequency = recharges.size();
            int frequencyScore = calculateFrequencyScore(frequency);

            // M - Monetary（消费金额）
            BigDecimal monetary = recharges.stream()
                .map(Recharge::getRechargeAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            int monetaryScore = calculateMonetaryScore(monetary);

            // 计算RFM综合得分
            BigDecimal rfmScore = calculateRFMScore(recencyScore, frequencyScore, monetaryScore);

            // 用户分层
            String userLevel = classifyUserLevel(rfmScore);

            // 消费分层
            Integer consumptionLevel = audience.getConsumptionLevel();

            // 活跃度评分
            BigDecimal activityScore = calculateActivityScore(recencyDays, frequency);

            // 预测LTV
            BigDecimal predictedLTV = predictLTV(monetary, frequency, recencyDays);

            // 其他统计
            LocalDateTime firstRechargeTime = recharges.stream()
                .map(Recharge::getRechargeTime)
                .min(LocalDateTime::compareTo)
                .orElse(null);

            BigDecimal avgAmount = monetary.divide(
                BigDecimal.valueOf(frequency), 2, RoundingMode.HALF_UP);

            int anchorCount = (int) recharges.stream()
                .map(Recharge::getAnchorId)
                .distinct()
                .count();

            return UserPortraitDTO.builder()
                .userId(audienceId)
                .userName(audience.getNickname())
                .recencyDays(recencyDays)
                .recencyScore(recencyScore)
                .frequency(frequency)
                .frequencyScore(frequencyScore)
                .monetary(monetary)
                .monetaryScore(monetaryScore)
                .rfmScore(rfmScore)
                .userLevel(userLevel)
                .consumptionLevel(consumptionLevel)
                .activityScore(activityScore)
                .predictedLTV(predictedLTV)
                .lastRechargeTime(lastRechargeTime)
                .firstRechargeTime(firstRechargeTime)
                .totalAmount(monetary)
                .avgAmount(avgAmount)
                .anchorCount(anchorCount)
                .build();

        } catch (AnalysisException e) {
            throw e;
        } catch (Exception e) {
            TraceLogger.error("UserPortraitService", "getUserPortrait", audienceId, e);
            throw new AnalysisException(5021, "获取用户画像失败: " + e.getMessage(), e);
        }
    }

    /**
     * 计算R得分（最近消费）
     * 5分: 7天内
     * 4分: 8-14天
     * 3分: 15-30天
     * 2分: 31-60天
     * 1分: 60天以上
     */
    private int calculateRecencyScore(int recencyDays) {
        if (recencyDays <= 7) return 5;
        if (recencyDays <= 14) return 4;
        if (recencyDays <= 30) return 3;
        if (recencyDays <= 60) return 2;
        return 1;
    }

    /**
     * 计算F得分（消费频次）
     * 5分: >= 20次
     * 4分: 10-19次
     * 3分: 5-9次
     * 2分: 2-4次
     * 1分: 1次
     */
    private int calculateFrequencyScore(int frequency) {
        if (frequency >= 20) return 5;
        if (frequency >= 10) return 4;
        if (frequency >= 5) return 3;
        if (frequency >= 2) return 2;
        return 1;
    }

    /**
     * 计算M得分（消费金额）
     * 根据消费金额在所有用户中的百分位数
     */
    private int calculateMonetaryScore(BigDecimal monetary) {
        // 简化实现，实际应查询所有用户的消费分位数
        if (monetary.compareTo(new BigDecimal("1000")) >= 0) return 5;
        if (monetary.compareTo(new BigDecimal("500")) >= 0) return 4;
        if (monetary.compareTo(new BigDecimal("200")) >= 0) return 3;
        if (monetary.compareTo(new BigDecimal("50")) >= 0) return 2;
        return 1;
    }

    /**
     * 计算RFM综合得分
     * RFM = R × 0.3 + F × 0.3 + M × 0.4
     */
    private BigDecimal calculateRFMScore(int rScore, int fScore, int mScore) {
        return BigDecimal.valueOf(rScore * 0.3 + fScore * 0.3 + mScore * 0.4)
            .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 用户分层
     * 高价值用户: RFM >= 4.0
     * 中价值用户: 3.0 <= RFM < 4.0
     * 低价值用户: RFM < 3.0
     */
    private String classifyUserLevel(BigDecimal rfmScore) {
        if (rfmScore.compareTo(new BigDecimal("4.0")) >= 0) {
            return "高价值用户";
        } else if (rfmScore.compareTo(new BigDecimal("3.0")) >= 0) {
            return "中价值用户";
        } else {
            return "低价值用户";
        }
    }

    /**
     * 计算活跃度评分（0-100）
     * 综合考虑最近活跃和频次
     */
    private BigDecimal calculateActivityScore(int recencyDays, int frequency) {
        // 活跃度 = (100 - recencyDays * 0.5) * 0.6 + (frequency * 2) * 0.4
        BigDecimal recencyPart = BigDecimal.valueOf(100 - Math.min(recencyDays * 0.5, 100))
            .multiply(BigDecimal.valueOf(0.6));
        
        BigDecimal frequencyPart = BigDecimal.valueOf(Math.min(frequency * 2, 100))
            .multiply(BigDecimal.valueOf(0.4));
        
        BigDecimal score = recencyPart.add(frequencyPart);
        
        return score.max(BigDecimal.ZERO)
            .min(BigDecimal.valueOf(100))
            .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 预测LTV（生命周期价值）
     * 简化公式: LTV = 平均消费金额 × 预期消费次数
     */
    private BigDecimal predictLTV(BigDecimal totalAmount, int frequency, int recencyDays) {
        if (frequency == 0) {
            return BigDecimal.ZERO;
        }

        // 平均单笔金额
        BigDecimal avgAmount = totalAmount.divide(
            BigDecimal.valueOf(frequency), 2, RoundingMode.HALF_UP);

        // 预期还会消费的次数（基于当前频率和活跃度）
        BigDecimal expectedFrequency = BigDecimal.valueOf(frequency * 2.0);
        
        // 根据最近活跃度调整
        if (recencyDays > 30) {
            expectedFrequency = expectedFrequency.multiply(BigDecimal.valueOf(0.5));
        } else if (recencyDays > 14) {
            expectedFrequency = expectedFrequency.multiply(BigDecimal.valueOf(0.7));
        }

        // 预测LTV = 已消费 + 预期消费
        BigDecimal predictedFutureValue = avgAmount.multiply(expectedFrequency);
        
        return totalAmount.add(predictedFutureValue)
            .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 构建空画像（无消费记录）
     */
    private UserPortraitDTO buildEmptyPortrait(Audience audience) {
        return UserPortraitDTO.builder()
            .userId(audience.getUserId())
            .userName(audience.getNickname())
            .recencyDays(999)
            .recencyScore(0)
            .frequency(0)
            .frequencyScore(0)
            .monetary(BigDecimal.ZERO)
            .monetaryScore(0)
            .rfmScore(BigDecimal.ZERO)
            .userLevel("无消费用户")
            .consumptionLevel(0)
            .activityScore(BigDecimal.ZERO)
            .predictedLTV(BigDecimal.ZERO)
            .totalAmount(BigDecimal.ZERO)
            .avgAmount(BigDecimal.ZERO)
            .anchorCount(0)
            .build();
    }

    /**
     * 批量获取用户画像
     */
    public List<UserPortraitDTO> batchGetUserPortraits(List<Long> audienceIds) {
        try {
            TraceLogger.info("UserPortraitService", "batchGetUserPortraits", 
                null, "count", audienceIds.size());

            return audienceIds.stream()
                .map(this::getUserPortrait)
                .collect(Collectors.toList());

        } catch (Exception e) {
            TraceLogger.error("UserPortraitService", "batchGetUserPortraits", null, e);
            throw new AnalysisException(5022, "批量获取用户画像失败: " + e.getMessage(), e);
        }
    }
}
