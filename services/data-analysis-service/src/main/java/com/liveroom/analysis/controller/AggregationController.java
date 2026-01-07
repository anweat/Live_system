package com.liveroom.analysis.controller;

import com.liveroom.analysis.dto.KeyMetricsDTO;
import com.liveroom.analysis.service.AggregationAnalysisService;
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
 * 聚合统计分析Controller
 */
@RestController
@RequestMapping("/api/analysis/aggregation")
@RequiredArgsConstructor
@Validated
public class AggregationController {

    private final AggregationAnalysisService aggregationAnalysisService;

    /**
     * 获取关键指标
     */
    @GetMapping("/metrics")
    public BaseResponse<KeyMetricsDTO> getKeyMetrics(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        TraceLogger.info("AggregationController", "getKeyMetrics", 
            null, "startTime", startTime, "endTime", endTime);

        KeyMetricsDTO data = aggregationAnalysisService.getKeyMetrics(startTime, endTime);
        return ResponseUtil.success(data);
    }
}
