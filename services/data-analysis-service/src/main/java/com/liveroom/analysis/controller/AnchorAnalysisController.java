package com.liveroom.analysis.controller;

import com.liveroom.analysis.service.AnchorAnalysisService;
import com.liveroom.analysis.vo.AnchorIncomeVO;
import common.bean.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 主播分析Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/analysis/anchor")
public class AnchorAnalysisController {

    @Autowired
    private AnchorAnalysisService anchorAnalysisService;

    /**
     * 查询主播收入分析
     * @param anchorId 主播ID
     * @param period 时间周期: day/week/month
     * @param days 查询天数（可选，默认30天）
     * @return 主播收入分析数据
     */
    @GetMapping("/{anchorId}/income")
    public Result<AnchorIncomeVO> getAnchorIncome(
            @PathVariable Long anchorId,
            @RequestParam(defaultValue = "day") String period,
            @RequestParam(defaultValue = "30") Integer days) {
        log.info("查询主播收入分析: anchorId={}, period={}, days={}", anchorId, period, days);
        
        AnchorIncomeVO incomeVO = anchorAnalysisService.analyzeAnchorIncome(anchorId, period, days);
        return Result.ok(incomeVO);
    }

    /**
     * 查询主播多维度评估（雷达图数据）
     * @param anchorId 主播ID
     * @return 雷达图数据
     */
    @GetMapping("/{anchorId}/radar")
    public Result<?> getAnchorRadar(@PathVariable Long anchorId) {
        log.info("查询主播雷达图数据: anchorId={}", anchorId);
        
        var radarData = anchorAnalysisService.getAnchorRadarData(anchorId);
        return Result.ok(radarData);
    }

    /**
     * 批量查询主播收入对比
     * @param anchorIds 主播ID列表（逗号分隔）
     * @param period 时间周期
     * @param days 查询天数
     * @return 主播对比数据
     */
    @GetMapping("/compare")
    public Result<?> compareAnchors(
            @RequestParam String anchorIds,
            @RequestParam(defaultValue = "day") String period,
            @RequestParam(defaultValue = "30") Integer days) {
        log.info("批量对比主播收入: anchorIds={}, period={}, days={}", anchorIds, period, days);
        
        var compareData = anchorAnalysisService.compareAnchors(anchorIds, period, days);
        return Result.ok(compareData);
    }
}
