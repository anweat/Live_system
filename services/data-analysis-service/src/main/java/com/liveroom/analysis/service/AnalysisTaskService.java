package com.liveroom.analysis.service;

import java.util.Map;

/**
 * 分析任务Service - 异步任务触发和管理
 */
public interface AnalysisTaskService {

    /**
     * 执行小时统计任务
     */
    void runHourlyStatistics();

    /**
     * 执行观众画像计算任务
     */
    void runAudiencePortraitCalculation();

    /**
     * 执行标签关联度计算任务
     */
    void runTagRelationCalculation();

    /**
     * 执行留存分析任务
     */
    void runRetentionAnalysis();

    /**
     * 获取任务执行状态
     */
    Map<String, Object> getTaskStatus();
}
