package com.liveroom.analysis.service.impl;

import com.liveroom.analysis.service.RankingService;
import com.liveroom.analysis.vo.RankingVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 排行榜Service实现
 */
@Slf4j
@Service
public class RankingServiceImpl implements RankingService {

    @Autowired
    @Qualifier("db2JdbcTemplate")
    private JdbcTemplate db2JdbcTemplate;

    @Autowired
    @Qualifier("db1JdbcTemplate")
    private JdbcTemplate db1JdbcTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_KEY_PREFIX = "analysis:ranking:";
    private static final long CACHE_TTL = 1800; // 30分钟

    @Override
    public RankingVO getAnchorIncomeRanking(String period, Integer limit) {
        String cacheKey = CACHE_KEY_PREFIX + "anchor:income:" + period;
        RankingVO cached = (RankingVO) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 计算时间范围
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = switch (period) {
            case "day" -> endDate.minusDays(1);
            case "week" -> endDate.minusDays(7);
            case "month" -> endDate.minusMonths(1);
            default -> endDate.minusMonths(1);
        };

        String sql = "SELECT " +
                "    anchor_id, " +
                "    anchor_name, " +
                "    SUM(settlement_amount) as total_income, " +
                "    COUNT(*) as recharge_count " +
                "FROM recharge_record " +
                "WHERE recharge_time >= ? " +
                "  AND recharge_time < ? " +
                "  AND settlement_status != 3 " +
                "GROUP BY anchor_id, anchor_name " +
                "ORDER BY total_income DESC " +
                "LIMIT ?";

        List<RankingVO.RankingItem> rankings = new ArrayList<>();
        db2JdbcTemplate.query(sql, rs -> {
            int rank = rankings.size() + 1;
            rankings.add(RankingVO.RankingItem.builder()
                    .rank(rank)
                    .userId(rs.getLong("anchor_id"))
                    .userName(rs.getString("anchor_name"))
                    .value(rs.getBigDecimal("total_income"))
                    .secondaryValue(rs.getInt("recharge_count"))
                    .build());
        }, startDate, endDate, limit);

        RankingVO result = RankingVO.builder()
                .rankingType("anchor_income")
                .period(period)
                .rankings(rankings)
                .build();

        redisTemplate.opsForValue().set(cacheKey, result, CACHE_TTL, TimeUnit.SECONDS);
        return result;
    }

    @Override
    public RankingVO getAudienceConsumptionRanking(String period, Integer limit) {
        String cacheKey = CACHE_KEY_PREFIX + "audience:consumption:" + period;
        RankingVO cached = (RankingVO) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = period.equals("all") ? LocalDate.of(2020, 1, 1) :
                switch (period) {
                    case "day" -> endDate.minusDays(1);
                    case "week" -> endDate.minusDays(7);
                    case "month" -> endDate.minusMonths(1);
                    default -> endDate.minusMonths(1);
                };

        String sql = "SELECT " +
                "    audience_id, " +
                "    audience_name, " +
                "    SUM(recharge_amount) as total_amount, " +
                "    COUNT(*) as recharge_count " +
                "FROM recharge_record " +
                "WHERE recharge_time >= ? " +
                "  AND recharge_time < ? " +
                "GROUP BY audience_id, audience_name " +
                "ORDER BY total_amount DESC " +
                "LIMIT ?";

        List<RankingVO.RankingItem> rankings = new ArrayList<>();
        db2JdbcTemplate.query(sql, rs -> {
            int rank = rankings.size() + 1;
            rankings.add(RankingVO.RankingItem.builder()
                    .rank(rank)
                    .userId(rs.getLong("audience_id"))
                    .userName(rs.getString("audience_name"))
                    .value(rs.getBigDecimal("total_amount"))
                    .secondaryValue(rs.getInt("recharge_count"))
                    .build());
        }, startDate, endDate, limit);

        RankingVO result = RankingVO.builder()
                .rankingType("audience_consumption")
                .period(period)
                .rankings(rankings)
                .build();

        redisTemplate.opsForValue().set(cacheKey, result, CACHE_TTL, TimeUnit.SECONDS);
        return result;
    }

    @Override
    public RankingVO getAnchorFansRanking(Integer limit) {
        String cacheKey = CACHE_KEY_PREFIX + "anchor:fans";
        RankingVO cached = (RankingVO) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        String sql = "SELECT " +
                "    a.user_id, " +
                "    u.nickname as user_name, " +
                "    a.fan_count, " +
                "    a.like_count " +
                "FROM anchor a " +
                "INNER JOIN user u ON a.user_id = u.user_id " +
                "ORDER BY a.fan_count DESC " +
                "LIMIT ?";

        List<RankingVO.RankingItem> rankings = new ArrayList<>();
        db1JdbcTemplate.query(sql, rs -> {
            int rank = rankings.size() + 1;
            rankings.add(RankingVO.RankingItem.builder()
                    .rank(rank)
                    .userId(rs.getLong("user_id"))
                    .userName(rs.getString("user_name"))
                    .value(BigDecimal.valueOf(rs.getLong("fan_count")))
                    .secondaryValue(rs.getInt("like_count"))
                    .build());
        }, limit);

        RankingVO result = RankingVO.builder()
                .rankingType("anchor_fans")
                .period("all")
                .rankings(rankings)
                .build();

        redisTemplate.opsForValue().set(cacheKey, result, CACHE_TTL, TimeUnit.SECONDS);
        return result;
    }

    @Override
    public RankingVO getLiveRoomPopularityRanking(Integer limit) {
        String cacheKey = CACHE_KEY_PREFIX + "liveroom:popularity";
        RankingVO cached = (RankingVO) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        String sql = "SELECT " +
                "    lr.live_room_id, " +
                "    lr.room_name, " +
                "    lrr.current_viewers, " +
                "    lrr.total_viewers " +
                "FROM live_room lr " +
                "INNER JOIN live_room_realtime lrr ON lr.live_room_id = lrr.live_room_id " +
                "WHERE lr.room_status = 1 " +  // 正在直播
                "ORDER BY lrr.current_viewers DESC " +
                "LIMIT ?";

        List<RankingVO.RankingItem> rankings = new ArrayList<>();
        db1JdbcTemplate.query(sql, rs -> {
            int rank = rankings.size() + 1;
            rankings.add(RankingVO.RankingItem.builder()
                    .rank(rank)
                    .userId(rs.getLong("live_room_id"))
                    .userName(rs.getString("room_name"))
                    .value(BigDecimal.valueOf(rs.getInt("current_viewers")))
                    .secondaryValue(rs.getInt("total_viewers"))
                    .build());
        }, limit);

        RankingVO result = RankingVO.builder()
                .rankingType("liveroom_popularity")
                .period("realtime")
                .rankings(rankings)
                .build();

        redisTemplate.opsForValue().set(cacheKey, result, 60, TimeUnit.SECONDS); // 1分钟缓存
        return result;
    }
}
