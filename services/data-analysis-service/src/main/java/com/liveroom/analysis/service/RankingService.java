package com.liveroom.analysis.service;

import com.liveroom.analysis.vo.RankingVO;

/**
 * 排行榜Service
 */
public interface RankingService {

    /**
     * 获取主播收入排行榜
     */
    RankingVO getAnchorIncomeRanking(String period, Integer limit);

    /**
     * 获取观众消费排行榜
     */
    RankingVO getAudienceConsumptionRanking(String period, Integer limit);

    /**
     * 获取主播粉丝数排行榜
     */
    RankingVO getAnchorFansRanking(Integer limit);

    /**
     * 获取直播间热度排行榜
     */
    RankingVO getLiveRoomPopularityRanking(Integer limit);
}
