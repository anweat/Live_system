package com.liveroom.analysis.controller;

import com.liveroom.analysis.dto.RankingItemDTO;
import com.liveroom.analysis.service.RankingAnalysisService;
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

/**
 * 排行榜分析Controller
 */
@RestController
@RequestMapping("/api/analysis/ranking")
@RequiredArgsConstructor
@Validated
public class RankingController {

    private final RankingAnalysisService rankingAnalysisService;

    /**
     * 获取主播TOP消费者排行榜
     */
    @GetMapping("/toppayers/{anchorId}")
    public BaseResponse<List<RankingItemDTO>> getTopPayersByAnchor(
            @PathVariable @NotNull Long anchorId,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {
        
        TraceLogger.info("RankingController", "getTopPayersByAnchor", 
            anchorId, "limit", limit, "startTime", startTime, "endTime", endTime);

        List<RankingItemDTO> data = rankingAnalysisService.getTopPayersByAnchor(
            anchorId, startTime, endTime, limit);
        return ResponseUtil.success(data);
    }
}
