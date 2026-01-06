package com.liveroom.analysis.service;

import com.liveroom.analysis.vo.TagHeatmapVO;

import java.util.Map;

/**
 * 标签分析Service
 */
public interface TagAnalysisService {

    /**
     * 获取标签热力图
     */
    TagHeatmapVO getTagHeatmap(Integer limit);

    /**
     * 获取关联标签
     */
    Map<String, Object> getRelatedTags(String tagName, Integer limit);

    /**
     * 获取热门标签
     */
    Map<String, Object> getHotTags(Integer limit);
}
