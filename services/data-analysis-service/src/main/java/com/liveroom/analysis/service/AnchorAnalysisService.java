package com.liveroom.analysis.service;

import com.liveroom.analysis.vo.AnchorIncomeVO;

import java.util.Map;

/**
 * 主播分析Service
 */
public interface AnchorAnalysisService {

    /**
     * 分析主播收入
     * @param anchorId 主播ID
     * @param period 时间周期 (day/week/month)
     * @param days 查询天数
     * @return 主播收入分析数据
     */
    AnchorIncomeVO analyzeAnchorIncome(Long anchorId, String period, Integer days);

    /**
     * 获取主播雷达图数据
     * @param anchorId 主播ID
     * @return 雷达图数据
     */
    Map<String, Object> getAnchorRadarData(Long anchorId);

    /**
     * 对比多个主播的收入
     * @param anchorIds 主播ID列表（逗号分隔）
     * @param period 时间周期
     * @param days 查询天数
     * @return 对比数据
     */
    Map<String, Object> compareAnchors(String anchorIds, String period, Integer days);
}
