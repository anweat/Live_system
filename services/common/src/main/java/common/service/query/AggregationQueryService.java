package common.service.query;

import common.bean.RechargeRecord;
import common.logger.TraceLogger;
import common.repository.RechargeRecordRepository;
import common.service.AnalysisQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 聚合统计查询Service
 *
 * 专门用于处理各种维度的聚合统计
 * 包括：
 * - 多维度的求和、平均、计数
 * - 按主播、观众等维度的聚合
 * - 关键指标汇总
 *
 * @author Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AggregationQueryService {

    private final AnalysisQueryService analysisQueryService;
    private final RechargeRecordRepository rechargeRecordRepository;

    /**
     * 获取时间范围内的平台关键指标
     * 使用RechargeRecord表，直接获取settlementAmount
     */
    @Transactional(readOnly = true)
    public KeyMetrics getKeyMetrics(LocalDateTime startTime, LocalDateTime endTime) {
        TraceLogger.info("AggregationQueryService", "getKeyMetrics",
            String.format("获取平台关键指标: %s - %s", startTime, endTime));

        List<RechargeRecord> records = rechargeRecordRepository.findAllByTimeRange(startTime, endTime);

        if (records.isEmpty()) {
            return KeyMetrics.builder()
                .totalGmv(BigDecimal.ZERO)
                .platformRevenue(BigDecimal.ZERO)
                .anchorRevenue(BigDecimal.ZERO)
                .transactionCount(0)
                .payingUsers(0)
                .uniqueAnchors(0)
                .paymentRate(BigDecimal.ZERO)
                .arpu(BigDecimal.ZERO)
                .arppu(BigDecimal.ZERO)
                .build();
        }

        BigDecimal totalGmv = records.stream()
            .map(RechargeRecord::getRechargeAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        int payingUsers = (int) records.stream()
            .map(RechargeRecord::getAudienceId)
            .distinct()
            .count();

        int uniqueAnchors = (int) records.stream()
            .map(RechargeRecord::getAnchorId)
            .distinct()
            .count();

        // 主播收入：直接求和settlementAmount
        BigDecimal anchorRevenue = records.stream()
            .map(r -> r.getSettlementAmount() != null ? r.getSettlementAmount() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, java.math.RoundingMode.HALF_UP);
        
        // 平台收入 = GMV - 主播分成
        BigDecimal platformRevenue = totalGmv.subtract(anchorRevenue)
            .setScale(2, java.math.RoundingMode.HALF_UP);

        // 计算支付率 (假设总用户数为付费用户的5倍)
        int estimatedTotalUsers = payingUsers * 5;
        BigDecimal paymentRate = java.math.BigDecimal.valueOf(payingUsers)
            .divide(java.math.BigDecimal.valueOf(estimatedTotalUsers), 4, java.math.RoundingMode.HALF_UP)
            .multiply(java.math.BigDecimal.valueOf(100))
            .setScale(2, java.math.RoundingMode.HALF_UP);

        // ARPU = 总收入 / 总用户数
        BigDecimal arpu = totalGmv.divide(java.math.BigDecimal.valueOf(estimatedTotalUsers), 2, java.math.RoundingMode.HALF_UP);

        // ARPPU = 总收入 / 付费用户数
        BigDecimal arppu = totalGmv.divide(java.math.BigDecimal.valueOf(payingUsers), 2, java.math.RoundingMode.HALF_UP);

        return KeyMetrics.builder()
            .totalGmv(totalGmv)
            .platformRevenue(platformRevenue)
            .anchorRevenue(anchorRevenue)
            .transactionCount(records.size())
            .payingUsers(payingUsers)
            .uniqueAnchors(uniqueAnchors)
            .paymentRate(paymentRate)
            .arpu(arpu)
            .arppu(arppu)
            .periodStart(startTime)
            .periodEnd(endTime)
            .build();
    }

    /**
     * 按主播维度获取聚合统计
     * 使用RechargeRecord表，直接获取settlementAmount
     */
    @Transactional(readOnly = true)
    public List<AnchorAggregation> getAnchorAggregations(LocalDateTime startTime, LocalDateTime endTime) {
        TraceLogger.info("AggregationQueryService", "getAnchorAggregations",
            String.format("获取主播维度聚合统计: %s - %s", startTime, endTime));

        List<RechargeRecord> records = rechargeRecordRepository.findAllByTimeRange(startTime, endTime);

        return records.stream()
            .collect(Collectors.groupingBy(RechargeRecord::getAnchorId))
            .entrySet().stream()
            .map(entry -> {
                List<RechargeRecord> anchorRecords = entry.getValue();
                BigDecimal totalAmount = anchorRecords.stream()
                    .map(RechargeRecord::getRechargeAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                // 主播收入：直接求和settlementAmount
                BigDecimal anchorIncome = anchorRecords.stream()
                    .map(r -> r.getSettlementAmount() != null ? r.getSettlementAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, java.math.RoundingMode.HALF_UP);

                int payingUsers = (int) anchorRecords.stream()
                    .map(RechargeRecord::getAudienceId)
                    .distinct()
                    .count();

                return AnchorAggregation.builder()
                    .anchorId(entry.getKey())
                    .anchorName(anchorRecords.get(0).getAnchorName())
                    .totalGmv(totalAmount)
                    .anchorIncome(anchorIncome)
                    .transactionCount(anchorRecords.size())
                    .payingUsers(payingUsers)
                    .avgAmount(totalAmount.divide(java.math.BigDecimal.valueOf(anchorRecords.size()), 2, java.math.RoundingMode.HALF_UP))
                    .build();
            })
            .sorted((a, b) -> b.getTotalGmv().compareTo(a.getTotalGmv()))
            .collect(Collectors.toList());
    }

    /**
     * 关键指标DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class KeyMetrics {
        private BigDecimal totalGmv; // 平台总流水
        private BigDecimal platformRevenue; // 平台收入
        private BigDecimal anchorRevenue; // 主播总收入
        private int transactionCount; // 交易数
        private int payingUsers; // 付费用户数
        private int uniqueAnchors; // 主播数
        private BigDecimal paymentRate; // 支付率 (%)
        private BigDecimal arpu; // 人均收入
        private BigDecimal arppu; // 付费用户人均收入
        private LocalDateTime periodStart;
        private LocalDateTime periodEnd;
    }

    /**
     * 主播聚合统计DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class AnchorAggregation {
        private Long anchorId;
        private String anchorName;
        private BigDecimal totalGmv;
        private BigDecimal anchorIncome;
        private int transactionCount;
        private int payingUsers;
        private BigDecimal avgAmount;
    }
}

