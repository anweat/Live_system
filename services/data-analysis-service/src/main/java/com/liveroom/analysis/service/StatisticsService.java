package com.liveroom.analysis.service;

import com.liveroom.analysis.vo.CashFlowTrendVO;

import java.time.LocalDate;
import java.util.Map;

/**
 * 统计分析Service
 */
public interface StatisticsService {

    /**
     * 获取GMV趋势
     */
    CashFlowTrendVO getGmvTrend(LocalDate startDate, LocalDate endDate, String granularity);

    /**
     * 获取平台关键指标
     */
    Map<String, Object> getKeyMetrics(LocalDate date);

    /**
     * 获取时段热力图
     */
    Map<String, Object> getTimeHeatmap();

    /**
     * 获取分类效果分析
     */
    Map<String, Object> getCategoryPerformance(Integer days);
}
