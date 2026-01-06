package com.liveroom.analysis.controller;

import com.liveroom.analysis.service.AudienceAnalysisService;
import com.liveroom.analysis.vo.AudiencePortraitVO;
import common.bean.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 观众分析Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/analysis/audience")
public class AudienceAnalysisController {

    @Autowired
    private AudienceAnalysisService audienceAnalysisService;

    /**
     * 查询观众画像
     * @param audienceId 观众ID
     * @return 观众画像数据
     */
    @GetMapping("/{audienceId}/portrait")
    public Result<AudiencePortraitVO> getAudiencePortrait(@PathVariable Long audienceId) {
        log.info("查询观众画像: audienceId={}", audienceId);
        
        AudiencePortraitVO portrait = audienceAnalysisService.getAudiencePortrait(audienceId);
        return Result.ok(portrait);
    }

    /**
     * 查询观众消费分层统计
     * @return 消费分层数据
     */
    @GetMapping("/consumption-distribution")
    public Result<?> getConsumptionDistribution() {
        log.info("查询观众消费分层统计");
        
        var distribution = audienceAnalysisService.getConsumptionDistribution();
        return Result.ok(distribution);
    }

    /**
     * 查询观众留存分析
     * @param days 留存天数: 1/7/30
     * @return 留存分析数据
     */
    @GetMapping("/retention")
    public Result<?> getRetentionAnalysis(@RequestParam(defaultValue = "7") Integer days) {
        log.info("查询观众留存分析: days={}", days);
        
        var retention = audienceAnalysisService.getRetentionAnalysis(days);
        return Result.ok(retention);
    }

    /**
     * 查询流失预警名单
     * @param riskLevel 风险等级: high/medium/low
     * @param limit 返回数量
     * @return 流失预警名单
     */
    @GetMapping("/churn-warning")
    public Result<?> getChurnWarning(
            @RequestParam(defaultValue = "high") String riskLevel,
            @RequestParam(defaultValue = "20") Integer limit) {
        log.info("查询流失预警名单: riskLevel={}, limit={}", riskLevel, limit);
        
        var churnList = audienceAnalysisService.getChurnWarning(riskLevel, limit);
        return Result.ok(churnList);
    }
}
