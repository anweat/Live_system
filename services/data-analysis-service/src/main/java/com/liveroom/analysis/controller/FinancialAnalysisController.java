package com.liveroom.analysis.controller;

import com.liveroom.analysis.dto.*;
import com.liveroom.analysis.service.*;
import common.logger.TraceLogger;
import common.response.BaseResponse;
import common.response.ResponseUtil;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 财务分析Controller
 */
@RestController
@RequestMapping("/api/v1/analysis/financial")
@RequiredArgsConstructor
@Validated
public class FinancialAnalysisController {

    private final FinancialAnalysisService financialAnalysisService;

    /**
     * 获取平台GMV（总流水）
     */
    @GetMapping("/gmv")
    public BaseResponse<BigDecimalDTO> getGMV(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        TraceLogger.info("FinancialAnalysisController", "getGMV", 
            null, "startTime", startTime, "endTime", endTime);

        BigDecimal gmv = financialAnalysisService.calculateGMV(startTime, endTime);
        return ResponseUtil.success(BigDecimalDTO.builder().value(gmv).build());
    }

    /**
     * 获取平台收入
     */
    @GetMapping("/platform-income")
    public BaseResponse<BigDecimalDTO> getPlatformIncome(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        TraceLogger.info("FinancialAnalysisController", "getPlatformIncome", 
            null, "startTime", startTime, "endTime", endTime);

        BigDecimal income = financialAnalysisService.calculatePlatformIncome(startTime, endTime);
        return ResponseUtil.success(BigDecimalDTO.builder().value(income).build());
    }

    /**
     * 获取主播收入
     */
    @GetMapping("/anchor-income")
    public BaseResponse<BigDecimalDTO> getAnchorIncome(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        TraceLogger.info("FinancialAnalysisController", "getAnchorIncome", 
            null, "startTime", startTime, "endTime", endTime);

        BigDecimal income = financialAnalysisService.calculateAnchorIncome(startTime, endTime);
        return ResponseUtil.success(BigDecimalDTO.builder().value(income).build());
    }

    /**
     * 获取ARPU
     */
    @GetMapping("/arpu")
    public BaseResponse<BigDecimalDTO> getARPU(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        TraceLogger.info("FinancialAnalysisController", "getARPU", 
            null, "startTime", startTime, "endTime", endTime);

        BigDecimal arpu = financialAnalysisService.calculateARPU(startTime, endTime);
        return ResponseUtil.success(BigDecimalDTO.builder().value(arpu).build());
    }

    /**
     * 获取ARPPU
     */
    @GetMapping("/arppu")
    public BaseResponse<BigDecimalDTO> getARPPU(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        TraceLogger.info("FinancialAnalysisController", "getARPPU", 
            null, "startTime", startTime, "endTime", endTime);

        BigDecimal arppu = financialAnalysisService.calculateARPPU(startTime, endTime);
        return ResponseUtil.success(BigDecimalDTO.builder().value(arppu).build());
    }

    /**
     * 获取付费率
     */
    @GetMapping("/payment-rate")
    public BaseResponse<BigDecimalDTO> getPaymentRate(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        TraceLogger.info("FinancialAnalysisController", "getPaymentRate", 
            null, "startTime", startTime, "endTime", endTime);

        BigDecimal rate = financialAnalysisService.calculatePaymentRate(startTime, endTime);
        return ResponseUtil.success(BigDecimalDTO.builder().value(rate).build());
    }

    /**
     * 获取复购率
     */
    @GetMapping("/repurchase-rate")
    public BaseResponse<BigDecimalDTO> getRepurchaseRate(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        TraceLogger.info("FinancialAnalysisController", "getRepurchaseRate", 
            null, "startTime", startTime, "endTime", endTime);

        BigDecimal rate = financialAnalysisService.calculateRepurchaseRate(startTime, endTime);
        return ResponseUtil.success(BigDecimalDTO.builder().value(rate).build());
    }

    /**
     * 获取所有财务指标
     */
    @GetMapping("/metrics")
    public BaseResponse<KeyMetricsDTO> getAllFinancialMetrics(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        TraceLogger.info("FinancialAnalysisController", "getAllFinancialMetrics", 
            null, "startTime", startTime, "endTime", endTime);

        KeyMetricsDTO metrics = financialAnalysisService.getAllFinancialMetrics(startTime, endTime);
        return ResponseUtil.success(metrics);
    }
}

/**
 * 用于包装BigDecimal响应的DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class BigDecimalDTO {
    private java.math.BigDecimal value;
}
