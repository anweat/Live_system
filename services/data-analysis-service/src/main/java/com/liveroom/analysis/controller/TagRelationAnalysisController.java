package com.liveroom.analysis.controller;

import com.liveroom.analysis.dto.*;
import com.liveroom.analysis.service.*;
import common.logger.TraceLogger;
import common.response.BaseResponse;
import common.response.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 标签关联分析Controller
 */
@RestController
@RequestMapping("/api/v1/analysis/tag")
@RequiredArgsConstructor
@Validated
public class TagRelationAnalysisController {

    private final TagRelationAnalysisService tagRelationAnalysisService;

    /**
     * 获取标签关联度
     */
    @GetMapping("/relation/{tagId1}/{tagId2}")
    public BaseResponse<TagRelationAnalysisDTO> getTagRelation(
            @PathVariable @NotNull Long tagId1,
            @PathVariable @NotNull Long tagId2) {
        
        TraceLogger.info("TagRelationAnalysisController", "getTagRelation", 
            null, "tagId1", tagId1, "tagId2", tagId2);

        TagRelationAnalysisDTO relation = tagRelationAnalysisService.getTagRelation(tagId1, tagId2);
        return ResponseUtil.success(relation);
    }

    /**
     * 获取TOP关联标签
     */
    @GetMapping("/relation/top/{tagId}")
    public BaseResponse<List<TagRelationAnalysisDTO>> getTopRelatedTags(
            @PathVariable @NotNull Long tagId,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit) {
        
        TraceLogger.info("TagRelationAnalysisController", "getTopRelatedTags", 
            tagId, "limit", limit);

        List<TagRelationAnalysisDTO> relations = tagRelationAnalysisService.getTopRelatedTags(tagId, limit);
        return ResponseUtil.success(relations);
    }

    /**
     * 获取标签关联热力图
     */
    @PostMapping("/relation/heatmap")
    public BaseResponse<Map<String, Map<String, java.math.BigDecimal>>> getTagRelationHeatmap(
            @RequestBody List<Long> tagIds) {
        
        TraceLogger.info("TagRelationAnalysisController", "getTagRelationHeatmap", 
            null, "tagCount", tagIds.size());

        Map<String, Map<String, java.math.BigDecimal>> heatmap = tagRelationAnalysisService.getTagRelationHeatmap(tagIds);
        return ResponseUtil.success(heatmap);
    }
}
