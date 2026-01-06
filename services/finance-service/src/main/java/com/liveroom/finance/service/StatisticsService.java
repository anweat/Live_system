package com.liveroom.finance.service;

import com.liveroom.finance.repository.RechargeRecordRepository;
import com.liveroom.finance.vo.AnchorRevenueVO;
import com.liveroom.finance.vo.HourlyStatisticsVO;
import com.liveroom.finance.vo.TopAudienceVO;
import common.logger.TraceLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 统计服务
 * 提供财务数据的统计分析功能
 */
@Service
@Slf4j
public class StatisticsService {

    @Autowired
    private RechargeRecordRepository rechargeRecordRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String STATISTICS_CACHE_KEY = "finance:statistics:";

    /**
     * 查询主播收入统计（带缓存）
     */
    public AnchorRevenueVO getAnchorRevenue(Long anchorId, LocalDateTime startTime, LocalDateTime endTime) {
        TraceLogger.info("StatisticsService", "getAnchorRevenue",
                "查询主播收入统计，anchorId: " + anchorId);

        // 尝试从缓存获取
        String cacheKey = STATISTICS_CACHE_KEY + "anchor:" + anchorId + ":" + 
                startTime.toLocalDate() + ":" + endTime.toLocalDate();
        AnchorRevenueVO cached = (AnchorRevenueVO) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 查询数据库
        BigDecimal totalAmount = rechargeRecordRepository.sumAmountByAnchorAndTime(
                anchorId, startTime, endTime);
        Long totalCount = rechargeRecordRepository.countByAnchorAndTime(
                anchorId, startTime, endTime);

        AnchorRevenueVO vo = AnchorRevenueVO.builder()
                .anchorId(anchorId)
                .totalAmount(totalAmount != null ? totalAmount : BigDecimal.ZERO)
                .totalCount(totalCount != null ? totalCount : 0L)
                .startTime(startTime)
                .endTime(endTime)
                .queryTime(LocalDateTime.now())
                .build();

        // 缓存1小时
        redisTemplate.opsForValue().set(cacheKey, vo, 1, TimeUnit.HOURS);

        return vo;
    }

    /**
     * 查询主播每小时收入统计
     */
    public List<HourlyStatisticsVO> getHourlyStatistics(Long anchorId, 
                                                         LocalDateTime startTime, 
                                                         LocalDateTime endTime) {
        TraceLogger.info("StatisticsService", "getHourlyStatistics",
                "查询主播每小时统计，anchorId: " + anchorId);

        // 尝试从缓存获取
        String cacheKey = STATISTICS_CACHE_KEY + "hourly:" + anchorId + ":" + 
                startTime.toLocalDate() + ":" + endTime.toLocalDate();
        List<HourlyStatisticsVO> cached = (List<HourlyStatisticsVO>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 查询数据库
        List<Object[]> results = rechargeRecordRepository.getHourlyStatistics(
                anchorId, startTime, endTime);

        List<HourlyStatisticsVO> statistics = results.stream()
                .map(row -> HourlyStatisticsVO.builder()
                        .anchorId(anchorId)
                        .statisticsHour((Integer) row[0])
                        .rechargeCount(((Number) row[1]).longValue())
                        .totalAmount((BigDecimal) row[2])
                        .build())
                .collect(Collectors.toList());

        // 缓存30分钟
        redisTemplate.opsForValue().set(cacheKey, statistics, 30, TimeUnit.MINUTES);

        return statistics;
    }

    /**
     * 查询主播TOP打赏观众
     */
    public List<TopAudienceVO> getTopAudiences(Long anchorId, 
                                                LocalDateTime startTime, 
                                                LocalDateTime endTime,
                                                int topN) {
        TraceLogger.info("StatisticsService", "getTopAudiences",
                "查询TOP打赏观众，anchorId: " + anchorId + ", topN: " + topN);

        // 尝试从缓存获取
        String cacheKey = STATISTICS_CACHE_KEY + "top:" + anchorId + ":" + 
                startTime.toLocalDate() + ":" + endTime.toLocalDate() + ":" + topN;
        List<TopAudienceVO> cached = (List<TopAudienceVO>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 查询数据库
        List<Object[]> results = rechargeRecordRepository.getTopAudiencesByAmount(
                anchorId, startTime, endTime, topN);

        List<TopAudienceVO> topList = results.stream()
                .map(row -> TopAudienceVO.builder()
                        .anchorId(anchorId)
                        .audienceId((Long) row[0])
                        .audienceName((String) row[1])
                        .totalAmount((BigDecimal) row[2])
                        .rechargeCount(((Number) row[3]).longValue())
                        .rank(results.indexOf(row) + 1)
                        .build())
                .collect(Collectors.toList());

        // 缓存30分钟
        redisTemplate.opsForValue().set(cacheKey, topList, 30, TimeUnit.MINUTES);

        return topList;
    }

    /**
     * 批量查询多个主播的收入统计（并发优化）
     */
    public List<AnchorRevenueVO> batchGetAnchorRevenue(List<Long> anchorIds, 
                                                         LocalDateTime startTime, 
                                                         LocalDateTime endTime) {
        TraceLogger.info("StatisticsService", "batchGetAnchorRevenue",
                "批量查询主播收入，数量: " + anchorIds.size());

        return anchorIds.stream()
                .map(anchorId -> getAnchorRevenue(anchorId, startTime, endTime))
                .collect(Collectors.toList());
    }

    /**
     * 清除主播统计缓存（在结算后调用）
     */
    public void clearAnchorStatisticsCache(Long anchorId) {
        TraceLogger.info("StatisticsService", "clearAnchorStatisticsCache",
                "清除主播统计缓存，anchorId: " + anchorId);

        String pattern = STATISTICS_CACHE_KEY + "*:" + anchorId + ":*";
        redisTemplate.delete(redisTemplate.keys(pattern));
    }

    /**
     * 获取指定时间段内所有主播的收入排名
     */
    public List<AnchorRevenueVO> getTopAnchorsByRevenue(LocalDateTime startTime, 
                                                          LocalDateTime endTime,
                                                          int topN) {
        TraceLogger.info("StatisticsService", "getTopAnchorsByRevenue",
                "查询主播收入排名，topN: " + topN);

        // 尝试从缓存获取
        String cacheKey = STATISTICS_CACHE_KEY + "topAnchors:" + 
                startTime.toLocalDate() + ":" + endTime.toLocalDate() + ":" + topN;
        List<AnchorRevenueVO> cached = (List<AnchorRevenueVO>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 查询数据库 - 获取所有主播的统计
        List<Object[]> results = rechargeRecordRepository.getTopAnchorsByRevenue(
                startTime, endTime, topN);

        List<AnchorRevenueVO> topList = new ArrayList<>();
        int rank = 1;
        for (Object[] row : results) {
            AnchorRevenueVO vo = AnchorRevenueVO.builder()
                    .anchorId((Long) row[0])
                    .anchorName((String) row[1])
                    .totalAmount((BigDecimal) row[2])
                    .totalCount(((Number) row[3]).longValue())
                    .rank(rank++)
                    .startTime(startTime)
                    .endTime(endTime)
                    .queryTime(LocalDateTime.now())
                    .build();
            topList.add(vo);
        }

        // 缓存30分钟
        redisTemplate.opsForValue().set(cacheKey, topList, 30, TimeUnit.MINUTES);

        return topList;
    }
}
