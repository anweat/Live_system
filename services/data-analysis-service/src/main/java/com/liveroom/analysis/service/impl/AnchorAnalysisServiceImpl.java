package com.liveroom.analysis.service.impl;

import com.liveroom.analysis.service.AnchorAnalysisService;
import com.liveroom.analysis.vo.AnchorIncomeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 主播分析Service实现
 */
@Slf4j
@Service
public class AnchorAnalysisServiceImpl implements AnchorAnalysisService {

    @Autowired
    @Qualifier("db2JdbcTemplate")
    private JdbcTemplate db2JdbcTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_KEY_PREFIX = "analysis:anchor:income:";
    private static final long CACHE_TTL = 3600; // 1小时

    @Override
    public AnchorIncomeVO analyzeAnchorIncome(Long anchorId, String period, Integer days) {
        // 尝试从缓存获取
        String cacheKey = CACHE_KEY_PREFIX + anchorId + ":" + period + ":" + days;
        AnchorIncomeVO cached = (AnchorIncomeVO) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.info("从缓存获取主播收入数据: anchorId={}", anchorId);
            return cached;
        }

        // 计算时间范围
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);

        // 查询收入数据
        String sql = "SELECT " +
                "    DATE(recharge_time) as date, " +
                "    SUM(settlement_amount) as income, " +
                "    COUNT(*) as recharge_count, " +
                "    COUNT(DISTINCT audience_id) as unique_payers " +
                "FROM recharge_record " +
                "WHERE anchor_id = ? " +
                "  AND recharge_time >= ? " +
                "  AND recharge_time < ? " +
                "  AND settlement_status != 3 " +  // 排除退款
                "GROUP BY DATE(recharge_time) " +
                "ORDER BY DATE(recharge_time)";

        List<AnchorIncomeVO.TimeSeriesData> timeSeries = db2JdbcTemplate.query(sql,
                (rs, rowNum) -> AnchorIncomeVO.TimeSeriesData.builder()
                        .timeLabel(rs.getString("date"))
                        .income(rs.getBigDecimal("income"))
                        .rechargeCount(rs.getInt("recharge_count"))
                        .payerCount(rs.getInt("unique_payers"))
                        .build(),
                anchorId, startDate, endDate);

        // 计算移动平均
        calculateMovingAverage(timeSeries);

        // 计算统计指标
        BigDecimal totalIncome = timeSeries.stream()
                .map(AnchorIncomeVO.TimeSeriesData::getIncome)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Integer totalCount = timeSeries.stream()
                .mapToInt(AnchorIncomeVO.TimeSeriesData::getRechargeCount)
                .sum();

        Set<Integer> allPayers = new HashSet<>();
        timeSeries.forEach(t -> allPayers.add(t.getPayerCount()));

        BigDecimal avgAmount = totalCount > 0 ?
                totalIncome.divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        BigDecimal maxAmount = timeSeries.stream()
                .map(AnchorIncomeVO.TimeSeriesData::getIncome)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        // 计算收入稳定性（变异系数CV）
        BigDecimal stabilityScore = calculateStability(timeSeries);

        // 计算趋势
        String trend = analyzeTrend(timeSeries);

        // 计算环比增长率
        BigDecimal growthRate = calculateGrowthRate(timeSeries);

        // 查询主播名称
        String anchorName = db2JdbcTemplate.queryForObject(
                "SELECT anchor_name FROM recharge_record WHERE anchor_id = ? LIMIT 1",
                String.class, anchorId);

        AnchorIncomeVO result = AnchorIncomeVO.builder()
                .anchorId(anchorId)
                .anchorName(anchorName)
                .timeSeries(timeSeries)
                .totalIncome(totalIncome)
                .totalRechargeCount(totalCount)
                .uniquePayerCount(allPayers.size())
                .avgAmount(avgAmount)
                .maxAmount(maxAmount)
                .stabilityScore(stabilityScore)
                .trend(trend)
                .growthRate(growthRate)
                .build();

        // 缓存结果
        redisTemplate.opsForValue().set(cacheKey, result, CACHE_TTL, TimeUnit.SECONDS);

        return result;
    }

    @Override
    public Map<String, Object> getAnchorRadarData(Long anchorId) {
        Map<String, Object> radarData = new HashMap<>();

        // 查询主播基本数据
        String sql = "SELECT " +
                "    SUM(settlement_amount) as total_income, " +
                "    COUNT(DISTINCT audience_id) as unique_payers, " +
                "    AVG(settlement_amount) as avg_amount " +
                "FROM recharge_record " +
                "WHERE anchor_id = ? " +
                "  AND recharge_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)";

        Map<String, Object> data = db2JdbcTemplate.queryForMap(sql, anchorId);

        BigDecimal totalIncome = (BigDecimal) data.get("total_income");
        Integer uniquePayers = ((Long) data.get("unique_payers")).intValue();

        // 1. 收入能力得分 (满分10分，每1万元1分)
        BigDecimal incomeScore = totalIncome.divide(BigDecimal.valueOf(10000), 2, RoundingMode.HALF_UP);
        incomeScore = incomeScore.min(BigDecimal.valueOf(10));

        // 2. 粉丝质量得分（付费转化率 * 10）
        BigDecimal fanQualityScore = BigDecimal.valueOf(uniquePayers).multiply(BigDecimal.valueOf(0.1));
        fanQualityScore = fanQualityScore.min(BigDecimal.valueOf(10));

        // 3. 互动能力得分（简化计算，基于打赏次数）
        BigDecimal engagementScore = BigDecimal.valueOf(uniquePayers).multiply(BigDecimal.valueOf(0.05));
        engagementScore = engagementScore.min(BigDecimal.valueOf(10));

        // 4. 收入稳定性得分
        BigDecimal stabilityScore = BigDecimal.valueOf(7.5); // 简化，实际需计算CV

        // 5. 增长潜力得分
        BigDecimal growthScore = BigDecimal.valueOf(8.0); // 简化，实际需计算增长率

        radarData.put("anchorId", anchorId);
        radarData.put("dimensions", Map.of(
                "income", incomeScore,
                "fanQuality", fanQualityScore,
                "engagement", engagementScore,
                "stability", stabilityScore,
                "growth", growthScore
        ));

        BigDecimal overallScore = incomeScore.add(fanQualityScore).add(engagementScore)
                .add(stabilityScore).add(growthScore)
                .divide(BigDecimal.valueOf(5), 2, RoundingMode.HALF_UP);
        radarData.put("overallScore", overallScore);

        return radarData;
    }

    @Override
    public Map<String, Object> compareAnchors(String anchorIds, String period, Integer days) {
        List<Long> ids = Arrays.stream(anchorIds.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());

        Map<String, Object> compareData = new HashMap<>();
        List<Map<String, Object>> anchorsData = new ArrayList<>();

        for (Long anchorId : ids) {
            AnchorIncomeVO incomeVO = analyzeAnchorIncome(anchorId, period, days);
            
            Map<String, Object> anchorData = new HashMap<>();
            anchorData.put("anchorId", incomeVO.getAnchorId());
            anchorData.put("anchorName", incomeVO.getAnchorName());
            anchorData.put("totalIncome", incomeVO.getTotalIncome());
            anchorData.put("timeSeries", incomeVO.getTimeSeries());
            
            anchorsData.add(anchorData);
        }

        compareData.put("anchors", anchorsData);
        return compareData;
    }

    /**
     * 计算移动平均
     */
    private void calculateMovingAverage(List<AnchorIncomeVO.TimeSeriesData> timeSeries) {
        for (int i = 0; i < timeSeries.size(); i++) {
            // 7日移动平均
            if (i >= 6) {
                BigDecimal sum7 = BigDecimal.ZERO;
                for (int j = i - 6; j <= i; j++) {
                    sum7 = sum7.add(timeSeries.get(j).getIncome());
                }
                timeSeries.get(i).setMa7(sum7.divide(BigDecimal.valueOf(7), 2, RoundingMode.HALF_UP));
            }

            // 30日移动平均
            if (i >= 29) {
                BigDecimal sum30 = BigDecimal.ZERO;
                for (int j = i - 29; j <= i; j++) {
                    sum30 = sum30.add(timeSeries.get(j).getIncome());
                }
                timeSeries.get(i).setMa30(sum30.divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP));
            }
        }
    }

    /**
     * 计算收入稳定性（变异系数CV）
     */
    private BigDecimal calculateStability(List<AnchorIncomeVO.TimeSeriesData> timeSeries) {
        if (timeSeries.isEmpty()) return BigDecimal.ZERO;

        List<BigDecimal> incomes = timeSeries.stream()
                .map(AnchorIncomeVO.TimeSeriesData::getIncome)
                .collect(Collectors.toList());

        // 计算平均值
        BigDecimal mean = incomes.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(incomes.size()), 2, RoundingMode.HALF_UP);

        // 计算标准差
        double variance = incomes.stream()
                .mapToDouble(income -> Math.pow(income.subtract(mean).doubleValue(), 2))
                .average()
                .orElse(0.0);
        double stdDev = Math.sqrt(variance);

        // 变异系数 CV = 标准差 / 平均值
        if (mean.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        
        BigDecimal cv = BigDecimal.valueOf(stdDev).divide(mean, 2, RoundingMode.HALF_UP);
        
        // 稳定性得分 = (1 - CV) * 10，取值0-10
        BigDecimal stability = BigDecimal.ONE.subtract(cv).multiply(BigDecimal.valueOf(10));
        return stability.max(BigDecimal.ZERO).min(BigDecimal.TEN);
    }

    /**
     * 分析趋势
     */
    private String analyzeTrend(List<AnchorIncomeVO.TimeSeriesData> timeSeries) {
        if (timeSeries.size() < 2) return "stable";

        // 比较最近7天平均值和之前7天平均值
        int size = timeSeries.size();
        int splitPoint = size / 2;

        BigDecimal recentAvg = timeSeries.subList(splitPoint, size).stream()
                .map(AnchorIncomeVO.TimeSeriesData::getIncome)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(size - splitPoint), 2, RoundingMode.HALF_UP);

        BigDecimal previousAvg = timeSeries.subList(0, splitPoint).stream()
                .map(AnchorIncomeVO.TimeSeriesData::getIncome)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(splitPoint), 2, RoundingMode.HALF_UP);

        BigDecimal change = recentAvg.subtract(previousAvg);
        BigDecimal changeRate = previousAvg.compareTo(BigDecimal.ZERO) > 0 ?
                change.divide(previousAvg, 4, RoundingMode.HALF_UP).abs() : BigDecimal.ZERO;

        if (changeRate.compareTo(BigDecimal.valueOf(0.05)) < 0) {
            return "stable";
        } else if (change.compareTo(BigDecimal.ZERO) > 0) {
            return "up";
        } else {
            return "down";
        }
    }

    /**
     * 计算环比增长率
     */
    private BigDecimal calculateGrowthRate(List<AnchorIncomeVO.TimeSeriesData> timeSeries) {
        if (timeSeries.size() < 2) return BigDecimal.ZERO;

        BigDecimal lastIncome = timeSeries.get(timeSeries.size() - 1).getIncome();
        BigDecimal previousIncome = timeSeries.get(timeSeries.size() - 2).getIncome();

        if (previousIncome.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;

        return lastIncome.subtract(previousIncome)
                .divide(previousIncome, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
}
