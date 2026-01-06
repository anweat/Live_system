package com.liveroom.analysis.service;

import com.liveroom.analysis.vo.AudiencePortraitVO;

import java.util.Map;

/**
 * 观众分析Service
 */
public interface AudienceAnalysisService {

    /**
     * 获取观众画像
     */
    AudiencePortraitVO getAudiencePortrait(Long audienceId);

    /**
     * 获取观众消费分层统计
     */
    Map<String, Object> getConsumptionDistribution();

    /**
     * 获取留存分析
     */
    Map<String, Object> getRetentionAnalysis(Integer days);

    /**
     * 获取流失预警名单
     */
    Map<String, Object> getChurnWarning(String riskLevel, Integer limit);
}
