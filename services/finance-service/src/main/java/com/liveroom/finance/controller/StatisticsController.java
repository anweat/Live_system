package com.liveroom.finance.controller;

import com.liveroom.finance.service.StatisticsService;
import com.liveroom.finance.vo.AnchorRevenueVO;
import com.liveroom.finance.vo.HourlyStatisticsVO;
import com.liveroom.finance.vo.TopAudienceVO;
import common.bean.ApiResponse;
import common.logger.TraceLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 统计控制器
 * 提供财务数据统计查询API
 */
@RestController
@RequestMapping("/api/finance/statistics")
@Slf4j
@Validated
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    /**
     * 查询主播收入统计
     */
    @GetMapping("/anchor/revenue/{anchorId}")
    public ApiResponse<AnchorRevenueVO> getAnchorRevenue(
            @PathVariable @NotNull Long anchorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        TraceLogger.info("StatisticsController", "getAnchorRevenue",
                "查询主播收入统计请求，anchorId: " + anchorId);

        AnchorRevenueVO vo = statisticsService.getAnchorRevenue(anchorId, startTime, endTime);
        return ApiResponse.success(vo);
    }

    /**
     * 查询主播每小时收入统计
     */
    @GetMapping("/anchor/hourly/{anchorId}")
    public ApiResponse<List<HourlyStatisticsVO>> getHourlyStatistics(
            @PathVariable @NotNull Long anchorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        TraceLogger.info("StatisticsController", "getHourlyStatistics",
                "查询主播每小时统计请求，anchorId: " + anchorId);

        List<HourlyStatisticsVO> statistics = statisticsService.getHourlyStatistics(
                anchorId, startTime, endTime);
        return ApiResponse.success(statistics);
    }

    /**
     * 查询主播TOP打赏观众
     */
    @GetMapping("/anchor/top-audiences/{anchorId}")
    public ApiResponse<List<TopAudienceVO>> getTopAudiences(
            @PathVariable @NotNull Long anchorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int topN) {
        
        TraceLogger.info("StatisticsController", "getTopAudiences",
                "查询TOP打赏观众请求，anchorId: " + anchorId + ", topN: " + topN);

        List<TopAudienceVO> topList = statisticsService.getTopAudiences(
                anchorId, startTime, endTime, topN);
        return ApiResponse.success(topList);
    }

    /**
     * 批量查询主播收入统计
     */
    @PostMapping("/anchor/batch-revenue")
    public ApiResponse<List<AnchorRevenueVO>> batchGetAnchorRevenue(
            @RequestBody @NotNull List<Long> anchorIds,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        TraceLogger.info("StatisticsController", "batchGetAnchorRevenue",
                "批量查询主播收入请求，数量: " + anchorIds.size());

        List<AnchorRevenueVO> revenues = statisticsService.batchGetAnchorRevenue(
                anchorIds, startTime, endTime);
        return ApiResponse.success(revenues);
    }

    /**
     * 查询主播收入排名
     */
    @GetMapping("/top-anchors")
    public ApiResponse<List<AnchorRevenueVO>> getTopAnchorsByRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int topN) {
        
        TraceLogger.info("StatisticsController", "getTopAnchorsByRevenue",
                "查询主播收入排名请求，topN: " + topN);

        List<AnchorRevenueVO> topList = statisticsService.getTopAnchorsByRevenue(
                startTime, endTime, topN);
        return ApiResponse.success(topList);
    }
}
