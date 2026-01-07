package com.liveroom.finance.controller;

import com.liveroom.finance.service.StatisticsService;
import com.liveroom.finance.vo.AnchorRevenueVO;
import com.liveroom.finance.vo.HourlyStatisticsVO;
import com.liveroom.finance.vo.TopAudienceVO;
import common.annotation.Log;
import common.exception.ValidationException;
import common.logger.TraceLogger;
import common.response.BaseResponse;
import common.response.ResponseUtil;
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
@RequestMapping("/api/v1/statistics")
@Slf4j
@Validated
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    /**
     * 查询主播收入统计
     * GET /api/v1/statistics/anchor/revenue/{anchorId}
     */
    @GetMapping("/anchor/revenue/{anchorId}")
    @Log("查询主播收入统计")
    public BaseResponse<AnchorRevenueVO> getAnchorRevenue(
            @PathVariable @NotNull Long anchorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        if (anchorId == null || anchorId <= 0) {
            throw new ValidationException("主播ID不合法");
        }
        if (startTime == null || endTime == null) {
            throw new ValidationException("开始时间和结束时间不能为空");
        }

        AnchorRevenueVO vo = statisticsService.getAnchorRevenue(anchorId, startTime, endTime);
        return ResponseUtil.success(vo);
    }

    /**
     * 查询主播每小时收入统计
     * GET /api/v1/statistics/anchor/hourly/{anchorId}
     */
    @GetMapping("/anchor/hourly/{anchorId}")
    @Log("查询主播每小时统计")
    public BaseResponse<List<HourlyStatisticsVO>> getHourlyStatistics(
            @PathVariable @NotNull Long anchorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        if (anchorId == null || anchorId <= 0) {
            throw new ValidationException("主播ID不合法");
        }
        if (startTime == null || endTime == null) {
            throw new ValidationException("开始时间和结束时间不能为空");
        }

        List<HourlyStatisticsVO> statistics = statisticsService.getHourlyStatistics(
                anchorId, startTime, endTime);
        return ResponseUtil.success(statistics);
    }

    /**
     * 查询主播TOP打赏观众
     * GET /api/v1/statistics/anchor/top-audiences/{anchorId}
     */
    @GetMapping("/anchor/top-audiences/{anchorId}")
    @Log("查询TOP打赏观众")
    public BaseResponse<List<TopAudienceVO>> getTopAudiences(
            @PathVariable @NotNull Long anchorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int topN) {
        
        if (anchorId == null || anchorId <= 0) {
            throw new ValidationException("主播ID不合法");
        }
        if (startTime == null || endTime == null) {
            throw new ValidationException("开始时间和结束时间不能为空");
        }

        List<TopAudienceVO> topList = statisticsService.getTopAudiences(
                anchorId, startTime, endTime, topN);
        return ResponseUtil.success(topList);
    }

    /**
     * 批量查询主播收入统计
     * POST /api/v1/statistics/anchor/batch-revenue
     */
    @PostMapping("/anchor/batch-revenue")
    @Log("批量查询主播收入统计")
    public BaseResponse<List<AnchorRevenueVO>> batchGetAnchorRevenue(
            @RequestBody @NotNull List<Long> anchorIds,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        if (anchorIds == null || anchorIds.isEmpty()) {
            throw new ValidationException("主播ID列表不能为空");
        }
        if (startTime == null || endTime == null) {
            throw new ValidationException("开始时间和结束时间不能为空");
        }

        List<AnchorRevenueVO> revenues = statisticsService.batchGetAnchorRevenue(
                anchorIds, startTime, endTime);
        return ResponseUtil.success(revenues);
    }

    /**
     * 查询主播收入排名
     * GET /api/v1/statistics/top-anchors
     */
    @GetMapping("/top-anchors")
    @Log("查询主播收入排名")
    public BaseResponse<List<AnchorRevenueVO>> getTopAnchorsByRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int topN) {
        
        if (startTime == null || endTime == null) {
            throw new ValidationException("开始时间和结束时间不能为空");
        }

        List<AnchorRevenueVO> topList = statisticsService.getTopAnchorsByRevenue(
                startTime, endTime, topN);
        return ResponseUtil.success(topList);
    }
}
