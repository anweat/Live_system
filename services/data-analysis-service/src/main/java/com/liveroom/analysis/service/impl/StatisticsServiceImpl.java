package com.liveroom.analysis.service.impl;

import com.liveroom.analysis.service.StatisticsService;
import com.liveroom.analysis.vo.CashFlowTrendVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 统计分析Service实现
 */
@Slf4j
@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    @Qualifier("db2JdbcTemplate")
    private JdbcTemplate db2JdbcTemplate;

    @Autowired
    @Qualifier("db1JdbcTemplate")
    private JdbcTemplate db1JdbcTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_KEY_PREFIX = "analysis:statistics:";
    private static final long CACHE_TTL = 86400; // 24小时

    @Override
    public CashFlowTrendVO getGmvTrend(String startDate, String endDate, String granularity) {
        String cacheKey = CACHE_KEY_PREFIX + "gmv:" + startDate + ":" + endDate + ":" + granularity;
        CashFlowTrendVO cached = (CashFlowTrendVO) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.info("从缓存获取GMV趋势");
            return cached;
        }

        // 构建SQL，根据粒度聚合数据
        String timeFormat = getTimeFormat(granularity);
        String sql = "SELECT " +
                "    " + timeFormat + " as time_label, " +
                "    SUM(settlement_amount) as gmv, " +
                "    COUNT(*) as transaction_count, " +
                "    COUNT(DISTINCT audience_id) as paying_users " +
                "FROM recharge_record " +
                "WHERE recharge_time BETWEEN ? AND ? " +
                "GROUP BY time_label " +
                "ORDER BY time_label";

        List<CashFlowTrendVO.TrendData> timeSeries = db2JdbcTemplate.query(sql, (rs, rowNum) -> {
            CashFlowTrendVO.TrendData data = new CashFlowTrendVO.TrendData();
            data.setTimeLabel(rs.getString("time_label"));
            data.setGmv(rs.getBigDecimal("gmv"));
            data.setTransactionCount(rs.getInt("transaction_count"));
            data.setPayingUsers(rs.getInt("paying_users"));
            return data;
        }, startDate, endDate);

        // 计算移动平均
        calculateMovingAverage(timeSeries);

        // 计算EMA
        calculateEMA(timeSeries);

        // 计算增长率
        calculateGrowthRate(timeSeries);

        // 计算总GMV和平台收入
        BigDecimal totalGmv = timeSeries.stream()
                .map(CashFlowTrendVO.TrendData::getGmv)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 平台抽成20%
        BigDecimal platformIncome = totalGmv.multiply(BigDecimal.valueOf(0.2))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal anchorIncome = totalGmv.multiply(BigDecimal.valueOf(0.8))
                .setScale(2, RoundingMode.HALF_UP);

        // 趋势分析
        CashFlowTrendVO.TrendAnalysis trendAnalysis = analyzeTrend(timeSeries);

        CashFlowTrendVO trend = CashFlowTrendVO.builder()
                .timeSeries(timeSeries)
                .totalGmv(totalGmv)
                .platformIncome(platformIncome)
                .anchorIncome(anchorIncome)
                .trendAnalysis(trendAnalysis)
                .build();

        // 缓存结果
        redisTemplate.opsForValue().set(cacheKey, trend, CACHE_TTL, TimeUnit.SECONDS);

        return trend;
    }

    @Override
    public Map<String, Object> getKeyMetrics(String startDate, String endDate) {
        String cacheKey = CACHE_KEY_PREFIX + "metrics:" + startDate + ":" + endDate;
        @SuppressWarnings("unchecked")
        Map<String, Object> cached = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // GMV和交易数据
        String sql1 = "SELECT " +
                "    SUM(settlement_amount) as total_gmv, " +
                "    COUNT(*) as total_transactions, " +
                "    COUNT(DISTINCT audience_id) as paying_users, " +
                "    COUNT(DISTINCT anchor_id) as earning_anchors, " +
                "    AVG(settlement_amount) as avg_transaction " +
                "FROM recharge_record " +
                "WHERE recharge_time BETWEEN ? AND ?";

        Map<String, Object> gmvData = db2JdbcTemplate.queryForMap(sql1, startDate, endDate);

        // 总用户数
        String sql2 = "SELECT COUNT(*) as total_users FROM audience";
        Integer totalUsers = db1JdbcTemplate.queryForObject(sql2, Integer.class);

        // 活跃用户数（有过登录或消费）
        String sql3 = "SELECT COUNT(DISTINCT audience_id) as active_users " +
                "FROM recharge " +
                "WHERE recharge_time BETWEEN ? AND ?";
        Integer activeUsers = db1JdbcTemplate.queryForObject(sql3, Integer.class, startDate, endDate);

        // 计算关键指标
        BigDecimal totalGmv = (BigDecimal) gmvData.get("total_gmv");
        Long payingUsers = (Long) gmvData.get("paying_users");

        // ARPU = GMV / 总用户数
        BigDecimal arpu = totalUsers > 0 ?
                totalGmv.divide(BigDecimal.valueOf(totalUsers), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        // ARPPU = GMV / 付费用户数
        BigDecimal arppu = payingUsers > 0 ?
                totalGmv.divide(BigDecimal.valueOf(payingUsers), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        // 付费率 = 付费用户数 / 总用户数
        BigDecimal paymentRate = totalUsers > 0 ?
                BigDecimal.valueOf(payingUsers * 100.0 / totalUsers).setScale(2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        // 活跃率 = 活跃用户数 / 总用户数
        BigDecimal activeRate = totalUsers > 0 ?
                BigDecimal.valueOf(activeUsers * 100.0 / totalUsers).setScale(2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalGmv", totalGmv);
        metrics.put("totalTransactions", gmvData.get("total_transactions"));
        metrics.put("payingUsers", payingUsers);
        metrics.put("earningAnchors", gmvData.get("earning_anchors"));
        metrics.put("avgTransaction", gmvData.get("avg_transaction"));
        metrics.put("totalUsers", totalUsers);
        metrics.put("activeUsers", activeUsers);
        metrics.put("arpu", arpu);
        metrics.put("arppu", arppu);
        metrics.put("paymentRate", paymentRate);
        metrics.put("activeRate", activeRate);
        metrics.put("startDate", startDate);
        metrics.put("endDate", endDate);

        redisTemplate.opsForValue().set(cacheKey, metrics, CACHE_TTL, TimeUnit.SECONDS);
        return metrics;
    }

    @Override
    public Map<String, Object> getTimeHeatmap(String date) {
        String cacheKey = CACHE_KEY_PREFIX + "timeheatmap:" + date;
        @SuppressWarnings("unchecked")
        Map<String, Object> cached = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 查询指定日期范围内（7天）的每小时数据
        LocalDate endDate = date == null ? LocalDate.now() : LocalDate.parse(date);
        LocalDate startDate = endDate.minusDays(6);

        String sql = "SELECT " +
                "    DAYOFWEEK(recharge_time) as day_of_week, " +
                "    HOUR(recharge_time) as hour, " +
                "    SUM(settlement_amount) as gmv, " +
                "    COUNT(*) as transaction_count " +
                "FROM recharge_record " +
                "WHERE DATE(recharge_time) BETWEEN ? AND ? " +
                "GROUP BY day_of_week, hour " +
                "ORDER BY day_of_week, hour";

        List<Map<String, Object>> rawData = db2JdbcTemplate.queryForList(sql, 
                startDate.toString(), endDate.toString());

        // 构建7×24矩阵
        List<List<BigDecimal>> matrix = new ArrayList<>();
        for (int day = 0; day < 7; day++) {
            List<BigDecimal> hourlyData = new ArrayList<>();
            for (int hour = 0; hour < 24; hour++) {
                hourlyData.add(BigDecimal.ZERO);
            }
            matrix.add(hourlyData);
        }

        // 填充数据
        for (Map<String, Object> row : rawData) {
            int dayOfWeek = (Integer) row.get("day_of_week") - 1; // MySQL DAYOFWEEK从1开始
            int hour = (Integer) row.get("hour");
            BigDecimal gmv = (BigDecimal) row.get("gmv");
            matrix.get(dayOfWeek).set(hour, gmv);
        }

        // 找出峰值时段
        List<Map<String, Object>> peakHours = new ArrayList<>();
        for (Map<String, Object> row : rawData) {
            int dayOfWeek = (Integer) row.get("day_of_week");
            int hour = (Integer) row.get("hour");
            BigDecimal gmv = (BigDecimal) row.get("gmv");
            Integer transactionCount = ((Long) row.get("transaction_count")).intValue();

            Map<String, Object> peak = new HashMap<>();
            peak.put("dayOfWeek", getDayOfWeekName(dayOfWeek));
            peak.put("hour", hour);
            peak.put("gmv", gmv);
            peak.put("transactionCount", transactionCount);
            peakHours.add(peak);
        }

        // 按GMV排序，取TOP 10
        peakHours.sort((a, b) -> ((BigDecimal) b.get("gmv")).compareTo((BigDecimal) a.get("gmv")));
        List<Map<String, Object>> topPeakHours = peakHours.stream().limit(10).toList();

        Map<String, Object> result = new HashMap<>();
        result.put("matrix", matrix);
        result.put("peakHours", topPeakHours);
        result.put("startDate", startDate.toString());
        result.put("endDate", endDate.toString());

        redisTemplate.opsForValue().set(cacheKey, result, CACHE_TTL, TimeUnit.SECONDS);
        return result;
    }

    @Override
    public Map<String, Object> getCategoryPerformance(String startDate, String endDate) {
        String cacheKey = CACHE_KEY_PREFIX + "category:" + startDate + ":" + endDate;
        @SuppressWarnings("unchecked")
        Map<String, Object> cached = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 查询各分类的表现
        String sql = "SELECT " +
                "    lr.category, " +
                "    SUM(rr.settlement_amount) as total_gmv, " +
                "    COUNT(DISTINCT rr.audience_id) as paying_users, " +
                "    COUNT(*) as transaction_count, " +
                "    COUNT(DISTINCT rr.anchor_id) as earning_anchors, " +
                "    AVG(rr.settlement_amount) as avg_transaction " +
                "FROM recharge_record rr " +
                "INNER JOIN live_room lr ON rr.live_room_id = lr.live_room_id " +
                "WHERE rr.recharge_time BETWEEN ? AND ? AND lr.category IS NOT NULL " +
                "GROUP BY lr.category " +
                "ORDER BY total_gmv DESC";

        List<Map<String, Object>> categories = db2JdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> category = new HashMap<>();
            category.put("category", rs.getString("category"));
            category.put("totalGmv", rs.getBigDecimal("total_gmv"));
            category.put("payingUsers", rs.getInt("paying_users"));
            category.put("transactionCount", rs.getLong("transaction_count"));
            category.put("earningAnchors", rs.getInt("earning_anchors"));
            category.put("avgTransaction", rs.getBigDecimal("avg_transaction"));
            return category;
        }, startDate, endDate);

        // 计算占比
        BigDecimal totalGmv = categories.stream()
                .map(c -> (BigDecimal) c.get("totalGmv"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        for (Map<String, Object> category : categories) {
            BigDecimal gmv = (BigDecimal) category.get("totalGmv");
            BigDecimal percentage = totalGmv.compareTo(BigDecimal.ZERO) > 0 ?
                    gmv.multiply(BigDecimal.valueOf(100)).divide(totalGmv, 2, RoundingMode.HALF_UP) :
                    BigDecimal.ZERO;
            category.put("gmvPercentage", percentage);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("categories", categories);
        result.put("totalGmv", totalGmv);
        result.put("startDate", startDate);
        result.put("endDate", endDate);

        redisTemplate.opsForValue().set(cacheKey, result, CACHE_TTL, TimeUnit.SECONDS);
        return result;
    }

    /**
     * 获取时间格式化表达式
     */
    private String getTimeFormat(String granularity) {
        return switch (granularity) {
            case "hour" -> "DATE_FORMAT(recharge_time, '%Y-%m-%d %H:00:00')";
            case "day" -> "DATE_FORMAT(recharge_time, '%Y-%m-%d')";
            case "week" -> "DATE_FORMAT(DATE_SUB(recharge_time, INTERVAL WEEKDAY(recharge_time) DAY), '%Y-%m-%d')";
            case "month" -> "DATE_FORMAT(recharge_time, '%Y-%m')";
            default -> "DATE_FORMAT(recharge_time, '%Y-%m-%d')";
        };
    }

    /**
     * 计算移动平均
     */
    private void calculateMovingAverage(List<CashFlowTrendVO.TrendData> timeSeries) {
        int windowSize = 7;
        for (int i = 0; i < timeSeries.size(); i++) {
            if (i >= windowSize - 1) {
                BigDecimal sum = BigDecimal.ZERO;
                for (int j = i - windowSize + 1; j <= i; j++) {
                    sum = sum.add(timeSeries.get(j).getGmv());
                }
                BigDecimal ma = sum.divide(BigDecimal.valueOf(windowSize), 2, RoundingMode.HALF_UP);
                timeSeries.get(i).setMovingAverage(ma);
            }
        }
    }

    /**
     * 计算指数移动平均（EMA）
     */
    private void calculateEMA(List<CashFlowTrendVO.TrendData> timeSeries) {
        if (timeSeries.isEmpty()) return;

        int n = 7; // 周期
        double alpha = 2.0 / (n + 1); // 平滑系数

        // 第一个值使用原始值
        timeSeries.get(0).setEma(timeSeries.get(0).getGmv());

        // 后续值使用EMA公式
        for (int i = 1; i < timeSeries.size(); i++) {
            BigDecimal currentValue = timeSeries.get(i).getGmv();
            BigDecimal previousEma = timeSeries.get(i - 1).getEma();

            BigDecimal ema = currentValue.multiply(BigDecimal.valueOf(alpha))
                    .add(previousEma.multiply(BigDecimal.valueOf(1 - alpha)))
                    .setScale(2, RoundingMode.HALF_UP);

            timeSeries.get(i).setEma(ema);
        }
    }

    /**
     * 计算增长率
     */
    private void calculateGrowthRate(List<CashFlowTrendVO.TrendData> timeSeries) {
        for (int i = 1; i < timeSeries.size(); i++) {
            BigDecimal current = timeSeries.get(i).getGmv();
            BigDecimal previous = timeSeries.get(i - 1).getGmv();

            if (previous.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal growthRate = current.subtract(previous)
                        .divide(previous, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);
                timeSeries.get(i).setGrowthRate(growthRate);
            }
        }
    }

    /**
     * 趋势分析
     */
    private CashFlowTrendVO.TrendAnalysis analyzeTrend(List<CashFlowTrendVO.TrendData> timeSeries) {
        if (timeSeries.isEmpty()) {
            return CashFlowTrendVO.TrendAnalysis.builder()
                    .direction("stable")
                    .strength("weak")
                    .volatility(BigDecimal.ZERO)
                    .avgGrowthRate(BigDecimal.ZERO)
                    .prediction(BigDecimal.ZERO)
                    .build();
        }

        // 计算平均增长率
        BigDecimal totalGrowth = BigDecimal.ZERO;
        int growthCount = 0;
        for (CashFlowTrendVO.TrendData data : timeSeries) {
            if (data.getGrowthRate() != null) {
                totalGrowth = totalGrowth.add(data.getGrowthRate());
                growthCount++;
            }
        }
        BigDecimal avgGrowthRate = growthCount > 0 ?
                totalGrowth.divide(BigDecimal.valueOf(growthCount), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        // 判断趋势方向
        String direction;
        if (avgGrowthRate.compareTo(BigDecimal.valueOf(5)) > 0) {
            direction = "up";
        } else if (avgGrowthRate.compareTo(BigDecimal.valueOf(-5)) < 0) {
            direction = "down";
        } else {
            direction = "stable";
        }

        // 计算趋势强度
        String strength;
        BigDecimal absGrowth = avgGrowthRate.abs();
        if (absGrowth.compareTo(BigDecimal.valueOf(20)) >= 0) {
            strength = "strong";
        } else if (absGrowth.compareTo(BigDecimal.valueOf(10)) >= 0) {
            strength = "moderate";
        } else {
            strength = "weak";
        }

        // 计算波动率（标准差）
        BigDecimal mean = timeSeries.stream()
                .map(CashFlowTrendVO.TrendData::getGmv)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(timeSeries.size()), 2, RoundingMode.HALF_UP);

        BigDecimal variance = BigDecimal.ZERO;
        for (CashFlowTrendVO.TrendData data : timeSeries) {
            BigDecimal diff = data.getGmv().subtract(mean);
            variance = variance.add(diff.multiply(diff));
        }
        variance = variance.divide(BigDecimal.valueOf(timeSeries.size()), 2, RoundingMode.HALF_UP);
        BigDecimal volatility = BigDecimal.valueOf(Math.sqrt(variance.doubleValue()))
                .setScale(2, RoundingMode.HALF_UP);

        // 预测下一期（使用EMA）
        BigDecimal prediction = timeSeries.isEmpty() ? BigDecimal.ZERO :
                timeSeries.get(timeSeries.size() - 1).getEma();

        return CashFlowTrendVO.TrendAnalysis.builder()
                .direction(direction)
                .strength(strength)
                .volatility(volatility)
                .avgGrowthRate(avgGrowthRate)
                .prediction(prediction)
                .build();
    }

    /**
     * 获取星期几的名称
     */
    private String getDayOfWeekName(int dayOfWeek) {
        return switch (dayOfWeek) {
            case 1 -> "周日";
            case 2 -> "周一";
            case 3 -> "周二";
            case 4 -> "周三";
            case 5 -> "周四";
            case 6 -> "周五";
            case 7 -> "周六";
            default -> "未知";
        };
    }
}
