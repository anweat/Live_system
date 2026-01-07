package com.liveroom.analysis.controller;

import com.liveroom.analysis.dto.*;
import com.liveroom.analysis.service.*;
import common.logger.TraceLogger;
import common.response.BaseResponse;
import common.response.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 主播收入分析Controller
 */
@RestController
@RequestMapping("/api/v1/analysis/anchor")
@RequiredArgsConstructor
@Validated
public class AnchorIncomeAnalysisController {

    private final AnchorIncomeAnalysisService anchorIncomeAnalysisService;

    /**
     * 获取主播收入分析
     */
    @GetMapping("/income/{anchorId}")
    public BaseResponse<AnchorIncomeAnalysisDTO> getAnchorIncomeAnalysis(
            @PathVariable @NotNull Long anchorId,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        TraceLogger.info("AnchorIncomeAnalysisController", "getAnchorIncomeAnalysis", 
            anchorId, "startTime", startTime, "endTime", endTime);

        AnchorIncomeAnalysisDTO analysis = anchorIncomeAnalysisService.getAnchorIncomeAnalysis(anchorId, startTime, endTime);
        return ResponseUtil.success(analysis);
    }

    /**
     * 获取主播收入排行榜
     */
    @GetMapping("/income-ranking")
    public BaseResponse<List<AnchorIncomeAnalysisDTO>> getTopAnchorsByIncome(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {
        
        TraceLogger.info("AnchorIncomeAnalysisController", "getTopAnchorsByIncome", 
            null, "startTime", startTime, "endTime", endTime, "limit", limit);

        List<AnchorIncomeAnalysisDTO> rankings = anchorIncomeAnalysisService.getTopAnchorsByIncome(startTime, endTime, limit);
        return ResponseUtil.success(rankings);
    }
}
