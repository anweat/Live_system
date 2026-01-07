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
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 财务分析查询Service
 *
 * 专门用于处理财务相关的数据分析
 * 包括：
 * - 平台GMV分析
 * - 收入分成分析
 * - ARPU/ARPPU计算
 * - 付费率分析
 * - 复购率分析
 *
 * @author Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FinancialAnalysisQueryService {

    private final AnalysisQueryService analysisQueryService;
    private final RechargeRecordRepository rechargeRecordRepository;

    /**
     * 计算平台GMV（总流水）
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return GMV金额
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateGMV(LocalDateTime startTime, LocalDateTime endTime) {
        TraceLogger.info("FinancialAnalysisQueryService", "calculateGMV",
            String.format("计算GMV: %s - %s", startTime, endTime));

        List<Recharge> recharges = analysisQueryService.getRechargesByTimeRange(startTime, endTime);

        return recharges.stream()
            .map(Recharge::getRechargeAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 计算平台收入（扣除主播分成后）
     * 使用RechargeRecord表：平台收入 = rechargeAmount - settlementAmount
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 平台收入
     */
    @Transactional(readOnly = true)
    public BigDecimal calculatePlatformRevenue(LocalDateTime startTime, LocalDateTime endTime) {
        TraceLogger.info("FinancialAnalysisQueryService", "calculatePlatformRevenue",
            String.format("计算平台收入: %s - %s", startTime, endTime));

        List<RechargeRecord> records = rechargeRecordRepository.findAllByTimeRange(startTime, endTime);

        // 平台收入 = 总流水 - 主播分成
        return records.stream()
            .map(record -> {
                BigDecimal gmv = record.getRechargeAmount();
                BigDecimal settlement = record.getSettlementAmount() != null ? record.getSettlementAmount() : BigDecimal.ZERO;
                return gmv.subtract(settlement);
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 计算主播总收入
     * 使用RechargeRecord表：直接求和 settlementAmount
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 主播总收入
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateAnchorRevenue(LocalDateTime startTime, LocalDateTime endTime) {
        TraceLogger.info("FinancialAnalysisQueryService", "calculateAnchorRevenue",
            String.format("计算主播总收入: %s - %s", startTime, endTime));

        List<RechargeRecord> records = rechargeRecordRepository.findAllByTimeRange(startTime, endTime);

        // 直接求和settlementAmount
        return records.stream()
            .map(r -> r.getSettlementAmount() != null ? r.getSettlementAmount() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 计算ARPU（平均每用户收入）
     * ARPU = 总收入 / 总用户数
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param totalUsers 总用户数（包括未付费用户）
     * @return ARPU金额
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateARPU(LocalDateTime startTime, LocalDateTime endTime, int totalUsers) {
        TraceLogger.info("FinancialAnalysisQueryService", "calculateARPU",
            String.format("计算ARPU: %s - %s, 总用户数: %d", startTime, endTime, totalUsers));

        if (totalUsers == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalRevenue = calculateGMV(startTime, endTime);

        return totalRevenue.divide(BigDecimal.valueOf(totalUsers), 2, RoundingMode.HALF_UP);
    }

    /**
     * 计算ARPPU（平均每付费用户收入）
     * ARPPU = 总收入 / 付费用户数
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return ARPPU金额
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateARPPU(LocalDateTime startTime, LocalDateTime endTime) {
        TraceLogger.info("FinancialAnalysisQueryService", "calculateARPPU",
            String.format("计算ARPPU: %s - %s", startTime, endTime));

        List<Recharge> recharges = analysisQueryService.getRechargesByTimeRange(startTime, endTime);

        if (recharges.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalRevenue = recharges.stream()
            .map(Recharge::getRechargeAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        int payingUsers = (int) recharges.stream()
            .map(Recharge::getAudienceId)
            .distinct()
            .count();

        if (payingUsers == 0) {
            return BigDecimal.ZERO;
        }

        return totalRevenue.divide(BigDecimal.valueOf(payingUsers), 2, RoundingMode.HALF_UP);
    }

    /**
     * 计算付费率
     * 付费率 = 付费用户数 / 总用户数 × 100%
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param totalUsers 总用户数
     * @return 付费率（百分比）
     */
    @Transactional(readOnly = true)
    public BigDecimal calculatePaymentRate(LocalDateTime startTime, LocalDateTime endTime, int totalUsers) {
        TraceLogger.info("FinancialAnalysisQueryService", "calculatePaymentRate",
            String.format("计算付费率: %s - %s, 总用户数: %d", startTime, endTime, totalUsers));

        if (totalUsers == 0) {
            return BigDecimal.ZERO;
        }

        List<Recharge> recharges = analysisQueryService.getRechargesByTimeRange(startTime, endTime);

        int payingUsers = (int) recharges.stream()
            .map(Recharge::getAudienceId)
            .distinct()
            .count();

        return BigDecimal.valueOf(payingUsers)
            .divide(BigDecimal.valueOf(totalUsers), 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100))
            .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 计算复购率
     * 复购率 = 多次付费用户数 / 总付费用户数 × 100%
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 复购率（百分比）
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateRepurchaseRate(LocalDateTime startTime, LocalDateTime endTime) {
        TraceLogger.info("FinancialAnalysisQueryService", "calculateRepurchaseRate",
            String.format("计算复购率: %s - %s", startTime, endTime));

        List<Recharge> recharges = analysisQueryService.getRechargesByTimeRange(startTime, endTime);

        if (recharges.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // 按观众分组统计消费次数
        Map<Long, Long> audienceRechargeCount = recharges.stream()
            .collect(Collectors.groupingBy(Recharge::getAudienceId, Collectors.counting()));

        // 多次付费用户数
        long repeatUsers = audienceRechargeCount.values().stream()
            .filter(count -> count > 1)
            .count();

        // 总付费用户数
        int totalPayingUsers = audienceRechargeCount.size();

        if (totalPayingUsers == 0) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(repeatUsers)
            .divide(BigDecimal.valueOf(totalPayingUsers), 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100))
            .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 获取综合财务报告
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param totalUsers 总用户数
     * @return 财务报告
     */
    @Transactional(readOnly = true)
    public FinancialReport getFinancialReport(LocalDateTime startTime, LocalDateTime endTime, int totalUsers) {
        TraceLogger.info("FinancialAnalysisQueryService", "getFinancialReport",
            String.format("生成财务报告: %s - %s", startTime, endTime));

        List<RechargeRecord> records = rechargeRecordRepository.findAllByTimeRange(startTime, endTime);

        BigDecimal gmv = records.stream()
            .map(RechargeRecord::getRechargeAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal anchorRevenue = records.stream()
            .map(r -> r.getSettlementAmount() != null ? r.getSettlementAmount() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal platformRevenue = gmv.subtract(anchorRevenue);

        int payingUsers = (int) records.stream()
            .map(RechargeRecord::getAudienceId)
            .distinct()
            .count();

        int transactionCount = records.size();

        BigDecimal arpu = totalUsers > 0 ?
            gmv.divide(BigDecimal.valueOf(totalUsers), 2, RoundingMode.HALF_UP) :
            BigDecimal.ZERO;

        BigDecimal arppu = payingUsers > 0 ?
            gmv.divide(BigDecimal.valueOf(payingUsers), 2, RoundingMode.HALF_UP) :
            BigDecimal.ZERO;

        BigDecimal paymentRate = totalUsers > 0 ?
            BigDecimal.valueOf(payingUsers)
                .divide(BigDecimal.valueOf(totalUsers), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP) :
            BigDecimal.ZERO;

        // 计算复购率
        Map<Long, Long> audienceRechargeCount = records.stream()
            .collect(Collectors.groupingBy(RechargeRecord::getAudienceId, Collectors.counting()));

        long repeatUsers = audienceRechargeCount.values().stream()
            .filter(count -> count > 1)
            .count();

        BigDecimal repurchaseRate = payingUsers > 0 ?
            BigDecimal.valueOf(repeatUsers)
                .divide(BigDecimal.valueOf(payingUsers), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP) :
            BigDecimal.ZERO;

        // 计算平均交易金额
        BigDecimal avgTransactionAmount = transactionCount > 0 ?
            gmv.divide(BigDecimal.valueOf(transactionCount), 2, RoundingMode.HALF_UP) :
            BigDecimal.ZERO;

        return FinancialReport.builder()
            .periodStart(startTime)
            .periodEnd(endTime)
            .gmv(gmv)
            .platformRevenue(platformRevenue)
            .anchorRevenue(anchorRevenue)
            .transactionCount(transactionCount)
            .totalUsers(totalUsers)
            .payingUsers(payingUsers)
            .arpu(arpu)
            .arppu(arppu)
            .paymentRate(paymentRate)
            .repurchaseRate(repurchaseRate)
            .avgTransactionAmount(avgTransactionAmount)
            .build();
    }

    /**
     * 财务报告DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class FinancialReport {
        private LocalDateTime periodStart; // 统计周期开始
        private LocalDateTime periodEnd; // 统计周期结束
        private BigDecimal gmv; // 平台总流水
        private BigDecimal platformRevenue; // 平台收入
        private BigDecimal anchorRevenue; // 主播总收入
        private Integer transactionCount; // 交易笔数
        private Integer totalUsers; // 总用户数
        private Integer payingUsers; // 付费用户数
        private BigDecimal arpu; // 人均收入
        private BigDecimal arppu; // 付费用户人均收入
        private BigDecimal paymentRate; // 付费率(%)
        private BigDecimal repurchaseRate; // 复购率(%)
        private BigDecimal avgTransactionAmount; // 平均交易金额
    }
}
