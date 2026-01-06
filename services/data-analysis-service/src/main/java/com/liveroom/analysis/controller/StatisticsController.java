package com.liveroom.analysis.controller;

import com.liveroom.analysis.service.StatisticsService;
import com.liveroom.analysis.vo.CashFlowTrendVO;
import common.bean.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 统计分析Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/analysis/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    /**
     * 查询GMV趋势
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param granularity 粒度: hour/day/week/month
     * @return GMV趋势数据
     */
    @GetMapping("/gmv-trend")
    public Result<CashFlowTrendVO> getGmvTrend(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "day") String granularity) {
        log.info("查询GMV趋势: startDate={}, endDate={}, granularity={}", startDate, endDate, granularity);
        
        CashFlowTrendVO trend = statisticsService.getGmvTrend(startDate, endDate, granularity);
        return Result.ok(trend);
    }

    /**
     * 查询平台关键指标
     * @param date 查询日期
     * @return 平台关键指标
     */
    @GetMapping("/key-metrics")
    public Result<?> getKeyMetrics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("查询平台关键指标: date={}", date);
        
        var metrics = statisticsService.getKeyMetrics(date != null ? date : LocalDate.now());
        return Result.ok(metrics);
    }

    /**
     * 查询时段热力图（一周内每小时的活跃度）
     * @return 时段热力图数据
     */
    @GetMapping("/time-heatmap")
    public Result<?> getTimeHeatmap() {
        log.info("查询时段热力图");
        
        var heatmap = statisticsService.getTimeHeatmap();
        return Result.ok(heatmap);
    }

    /**
     * 查询分类效果分析
     * @param days 查询天数
     * @return 分类效果数据
     */
    @GetMapping("/category-performance")
    public Result<?> getCategoryPerformance(@RequestParam(defaultValue = "30") Integer days) {
        log.info("查询分类效果分析: days={}", days);
        
        var performance = statisticsService.getCategoryPerformance(days);
        return Result.ok(performance);
    }
}
