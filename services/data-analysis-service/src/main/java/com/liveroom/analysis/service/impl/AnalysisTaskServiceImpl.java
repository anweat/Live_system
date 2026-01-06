package com.liveroom.analysis.service.impl;

import com.liveroom.analysis.service.AnalysisTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 分析任务Service实现
 */
@Slf4j
@Service
public class AnalysisTaskServiceImpl implements AnalysisTaskService {

    @Autowired
    @Qualifier("db1JdbcTemplate")
    private JdbcTemplate db1JdbcTemplate;

    @Autowired
    @Qualifier("db2JdbcTemplate")
    private JdbcTemplate db2JdbcTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String TASK_STATUS_KEY_PREFIX = "analysis:task:status:";

    @Override
    @Async
    public void runHourlyStatistics() {
        String taskId = "hourly-statistics-" + System.currentTimeMillis();
        updateTaskStatus(taskId, "running", "小时统计任务启动");

        try {
            log.info("开始执行小时统计任务");

            // 统计上一小时的数据
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime lastHour = now.minusHours(1);
            
            String startTime = lastHour.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00:00"));
            String endTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00:00"));

            // 统计GMV
            String sql1 = "SELECT " +
                    "    SUM(settlement_amount) as gmv, " +
                    "    COUNT(*) as transaction_count, " +
                    "    COUNT(DISTINCT audience_id) as paying_users " +
                    "FROM recharge_record " +
                    "WHERE recharge_time BETWEEN ? AND ?";

            Map<String, Object> gmvStats = db2JdbcTemplate.queryForMap(sql1, startTime, endTime);

            // 统计活跃主播
            String sql2 = "SELECT COUNT(DISTINCT anchor_id) as active_anchors " +
                    "FROM recharge_record " +
                    "WHERE recharge_time BETWEEN ? AND ?";

            Integer activeAnchors = db2JdbcTemplate.queryForObject(sql2, Integer.class, startTime, endTime);

            // 保存统计结果到Redis
            Map<String, Object> hourlyStats = new HashMap<>();
            hourlyStats.put("hour", startTime);
            hourlyStats.put("gmv", gmvStats.get("gmv"));
            hourlyStats.put("transactionCount", gmvStats.get("transaction_count"));
            hourlyStats.put("payingUsers", gmvStats.get("paying_users"));
            hourlyStats.put("activeAnchors", activeAnchors);
            hourlyStats.put("timestamp", LocalDateTime.now().toString());

            String statsKey = "analysis:hourly:stats:" + startTime;
            redisTemplate.opsForValue().set(statsKey, hourlyStats, 7, TimeUnit.DAYS);

            log.info("小时统计任务完成: hour={}, gmv={}", startTime, gmvStats.get("gmv"));
            updateTaskStatus(taskId, "completed", "小时统计任务完成");

        } catch (Exception e) {
            log.error("小时统计任务失败", e);
            updateTaskStatus(taskId, "failed", "小时统计任务失败: " + e.getMessage());
        }
    }

    @Override
    @Async
    public void runAudiencePortraitCalculation() {
        String taskId = "audience-portrait-" + System.currentTimeMillis();
        updateTaskStatus(taskId, "running", "观众画像计算任务启动");

        try {
            log.info("开始执行观众画像计算任务");

            // 查询所有观众
            String sql = "SELECT user_id FROM audience LIMIT 1000"; // 分批处理
            List<Long> audienceIds = db1JdbcTemplate.queryForList(sql, Long.class);

            int processed = 0;
            for (Long audienceId : audienceIds) {
                try {
                    calculateAudiencePortrait(audienceId);
                    processed++;
                } catch (Exception e) {
                    log.error("计算观众画像失败: audienceId={}", audienceId, e);
                }
            }

            log.info("观众画像计算任务完成: processed={}/{}", processed, audienceIds.size());
            updateTaskStatus(taskId, "completed", 
                    String.format("观众画像计算任务完成，处理 %d/%d", processed, audienceIds.size()));

        } catch (Exception e) {
            log.error("观众画像计算任务失败", e);
            updateTaskStatus(taskId, "failed", "观众画像计算任务失败: " + e.getMessage());
        }
    }

    @Override
    @Async
    public void runTagRelationCalculation() {
        String taskId = "tag-relation-" + System.currentTimeMillis();
        updateTaskStatus(taskId, "running", "标签关联度计算任务启动");

        try {
            log.info("开始执行标签关联度计算任务");

            // 查询所有标签
            String sql = "SELECT tag_id, tag_name FROM tag";
            List<Map<String, Object>> tags = db1JdbcTemplate.queryForList(sql);

            // 计算标签间的关联度
            int relationCount = 0;
            for (int i = 0; i < tags.size(); i++) {
                for (int j = i + 1; j < tags.size(); j++) {
                    Long tagId1 = (Long) tags.get(i).get("tag_id");
                    Long tagId2 = (Long) tags.get(j).get("tag_id");

                    BigDecimal relationScore = calculateTagRelation(tagId1, tagId2);
                    
                    if (relationScore.compareTo(BigDecimal.valueOf(5)) > 0) {
                        saveTagRelation(tagId1, tagId2, relationScore);
                        relationCount++;
                    }
                }
            }

            log.info("标签关联度计算任务完成: relations={}", relationCount);
            updateTaskStatus(taskId, "completed", 
                    String.format("标签关联度计算任务完成，生成 %d 个关联关系", relationCount));

        } catch (Exception e) {
            log.error("标签关联度计算任务失败", e);
            updateTaskStatus(taskId, "failed", "标签关联度计算任务失败: " + e.getMessage());
        }
    }

    @Override
    @Async
    public void runRetentionAnalysis() {
        String taskId = "retention-analysis-" + System.currentTimeMillis();
        updateTaskStatus(taskId, "running", "留存率分析任务启动");

        try {
            log.info("开始执行留存率分析任务");

            // 计算1日、7日、30日留存率
            int[] days = {1, 7, 30};
            Map<String, BigDecimal> retentionRates = new HashMap<>();

            for (int day : days) {
                BigDecimal retentionRate = calculateRetentionRate(day);
                retentionRates.put("day" + day, retentionRate);
                log.info("{}日留存率: {}%", day, retentionRate);
            }

            // 保存结果到Redis
            String resultKey = "analysis:retention:latest";
            redisTemplate.opsForValue().set(resultKey, retentionRates, 24, TimeUnit.HOURS);

            log.info("留存率分析任务完成");
            updateTaskStatus(taskId, "completed", "留存率分析任务完成");

        } catch (Exception e) {
            log.error("留存率分析任务失败", e);
            updateTaskStatus(taskId, "failed", "留存率分析任务失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getTaskStatus(String taskType) {
        // 从Redis查询最近的任务状态
        String pattern = TASK_STATUS_KEY_PREFIX + taskType + "*";
        Set<String> keys = redisTemplate.keys(pattern);

        if (keys == null || keys.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("taskType", taskType);
            result.put("status", "no_record");
            result.put("message", "未找到任务记录");
            return result;
        }

        // 获取最新的任务状态
        String latestKey = keys.stream()
                .max(Comparator.naturalOrder())
                .orElse(null);

        if (latestKey != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> status = (Map<String, Object>) redisTemplate.opsForValue().get(latestKey);
            return status != null ? status : Collections.emptyMap();
        }

        return Collections.emptyMap();
    }

    /**
     * 定时任务：每小时执行
     */
    @Scheduled(cron = "${analysis.cron.hourly:0 0 * * * ?}")
    public void scheduledHourlyStatistics() {
        log.info("定时任务触发：小时统计");
        runHourlyStatistics();
    }

    /**
     * 定时任务：每天凌晨2点执行
     */
    @Scheduled(cron = "${analysis.cron.daily:0 0 2 * * ?}")
    public void scheduledAudiencePortrait() {
        log.info("定时任务触发：观众画像计算");
        runAudiencePortraitCalculation();
    }

    /**
     * 定时任务：每天凌晨3点执行
     */
    @Scheduled(cron = "${analysis.cron.daily-tag:0 0 3 * * ?}")
    public void scheduledTagRelation() {
        log.info("定时任务触发：标签关联度计算");
        runTagRelationCalculation();
    }

    /**
     * 定时任务：每周一凌晨5点执行
     */
    @Scheduled(cron = "${analysis.cron.weekly:0 0 5 ? * MON}")
    public void scheduledRetentionAnalysis() {
        log.info("定时任务触发：留存率分析");
        runRetentionAnalysis();
    }

    /**
     * 更新任务状态
     */
    private void updateTaskStatus(String taskId, String status, String message) {
        Map<String, Object> taskStatus = new HashMap<>();
        taskStatus.put("taskId", taskId);
        taskStatus.put("status", status);
        taskStatus.put("message", message);
        taskStatus.put("timestamp", LocalDateTime.now().toString());

        String key = TASK_STATUS_KEY_PREFIX + taskId;
        redisTemplate.opsForValue().set(key, taskStatus, 7, TimeUnit.DAYS);
    }

    /**
     * 计算观众画像
     */
    private void calculateAudiencePortrait(Long audienceId) {
        // 查询观众消费数据
        String sql = "SELECT " +
                "    SUM(recharge_amount) as total_amount, " +
                "    COUNT(*) as recharge_count, " +
                "    MAX(recharge_time) as last_recharge_time " +
                "FROM recharge " +
                "WHERE audience_id = ?";

        Map<String, Object> data = db1JdbcTemplate.queryForMap(sql, audienceId);

        // 计算消费等级
        BigDecimal totalAmount = (BigDecimal) data.get("total_amount");
        Integer consumptionLevel;
        if (totalAmount.compareTo(BigDecimal.valueOf(5000)) >= 0) {
            consumptionLevel = 2; // 高消费
        } else if (totalAmount.compareTo(BigDecimal.valueOf(1000)) >= 0) {
            consumptionLevel = 1; // 中消费
        } else {
            consumptionLevel = 0; // 低消费
        }

        // 更新观众表
        String updateSql = "UPDATE audience SET " +
                "consumption_level = ?, " +
                "total_recharge_amount = ?, " +
                "total_recharge_count = ?, " +
                "last_recharge_time = ? " +
                "WHERE user_id = ?";

        db1JdbcTemplate.update(updateSql,
                consumptionLevel,
                totalAmount,
                data.get("recharge_count"),
                data.get("last_recharge_time"),
                audienceId);
    }

    /**
     * 计算标签关联度
     */
    private BigDecimal calculateTagRelation(Long tagId1, Long tagId2) {
        // 查询拥有标签1的用户
        String sql1 = "SELECT DISTINCT user_id FROM (" +
                "    SELECT anchor_id as user_id FROM anchor_tag WHERE tag_id = ?" +
                "    UNION " +
                "    SELECT audience_id as user_id FROM audience_tag WHERE tag_id = ?" +
                ") t1";
        
        Set<Long> users1 = new HashSet<>(db1JdbcTemplate.queryForList(sql1, Long.class, tagId1, tagId1));

        // 查询拥有标签2的用户
        String sql2 = "SELECT DISTINCT user_id FROM (" +
                "    SELECT anchor_id as user_id FROM anchor_tag WHERE tag_id = ?" +
                "    UNION " +
                "    SELECT audience_id as user_id FROM audience_tag WHERE tag_id = ?" +
                ") t2";
        
        Set<Long> users2 = new HashSet<>(db1JdbcTemplate.queryForList(sql2, Long.class, tagId2, tagId2));

        // 计算Jaccard相似度
        Set<Long> intersection = new HashSet<>(users1);
        intersection.retainAll(users2);

        Set<Long> union = new HashSet<>(users1);
        union.addAll(users2);

        if (union.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(intersection.size() * 100.0 / union.size())
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 保存标签关联关系
     */
    private void saveTagRelation(Long tagId1, Long tagId2, BigDecimal relationScore) {
        // 计算强度等级
        int strengthLevel;
        if (relationScore.compareTo(BigDecimal.valueOf(50)) >= 0) {
            strengthLevel = 3; // 强关联
        } else if (relationScore.compareTo(BigDecimal.valueOf(30)) >= 0) {
            strengthLevel = 2; // 中关联
        } else {
            strengthLevel = 1; // 弱关联
        }

        // 查询共现次数
        String sql = "SELECT COUNT(*) FROM (" +
                "    SELECT at1.anchor_id as user_id FROM anchor_tag at1 " +
                "    INNER JOIN anchor_tag at2 ON at1.anchor_id = at2.anchor_id " +
                "    WHERE at1.tag_id = ? AND at2.tag_id = ? " +
                "    UNION ALL " +
                "    SELECT aut1.audience_id as user_id FROM audience_tag aut1 " +
                "    INNER JOIN audience_tag aut2 ON aut1.audience_id = aut2.audience_id " +
                "    WHERE aut1.tag_id = ? AND aut2.tag_id = ? " +
                ") co";

        Integer cooccurrenceCount = db1JdbcTemplate.queryForObject(sql, Integer.class, 
                tagId1, tagId2, tagId1, tagId2);

        // 插入或更新关联记录
        String insertSql = "INSERT INTO tag_relation " +
                "(tag_id1, tag_id2, relation_score, cooccurrence_count, strength_level) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "relation_score = VALUES(relation_score), " +
                "cooccurrence_count = VALUES(cooccurrence_count), " +
                "strength_level = VALUES(strength_level)";

        db1JdbcTemplate.update(insertSql, tagId1, tagId2, relationScore, cooccurrenceCount, strengthLevel);
    }

    /**
     * 计算留存率
     */
    private BigDecimal calculateRetentionRate(int days) {
        // 查询N日前注册的用户
        String sql1 = "SELECT COUNT(*) as total " +
                "FROM audience " +
                "WHERE DATE(create_time) = DATE_SUB(CURDATE(), INTERVAL ? DAY)";

        Integer totalNewUsers = db1JdbcTemplate.queryForObject(sql1, Integer.class, days);

        if (totalNewUsers == null || totalNewUsers == 0) {
            return BigDecimal.ZERO;
        }

        // 查询这些用户在之后是否有消费
        String sql2 = "SELECT COUNT(DISTINCT a.user_id) as retained " +
                "FROM audience a " +
                "INNER JOIN recharge r ON a.user_id = r.audience_id " +
                "WHERE DATE(a.create_time) = DATE_SUB(CURDATE(), INTERVAL ? DAY) " +
                "  AND r.recharge_time > DATE_ADD(a.create_time, INTERVAL 1 DAY)";

        Integer retainedUsers = db1JdbcTemplate.queryForObject(sql2, Integer.class, days);

        if (retainedUsers == null) {
            retainedUsers = 0;
        }

        return BigDecimal.valueOf(retainedUsers * 100.0 / totalNewUsers)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
