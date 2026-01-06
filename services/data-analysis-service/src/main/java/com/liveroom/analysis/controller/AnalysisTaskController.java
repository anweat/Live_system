package com.liveroom.analysis.controller;

import com.liveroom.analysis.service.AnalysisTaskService;
import common.bean.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 分析任务Controller - 用于手动触发异步分析任务
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/analysis/task")
public class AnalysisTaskController {

    @Autowired
    private AnalysisTaskService analysisTaskService;

    /**
     * 触发小时统计任务
     */
    @PostMapping("/trigger/hourly-statistics")
    public Result<String> triggerHourlyStatistics() {
        log.info("手动触发小时统计任务");
        analysisTaskService.runHourlyStatistics();
        return Result.ok("小时统计任务已触发");
    }

    /**
     * 触发观众画像计算任务
     */
    @PostMapping("/trigger/audience-portrait")
    public Result<String> triggerAudiencePortrait() {
        log.info("手动触发观众画像计算任务");
        analysisTaskService.runAudiencePortraitCalculation();
        return Result.ok("观众画像计算任务已触发");
    }

    /**
     * 触发标签关联度计算任务
     */
    @PostMapping("/trigger/tag-relation")
    public Result<String> triggerTagRelation() {
        log.info("手动触发标签关联度计算任务");
        analysisTaskService.runTagRelationCalculation();
        return Result.ok("标签关联度计算任务已触发");
    }

    /**
     * 触发留存分析任务
     */
    @PostMapping("/trigger/retention-analysis")
    public Result<String> triggerRetentionAnalysis() {
        log.info("手动触发留存分析任务");
        analysisTaskService.runRetentionAnalysis();
        return Result.ok("留存分析任务已触发");
    }

    /**
     * 查询任务执行状态
     */
    @GetMapping("/status")
    public Result<?> getTaskStatus() {
        log.info("查询任务执行状态");
        var status = analysisTaskService.getTaskStatus();
        return Result.ok(status);
    }
}
