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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 留存分析Controller
 */
@RestController
@RequestMapping("/api/v1/analysis/retention")
@RequiredArgsConstructor
@Validated
public class RetentionAnalysisController {

    private final RetentionAnalysisService retentionAnalysisService;

    /**
     * 获取用户留存分析
     */
    @GetMapping("/daily/{analysisDate}")
    public BaseResponse<RetentionAnalysisDTO> getRetentionAnalysis(
            @PathVariable @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate analysisDate) {
        
        TraceLogger.info("RetentionAnalysisController", "getRetentionAnalysis", 
            null, "analysisDate", analysisDate);

        RetentionAnalysisDTO analysis = retentionAnalysisService.getRetentionAnalysis(analysisDate);
        return ResponseUtil.success(analysis);
    }

    /**
     * 获取时间段留存分析
     */
    @GetMapping("/period")
    public BaseResponse<List<RetentionAnalysisDTO>> getPeriodRetentionAnalysis(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        TraceLogger.info("RetentionAnalysisController", "getPeriodRetentionAnalysis", 
            null, "startTime", startTime, "endTime", endTime);

        List<RetentionAnalysisDTO> analysis = retentionAnalysisService.getPeriodRetentionAnalysis(startTime, endTime);
        return ResponseUtil.success(analysis);
    }
}
