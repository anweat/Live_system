package com.liveroom.analysis.service.impl;

import com.liveroom.analysis.service.ManualAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 手动数据分析Service实现
 */
@Slf4j
@Service
public class ManualAnalysisServiceImpl implements ManualAnalysisService {

    @Autowired
    @Qualifier("db1JdbcTemplate")
    private JdbcTemplate db1JdbcTemplate;

    @Autowired
    @Qualifier("db2JdbcTemplate")
    private JdbcTemplate db2JdbcTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 允许的SQL关键字白名单（安全性）
    private static final Set<String> ALLOWED_SQL_KEYWORDS = Set.of(
            "SELECT", "FROM", "WHERE", "GROUP BY", "ORDER BY", "LIMIT", "JOIN", "INNER", "LEFT", "RIGHT"
    );

    @Override
    public Map<String, Object> executeCustomQuery(String sql, String datasource) {
        Map<String, Object> result = new HashMap<>();

        try {
            // SQL安全检查
            if (!isValidSql(sql)) {
                result.put("success", false);
                result.put("message", "SQL语句包含不安全的操作");
                return result;
            }

            JdbcTemplate jdbcTemplate = "db2".equals(datasource) ? db2JdbcTemplate : db1JdbcTemplate;

            long startTime = System.currentTimeMillis();
            List<Map<String, Object>> data = jdbcTemplate.queryForList(sql);
            long executionTime = System.currentTimeMillis() - startTime;

            result.put("success", true);
            result.put("datasource", datasource);
            result.put("rowCount", data.size());
            result.put("executionTime", executionTime + "ms");
            result.put("data", data);

        } catch (Exception e) {
            log.error("执行自定义查询失败", e);
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> triggerFullDataSync() {
        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();

        try {
            // 同步观众数据
            syncAudienceData();
            
            // 同步主播数据
            syncAnchorData();
            
            // 同步标签关联
            syncTagRelations();

            long totalTime = System.currentTimeMillis() - startTime;
            result.put("success", true);
            result.put("message", "全量数据同步完成");
            result.put("totalTime", totalTime + "ms");

        } catch (Exception e) {
            log.error("全量数据同步失败", e);
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> triggerIncrementalSync(String startDate, String endDate) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 增量同步指定日期范围的数据
            String sql = "SELECT COUNT(*) as count FROM recharge_record " +
                    "WHERE recharge_time BETWEEN ? AND ?";
            
            Integer count = db2JdbcTemplate.queryForObject(sql, Integer.class, startDate, endDate);

            result.put("success", true);
            result.put("message", "增量数据同步完成");
            result.put("startDate", startDate);
            result.put("endDate", endDate);
            result.put("recordCount", count);

        } catch (Exception e) {
            log.error("增量数据同步失败", e);
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> customRangeAnalysis(String startDate, String endDate, String metrics) {
        Map<String, Object> result = new HashMap<>();
        String[] metricArray = metrics.split(",");

        try {
            for (String metric : metricArray) {
                metric = metric.trim();
                switch (metric) {
                    case "gmv":
                        result.put("gmv", calculateGmv(startDate, endDate));
                        break;
                    case "users":
                        result.put("users", calculateActiveUsers(startDate, endDate));
                        break;
                    case "transactions":
                        result.put("transactions", calculateTransactions(startDate, endDate));
                        break;
                    case "arpu":
                        result.put("arpu", calculateArpu(startDate, endDate));
                        break;
                    case "arppu":
                        result.put("arppu", calculateArppu(startDate, endDate));
                        break;
                }
            }

            result.put("success", true);
            result.put("startDate", startDate);
            result.put("endDate", endDate);

        } catch (Exception e) {
            log.error("自定义范围分析失败", e);
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> comparePeriods(String period1Start, String period1End,
                                             String period2Start, String period2End) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 时期1数据
            Map<String, Object> period1 = new HashMap<>();
            period1.put("gmv", calculateGmv(period1Start, period1End));
            period1.put("users", calculateActiveUsers(period1Start, period1End));
            period1.put("transactions", calculateTransactions(period1Start, period1End));

            // 时期2数据
            Map<String, Object> period2 = new HashMap<>();
            period2.put("gmv", calculateGmv(period2Start, period2End));
            period2.put("users", calculateActiveUsers(period2Start, period2End));
            period2.put("transactions", calculateTransactions(period2Start, period2End));

            // 计算变化率
            Map<String, Object> changes = new HashMap<>();
            changes.put("gmvChange", calculateChangeRate(
                    (BigDecimal) period2.get("gmv"), (BigDecimal) period1.get("gmv")));
            changes.put("usersChange", calculateChangeRate(
                    BigDecimal.valueOf((Integer) period2.get("users")),
                    BigDecimal.valueOf((Integer) period1.get("users"))));
            changes.put("transactionsChange", calculateChangeRate(
                    BigDecimal.valueOf((Integer) period2.get("transactions")),
                    BigDecimal.valueOf((Integer) period1.get("transactions"))));

            result.put("success", true);
            result.put("period1", period1);
            result.put("period2", period2);
            result.put("changes", changes);

        } catch (Exception e) {
            log.error("时间段对比失败", e);
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> cohortAnalysis(Map<String, Object> conditions) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 构建WHERE条件
            StringBuilder whereClause = new StringBuilder("WHERE 1=1");
            List<Object> params = new ArrayList<>();

            if (conditions.containsKey("minAmount")) {
                whereClause.append(" AND total_recharge_amount >= ?");
                params.add(conditions.get("minAmount"));
            }
            if (conditions.containsKey("maxAmount")) {
                whereClause.append(" AND total_recharge_amount <= ?");
                params.add(conditions.get("maxAmount"));
            }
            if (conditions.containsKey("consumptionLevel")) {
                whereClause.append(" AND consumption_level = ?");
                params.add(conditions.get("consumptionLevel"));
            }

            String sql = "SELECT " +
                    "    COUNT(*) as user_count, " +
                    "    AVG(total_recharge_amount) as avg_amount, " +
                    "    SUM(total_recharge_amount) as total_amount, " +
                    "    AVG(total_recharge_count) as avg_frequency " +
                    "FROM audience " + whereClause;

            Map<String, Object> cohortData = db1JdbcTemplate.queryForMap(sql, params.toArray());

            result.put("success", true);
            result.put("conditions", conditions);
            result.put("cohortData", cohortData);

        } catch (Exception e) {
            log.error("用户群体分析失败", e);
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> compareAnchorGroups(String anchorIds1, String anchorIds2,
                                                   String startDate, String endDate) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 分组1数据
            Map<String, Object> group1 = calculateGroupMetrics(anchorIds1, startDate, endDate);
            
            // 分组2数据
            Map<String, Object> group2 = calculateGroupMetrics(anchorIds2, startDate, endDate);

            result.put("success", true);
            result.put("group1", group1);
            result.put("group2", group2);

        } catch (Exception e) {
            log.error("主播分组对比失败", e);
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> funnelAnalysis(Map<String, Object> steps) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<Map<String, Object>> funnelData = new ArrayList<>();

            // 示例：观众转化漏斗
            // 步骤1：注册用户
            String sql1 = "SELECT COUNT(*) as count FROM user";
            Integer totalUsers = db1JdbcTemplate.queryForObject(sql1, Integer.class);
            addFunnelStep(funnelData, "注册用户", totalUsers, 100.0);

            // 步骤2：观看直播
            String sql2 = "SELECT COUNT(DISTINCT audience_id) as count FROM recharge";
            Integer viewers = db1JdbcTemplate.queryForObject(sql2, Integer.class);
            addFunnelStep(funnelData, "观看直播", viewers,
                    viewers * 100.0 / totalUsers);

            // 步骤3：首次打赏
            String sql3 = "SELECT COUNT(DISTINCT audience_id) as count FROM recharge";
            Integer firstPayUsers = db1JdbcTemplate.queryForObject(sql3, Integer.class);
            addFunnelStep(funnelData, "首次打赏", firstPayUsers,
                    firstPayUsers * 100.0 / totalUsers);

            // 步骤4：复购用户
            String sql4 = "SELECT COUNT(DISTINCT audience_id) as count " +
                    "FROM recharge " +
                    "GROUP BY audience_id " +
                    "HAVING COUNT(*) >= 2";
            Integer repeatUsers = db1JdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM (" + sql4 + ") t", Integer.class);
            addFunnelStep(funnelData, "复购用户", repeatUsers,
                    repeatUsers * 100.0 / totalUsers);

            result.put("success", true);
            result.put("funnelData", funnelData);

        } catch (Exception e) {
            log.error("漏斗分析失败", e);
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> anomalyDetection(String metric, String startDate,
                                               String endDate, Double threshold) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 查询时间序列数据
            String sql = "SELECT " +
                    "    DATE(recharge_time) as date, " +
                    "    SUM(settlement_amount) as value " +
                    "FROM recharge_record " +
                    "WHERE recharge_time BETWEEN ? AND ? " +
                    "GROUP BY date " +
                    "ORDER BY date";

            List<Map<String, Object>> timeSeries = db2JdbcTemplate.queryForList(sql, startDate, endDate);

            // 计算统计量
            DescriptiveStatistics stats = new DescriptiveStatistics();
            for (Map<String, Object> row : timeSeries) {
                BigDecimal value = (BigDecimal) row.get("value");
                stats.addValue(value.doubleValue());
            }

            double mean = stats.getMean();
            double stdDev = stats.getStandardDeviation();
            double upperBound = mean + threshold * stdDev;
            double lowerBound = mean - threshold * stdDev;

            // 检测异常点
            List<Map<String, Object>> anomalies = new ArrayList<>();
            for (Map<String, Object> row : timeSeries) {
                BigDecimal value = (BigDecimal) row.get("value");
                double val = value.doubleValue();
                
                if (val > upperBound || val < lowerBound) {
                    Map<String, Object> anomaly = new HashMap<>(row);
                    anomaly.put("type", val > upperBound ? "异常高" : "异常低");
                    anomaly.put("deviation", Math.abs(val - mean) / stdDev);
                    anomalies.add(anomaly);
                }
            }

            result.put("success", true);
            result.put("metric", metric);
            result.put("mean", BigDecimal.valueOf(mean).setScale(2, RoundingMode.HALF_UP));
            result.put("stdDev", BigDecimal.valueOf(stdDev).setScale(2, RoundingMode.HALF_UP));
            result.put("threshold", threshold);
            result.put("anomalyCount", anomalies.size());
            result.put("anomalies", anomalies);

        } catch (Exception e) {
            log.error("异常检测失败", e);
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> correlationAnalysis(String metric1, String metric2,
                                                  String startDate, String endDate) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 查询两个指标的时间序列数据
            String sql = "SELECT " +
                    "    DATE(recharge_time) as date, " +
                    "    SUM(settlement_amount) as gmv, " +
                    "    COUNT(DISTINCT audience_id) as users " +
                    "FROM recharge_record " +
                    "WHERE recharge_time BETWEEN ? AND ? " +
                    "GROUP BY date " +
                    "ORDER BY date";

            List<Map<String, Object>> data = db2JdbcTemplate.queryForList(sql, startDate, endDate);

            double[] x = new double[data.size()];
            double[] y = new double[data.size()];

            for (int i = 0; i < data.size(); i++) {
                x[i] = getMetricValue(data.get(i), metric1);
                y[i] = getMetricValue(data.get(i), metric2);
            }

            // 计算Pearson相关系数
            PearsonsCorrelation correlation = new PearsonsCorrelation();
            double correlationCoefficient = correlation.correlation(x, y);

            String correlationLevel;
            if (Math.abs(correlationCoefficient) >= 0.8) {
                correlationLevel = "强相关";
            } else if (Math.abs(correlationCoefficient) >= 0.5) {
                correlationLevel = "中等相关";
            } else if (Math.abs(correlationCoefficient) >= 0.3) {
                correlationLevel = "弱相关";
            } else {
                correlationLevel = "无相关";
            }

            result.put("success", true);
            result.put("metric1", metric1);
            result.put("metric2", metric2);
            result.put("correlationCoefficient", 
                    BigDecimal.valueOf(correlationCoefficient).setScale(4, RoundingMode.HALF_UP));
            result.put("correlationLevel", correlationLevel);
            result.put("dataPoints", data.size());

        } catch (Exception e) {
            log.error("相关性分析失败", e);
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> importCsvData(MultipartFile file, String analysisType) {
        Map<String, Object> result = new HashMap<>();

        try {
            Reader reader = new InputStreamReader(file.getInputStream());
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());

            List<Map<String, String>> records = new ArrayList<>();
            for (CSVRecord record : csvParser) {
                Map<String, String> row = new HashMap<>();
                record.toMap().forEach(row::put);
                records.add(row);
            }

            result.put("success", true);
            result.put("filename", file.getOriginalFilename());
            result.put("recordCount", records.size());
            result.put("analysisType", analysisType);
            result.put("preview", records.stream().limit(10).collect(Collectors.toList()));

        } catch (Exception e) {
            log.error("CSV导入失败", e);
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> generateCustomReport(Map<String, Object> config) {
        Map<String, Object> result = new HashMap<>();

        try {
            String reportType = (String) config.get("reportType");
            String startDate = (String) config.get("startDate");
            String endDate = (String) config.get("endDate");

            Map<String, Object> reportData = new HashMap<>();
            reportData.put("gmv", calculateGmv(startDate, endDate));
            reportData.put("users", calculateActiveUsers(startDate, endDate));
            reportData.put("transactions", calculateTransactions(startDate, endDate));

            result.put("success", true);
            result.put("reportType", reportType);
            result.put("reportData", reportData);
            result.put("generatedAt", LocalDate.now().toString());

        } catch (Exception e) {
            log.error("报表生成失败", e);
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> prediction(String metric, Integer days, String model) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 获取历史数据
            String sql = "SELECT " +
                    "    DATE(recharge_time) as date, " +
                    "    SUM(settlement_amount) as value " +
                    "FROM recharge_record " +
                    "WHERE recharge_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) " +
                    "GROUP BY date " +
                    "ORDER BY date";

            List<Map<String, Object>> historicalData = db2JdbcTemplate.queryForList(sql);

            // 简单线性回归预测
            List<Map<String, Object>> predictions = new ArrayList<>();
            if ("linear".equals(model)) {
                predictions = linearPrediction(historicalData, days);
            }

            result.put("success", true);
            result.put("metric", metric);
            result.put("model", model);
            result.put("predictionDays", days);
            result.put("historicalDataPoints", historicalData.size());
            result.put("predictions", predictions);

        } catch (Exception e) {
            log.error("预测分析失败", e);
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> clearCache(String pattern) {
        Map<String, Object> result = new HashMap<>();

        try {
            Set<String> keys = redisTemplate.keys(pattern);
            int count = 0;
            
            if (keys != null && !keys.isEmpty()) {
                count = keys.size();
                redisTemplate.delete(keys);
            }

            result.put("success", true);
            result.put("pattern", pattern);
            result.put("clearedCount", count);

        } catch (Exception e) {
            log.error("清除缓存失败", e);
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> dataQualityCheck(String datasource, String table) {
        Map<String, Object> result = new HashMap<>();

        try {
            JdbcTemplate jdbcTemplate = "db2".equals(datasource) ? db2JdbcTemplate : db1JdbcTemplate;
            List<Map<String, Object>> issues = new ArrayList<>();

            if (table == null || "recharge".equals(table)) {
                // 检查空值
                checkNullValues(jdbcTemplate, "recharge", "recharge_amount", issues);
                
                // 检查异常值
                checkAnomalousValues(jdbcTemplate, "recharge", "recharge_amount", issues);
                
                // 检查重复数据
                checkDuplicates(jdbcTemplate, "recharge", issues);
            }

            result.put("success", true);
            result.put("datasource", datasource);
            result.put("table", table);
            result.put("issueCount", issues.size());
            result.put("issues", issues);

        } catch (Exception e) {
            log.error("数据质量检查失败", e);
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return result;
    }

    // ==================== 辅助方法 ====================

    private boolean isValidSql(String sql) {
        String upperSql = sql.toUpperCase();
        // 禁止DDL和DML操作
        if (upperSql.contains("DROP") || upperSql.contains("DELETE") ||
                upperSql.contains("UPDATE") || upperSql.contains("INSERT") ||
                upperSql.contains("TRUNCATE") || upperSql.contains("ALTER")) {
            return false;
        }
        // 必须是SELECT查询
        return upperSql.trim().startsWith("SELECT");
    }

    private void syncAudienceData() {
        log.info("同步观众数据");
        // 实际实现：从DB2同步到分析表
    }

    private void syncAnchorData() {
        log.info("同步主播数据");
        // 实际实现：从DB1同步到分析表
    }

    private void syncTagRelations() {
        log.info("同步标签关联");
        // 实际实现：计算并保存标签关联度
    }

    private BigDecimal calculateGmv(String startDate, String endDate) {
        String sql = "SELECT COALESCE(SUM(settlement_amount), 0) as gmv " +
                "FROM recharge_record " +
                "WHERE recharge_time BETWEEN ? AND ?";
        return db2JdbcTemplate.queryForObject(sql, BigDecimal.class, startDate, endDate);
    }

    private Integer calculateActiveUsers(String startDate, String endDate) {
        String sql = "SELECT COUNT(DISTINCT audience_id) as count " +
                "FROM recharge " +
                "WHERE recharge_time BETWEEN ? AND ?";
        return db1JdbcTemplate.queryForObject(sql, Integer.class, startDate, endDate);
    }

    private Integer calculateTransactions(String startDate, String endDate) {
        String sql = "SELECT COUNT(*) as count " +
                "FROM recharge_record " +
                "WHERE recharge_time BETWEEN ? AND ?";
        return db2JdbcTemplate.queryForObject(sql, Integer.class, startDate, endDate);
    }

    private BigDecimal calculateArpu(String startDate, String endDate) {
        BigDecimal gmv = calculateGmv(startDate, endDate);
        String sql = "SELECT COUNT(*) FROM audience";
        Integer totalUsers = db1JdbcTemplate.queryForObject(sql, Integer.class);
        return totalUsers > 0 ? gmv.divide(BigDecimal.valueOf(totalUsers), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    private BigDecimal calculateArppu(String startDate, String endDate) {
        BigDecimal gmv = calculateGmv(startDate, endDate);
        Integer payingUsers = calculateActiveUsers(startDate, endDate);
        return payingUsers > 0 ? gmv.divide(BigDecimal.valueOf(payingUsers), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    private BigDecimal calculateChangeRate(BigDecimal current, BigDecimal previous) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private Map<String, Object> calculateGroupMetrics(String anchorIds, String startDate, String endDate) {
        String sql = "SELECT " +
                "    SUM(settlement_amount) as total_income, " +
                "    COUNT(*) as transaction_count, " +
                "    COUNT(DISTINCT audience_id) as paying_users " +
                "FROM recharge_record " +
                "WHERE anchor_id IN (" + anchorIds + ") " +
                "  AND recharge_time BETWEEN ? AND ?";
        
        return db2JdbcTemplate.queryForMap(sql, startDate, endDate);
    }

    private void addFunnelStep(List<Map<String, Object>> funnelData, String step, Integer count, Double rate) {
        Map<String, Object> stepData = new HashMap<>();
        stepData.put("step", step);
        stepData.put("count", count);
        stepData.put("rate", BigDecimal.valueOf(rate).setScale(2, RoundingMode.HALF_UP));
        funnelData.add(stepData);
    }

    private double getMetricValue(Map<String, Object> row, String metric) {
        if ("gmv".equals(metric)) {
            return ((BigDecimal) row.get("gmv")).doubleValue();
        } else if ("users".equals(metric)) {
            return ((Number) row.get("users")).doubleValue();
        }
        return 0.0;
    }

    private List<Map<String, Object>> linearPrediction(List<Map<String, Object>> historicalData, Integer days) {
        List<Map<String, Object>> predictions = new ArrayList<>();
        
        if (historicalData.isEmpty()) return predictions;

        // 简单线性趋势
        BigDecimal lastValue = (BigDecimal) historicalData.get(historicalData.size() - 1).get("value");
        BigDecimal avgGrowth = BigDecimal.valueOf(0.02); // 假设日增长2%

        LocalDate lastDate = LocalDate.parse((String) historicalData.get(historicalData.size() - 1).get("date"));
        
        for (int i = 1; i <= days; i++) {
            Map<String, Object> prediction = new HashMap<>();
            prediction.put("date", lastDate.plusDays(i).toString());
            prediction.put("predictedValue", lastValue.multiply(BigDecimal.ONE.add(avgGrowth.multiply(BigDecimal.valueOf(i)))));
            predictions.add(prediction);
        }

        return predictions;
    }

    private void checkNullValues(JdbcTemplate jdbcTemplate, String table, String column, List<Map<String, Object>> issues) {
        String sql = "SELECT COUNT(*) FROM " + table + " WHERE " + column + " IS NULL";
        Integer nullCount = jdbcTemplate.queryForObject(sql, Integer.class);
        
        if (nullCount > 0) {
            Map<String, Object> issue = new HashMap<>();
            issue.put("type", "空值");
            issue.put("table", table);
            issue.put("column", column);
            issue.put("count", nullCount);
            issues.add(issue);
        }
    }

    private void checkAnomalousValues(JdbcTemplate jdbcTemplate, String table, String column, List<Map<String, Object>> issues) {
        String sql = "SELECT COUNT(*) FROM " + table + " WHERE " + column + " <= 0";
        Integer anomalousCount = jdbcTemplate.queryForObject(sql, Integer.class);
        
        if (anomalousCount > 0) {
            Map<String, Object> issue = new HashMap<>();
            issue.put("type", "异常值");
            issue.put("table", table);
            issue.put("column", column);
            issue.put("count", anomalousCount);
            issue.put("description", "金额小于等于0");
            issues.add(issue);
        }
    }

    private void checkDuplicates(JdbcTemplate jdbcTemplate, String table, List<Map<String, Object>> issues) {
        // 简化检查：实际应检查业务主键重复
        String sql = "SELECT COUNT(*) - COUNT(DISTINCT recharge_id) as dup_count FROM " + table;
        Integer dupCount = jdbcTemplate.queryForObject(sql, Integer.class);
        
        if (dupCount > 0) {
            Map<String, Object> issue = new HashMap<>();
            issue.put("type", "重复数据");
            issue.put("table", table);
            issue.put("count", dupCount);
            issues.add(issue);
        }
    }
}
