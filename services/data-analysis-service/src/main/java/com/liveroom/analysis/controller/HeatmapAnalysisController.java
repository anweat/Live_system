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
 * 热力图分析Controller
 */
@RestController
@RequestMapping("/api/v1/analysis/heatmap")
@RequiredArgsConstructor
@Validated
public class HeatmapAnalysisController {

    private final HeatmapAnalysisService heatmapAnalysisService;

    /**
     * 获取时段热力图
     */
    @GetMapping("/hourly")
    public BaseResponse<HeatmapDataDTO> getHourlyHeatmap(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        TraceLogger.info("HeatmapAnalysisController", "getHourlyHeatmap", 
            null, "startTime", startTime, "endTime", endTime);

        HeatmapDataDTO heatmap = heatmapAnalysisService.getHourlyHeatmap(startTime, endTime);
        return ResponseUtil.success(heatmap);
    }

    /**
     * 获取主播时段热力图
     */
    @GetMapping("/anchor-hourly/{anchorId}")
    public BaseResponse<HeatmapDataDTO> getAnchorHourlyHeatmap(
            @PathVariable @NotNull Long anchorId,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        TraceLogger.info("HeatmapAnalysisController", "getAnchorHourlyHeatmap", 
            anchorId, "startTime", startTime, "endTime", endTime);

        HeatmapDataDTO heatmap = heatmapAnalysisService.getAnchorHourlyHeatmap(anchorId, startTime, endTime);
        return ResponseUtil.success(heatmap);
    }

    /**
     * 获取每日热力图
     */
    @GetMapping("/daily")
    public BaseResponse<HeatmapDataDTO> getDailyHeatmap(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        TraceLogger.info("HeatmapAnalysisController", "getDailyHeatmap", 
            null, "startTime", startTime, "endTime", endTime);

        HeatmapDataDTO heatmap = heatmapAnalysisService.getDailyHeatmap(startTime, endTime);
        return ResponseUtil.success(heatmap);
    }

    /**
     * 获取用户活跃热力图
     */
    @GetMapping("/user-activity")
    public BaseResponse<Object> getUserActivityHeatmap(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        TraceLogger.info("HeatmapAnalysisController", "getUserActivityHeatmap", 
            null, "startTime", startTime, "endTime", endTime);

        Object heatmap = heatmapAnalysisService.getUserActivityHeatmap(startTime, endTime);
        return ResponseUtil.success(heatmap);
    }
}
