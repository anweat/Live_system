package common.service.query;

import common.bean.Recharge;
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
 * 排行榜查询Service
 *
 * 专门用于处理TOP排行、排序相关的数据查询
 * 包括：
 * - 主播收入排行榜
 * - 观众消费排行榜
 * - 直播间热度排行榜
 * - 增长率排行榜
 *
 * @author Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RankingQueryService {

    private final AnalysisQueryService analysisQueryService;
    private final RechargeRecordRepository rechargeRecordRepository;

    /**
     * 获取主播收入TOP排行榜
     * 使用RechargeRecord表（财务服务DB2），直接获取实际的分成金额
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param limit 返回数量
     * @return 主播收入排行榜
     */
    @Transactional(readOnly = true)
    public List<AnchorIncomeRanking> getTopAnchorsByIncome(LocalDateTime startTime, LocalDateTime endTime, int limit) {
        TraceLogger.info("RankingQueryService", "getTopAnchorsByIncome",
            String.format("获取主播收入TOP%d: %s - %s", limit, startTime, endTime));

        // 使用RechargeRecord表，直接获取settlementAmount（主播实际分成）
        List<RechargeRecord> records = rechargeRecordRepository.findAllByTimeRange(startTime, endTime);

        return records.stream()
            .collect(Collectors.groupingBy(RechargeRecord::getAnchorId))
            .entrySet().stream()
            .map(entry -> {
                List<RechargeRecord> anchorRecords = entry.getValue();
                
                // 主播实际收入：直接使用settlementAmount
                BigDecimal anchorIncome = anchorRecords.stream()
                    .map(r -> r.getSettlementAmount() != null ? r.getSettlementAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                // 总流水GMV
                BigDecimal totalGmv = anchorRecords.stream()
                    .map(RechargeRecord::getRechargeAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                int payingUsers = (int) anchorRecords.stream()
                    .map(RechargeRecord::getAudienceId)
                    .distinct()
                    .count();

                BigDecimal avgAmount = totalGmv.divide(
                    BigDecimal.valueOf(anchorRecords.size()), 2, java.math.RoundingMode.HALF_UP);

                return AnchorIncomeRanking.builder()
                    .anchorId(entry.getKey())
                    .anchorName(anchorRecords.get(0).getAnchorName())
                    .totalIncome(anchorIncome)
                    .totalGmv(totalGmv)
                    .rechargeCount(anchorRecords.size())
                    .payingUsers(payingUsers)
                    .avgAmount(avgAmount)
                    .maxAmount(anchorRecords.stream()
                        .map(RechargeRecord::getRechargeAmount)
                        .max(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO))
                    .build();
            })
            .sorted((a, b) -> b.getTotalIncome().compareTo(a.getTotalIncome()))
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * 获取观众消费TOP排行榜
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param limit 返回数量
     * @return 观众消费排行榜
     */
    @Transactional(readOnly = true)
    public List<AudienceConsumptionRanking> getTopAudiencesByConsumption(LocalDateTime startTime, LocalDateTime endTime, int limit) {
        TraceLogger.info("RankingQueryService", "getTopAudiencesByConsumption",
            String.format("获取观众消费TOP%d: %s - %s", limit, startTime, endTime));

        List<Recharge> recharges = analysisQueryService.getRechargesByTimeRange(startTime, endTime);

        return recharges.stream()
            .collect(Collectors.groupingBy(Recharge::getAudienceId))
            .entrySet().stream()
            .map(entry -> {
                List<Recharge> audienceRecharges = entry.getValue();
                BigDecimal totalAmount = audienceRecharges.stream()
                    .map(Recharge::getRechargeAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                int uniqueAnchors = (int) audienceRecharges.stream()
                    .map(Recharge::getAnchorId)
                    .distinct()
                    .count();

                BigDecimal avgAmount = totalAmount.divide(
                    BigDecimal.valueOf(audienceRecharges.size()), 2, java.math.RoundingMode.HALF_UP);

                // 获取最近一次消费时间
                LocalDateTime lastRechargeTime = audienceRecharges.stream()
                    .map(Recharge::getRechargeTime)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);

                return AudienceConsumptionRanking.builder()
                    .audienceId(entry.getKey())
                    .audienceName(audienceRecharges.get(0).getAudienceNickname())
                    .totalAmount(totalAmount)
                    .rechargeCount(audienceRecharges.size())
                    .uniqueAnchors(uniqueAnchors)
                    .avgAmount(avgAmount)
                    .maxAmount(audienceRecharges.stream()
                        .map(Recharge::getRechargeAmount)
                        .max(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO))
                    .lastRechargeTime(lastRechargeTime)
                    .build();
            })
            .sorted((a, b) -> b.getTotalAmount().compareTo(a.getTotalAmount()))
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * 获取主播收入增长率排行榜
     * 使用RechargeRecord表，直接获取settlementAmount
     *
     * @param currentStart 当前周期开始时间
     * @param currentEnd 当前周期结束时间
     * @param previousStart 上一周期开始时间
     * @param previousEnd 上一周期结束时间
     * @param limit 返回数量
     * @return 主播增长率排行榜
     */
    @Transactional(readOnly = true)
    public List<AnchorGrowthRanking> getTopAnchorsByGrowth(
            LocalDateTime currentStart, LocalDateTime currentEnd,
            LocalDateTime previousStart, LocalDateTime previousEnd,
            int limit) {
        TraceLogger.info("RankingQueryService", "getTopAnchorsByGrowth",
            String.format("获取主播增长率TOP%d", limit));

        // 使用RechargeRecord表，直接获取settlementAmount
        Map<Long, BigDecimal> currentIncome = rechargeRecordRepository.findAllByTimeRange(currentStart, currentEnd)
            .stream()
            .collect(Collectors.groupingBy(
                RechargeRecord::getAnchorId,
                Collectors.reducing(BigDecimal.ZERO, 
                    r -> r.getSettlementAmount() != null ? r.getSettlementAmount() : BigDecimal.ZERO, 
                    BigDecimal::add)
            ));

        // 获取上一周期数据
        Map<Long, BigDecimal> previousIncome = rechargeRecordRepository.findAllByTimeRange(previousStart, previousEnd)
            .stream()
            .collect(Collectors.groupingBy(
                RechargeRecord::getAnchorId,
                Collectors.reducing(BigDecimal.ZERO, 
                    r -> r.getSettlementAmount() != null ? r.getSettlementAmount() : BigDecimal.ZERO, 
                    BigDecimal::add)
            ));

        // 计算增长率
        return currentIncome.entrySet().stream()
            .filter(entry -> previousIncome.containsKey(entry.getKey()))
            .map(entry -> {
                Long anchorId = entry.getKey();
                BigDecimal current = entry.getValue();
                BigDecimal previous = previousIncome.get(anchorId);

                // 增长率 = (当前 - 上期) / 上期 * 100
                BigDecimal growthRate = BigDecimal.ZERO;
                if (previous.compareTo(BigDecimal.ZERO) > 0) {
                    growthRate = current.subtract(previous)
                        .divide(previous, 4, java.math.RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, java.math.RoundingMode.HALF_UP);
                }

                return AnchorGrowthRanking.builder()
                    .anchorId(anchorId)
                    .currentIncome(current)
                    .previousIncome(previous)
                    .growthRate(growthRate)
                    .growthAmount(current.subtract(previous))
                    .build();
            })
            .sorted((a, b) -> b.getGrowthRate().compareTo(a.getGrowthRate()))
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * 主播收入排行榜DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class AnchorIncomeRanking {
        private Long anchorId;
        private String anchorName;
        private BigDecimal totalIncome; // 主播实际收入
        private BigDecimal totalGmv; // 总流水
        private Integer rechargeCount; // 打赏次数
        private Integer payingUsers; // 付费观众数
        private BigDecimal avgAmount; // 平均打赏金额
        private BigDecimal maxAmount; // 最大单笔打赏
    }

    /**
     * 观众消费排行榜DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class AudienceConsumptionRanking {
        private Long audienceId;
        private String audienceName;
        private BigDecimal totalAmount; // 总消费金额
        private Integer rechargeCount; // 消费次数
        private Integer uniqueAnchors; // 打赏主播数
        private BigDecimal avgAmount; // 平均消费金额
        private BigDecimal maxAmount; // 最大单笔消费
        private LocalDateTime lastRechargeTime; // 最近消费时间
    }

    /**
     * 主播增长率排行榜DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class AnchorGrowthRanking {
        private Long anchorId;
        private String anchorName;
        private BigDecimal currentIncome; // 当前周期收入
        private BigDecimal previousIncome; // 上一周期收入
        private BigDecimal growthRate; // 增长率(%)
        private BigDecimal growthAmount; // 增长金额
    }
}
