package com.liveroom.analysis.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 流水趋势分析VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashFlowTrendVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 时间序列数据
     */
    private List<TrendData> timeSeries;

    /**
     * 总GMV
     */
    private BigDecimal totalGmv;

    /**
     * 平台收入
     */
    private BigDecimal platformIncome;

    /**
     * 主播总收入
     */
    private BigDecimal anchorIncome;

    /**
     * 趋势分析
     */
    private TrendAnalysis trendAnalysis;

    /**
     * 趋势数据点
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendData implements Serializable {
        /**
         * 时间标签
         */
        private String timeLabel;

        /**
         * GMV
         */
        private BigDecimal gmv;

        /**
         * 交易笔数
         */
        private Integer transactionCount;

        /**
         * 付费用户数
         */
        private Integer payingUsers;

        /**
         * 移动平均值
         */
        private BigDecimal movingAverage;

        /**
         * 指数移动平均
         */
        private BigDecimal ema;

        /**
         * 环比增长率(%)
         */
        private BigDecimal growthRate;
    }

    /**
     * 趋势分析结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendAnalysis implements Serializable {
        /**
         * 趋势方向（up/down/stable）
         */
        private String direction;

        /**
         * 趋势强度
         */
        private BigDecimal strength;

        /**
         * 波动率
         */
        private BigDecimal volatility;

        /**
         * 平均增长率
         */
        private BigDecimal avgGrowthRate;

        /**
         * 预测描述
         */
        private String prediction;
    }
}
