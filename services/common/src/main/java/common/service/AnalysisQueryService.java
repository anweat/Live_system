package common.service;

import common.bean.Recharge;
import common.logger.TraceLogger;
import common.repository.RechargeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据分析查询Service
 *
 * 为data-analysis-service提供各种聚合查询接口
 * 包括：时间范围聚合、分组统计、趋势分析数据
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisQueryService {

    private final RechargeRepository rechargeRepository;

    /**
     * 查询时间范围内的打赏记录
     */
    @Transactional(readOnly = true)
    public List<Recharge> getRechargesByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("时间范围不能为空");
        }
        TraceLogger.info("AnalysisQueryService", "getRechargesByTimeRange",
            String.format("查询打赏记录: %s - %s", startTime, endTime));
        return rechargeRepository.findByTimeRange(startTime, endTime);
    }

    /**
     * 查询指定主播在时间范围内的打赏记录
     */
    @Transactional(readOnly = true)
    public List<Recharge> getRechargesByAnchorAndTimeRange(Long anchorId, LocalDateTime startTime, LocalDateTime endTime) {
        if (anchorId == null || startTime == null || endTime == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        TraceLogger.info("AnalysisQueryService", "getRechargesByAnchorAndTimeRange",
            String.format("查询主播%d的打赏: %s - %s", anchorId, startTime, endTime));

        List<Recharge> allRecharges = rechargeRepository.findByTimeRange(startTime, endTime);
        return allRecharges.stream()
            .filter(r -> r.getAnchorId().equals(anchorId))
            .collect(Collectors.toList());
    }

    /**
     * 查询指定观众在时间范围内的打赏记录
     */
    @Transactional(readOnly = true)
    public List<Recharge> getRechargesByAudienceAndTimeRange(Long audienceId, LocalDateTime startTime, LocalDateTime endTime) {
        if (audienceId == null || startTime == null || endTime == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        TraceLogger.info("AnalysisQueryService", "getRechargesByAudienceAndTimeRange",
            String.format("查询观众%d的打赏: %s - %s", audienceId, startTime, endTime));

        List<Recharge> allRecharges = rechargeRepository.findByTimeRange(startTime, endTime);
        return allRecharges.stream()
            .filter(r -> r.getAudienceId().equals(audienceId))
            .collect(Collectors.toList());
    }

    /**
     * 按天统计时间范围内的打赏数据
     */
    @Transactional(readOnly = true)
    public Map<String, RechargeStats> getRechargeStatsByDay(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("时间范围不能为空");
        }
        TraceLogger.info("AnalysisQueryService", "getRechargeStatsByDay",
            String.format("统计打赏数据（按天）: %s - %s", startTime, endTime));

        List<Recharge> recharges = rechargeRepository.findByTimeRange(startTime, endTime);

        return recharges.stream()
            .collect(Collectors.groupingBy(
                r -> r.getRechargeTime().toLocalDate().toString(),
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    list -> RechargeStats.builder()
                        .totalAmount(list.stream()
                            .map(Recharge::getRechargeAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add))
                        .count(list.size())
                        .uniqueAudiences((int) list.stream()
                            .map(Recharge::getAudienceId)
                            .distinct()
                            .count())
                        .uniqueAnchors((int) list.stream()
                            .map(Recharge::getAnchorId)
                            .distinct()
                            .count())
                        .build()
                )
            ));
    }

    /**
     * 按小时统计时间范围内的打赏数据
     */
    @Transactional(readOnly = true)
    public Map<String, RechargeStats> getRechargeStatsByHour(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("时间范围不能为空");
        }
        TraceLogger.info("AnalysisQueryService", "getRechargeStatsByHour",
            String.format("统计打赏数据（按小时）: %s - %s", startTime, endTime));

        List<Recharge> recharges = rechargeRepository.findByTimeRange(startTime, endTime);

        return recharges.stream()
            .collect(Collectors.groupingBy(
                r -> r.getRechargeTime().toLocalDate().toString() + " " +
                     String.format("%02d:00", r.getRechargeTime().getHour()),
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    list -> RechargeStats.builder()
                        .totalAmount(list.stream()
                            .map(Recharge::getRechargeAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add))
                        .count(list.size())
                        .uniqueAudiences((int) list.stream()
                            .map(Recharge::getAudienceId)
                            .distinct()
                            .count())
                        .uniqueAnchors((int) list.stream()
                            .map(Recharge::getAnchorId)
                            .distinct()
                            .count())
                        .build()
                )
            ));
    }

    /**
     * 查询指定主播的打赏统计（按天）
     */
    @Transactional(readOnly = true)
    public Map<String, RechargeStats> getAnchorRechargeStatsByDay(Long anchorId, LocalDateTime startTime, LocalDateTime endTime) {
        if (anchorId == null || startTime == null || endTime == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        TraceLogger.info("AnalysisQueryService", "getAnchorRechargeStatsByDay",
            String.format("统计主播%d的打赏（按天）: %s - %s", anchorId, startTime, endTime));

        List<Recharge> recharges = getRechargesByAnchorAndTimeRange(anchorId, startTime, endTime);

        return recharges.stream()
            .collect(Collectors.groupingBy(
                r -> r.getRechargeTime().toLocalDate().toString(),
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    list -> RechargeStats.builder()
                        .totalAmount(list.stream()
                            .map(Recharge::getRechargeAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add))
                        .count(list.size())
                        .uniqueAudiences((int) list.stream()
                            .map(Recharge::getAudienceId)
                            .distinct()
                            .count())
                        .build()
                )
            ));
    }

    /**
     * 查询指定主播TOP打赏用户
     */
    @Transactional(readOnly = true)
    public List<TopPayerStats> getTopPayersByAnchor(Long anchorId, LocalDateTime startTime, LocalDateTime endTime, int limit) {
        if (anchorId == null || startTime == null || endTime == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        TraceLogger.info("AnalysisQueryService", "getTopPayersByAnchor",
            String.format("查询主播%d的TOP消费者（limit=%d）: %s - %s", anchorId, limit, startTime, endTime));

        List<Recharge> recharges = getRechargesByAnchorAndTimeRange(anchorId, startTime, endTime);

        return recharges.stream()
            .collect(Collectors.groupingBy(
                Recharge::getAudienceId,
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    list -> TopPayerStats.builder()
                        .audienceId(list.get(0).getAudienceId())
                        .audienceName(list.get(0).getAudienceNickname())
                        .totalAmount(list.stream()
                            .map(Recharge::getRechargeAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add))
                        .count(list.size())
                        .lastRechargeTime(list.stream()
                            .map(Recharge::getRechargeTime)
                            .max(LocalDateTime::compareTo)
                            .orElse(null))
                        .build()
                )
            ))
            .values().stream()
            .sorted((a, b) -> b.getTotalAmount().compareTo(a.getTotalAmount()))
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * 查询观众的消费统计
     */
    @Transactional(readOnly = true)
    public AudienceConsumptionStats getAudienceConsumptionStats(Long audienceId) {
        if (audienceId == null) {
            throw new IllegalArgumentException("观众ID不能为空");
        }
        TraceLogger.info("AnalysisQueryService", "getAudienceConsumptionStats",
            String.format("查询观众%d的消费统计", audienceId));

        List<Recharge> recharges = rechargeRepository.findByAudienceId(audienceId);

        if (recharges.isEmpty()) {
            return AudienceConsumptionStats.builder()
                .audienceId(audienceId)
                .totalAmount(BigDecimal.ZERO)
                .totalCount(0)
                .uniqueAnchors(0)
                .avgAmount(BigDecimal.ZERO)
                .maxAmount(BigDecimal.ZERO)
                .minAmount(BigDecimal.ZERO)
                .lastRechargeTime(null)
                .firstRechargeTime(null)
                .build();
        }

        BigDecimal totalAmount = recharges.stream()
            .map(Recharge::getRechargeAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgAmount = totalAmount.divide(java.math.BigDecimal.valueOf(recharges.size()), 2, java.math.RoundingMode.HALF_UP);

        return AudienceConsumptionStats.builder()
            .audienceId(audienceId)
            .totalAmount(totalAmount)
            .totalCount(recharges.size())
            .uniqueAnchors((int) recharges.stream()
                .map(Recharge::getAnchorId)
                .distinct()
                .count())
            .avgAmount(avgAmount)
            .maxAmount(recharges.stream()
                .map(Recharge::getRechargeAmount)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO))
            .minAmount(recharges.stream()
                .map(Recharge::getRechargeAmount)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO))
            .lastRechargeTime(recharges.stream()
                .map(Recharge::getRechargeTime)
                .max(LocalDateTime::compareTo)
                .orElse(null))
            .firstRechargeTime(recharges.stream()
                .map(Recharge::getRechargeTime)
                .min(LocalDateTime::compareTo)
                .orElse(null))
            .build();
    }

    /**
     * 打赏统计数据DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class RechargeStats {
        private BigDecimal totalAmount;
        private int count;
        private int uniqueAudiences;
        private int uniqueAnchors;
    }

    /**
     * TOP消费者统计DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class TopPayerStats {
        private Long audienceId;
        private String audienceName;
        private BigDecimal totalAmount;
        private int count;
        private LocalDateTime lastRechargeTime;
    }

    /**
     * 按周统计时间范围内的打赏数据
     */
    @Transactional(readOnly = true)
    public Map<String, RechargeStats> getRechargeStatsByWeek(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("时间范围不能为空");
        }
        TraceLogger.info("AnalysisQueryService", "getRechargeStatsByWeek",
            String.format("统计打赏数据（按周）: %s - %s", startTime, endTime));

        List<Recharge> recharges = rechargeRepository.findByTimeRange(startTime, endTime);

        return recharges.stream()
            .collect(Collectors.groupingBy(
                r -> {
                    java.time.temporal.WeekFields weekFields = java.time.temporal.WeekFields.of(java.util.Locale.CHINA);
                    int year = r.getRechargeTime().getYear();
                    int week = r.getRechargeTime().get(weekFields.weekOfYear());
                    return String.format("%d-W%02d", year, week);
                },
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    list -> RechargeStats.builder()
                        .totalAmount(list.stream()
                            .map(Recharge::getRechargeAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add))
                        .count(list.size())
                        .uniqueAudiences((int) list.stream()
                            .map(Recharge::getAudienceId)
                            .distinct()
                            .count())
                        .uniqueAnchors((int) list.stream()
                            .map(Recharge::getAnchorId)
                            .distinct()
                            .count())
                        .build()
                )
            ));
    }

    /**
     * 按月统计时间范围内的打赏数据
     */
    @Transactional(readOnly = true)
    public Map<String, RechargeStats> getRechargeStatsByMonth(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("时间范围不能为空");
        }
        TraceLogger.info("AnalysisQueryService", "getRechargeStatsByMonth",
            String.format("统计打赏数据（按月）: %s - %s", startTime, endTime));

        List<Recharge> recharges = rechargeRepository.findByTimeRange(startTime, endTime);

        return recharges.stream()
            .collect(Collectors.groupingBy(
                r -> String.format("%04d-%02d", r.getRechargeTime().getYear(), r.getRechargeTime().getMonthValue()),
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    list -> RechargeStats.builder()
                        .totalAmount(list.stream()
                            .map(Recharge::getRechargeAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add))
                        .count(list.size())
                        .uniqueAudiences((int) list.stream()
                            .map(Recharge::getAudienceId)
                            .distinct()
                            .count())
                        .uniqueAnchors((int) list.stream()
                            .map(Recharge::getAnchorId)
                            .distinct()
                            .count())
                        .build()
                )
            ));
    }

    /**
     * 查询指定主播按小时的打赏统计
     */
    @Transactional(readOnly = true)
    public Map<String, RechargeStats> getAnchorRechargeStatsByHour(Long anchorId, LocalDateTime startTime, LocalDateTime endTime) {
        if (anchorId == null || startTime == null || endTime == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        TraceLogger.info("AnalysisQueryService", "getAnchorRechargeStatsByHour",
            String.format("统计主播%d的打赏（按小时）: %s - %s", anchorId, startTime, endTime));

        List<Recharge> recharges = getRechargesByAnchorAndTimeRange(anchorId, startTime, endTime);

        return recharges.stream()
            .collect(Collectors.groupingBy(
                r -> r.getRechargeTime().toLocalDate().toString() + " " +
                     String.format("%02d:00", r.getRechargeTime().getHour()),
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    list -> RechargeStats.builder()
                        .totalAmount(list.stream()
                            .map(Recharge::getRechargeAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add))
                        .count(list.size())
                        .uniqueAudiences((int) list.stream()
                            .map(Recharge::getAudienceId)
                            .distinct()
                            .count())
                        .build()
                )
            ));
    }

    /**
     * 查询时段（按小时）的打赏热力数据
     * 用于生成时间热力图
     */
    @Transactional(readOnly = true)
    public Map<String, Map<Integer, RechargeStats>> getHourlyHeatmapData(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("时间范围不能为空");
        }
        TraceLogger.info("AnalysisQueryService", "getHourlyHeatmapData",
            String.format("查询时间热力图数据: %s - %s", startTime, endTime));

        List<Recharge> recharges = rechargeRepository.findByTimeRange(startTime, endTime);

        // 按日期和小时分组
        return recharges.stream()
            .collect(Collectors.groupingBy(
                r -> r.getRechargeTime().toLocalDate().toString(),
                Collectors.groupingBy(
                    r -> r.getRechargeTime().getHour(),
                    Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> RechargeStats.builder()
                            .totalAmount(list.stream()
                                .map(Recharge::getRechargeAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add))
                            .count(list.size())
                            .uniqueAudiences((int) list.stream()
                                .map(Recharge::getAudienceId)
                                .distinct()
                                .count())
                            .build()
                    )
                )
            ));
    }

    /**
     * 查询指定主播在时间范围内的消费者信息（按消费金额排序）
     */
    @Transactional(readOnly = true)
    public List<AudienceConsumptionStats> getAnchorConsumers(Long anchorId, LocalDateTime startTime, LocalDateTime endTime, int limit) {
        if (anchorId == null || startTime == null || endTime == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        TraceLogger.info("AnalysisQueryService", "getAnchorConsumers",
            String.format("查询主播%d的消费者（limit=%d）: %s - %s", anchorId, limit, startTime, endTime));

        List<Recharge> recharges = getRechargesByAnchorAndTimeRange(anchorId, startTime, endTime);

        return recharges.stream()
            .collect(Collectors.groupingBy(Recharge::getAudienceId))
            .entrySet().stream()
            .map(entry -> {
                List<Recharge> audienceRecharges = entry.getValue();
                BigDecimal totalAmount = audienceRecharges.stream()
                    .map(Recharge::getRechargeAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                return AudienceConsumptionStats.builder()
                    .audienceId(entry.getKey())
                    .audienceName(audienceRecharges.get(0).getAudienceNickname())
                    .totalAmount(totalAmount)
                    .totalCount(audienceRecharges.size())
                    .avgAmount(totalAmount.divide(java.math.BigDecimal.valueOf(audienceRecharges.size()), 2, java.math.RoundingMode.HALF_UP))
                    .maxAmount(audienceRecharges.stream()
                        .map(Recharge::getRechargeAmount)
                        .max(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO))
                    .minAmount(audienceRecharges.stream()
                        .map(Recharge::getRechargeAmount)
                        .min(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO))
                    .lastRechargeTime(audienceRecharges.stream()
                        .map(Recharge::getRechargeTime)
                        .max(LocalDateTime::compareTo)
                        .orElse(null))
                    .firstRechargeTime(audienceRecharges.stream()
                        .map(Recharge::getRechargeTime)
                        .min(LocalDateTime::compareTo)
                        .orElse(null))
                    .build();
            })
            .sorted((a, b) -> b.getTotalAmount().compareTo(a.getTotalAmount()))
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * 计算移动平均（Moving Average）
     * 用于趋势平滑分析
     */
    public static Map<String, BigDecimal> calculateMovingAverage(Map<String, RechargeStats> statsByPeriod, int windowSize) {
        TraceLogger.debug("AnalysisQueryService", "calculateMovingAverage",
            String.format("计算%d周期移动平均", windowSize));

        List<String> keys = statsByPeriod.keySet().stream()
            .sorted()
            .collect(Collectors.toList());

        Map<String, BigDecimal> maMap = new java.util.LinkedHashMap<>();
        for (int i = 0; i < keys.size(); i++) {
            int start = Math.max(0, i - windowSize + 1);
            BigDecimal sum = BigDecimal.ZERO;
            for (int j = start; j <= i; j++) {
                sum = sum.add(statsByPeriod.get(keys.get(j)).getTotalAmount());
            }
            int count = i - start + 1;
            BigDecimal ma = sum.divide(java.math.BigDecimal.valueOf(count), 2, java.math.RoundingMode.HALF_UP);
            maMap.put(keys.get(i), ma);
        }
        return maMap;
    }

    /**
     * 计算指数移动平均（Exponential Moving Average）
     * 用于更敏感的趋势捕捉
     */
    public static Map<String, BigDecimal> calculateEMA(Map<String, RechargeStats> statsByPeriod, int period) {
        TraceLogger.debug("AnalysisQueryService", "calculateEMA",
            String.format("计算%d周期指数移动平均", period));

        List<String> keys = statsByPeriod.keySet().stream()
            .sorted()
            .collect(Collectors.toList());

        if (keys.isEmpty()) {
            return new java.util.HashMap<>();
        }

        double alpha = 2.0 / (period + 1);
        Map<String, BigDecimal> emaMap = new java.util.LinkedHashMap<>();

        // 初始EMA为前period个数据的简单平均
        BigDecimal initialSum = BigDecimal.ZERO;
        int initialCount = Math.min(period, keys.size());
        for (int i = 0; i < initialCount; i++) {
            initialSum = initialSum.add(statsByPeriod.get(keys.get(i)).getTotalAmount());
        }
        BigDecimal ema = initialSum.divide(java.math.BigDecimal.valueOf(initialCount), 2, java.math.RoundingMode.HALF_UP);
        emaMap.put(keys.get(0), ema);

        // 递推计算EMA
        for (int i = 1; i < keys.size(); i++) {
            BigDecimal currentValue = statsByPeriod.get(keys.get(i)).getTotalAmount();
            ema = currentValue.multiply(java.math.BigDecimal.valueOf(alpha))
                .add(ema.multiply(java.math.BigDecimal.valueOf(1 - alpha)))
                .setScale(2, java.math.RoundingMode.HALF_UP);
            emaMap.put(keys.get(i), ema);
        }
        return emaMap;
    }

    /**
     * 计算增长率
     */
    public static Map<String, BigDecimal> calculateGrowthRate(Map<String, RechargeStats> statsByPeriod) {
        TraceLogger.debug("AnalysisQueryService", "calculateGrowthRate", "计算增长率");

        List<String> keys = statsByPeriod.keySet().stream()
            .sorted()
            .collect(Collectors.toList());

        Map<String, BigDecimal> growthRateMap = new java.util.LinkedHashMap<>();
        growthRateMap.put(keys.get(0), BigDecimal.ZERO); // 第一个没有增长率

        for (int i = 1; i < keys.size(); i++) {
            BigDecimal prevAmount = statsByPeriod.get(keys.get(i - 1)).getTotalAmount();
            BigDecimal currentAmount = statsByPeriod.get(keys.get(i)).getTotalAmount();

            BigDecimal growthRate;
            if (prevAmount.compareTo(BigDecimal.ZERO) == 0) {
                growthRate = BigDecimal.ZERO;
            } else {
                growthRate = currentAmount.subtract(prevAmount)
                    .divide(prevAmount, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(java.math.BigDecimal.valueOf(100))
                    .setScale(2, java.math.RoundingMode.HALF_UP);
            }
            growthRateMap.put(keys.get(i), growthRate);
        }
        return growthRateMap;
    }

    /**
     * 观众消费统计DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class AudienceConsumptionStats {
        private Long audienceId;
        private String audienceName;
        private BigDecimal totalAmount;
        private int totalCount;
        private int uniqueAnchors;
        private BigDecimal avgAmount;
        private BigDecimal maxAmount;
        private BigDecimal minAmount;
        private LocalDateTime lastRechargeTime;
        private LocalDateTime firstRechargeTime;
    }
}

