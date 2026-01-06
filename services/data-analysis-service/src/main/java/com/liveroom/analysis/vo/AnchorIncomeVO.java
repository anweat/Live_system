package com.liveroom.analysis.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 主播收入分析VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnchorIncomeVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主播ID
     */
    private Long anchorId;

    /**
     * 主播名称
     */
    private String anchorName;

    /**
     * 时间维度数据列表
     */
    private List<TimeSeriesData> timeSeries;

    /**
     * 总收入
     */
    private BigDecimal totalIncome;

    /**
     * 打赏总次数
     */
    private Integer totalRechargeCount;

    /**
     * 唯一付费用户数
     */
    private Integer uniquePayerCount;

    /**
     * 平均单笔金额
     */
    private BigDecimal avgAmount;

    /**
     * 最大单笔金额
     */
    private BigDecimal maxAmount;

    /**
     * 收入稳定性（变异系数CV）
     */
    private BigDecimal stabilityScore;

    /**
     * 收入趋势（up/down/stable）
     */
    private String trend;

    /**
     * 环比增长率
     */
    private BigDecimal growthRate;

    /**
     * 时间序列数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSeriesData implements Serializable {
        /**
         * 时间标签（日期/小时）
         */
        private String timeLabel;

        /**
         * 收入金额
         */
        private BigDecimal income;

        /**
         * 打赏次数
         */
        private Integer rechargeCount;

        /**
         * 付费用户数
         */
        private Integer payerCount;

        /**
         * 7日移动平均
         */
        private BigDecimal ma7;

        /**
         * 30日移动平均
         */
        private BigDecimal ma30;
    }
}
