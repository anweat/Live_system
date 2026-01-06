package com.liveroom.analysis.service.impl;

import com.liveroom.analysis.service.AudienceAnalysisService;
import com.liveroom.analysis.vo.AudiencePortraitVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 观众分析Service实现
 */
@Slf4j
@Service
public class AudienceAnalysisServiceImpl implements AudienceAnalysisService {

    @Autowired
    @Qualifier("db1JdbcTemplate")
    private JdbcTemplate db1JdbcTemplate;

    @Autowired
    @Qualifier("db2JdbcTemplate")
    private JdbcTemplate db2JdbcTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_KEY_PREFIX = "analysis:audience:portrait:";
    private static final long CACHE_TTL = 43200; // 12小时

    @Override
    public AudiencePortraitVO getAudiencePortrait(Long audienceId) {
        // 尝试从缓存获取
        String cacheKey = CACHE_KEY_PREFIX + audienceId;
        AudiencePortraitVO cached = (AudiencePortraitVO) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.info("从缓存获取观众画像: audienceId={}", audienceId);
            return cached;
        }

        // 查询观众基本信息和消费数据
        String sql = "SELECT " +
                "    a.user_id, " +
                "    u.nickname, " +
                "    a.total_recharge_amount, " +
                "    a.total_recharge_count, " +
                "    a.consumption_level, " +
                "    a.last_recharge_time, " +
                "    a.vip_level, " +
                "    a.create_time " +
                "FROM audience a " +
                "INNER JOIN user u ON a.user_id = u.user_id " +
                "WHERE a.user_id = ?";

        Map<String, Object> audienceData = db1JdbcTemplate.queryForMap(sql, audienceId);

        String audienceName = (String) audienceData.get("nickname");
        BigDecimal totalAmount = (BigDecimal) audienceData.get("total_recharge_amount");
        Integer rechargeCount = ((Long) audienceData.get("total_recharge_count")).intValue();
        Integer consumptionLevel = (Integer) audienceData.get("consumption_level");
        LocalDateTime lastRechargeTime = (LocalDateTime) audienceData.get("last_recharge_time");
        LocalDateTime createTime = (LocalDateTime) audienceData.get("create_time");

        // 计算消费分位数
        Integer percentile = calculatePercentile(audienceId, totalAmount);

        // 计算RFM评分
        AudiencePortraitVO.RFMScore rfmScore = calculateRFMScore(
                lastRechargeTime, rechargeCount, totalAmount);

        // 计算活跃度
        Integer activityLevel = calculateActivityLevel(audienceId, rechargeCount, lastRechargeTime);

        // 预测LTV
        BigDecimal predictedLtv = predictLTV(totalAmount, rechargeCount, createTime);

        // 计算留存天数
        Integer retentionDays = (int) ChronoUnit.DAYS.between(createTime, LocalDateTime.now());

        // 查询最喜欢的主播
        List<AudiencePortraitVO.FavoriteAnchor> favoriteAnchors = getFavoriteAnchors(audienceId);

        // 查询偏好分类
        List<String> favoriteCategories = getFavoriteCategories(audienceId);

        // 生成标签
        List<String> tags = generateTags(rfmScore, consumptionLevel, activityLevel);

        AudiencePortraitVO portrait = AudiencePortraitVO.builder()
                .audienceId(audienceId)
                .audienceName(audienceName)
                .totalAmount(totalAmount)
                .rechargeCount(rechargeCount)
                .percentile(percentile)
                .consumptionLevel(consumptionLevel)
                .consumptionLevelDesc(getConsumptionLevelDesc(consumptionLevel))
                .rfmScore(rfmScore)
                .activityLevel(activityLevel)
                .activityLevelDesc(getActivityLevelDesc(activityLevel))
                .predictedLtv(predictedLtv)
                .retentionDays(retentionDays)
                .lastRechargeTime(lastRechargeTime != null ? lastRechargeTime.toString() : null)
                .firstRechargeTime(createTime.toString())
                .favoriteAnchors(favoriteAnchors)
                .favoriteCategories(favoriteCategories)
                .tags(tags)
                .build();

        // 缓存结果
        redisTemplate.opsForValue().set(cacheKey, portrait, CACHE_TTL, TimeUnit.SECONDS);

        return portrait;
    }

    @Override
    public Map<String, Object> getConsumptionDistribution() {
        String sql = "SELECT " +
                "    consumption_level, " +
                "    COUNT(*) as user_count, " +
                "    SUM(total_recharge_amount) as total_amount, " +
                "    AVG(total_recharge_amount) as avg_amount " +
                "FROM audience " +
                "GROUP BY consumption_level " +
                "ORDER BY consumption_level DESC";

        List<Map<String, Object>> distribution = db1JdbcTemplate.queryForList(sql);

        Integer totalUsers = distribution.stream()
                .mapToInt(m -> ((Long) m.get("user_count")).intValue())
                .sum();

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : distribution) {
            Integer level = (Integer) row.get("consumption_level");
            Integer userCount = ((Long) row.get("user_count")).intValue();
            BigDecimal totalAmount = (BigDecimal) row.get("total_amount");
            BigDecimal avgAmount = (BigDecimal) row.get("avg_amount");

            Map<String, Object> item = new HashMap<>();
            item.put("level", level);
            item.put("levelDesc", getConsumptionLevelDesc(level));
            item.put("userCount", userCount);
            item.put("percentage", BigDecimal.valueOf(userCount * 100.0 / totalUsers).setScale(2, RoundingMode.HALF_UP));
            item.put("totalAmount", totalAmount);
            item.put("avgAmount", avgAmount);

            result.add(item);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("distribution", result);
        response.put("totalUsers", totalUsers);

        return response;
    }

    @Override
    public Map<String, Object> getRetentionAnalysis(Integer days) {
        // 查询N日前注册的用户
        String sql1 = "SELECT user_id, create_time " +
                "FROM audience " +
                "WHERE DATE(create_time) = DATE_SUB(CURDATE(), INTERVAL ? DAY)";

        List<Map<String, Object>> newUsers = db1JdbcTemplate.queryForList(sql1, days);
        Integer totalNewUsers = newUsers.size();

        if (totalNewUsers == 0) {
            Map<String, Object> result = new HashMap<>();
            result.put("days", days);
            result.put("retentionRate", 0);
            result.put("totalNewUsers", 0);
            result.put("retainedUsers", 0);
            return result;
        }

        // 查询这些用户在之后是否有消费行为
        List<Long> userIds = new ArrayList<>();
        for (Map<String, Object> user : newUsers) {
            userIds.add(((Long) user.get("user_id")));
        }

        String sql2 = "SELECT COUNT(DISTINCT audience_id) as retained_count " +
                "FROM recharge " +
                "WHERE audience_id IN (" + String.join(",", Collections.nCopies(userIds.size(), "?")) + ") " +
                "  AND recharge_time > DATE_ADD(DATE_SUB(CURDATE(), INTERVAL ? DAY), INTERVAL 1 DAY)";

        Object[] params = new Object[userIds.size() + 1];
        for (int i = 0; i < userIds.size(); i++) {
            params[i] = userIds.get(i);
        }
        params[userIds.size()] = days;

        Integer retainedUsers = db1JdbcTemplate.queryForObject(sql2, Integer.class, params);

        BigDecimal retentionRate = BigDecimal.valueOf(retainedUsers * 100.0 / totalNewUsers)
                .setScale(2, RoundingMode.HALF_UP);

        Map<String, Object> result = new HashMap<>();
        result.put("days", days);
        result.put("retentionRate", retentionRate);
        result.put("totalNewUsers", totalNewUsers);
        result.put("retainedUsers", retainedUsers);

        return result;
    }

    @Override
    public Map<String, Object> getChurnWarning(String riskLevel, Integer limit) {
        // 基于逻辑回归模型预测流失概率
        // 流失特征：最近消费天数长、消费频次低、消费金额低、活跃度低
        
        String sql = "SELECT " +
                "    a.user_id, " +
                "    u.nickname, " +
                "    a.total_recharge_amount, " +
                "    a.total_recharge_count, " +
                "    DATEDIFF(NOW(), a.last_recharge_time) as days_since_last, " +
                "    a.consumption_level " +
                "FROM audience a " +
                "INNER JOIN user u ON a.user_id = u.user_id " +
                "WHERE a.last_recharge_time IS NOT NULL " +
                "ORDER BY days_since_last DESC, total_recharge_count ASC " +
                "LIMIT ?";

        List<Map<String, Object>> users = db1JdbcTemplate.queryForList(sql, limit * 2);

        List<Map<String, Object>> warnings = new ArrayList<>();
        for (Map<String, Object> user : users) {
            Long userId = (Long) user.get("user_id");
            String nickname = (String) user.get("nickname");
            BigDecimal totalAmount = (BigDecimal) user.get("total_recharge_amount");
            Long totalCount = (Long) user.get("total_recharge_count");
            Integer daysSinceLast = (Integer) user.get("days_since_last");
            Integer consumptionLevel = (Integer) user.get("consumption_level");

            // 计算流失概率（简化的逻辑回归模型）
            BigDecimal churnProb = calculateChurnProbability(
                    daysSinceLast, totalCount.intValue(), totalAmount, consumptionLevel);

            String risk = getRiskLevel(churnProb);
            
            if (matchesRiskLevel(risk, riskLevel)) {
                Map<String, Object> warning = new HashMap<>();
                warning.put("audienceId", userId);
                warning.put("audienceName", nickname);
                warning.put("churnProbability", churnProb);
                warning.put("riskLevel", risk);
                warning.put("daysSinceLastRecharge", daysSinceLast);
                warning.put("totalRechargeCount", totalCount);
                warning.put("totalAmount", totalAmount);
                
                warnings.add(warning);
                
                if (warnings.size() >= limit) break;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("riskLevel", riskLevel);
        result.put("warnings", warnings);
        result.put("totalCount", warnings.size());

        return result;
    }

    /**
     * 计算消费分位数
     */
    private Integer calculatePercentile(Long audienceId, BigDecimal totalAmount) {
        String sql = "SELECT COUNT(*) as count " +
                "FROM audience " +
                "WHERE total_recharge_amount < ?";
        
        Integer lowerCount = db1JdbcTemplate.queryForObject(sql, Integer.class, totalAmount);
        
        String sql2 = "SELECT COUNT(*) as total FROM audience";
        Integer total = db1JdbcTemplate.queryForObject(sql2, Integer.class);
        
        if (total == 0) return 0;
        
        return (int) (lowerCount * 100.0 / total);
    }

    /**
     * 计算RFM评分
     */
    private AudiencePortraitVO.RFMScore calculateRFMScore(
            LocalDateTime lastRechargeTime, Integer frequency, BigDecimal monetary) {
        
        // R得分（最近消费）
        Integer rScore = 1;
        if (lastRechargeTime != null) {
            long daysSince = ChronoUnit.DAYS.between(lastRechargeTime, LocalDateTime.now());
            if (daysSince <= 7) rScore = 5;
            else if (daysSince <= 14) rScore = 4;
            else if (daysSince <= 30) rScore = 3;
            else if (daysSince <= 60) rScore = 2;
        }

        // F得分（消费频次）
        Integer fScore = 1;
        if (frequency >= 20) fScore = 5;
        else if (frequency >= 10) fScore = 4;
        else if (frequency >= 5) fScore = 3;
        else if (frequency >= 2) fScore = 2;

        // M得分（消费金额）- 基于分位数
        Integer mScore = 1;
        if (monetary.compareTo(BigDecimal.valueOf(10000)) >= 0) mScore = 5;
        else if (monetary.compareTo(BigDecimal.valueOf(5000)) >= 0) mScore = 4;
        else if (monetary.compareTo(BigDecimal.valueOf(1000)) >= 0) mScore = 3;
        else if (monetary.compareTo(BigDecimal.valueOf(100)) >= 0) mScore = 2;

        // 综合得分：R×0.3 + F×0.3 + M×0.4
        BigDecimal totalScore = BigDecimal.valueOf(rScore * 0.3 + fScore * 0.3 + mScore * 0.4)
                .setScale(2, RoundingMode.HALF_UP);

        String valueLevel = totalScore.compareTo(BigDecimal.valueOf(4.0)) >= 0 ? "高价值用户" :
                totalScore.compareTo(BigDecimal.valueOf(3.0)) >= 0 ? "中价值用户" : "低价值用户";

        return AudiencePortraitVO.RFMScore.builder()
                .recencyScore(rScore)
                .frequencyScore(fScore)
                .monetaryScore(mScore)
                .totalScore(totalScore)
                .valueLevel(valueLevel)
                .build();
    }

    /**
     * 计算活跃度等级
     */
    private Integer calculateActivityLevel(Long audienceId, Integer rechargeCount, LocalDateTime lastRechargeTime) {
        // 简化计算：基于打赏频次和最近活跃时间
        if (lastRechargeTime == null) return 0;

        long daysSince = ChronoUnit.DAYS.between(lastRechargeTime, LocalDateTime.now());
        
        if (rechargeCount >= 10 && daysSince <= 7) return 2; // 高活跃
        else if (rechargeCount >= 5 && daysSince <= 30) return 1; // 中活跃
        else return 0; // 低活跃
    }

    /**
     * 预测LTV（生命周期价值）
     */
    private BigDecimal predictLTV(BigDecimal totalAmount, Integer rechargeCount, LocalDateTime createTime) {
        // 简化公式：LTV = ARPPU × 平均消费频次 × 预期留存月数
        
        if (rechargeCount == 0) return BigDecimal.ZERO;

        long daysSinceJoin = ChronoUnit.DAYS.between(createTime, LocalDateTime.now());
        if (daysSinceJoin == 0) daysSinceJoin = 1;

        // ARPPU
        BigDecimal arppu = totalAmount.divide(BigDecimal.valueOf(rechargeCount), 2, RoundingMode.HALF_UP);

        // 平均消费频次（次/月）
        BigDecimal avgFrequency = BigDecimal.valueOf(rechargeCount * 30.0 / daysSinceJoin)
                .setScale(2, RoundingMode.HALF_UP);

        // 预期留存月数（假设平均留存6个月）
        Integer expectedMonths = 6;

        // 预测LTV
        return arppu.multiply(avgFrequency).multiply(BigDecimal.valueOf(expectedMonths))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 获取最喜欢的主播
     */
    private List<AudiencePortraitVO.FavoriteAnchor> getFavoriteAnchors(Long audienceId) {
        String sql = "SELECT " +
                "    anchor_id, " +
                "    MAX(anchor_name) as anchor_name, " +
                "    SUM(recharge_amount) as total_amount, " +
                "    COUNT(*) as recharge_count " +
                "FROM recharge " +
                "WHERE audience_id = ? " +
                "GROUP BY anchor_id " +
                "ORDER BY total_amount DESC " +
                "LIMIT 3";

        return db1JdbcTemplate.query(sql, (rs, rowNum) ->
                AudiencePortraitVO.FavoriteAnchor.builder()
                        .anchorId(rs.getLong("anchor_id"))
                        .anchorName(rs.getString("anchor_name"))
                        .totalAmount(rs.getBigDecimal("total_amount"))
                        .rechargeCount(rs.getInt("recharge_count"))
                        .build(),
                audienceId);
    }

    /**
     * 获取偏好分类
     */
    private List<String> getFavoriteCategories(Long audienceId) {
        String sql = "SELECT " +
                "    lr.category, " +
                "    COUNT(*) as count " +
                "FROM recharge r " +
                "INNER JOIN live_room lr ON r.live_room_id = lr.live_room_id " +
                "WHERE r.audience_id = ? AND lr.category IS NOT NULL " +
                "GROUP BY lr.category " +
                "ORDER BY count DESC " +
                "LIMIT 3";

        return db1JdbcTemplate.queryForList(sql, String.class, audienceId);
    }

    /**
     * 生成标签
     */
    private List<String> generateTags(AudiencePortraitVO.RFMScore rfmScore, 
                                     Integer consumptionLevel, Integer activityLevel) {
        List<String> tags = new ArrayList<>();
        
        // RFM标签
        tags.add(rfmScore.getValueLevel());
        
        // 消费等级标签
        tags.add(getConsumptionLevelDesc(consumptionLevel));
        
        // 活跃度标签
        tags.add(getActivityLevelDesc(activityLevel));
        
        // RFM特征标签
        if (rfmScore.getRecencyScore() >= 4) tags.add("近期活跃");
        if (rfmScore.getFrequencyScore() >= 4) tags.add("高频消费");
        if (rfmScore.getMonetaryScore() >= 4) tags.add("高额消费");
        
        return tags;
    }

    /**
     * 计算流失概率（逻辑回归简化模型）
     */
    private BigDecimal calculateChurnProbability(Integer daysSinceLast, Integer frequency, 
                                                 BigDecimal monetary, Integer consumptionLevel) {
        // z = β₀ + β₁×天数 + β₂×频次 + β₃×金额 + β₄×消费等级
        // P = 1 / (1 + e^(-z))
        
        // 简化的系数（实际应通过历史数据训练）
        double z = 2.0 + 
                   0.05 * daysSinceLast -     // 天数越多，流失概率越高
                   0.1 * frequency -           // 频次越高，流失概率越低
                   0.0001 * monetary.doubleValue() - // 金额越高，流失概率越低
                   0.5 * consumptionLevel;     // 等级越高，流失概率越低

        double probability = 1.0 / (1.0 + Math.exp(-z));
        
        return BigDecimal.valueOf(probability * 100).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 获取风险等级
     */
    private String getRiskLevel(BigDecimal churnProb) {
        if (churnProb.compareTo(BigDecimal.valueOf(70)) > 0) return "high";
        else if (churnProb.compareTo(BigDecimal.valueOf(40)) > 0) return "medium";
        else return "low";
    }

    /**
     * 匹配风险等级
     */
    private boolean matchesRiskLevel(String risk, String targetLevel) {
        return risk.equals(targetLevel);
    }

    private String getConsumptionLevelDesc(Integer level) {
        return switch (level) {
            case 2 -> "高消费";
            case 1 -> "中消费";
            default -> "低消费";
        };
    }

    private String getActivityLevelDesc(Integer level) {
        return switch (level) {
            case 2 -> "高活跃";
            case 1 -> "中活跃";
            default -> "低活跃";
        };
    }
}
