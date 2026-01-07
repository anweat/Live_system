package common.service.query;

import common.bean.Recharge;
import common.logger.TraceLogger;
import common.service.AnalysisQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 热力图分析查询Service
 *
 * 专门用于处理热力图相关的数据分析
 * 包括：
 * - 时段热力图（一周×24小时）
 * - 活跃度热力图
 * - 消费热力图
 *
 * @author Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HeatmapAnalysisQueryService {

    private final AnalysisQueryService analysisQueryService;

    /**
     * 生成时段热力图数据（一周内每小时的活跃度）
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 时段热力图数据
     */
    @Transactional(readOnly = true)
    public TimeHeatmapData generateTimeHeatmap(LocalDateTime startTime, LocalDateTime endTime) {
        TraceLogger.info("HeatmapAnalysisQueryService", "generateTimeHeatmap",
            String.format("生成时段热力图: %s - %s", startTime, endTime));

        List<Recharge> recharges = analysisQueryService.getRechargesByTimeRange(startTime, endTime);

        // 初始化7天×24小时的矩阵
        int[][] rechargeCountMatrix = new int[7][24];
        BigDecimal[][] rechargeAmountMatrix = new BigDecimal[7][24];

        // 初始化金额矩阵
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 24; j++) {
                rechargeAmountMatrix[i][j] = BigDecimal.ZERO;
            }
        }

        // 统计数据
        for (Recharge recharge : recharges) {
            LocalDateTime rechargeTime = recharge.getRechargeTime();
            int dayOfWeek = rechargeTime.getDayOfWeek().getValue() - 1; // 0=周一, 6=周日
            int hour = recharge.getRechargeTime().getHour();

            rechargeCountMatrix[dayOfWeek][hour]++;
            rechargeAmountMatrix[dayOfWeek][hour] = 
                rechargeAmountMatrix[dayOfWeek][hour].add(recharge.getRechargeAmount());
        }

        return TimeHeatmapData.builder()
            .rechargeCountMatrix(rechargeCountMatrix)
            .rechargeAmountMatrix(rechargeAmountMatrix)
            .dayLabels(Arrays.asList("周一", "周二", "周三", "周四", "周五", "周六", "周日"))
            .hourLabels(generateHourLabels())
            .build();
    }

    /**
     * 识别高峰时段
     *
     * @param heatmapData 热力图数据
     * @param topN 返回前N个高峰时段
     * @return 高峰时段列表
     */
    public List<PeakTimeSlot> identifyPeakTimeSlots(TimeHeatmapData heatmapData, int topN) {
        TraceLogger.info("HeatmapAnalysisQueryService", "identifyPeakTimeSlots",
            String.format("识别TOP%d高峰时段", topN));

        List<PeakTimeSlot> timeSlots = new ArrayList<>();
        int[][] countMatrix = heatmapData.getRechargeCountMatrix();
        BigDecimal[][] amountMatrix = heatmapData.getRechargeAmountMatrix();

        for (int day = 0; day < 7; day++) {
            for (int hour = 0; hour < 24; hour++) {
                timeSlots.add(PeakTimeSlot.builder()
                    .dayOfWeek(day + 1) // 1=周一, 7=周日
                    .hour(hour)
                    .dayLabel(heatmapData.getDayLabels().get(day))
                    .hourLabel(String.format("%02d:00", hour))
                    .rechargeCount(countMatrix[day][hour])
                    .rechargeAmount(amountMatrix[day][hour])
                    .build());
            }
        }

        // 按交易次数排序
        return timeSlots.stream()
            .sorted((a, b) -> Integer.compare(b.getRechargeCount(), a.getRechargeCount()))
            .limit(topN)
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 计算时段活跃度评分
     *
     * @param rechargeCount 打赏次数
     * @param rechargeAmount 打赏金额
     * @return 活跃度评分(0-100)
     */
    public BigDecimal calculateTimeSlotActivityScore(int rechargeCount, BigDecimal rechargeAmount) {
        // 归一化计算（实际应用中应根据历史数据动态计算）
        double countScore = Math.min(rechargeCount / 100.0, 1.0) * 50; // 次数最高50分
        double amountScore = Math.min(rechargeAmount.doubleValue() / 10000.0, 1.0) * 50; // 金额最高50分

        return BigDecimal.valueOf(countScore + amountScore)
            .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * 分析最佳直播时段
     *
     * @param heatmapData 热力图数据
     * @return 最佳直播时段建议
     */
    public BestTimeSlotRecommendation recommendBestTimeSlots(TimeHeatmapData heatmapData) {
        TraceLogger.info("HeatmapAnalysisQueryService", "recommendBestTimeSlots",
            "分析最佳直播时段");

        List<PeakTimeSlot> peakSlots = identifyPeakTimeSlots(heatmapData, 3);

        // 计算工作日和周末的平均活跃度
        BigDecimal weekdayAvgActivity = calculateAvgActivityForWeekdays(heatmapData);
        BigDecimal weekendAvgActivity = calculateAvgActivityForWeekends(heatmapData);

        return BestTimeSlotRecommendation.builder()
            .topPeakTimeSlots(peakSlots)
            .weekdayAvgActivity(weekdayAvgActivity)
            .weekendAvgActivity(weekendAvgActivity)
            .recommendation(generateRecommendationText(peakSlots))
            .build();
    }

    /**
     * 计算工作日平均活跃度
     */
    private BigDecimal calculateAvgActivityForWeekdays(TimeHeatmapData heatmapData) {
        int totalCount = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (int day = 0; day < 5; day++) { // 周一到周五
            for (int hour = 0; hour < 24; hour++) {
                totalCount += heatmapData.getRechargeCountMatrix()[day][hour];
                totalAmount = totalAmount.add(heatmapData.getRechargeAmountMatrix()[day][hour]);
            }
        }

        return calculateTimeSlotActivityScore(totalCount / (5 * 24), 
            totalAmount.divide(BigDecimal.valueOf(5 * 24), 2, java.math.RoundingMode.HALF_UP));
    }

    /**
     * 计算周末平均活跃度
     */
    private BigDecimal calculateAvgActivityForWeekends(TimeHeatmapData heatmapData) {
        int totalCount = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (int day = 5; day < 7; day++) { // 周六和周日
            for (int hour = 0; hour < 24; hour++) {
                totalCount += heatmapData.getRechargeCountMatrix()[day][hour];
                totalAmount = totalAmount.add(heatmapData.getRechargeAmountMatrix()[day][hour]);
            }
        }

        return calculateTimeSlotActivityScore(totalCount / (2 * 24),
            totalAmount.divide(BigDecimal.valueOf(2 * 24), 2, java.math.RoundingMode.HALF_UP));
    }

    /**
     * 生成推荐文本
     */
    private String generateRecommendationText(List<PeakTimeSlot> peakSlots) {
        if (peakSlots.isEmpty()) {
            return "暂无足够数据提供推荐";
        }

        PeakTimeSlot topSlot = peakSlots.get(0);
        return String.format("建议在%s %s时段直播，该时段活跃度最高（%d次打赏，%.2f元）",
            topSlot.getDayLabel(),
            topSlot.getHourLabel(),
            topSlot.getRechargeCount(),
            topSlot.getRechargeAmount());
    }

    /**
     * 生成小时标签
     */
    private List<String> generateHourLabels() {
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            labels.add(String.format("%02d:00", i));
        }
        return labels;
    }

    /**
     * 时段热力图数据DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class TimeHeatmapData {
        private int[][] rechargeCountMatrix; // 打赏次数矩阵 [7天][24小时]
        private BigDecimal[][] rechargeAmountMatrix; // 打赏金额矩阵 [7天][24小时]
        private List<String> dayLabels; // 星期标签
        private List<String> hourLabels; // 小时标签
    }

    /**
     * 高峰时段DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class PeakTimeSlot {
        private Integer dayOfWeek; // 星期(1-7)
        private Integer hour; // 小时(0-23)
        private String dayLabel; // 星期标签
        private String hourLabel; // 小时标签
        private Integer rechargeCount; // 打赏次数
        private BigDecimal rechargeAmount; // 打赏金额
        private BigDecimal activityScore; // 活跃度评分
    }

    /**
     * 最佳时段推荐DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class BestTimeSlotRecommendation {
        private List<PeakTimeSlot> topPeakTimeSlots; // TOP高峰时段
        private BigDecimal weekdayAvgActivity; // 工作日平均活跃度
        private BigDecimal weekendAvgActivity; // 周末平均活跃度
        private String recommendation; // 推荐建议文本
    }
}
