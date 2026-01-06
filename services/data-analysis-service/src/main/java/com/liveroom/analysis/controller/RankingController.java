package com.liveroom.analysis.controller;

import com.liveroom.analysis.service.RankingService;
import com.liveroom.analysis.vo.RankingVO;
import common.bean.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 排行榜Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/analysis/ranking")
public class RankingController {

    @Autowired
    private RankingService rankingService;

    /**
     * 查询主播收入排行榜
     * @param period 时间周期: day/week/month
     * @param limit 返回数量
     * @return 排行榜数据
     */
    @GetMapping("/anchor/income")
    public Result<RankingVO> getAnchorIncomeRanking(
            @RequestParam(defaultValue = "month") String period,
            @RequestParam(defaultValue = "10") Integer limit) {
        log.info("查询主播收入排行榜: period={}, limit={}", period, limit);
        
        RankingVO ranking = rankingService.getAnchorIncomeRanking(period, limit);
        return Result.ok(ranking);
    }

    /**
     * 查询观众消费排行榜
     * @param period 时间周期: day/week/month/all
     * @param limit 返回数量
     * @return 排行榜数据
     */
    @GetMapping("/audience/consumption")
    public Result<RankingVO> getAudienceConsumptionRanking(
            @RequestParam(defaultValue = "month") String period,
            @RequestParam(defaultValue = "10") Integer limit) {
        log.info("查询观众消费排行榜: period={}, limit={}", period, limit);
        
        RankingVO ranking = rankingService.getAudienceConsumptionRanking(period, limit);
        return Result.ok(ranking);
    }

    /**
     * 查询主播粉丝数排行榜
     * @param limit 返回数量
     * @return 排行榜数据
     */
    @GetMapping("/anchor/fans")
    public Result<RankingVO> getAnchorFansRanking(@RequestParam(defaultValue = "10") Integer limit) {
        log.info("查询主播粉丝数排行榜: limit={}", limit);
        
        RankingVO ranking = rankingService.getAnchorFansRanking(limit);
        return Result.ok(ranking);
    }

    /**
     * 查询直播间热度排行榜
     * @param limit 返回数量
     * @return 排行榜数据
     */
    @GetMapping("/live-room/popularity")
    public Result<RankingVO> getLiveRoomPopularityRanking(@RequestParam(defaultValue = "10") Integer limit) {
        log.info("查询直播间热度排行榜: limit={}", limit);
        
        RankingVO ranking = rankingService.getLiveRoomPopularityRanking(limit);
        return Result.ok(ranking);
    }
}
