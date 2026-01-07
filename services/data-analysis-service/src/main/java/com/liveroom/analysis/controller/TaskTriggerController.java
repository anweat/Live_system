package com.liveroom.analysis.controller;

import com.liveroom.analysis.task.*;
import common.logger.TraceLogger;
import common.response.BaseResponse;
import common.response.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 任务触发器Controller
 * 提供手动触发各种分析任务的接口
 */
@RestController
@RequestMapping("/api/v1/analysis/trigger")
@RequiredArgsConstructor
public class TaskTriggerController {

    private final HourlyStatisticsTask hourlyStatisticsTask;
    private final DailyStatisticsTask dailyStatisticsTask;
    private final WeeklyStatisticsTask weeklyStatisticsTask;
    private final MonthlyStatisticsTask monthlyStatisticsTask;

    /**
     * 手动触发小时统计任务
     */
    @PostMapping("/hourly-statistics")
    public BaseResponse<String> triggerHourlyStatistics() {
        TraceLogger.info("TaskTriggerController", "triggerHourlyStatistics", null);
        
        try {
            hourlyStatisticsTask.processHourlyStatistics();
            return ResponseUtil.success("小时统计任务触发成功");
        } catch (Exception e) {
            TraceLogger.error("TaskTriggerController", "triggerHourlyStatistics", null, e);
            return ResponseUtil.error(500, "小时统计任务触发失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发日统计任务
     */
    @PostMapping("/daily-statistics")
    public BaseResponse<String> triggerDailyStatistics() {
        TraceLogger.info("TaskTriggerController", "triggerDailyStatistics", null);
        
        try {
            dailyStatisticsTask.processDailyStatistics();
            return ResponseUtil.success("日统计任务触发成功");
        } catch (Exception e) {
            TraceLogger.error("TaskTriggerController", "triggerDailyStatistics", null, e);
            return ResponseUtil.error(500, "日统计任务触发失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发留存分析任务
     */
    @PostMapping("/retention-analysis")
    public BaseResponse<String> triggerRetentionAnalysis() {
        TraceLogger.info("TaskTriggerController", "triggerRetentionAnalysis", null);
        
        try {
            dailyStatisticsTask.processRetentionAnalysis();
            return ResponseUtil.success("留存分析任务触发成功");
        } catch (Exception e) {
            TraceLogger.error("TaskTriggerController", "triggerRetentionAnalysis", null, e);
            return ResponseUtil.error(500, "留存分析任务触发失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发标签关联度计算任务
     */
    @PostMapping("/tag-relation")
    public BaseResponse<String> triggerTagRelationCalculation() {
        TraceLogger.info("TaskTriggerController", "triggerTagRelationCalculation", null);
        
        try {
            dailyStatisticsTask.processTagRelationCalculation();
            return ResponseUtil.success("标签关联度计算任务触发成功");
        } catch (Exception e) {
            TraceLogger.error("TaskTriggerController", "triggerTagRelationCalculation", null, e);
            return ResponseUtil.error(500, "标签关联度计算任务触发失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发周统计任务
     */
    @PostMapping("/weekly-statistics")
    public BaseResponse<String> triggerWeeklyStatistics() {
        TraceLogger.info("TaskTriggerController", "triggerWeeklyStatistics", null);
        
        try {
            weeklyStatisticsTask.processWeeklyStatistics();
            return ResponseUtil.success("周统计任务触发成功");
        } catch (Exception e) {
            TraceLogger.error("TaskTriggerController", "triggerWeeklyStatistics", null, e);
            return ResponseUtil.error(500, "周统计任务触发失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发月统计任务
     */
    @PostMapping("/monthly-statistics")
    public BaseResponse<String> triggerMonthlyStatistics() {
        TraceLogger.info("TaskTriggerController", "triggerMonthlyStatistics", null);
        
        try {
            monthlyStatisticsTask.processMonthlyStatistics();
            return ResponseUtil.success("月统计任务触发成功");
        } catch (Exception e) {
            TraceLogger.error("TaskTriggerController", "triggerMonthlyStatistics", null, e);
            return ResponseUtil.error(500, "月统计任务触发失败: " + e.getMessage());
        }
    }
}
