package com.liveroom.analysis.controller;

import com.liveroom.analysis.dto.TimeSeriesDataDTO;
import com.liveroom.analysis.service.TimeSeriesAnalysisService;
import common.logger.TraceLogger;
import common.response.BaseResponse;
import common.response.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 时间序列分析Controller
 */
@RestController
@RequestMapping("/api/analysis/timeseries")
@RequiredArgsConstructor
@Validated
public class TimeSeriesController {

    private final TimeSeriesAnalysisService timeSeriesAnalysisService;

    /**
     * 获取每日时间序列数据
     */
    @GetMapping("/daily")
    public BaseResponse<TimeSeriesDataDTO> getDailyTimeSeries(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        TraceLogger.info("TimeSeriesController", "getDailyTimeSeries", 
            null, "startTime", startTime, "endTime", endTime);

        TimeSeriesDataDTO data = timeSeriesAnalysisService.getDailyTimeSeries(startTime, endTime);
        return ResponseUtil.success(data);
    }

    /**
     * 获取每小时时间序列数据
     */
    @GetMapping("/hourly")
    public BaseResponse<TimeSeriesDataDTO> getHourlyTimeSeries(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        TraceLogger.info("TimeSeriesController", "getHourlyTimeSeries", 
            null, "startTime", startTime, "endTime", endTime);

        TimeSeriesDataDTO data = timeSeriesAnalysisService.getHourlyTimeSeries(startTime, endTime);
        return ResponseUtil.success(data);
    }
}
