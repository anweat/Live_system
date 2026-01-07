package common.service.query;

import common.logger.TraceLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

/**
 * 用户留存分析查询Service
 *
 * 专门用于处理用户留存相关的数据分析
 * 包括：
 * - N日留存率计算
 * - 留存曲线分析
 * - 流失概率预测
 * - 留存趋势分析
 *
 * @author Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RetentionAnalysisQueryService {

    /**
     * 计算N日留存率
     *
     * N日留存率 = 第N日活跃用户数 / 首日新增用户数 × 100%
     *
     * @param cohortDate 用户群组日期（首日日期）
     * @param targetDate 目标日期（第N日）
     * @param cohortUserIds 首日新增用户ID列表
     * @param activeUserIds 目标日活跃用户ID列表
     * @return N日留存率（百分比）
     */
    public BigDecimal calculateRetentionRate(
            LocalDate cohortDate,
            LocalDate targetDate,
            Set<Long> cohortUserIds,
            Set<Long> activeUserIds) {

        TraceLogger.info("RetentionAnalysisQueryService", "calculateRetentionRate",
            String.format("计算留存率: 群组日期=%s, 目标日期=%s", cohortDate, targetDate));

        if (cohortUserIds.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // 计算目标日期仍然活跃的用户数
        Set<Long> retainedUsers = new HashSet<>(cohortUserIds);
        retainedUsers.retainAll(activeUserIds);

        return BigDecimal.valueOf(retainedUsers.size())
            .divide(BigDecimal.valueOf(cohortUserIds.size()), 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100))
            .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 生成留存曲线数据
     *
     * @param cohortDate 用户群组日期
     * @param cohortUserIds 首日新增用户ID列表
     * @param dailyActiveUsers 每日活跃用户数据（日期 -> 活跃用户ID集合）
     * @param days 统计天数
     * @return 留存曲线数据
     */
    public RetentionCurve generateRetentionCurve(
            LocalDate cohortDate,
            Set<Long> cohortUserIds,
            Map<LocalDate, Set<Long>> dailyActiveUsers,
            int days) {

        TraceLogger.info("RetentionAnalysisQueryService", "generateRetentionCurve",
            String.format("生成留存曲线: 群组日期=%s, 统计天数=%d", cohortDate, days));

        List<Integer> dayList = new ArrayList<>();
        List<BigDecimal> retentionRates = new ArrayList<>();
        List<Integer> retainedCounts = new ArrayList<>();

        for (int day = 0; day <= days; day++) {
            LocalDate targetDate = cohortDate.plusDays(day);
            Set<Long> activeUsers = dailyActiveUsers.getOrDefault(targetDate, Collections.emptySet());

            Set<Long> retainedUsers = new HashSet<>(cohortUserIds);
            retainedUsers.retainAll(activeUsers);

            BigDecimal retentionRate = cohortUserIds.isEmpty() ? BigDecimal.ZERO :
                BigDecimal.valueOf(retainedUsers.size())
                    .divide(BigDecimal.valueOf(cohortUserIds.size()), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);

            dayList.add(day);
            retentionRates.add(retentionRate);
            retainedCounts.add(retainedUsers.size());
        }

        return RetentionCurve.builder()
            .cohortDate(cohortDate)
            .cohortSize(cohortUserIds.size())
            .days(dayList)
            .retentionRates(retentionRates)
            .retainedCounts(retainedCounts)
            .build();
    }

    /**
     * 预测流失概率（简化版逻辑回归）
     *
     * P(流失) = 1 / (1 + e^(-z))
     * z = β0 + β1×最近消费天数 + β2×消费频次 + β3×消费金额 + β4×活跃度
     *
     * @param daysSinceLastRecharge 最近消费天数
     * @param rechargeFrequency 消费频次
     * @param totalRechargeAmount 累计消费金额
     * @param activityScore 活跃度评分(0-1)
     * @return 流失概率(0-1)
     */
    public BigDecimal predictChurnProbability(
            int daysSinceLastRecharge,
            int rechargeFrequency,
            BigDecimal totalRechargeAmount,
            BigDecimal activityScore) {

        TraceLogger.debug("RetentionAnalysisQueryService", "predictChurnProbability",
            String.format("预测流失概率: 最近消费=%d天, 频次=%d, 金额=%s, 活跃度=%s",
                daysSinceLastRecharge, rechargeFrequency, totalRechargeAmount, activityScore));

        // 简化的回归系数（实际应用中应通过历史数据训练得到）
        double beta0 = 2.0;  // 截距
        double beta1 = 0.05; // 最近消费天数系数（正相关，天数越多流失概率越高）
        double beta2 = -0.3; // 消费频次系数（负相关，频次越高流失概率越低）
        double beta3 = -0.001; // 消费金额系数（负相关，金额越高流失概率越低）
        double beta4 = -3.0; // 活跃度系数（负相关，活跃度越高流失概率越低）

        // 计算z值
        double z = beta0
            + beta1 * daysSinceLastRecharge
            + beta2 * rechargeFrequency
            + beta3 * totalRechargeAmount.doubleValue()
            + beta4 * activityScore.doubleValue();

        // Sigmoid函数: P = 1 / (1 + e^(-z))
        double probability = 1.0 / (1.0 + Math.exp(-z));

        // 限制在0-1范围内
        probability = Math.max(0.0, Math.min(1.0, probability));

        return BigDecimal.valueOf(probability).setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * 评估流失风险等级
     *
     * @param churnProbability 流失概率
     * @return 风险等级: 3=高风险, 2=中风险, 1=低风险, 0=安全
     */
    public int evaluateChurnRiskLevel(BigDecimal churnProbability) {
        if (churnProbability.compareTo(BigDecimal.valueOf(0.7)) > 0) {
            return 3; // 高风险
        } else if (churnProbability.compareTo(BigDecimal.valueOf(0.4)) > 0) {
            return 2; // 中风险
        } else if (churnProbability.compareTo(BigDecimal.valueOf(0.2)) > 0) {
            return 1; // 低风险
        } else {
            return 0; // 安全
        }
    }

    /**
     * 计算用户留存健康度综合评分
     *
     * @param retention1Day 次日留存率
     * @param retention7Day 7日留存率
     * @param retention30Day 30日留存率
     * @return 健康度评分(0-100)
     */
    public BigDecimal calculateRetentionHealthScore(
            BigDecimal retention1Day,
            BigDecimal retention7Day,
            BigDecimal retention30Day) {

        TraceLogger.info("RetentionAnalysisQueryService", "calculateRetentionHealthScore",
            "计算留存健康度评分");

        // 权重: 次日留存30%, 7日留存40%, 30日留存30%
        BigDecimal score = retention1Day.multiply(BigDecimal.valueOf(0.3))
            .add(retention7Day.multiply(BigDecimal.valueOf(0.4)))
            .add(retention30Day.multiply(BigDecimal.valueOf(0.3)));

        return score.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 留存曲线DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class RetentionCurve {
        private LocalDate cohortDate; // 群组日期
        private Integer cohortSize; // 群组规模（首日新增用户数）
        private List<Integer> days; // 天数列表 [0, 1, 2, ..., N]
        private List<BigDecimal> retentionRates; // 留存率列表（百分比）
        private List<Integer> retainedCounts; // 留存用户数列表
    }

    /**
     * 流失预警DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class ChurnAlert {
        private Long userId; // 用户ID
        private String userName; // 用户名称
        private BigDecimal churnProbability; // 流失概率
        private Integer riskLevel; // 风险等级(0-3)
        private String riskDesc; // 风险描述
        private Integer daysSinceLastActive; // 最近活跃天数
        private Integer activityCount; // 活动次数
        private BigDecimal totalRechargeAmount; // 累计消费金额
    }

    /**
     * 留存健康报告DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class RetentionHealthReport {
        private LocalDate reportDate; // 报告日期
        private BigDecimal retention1Day; // 次日留存率
        private BigDecimal retention7Day; // 7日留存率
        private BigDecimal retention30Day; // 30日留存率
        private BigDecimal healthScore; // 健康度评分(0-100)
        private String healthLevel; // 健康等级（优秀/良好/一般/差）
        private Integer totalNewUsers; // 总新增用户数
        private Integer highRiskUsers; // 高风险用户数
        private Integer mediumRiskUsers; // 中风险用户数
    }
}
