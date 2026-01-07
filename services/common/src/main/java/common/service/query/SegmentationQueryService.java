package common.service.query;

import common.logger.TraceLogger;
import common.service.AnalysisQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 分段分层查询Service
 *
 * 专门用于处理数据的分段和分层分析
 * 包括：
 * - 按消费金额分层（高中低消费）
 * - 按消费频次分段
 * - RFM模型分析
 *
 * @author Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SegmentationQueryService {

    private final AnalysisQueryService analysisQueryService;

    /**
     * 获取观众消费分层数据
     * 将观众分为高消费、中消费、低消费三层
     */
    @Transactional(readOnly = true)
    public ConsumptionSegmentation getConsumptionSegmentation(LocalDateTime startTime, LocalDateTime endTime) {
        TraceLogger.info("SegmentationQueryService", "getConsumptionSegmentation",
            String.format("获取消费分层数据: %s - %s", startTime, endTime));

        List<common.bean.Recharge> recharges = analysisQueryService.getRechargesByTimeRange(startTime, endTime);

        // 按观众分组统计
        Map<Long, BigDecimal> audienceConsumption = recharges.stream()
            .collect(Collectors.groupingBy(
                common.bean.Recharge::getAudienceId,
                Collectors.reducing(
                    BigDecimal.ZERO,
                    common.bean.Recharge::getRechargeAmount,
                    BigDecimal::add
                )
            ));

        if (audienceConsumption.isEmpty()) {
            return ConsumptionSegmentation.builder()
                .highConsumers(Collections.emptyList())
                .mediumConsumers(Collections.emptyList())
                .lowConsumers(Collections.emptyList())
                .build();
        }

        // 计算分位数
        List<BigDecimal> amounts = audienceConsumption.values().stream()
            .sorted()
            .collect(Collectors.toList());

        BigDecimal p80 = amounts.get((int) (amounts.size() * 0.8));
        BigDecimal p20 = amounts.get((int) (amounts.size() * 0.2));

        // 分段
        List<ConsumptionSegmentation.ConsumerInfo> highConsumers = new ArrayList<>();
        List<ConsumptionSegmentation.ConsumerInfo> mediumConsumers = new ArrayList<>();
        List<ConsumptionSegmentation.ConsumerInfo> lowConsumers = new ArrayList<>();

        for (Map.Entry<Long, BigDecimal> entry : audienceConsumption.entrySet()) {
            BigDecimal amount = entry.getValue();
            List<common.bean.Recharge> consumerRecharges = recharges.stream()
                .filter(r -> r.getAudienceId().equals(entry.getKey()))
                .collect(Collectors.toList());

            ConsumptionSegmentation.ConsumerInfo info = ConsumptionSegmentation.ConsumerInfo.builder()
                .audienceId(entry.getKey())
                .audienceName(consumerRecharges.get(0).getAudienceNickname())
                .totalAmount(amount)
                .transactionCount(consumerRecharges.size())
                .build();

            if (amount.compareTo(p80) >= 0) {
                highConsumers.add(info);
            } else if (amount.compareTo(p20) >= 0) {
                mediumConsumers.add(info);
            } else {
                lowConsumers.add(info);
            }
        }

        return ConsumptionSegmentation.builder()
            .highConsumers(highConsumers)
            .mediumConsumers(mediumConsumers)
            .lowConsumers(lowConsumers)
            .highConsumerCount(highConsumers.size())
            .mediumConsumerCount(mediumConsumers.size())
            .lowConsumerCount(lowConsumers.size())
            .periodStart(startTime)
            .periodEnd(endTime)
            .build();
    }

    /**
     * 消费分层结果DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class ConsumptionSegmentation {
        private List<ConsumerInfo> highConsumers; // 前20%
        private List<ConsumerInfo> mediumConsumers; // 中间60%
        private List<ConsumerInfo> lowConsumers; // 后20%
        private int highConsumerCount;
        private int mediumConsumerCount;
        private int lowConsumerCount;
        private LocalDateTime periodStart;
        private LocalDateTime periodEnd;

        /**
         * 消费者信息
         */
        @lombok.Data
        @lombok.Builder
        public static class ConsumerInfo {
            private Long audienceId;
            private String audienceName;
            private BigDecimal totalAmount;
            private int transactionCount;
        }
    }
}

