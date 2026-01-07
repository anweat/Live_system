package com.liveroom.analysis.service;

import com.liveroom.analysis.dto.RankingItemDTO;
import common.exception.AnalysisException;
import common.logger.TraceLogger;
import common.service.DataAccessFacade;
import common.service.AnalysisQueryService.TopPayerStats;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 排行榜分析Service
 */
@Service
@RequiredArgsConstructor
public class RankingAnalysisService {

    private final DataAccessFacade dataAccessFacade;

    /**
     * 获取主播TOP消费者排行榜
     */
    @Cacheable(value = "analysis:ranking", key = "'toppayers:' + #anchorId + ':' + #startTime + ':' + #endTime + ':' + #limit")
    public List<RankingItemDTO> getTopPayersByAnchor(Long anchorId, LocalDateTime startTime, 
                                                       LocalDateTime endTime, int limit) {
        try {
            TraceLogger.info("RankingAnalysisService", "getTopPayersByAnchor", 
                anchorId, "limit", limit, "startTime", startTime, "endTime", endTime);

            List<TopPayerStats> topPayers = dataAccessFacade.analysisQuery()
                .getTopPayersByAnchor(anchorId, startTime, endTime, limit);

            List<RankingItemDTO> result = new ArrayList<>();
            int rank = 1;
            for (TopPayerStats stats : topPayers) {
                result.add(RankingItemDTO.builder()
                    .rank(rank++)
                    .userId(stats.getAudienceId())
                    .userName(stats.getAudienceName())
                    .value(stats.getTotalAmount())
                    .count(stats.getCount())
                    .extra(stats.getLastRechargeTime().toString())
                    .build());
            }

            return result;

        } catch (Exception e) {
            TraceLogger.error("RankingAnalysisService", "getTopPayersByAnchor", 
                anchorId, e, "limit", limit);
            throw new AnalysisException(5003, "获取TOP消费者排行榜失败: " + e.getMessage(), e);
        }
    }
}
