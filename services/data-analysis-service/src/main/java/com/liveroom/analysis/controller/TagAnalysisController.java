package com.liveroom.analysis.controller;

import com.liveroom.analysis.service.TagAnalysisService;
import com.liveroom.analysis.vo.TagHeatmapVO;
import common.bean.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 标签分析Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/analysis/tag")
public class TagAnalysisController {

    @Autowired
    private TagAnalysisService tagAnalysisService;

    /**
     * 查询标签热力图
     * @param limit 返回标签数量（默认20）
     * @return 标签热力图数据
     */
    @GetMapping("/heatmap")
    public Result<TagHeatmapVO> getTagHeatmap(@RequestParam(defaultValue = "20") Integer limit) {
        log.info("查询标签热力图: limit={}", limit);
        
        TagHeatmapVO heatmap = tagAnalysisService.getTagHeatmap(limit);
        return Result.ok(heatmap);
    }

    /**
     * 查询指定标签的关联标签
     * @param tagName 标签名称
     * @param limit 返回数量
     * @return 关联标签列表
     */
    @GetMapping("/related")
    public Result<?> getRelatedTags(
            @RequestParam String tagName,
            @RequestParam(defaultValue = "10") Integer limit) {
        log.info("查询关联标签: tagName={}, limit={}", tagName, limit);
        
        var relatedTags = tagAnalysisService.getRelatedTags(tagName, limit);
        return Result.ok(relatedTags);
    }

    /**
     * 查询热门标签排行
     * @param limit 返回数量
     * @return 热门标签列表
     */
    @GetMapping("/hot")
    public Result<?> getHotTags(@RequestParam(defaultValue = "20") Integer limit) {
        log.info("查询热门标签: limit={}", limit);
        
        var hotTags = tagAnalysisService.getHotTags(limit);
        return Result.ok(hotTags);
    }
}
