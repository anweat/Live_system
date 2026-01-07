package com.liveroom.analysis.service;

import com.liveroom.analysis.dto.KeyMetricsDTO;
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
import java.util.List;
import java.util.stream.Collectors;

/**
 * 财务分析Service
 * 提供GMV、ARPU、ARPPU等财务指标分析
 */
@Service
@RequiredArgsConstructor
public class FinancialAnalysisService {

    private final DataAccessFacade dataAccessFacade;

    /**
     * 获取平台GMV（总流水）
     */
    @Cacheable(value = "analysis:financial", key = "'gmv:' + #startTime + ':' + #endTime")
    public BigDecimal calculateGMV(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            TraceLogger.info("FinancialAnalysisService", "calculateGMV",
                null, "startTime", startTime, "endTime", endTime);

            List<Recharge> recharges = dataAccessFacade.analysisQuery()
                .getRechargesByTimeRange(startTime, endTime);

            BigDecimal gmv = recharges.stream()
                .map(Recharge::getRechargeAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            TraceLogger.info("FinancialAnalysisService", "calculateGMV",
                null, "result", gmv);

            return gmv;

        } catch (Exception e) {
            TraceLogger.error("FinancialAnalysisService", "calculateGMV",
                null, e);
            throw new AnalysisException(5010, "计算GMV失败: " + e.getMessage(), e);
        }
    }

    /**
     * 计算平台实际收入（扣除主播分成）
     */
    @Cacheable(value = "analysis:financial", key = "'platform-income:' + #startTime + ':' + #endTime")
    public BigDecimal calculatePlatformIncome(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            TraceLogger.info("FinancialAnalysisService", "calculatePlatformIncome",
                null, "startTime", startTime, "endTime", endTime);

            List<Recharge> recharges = dataAccessFacade.analysisQuery()
                .getRechargesByTimeRange(startTime, endTime);

            BigDecimal platformIncome = recharges.stream()
                .map(Recharge::getRechargeAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            return platformIncome;

        } catch (Exception e) {
            TraceLogger.error("FinancialAnalysisService", "calculatePlatformIncome",
                null, e);
            throw new AnalysisException(5011, "计算平台收入失败: " + e.getMessage(), e);
        }
    }

    /**
     * 计算主播实际收入
     */
    @Cacheable(value = "analysis:financial", key = "'anchor-income:' + #startTime + ':' + #endTime")
    public BigDecimal calculateAnchorIncome(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            TraceLogger.info("FinancialAnalysisService", "calculateAnchorIncome",
                null, "startTime", startTime, "endTime", endTime);

            List<Recharge> recharges = dataAccessFacade.analysisQuery()
                .getRechargesByTimeRange(startTime, endTime);

            BigDecimal anchorIncome = recharges.stream()
                .map(Recharge::getRechargeAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            return anchorIncome;

        } catch (Exception e) {
            TraceLogger.error("FinancialAnalysisService", "calculateAnchorIncome",
                null, e);
            throw new AnalysisException(5012, "计算主播收入失败: " + e.getMessage(), e);
        }
    }

    /**
     * 计算ARPU（Average Revenue Per User）
     */
    @Cacheable(value = "analysis:financial", key = "'arpu:' + #startTime + ':' + #endTime")
    public BigDecimal calculateARPU(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            TraceLogger.info("FinancialAnalysisService", "calculateARPU",
                null, "startTime", startTime, "endTime", endTime);

            BigDecimal gmv = calculateGMV(startTime, endTime);
            long totalUsers = dataAccessFacade.audience().count();

            if (totalUsers == 0) {
                return BigDecimal.ZERO;
            }

            BigDecimal arpu = gmv.divide(BigDecimal.valueOf(totalUsers), 2, RoundingMode.HALF_UP);

            TraceLogger.info("FinancialAnalysisService", "calculateARPU",
                null, "totalUsers", totalUsers, "arpu", arpu);

            return arpu;

        } catch (Exception e) {
            TraceLogger.error("FinancialAnalysisService", "calculateARPU",
                null, e);
            throw new AnalysisException(5013, "计算ARPU失败: " + e.getMessage(), e);
        }
    }

    /**
     * 计算ARPPU（Average Revenue Per Paying User）
     */
    @Cacheable(value = "analysis:financial", key = "'arppu:' + #startTime + ':' + #endTime")
    public BigDecimal calculateARPPU(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            TraceLogger.info("FinancialAnalysisService", "calculateARPPU",
                null, "startTime", startTime, "endTime", endTime);

            List<Recharge> recharges = dataAccessFacade.analysisQuery()
                .getRechargesByTimeRange(startTime, endTime);

            if (recharges.isEmpty()) {
                return BigDecimal.ZERO;
            }

            BigDecimal gmv = recharges.stream()
                .map(Recharge::getRechargeAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            long payingUsers = recharges.stream()
                .map(Recharge::getAudienceId)
                .distinct()
                .count();

            BigDecimal arppu = gmv.divide(BigDecimal.valueOf(payingUsers), 2, RoundingMode.HALF_UP);

            TraceLogger.info("FinancialAnalysisService", "calculateARPPU",
                null, "payingUsers", payingUsers, "arppu", arppu);

            return arppu;

        } catch (Exception e) {
            TraceLogger.error("FinancialAnalysisService", "calculateARPPU",
                null, e);
            throw new AnalysisException(5014, "计算ARPPU失败: " + e.getMessage(), e);
        }
    }

    /**
     * 计算付费率
     */
    @Cacheable(value = "analysis:financial", key = "'payment-rate:' + #startTime + ':' + #endTime")
    public BigDecimal calculatePaymentRate(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            TraceLogger.info("FinancialAnalysisService", "calculatePaymentRate",
                null, "startTime", startTime, "endTime", endTime);

            List<Recharge> recharges = dataAccessFacade.analysisQuery()
                .getRechargesByTimeRange(startTime, endTime);

            long payingUsers = recharges.stream()
                .map(Recharge::getAudienceId)
                .distinct()
                .count();

            long totalUsers = dataAccessFacade.audience().count();

            if (totalUsers == 0) {
                return BigDecimal.ZERO;
            }

            BigDecimal paymentRate = BigDecimal.valueOf(payingUsers)
                .divide(BigDecimal.valueOf(totalUsers), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

            TraceLogger.info("FinancialAnalysisService", "calculatePaymentRate",
                null, "payingUsers", payingUsers, "totalUsers", totalUsers, "rate", paymentRate);

            return paymentRate;

        } catch (Exception e) {
            TraceLogger.error("FinancialAnalysisService", "calculatePaymentRate",
                null, e);
            throw new AnalysisException(5015, "计算付费率失败: " + e.getMessage(), e);
        }
    }

    /**
     * 计算复购率
     */
    @Cacheable(value = "analysis:financial", key = "'repurchase-rate:' + #startTime + ':' + #endTime")
    public BigDecimal calculateRepurchaseRate(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            TraceLogger.info("FinancialAnalysisService", "calculateRepurchaseRate",
                null, "startTime", startTime, "endTime", endTime);

            List<Recharge> recharges = dataAccessFacade.analysisQuery()
                .getRechargesByTimeRange(startTime, endTime);

            // 统计每个用户的打赏次数
            var userRechargeCount = recharges.stream()
                .collect(Collectors.groupingBy(
                    Recharge::getAudienceId,
                    Collectors.counting()
                ));

            long multiplePayingUsers = userRechargeCount.values().stream()
                .filter(count -> count > 1)
                .count();

            long totalPayingUsers = userRechargeCount.size();

            if (totalPayingUsers == 0) {
                return BigDecimal.ZERO;
            }

            BigDecimal repurchaseRate = BigDecimal.valueOf(multiplePayingUsers)
                .divide(BigDecimal.valueOf(totalPayingUsers), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

            TraceLogger.info("FinancialAnalysisService", "calculateRepurchaseRate",
                null, "multiplePayingUsers", multiplePayingUsers, 
                "totalPayingUsers", totalPayingUsers, "rate", repurchaseRate);

            return repurchaseRate;

        } catch (Exception e) {
            TraceLogger.error("FinancialAnalysisService", "calculateRepurchaseRate",
                null, e);
            throw new AnalysisException(5016, "计算复购率失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取完整的财务指标
     */
    @Cacheable(value = "analysis:financial", key = "'all-metrics:' + #startTime + ':' + #endTime")
    public KeyMetricsDTO getAllFinancialMetrics(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            TraceLogger.info("FinancialAnalysisService", "getAllFinancialMetrics",
                null, "startTime", startTime, "endTime", endTime);

            List<Recharge> recharges = dataAccessFacade.analysisQuery()
                .getRechargesByTimeRange(startTime, endTime);

            BigDecimal gmv = recharges.stream()
                .map(Recharge::getRechargeAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            int totalOrders = recharges.size();
            
            int payingUserCount = (int) recharges.stream()
                .map(Recharge::getAudienceId)
                .distinct()
                .count();

            int totalUserCount = (int) dataAccessFacade.audience().count();

            BigDecimal paymentRate = totalUserCount == 0 ? BigDecimal.ZERO :
                BigDecimal.valueOf(payingUserCount)
                    .divide(BigDecimal.valueOf(totalUserCount), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            BigDecimal arpu = totalUserCount == 0 ? BigDecimal.ZERO :
                gmv.divide(BigDecimal.valueOf(totalUserCount), 2, RoundingMode.HALF_UP);

            BigDecimal arppu = payingUserCount == 0 ? BigDecimal.ZERO :
                gmv.divide(BigDecimal.valueOf(payingUserCount), 2, RoundingMode.HALF_UP);

            BigDecimal avgOrderAmount = totalOrders == 0 ? BigDecimal.ZERO :
                gmv.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP);

            return KeyMetricsDTO.builder()
                .gmv(gmv)
                .payingUserCount(payingUserCount)
                .totalUserCount(totalUserCount)
                .paymentRate(paymentRate)
                .arpu(arpu)
                .arppu(arppu)
                .avgOrderAmount(avgOrderAmount)
                .totalOrders(totalOrders)
                .build();

        } catch (Exception e) {
            TraceLogger.error("FinancialAnalysisService", "getAllFinancialMetrics",
                null, e);
            throw new AnalysisException(5017, "获取财务指标失败: " + e.getMessage(), e);
        }
    }
}
