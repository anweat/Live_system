package com.liveroom.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 主播收入分析DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnchorIncomeAnalysisDTO {
    
    /** 主播ID */
    private Long anchorId;
    
    /** 主播名称 */
    private String anchorName;
    
    /** 总收入 */
    private BigDecimal totalIncome;
    
    /** 平均日收入 */
    private BigDecimal avgDailyIncome;
    
    /** 最高日收入 */
    private BigDecimal maxDailyIncome;
    
    /** 最低日收入 */
    private BigDecimal minDailyIncome;
    
    /** 收入稳定性（变异系数CV） */
    private BigDecimal stabilityScore;
    
    /** 稳定性评级 (稳定/一般/波动大) */
    private String stabilityLevel;
    
    /** 收入增长率 */
    private BigDecimal growthRate;
    
    /** 打赏次数 */
    private Integer rechargeCount;
    
    /** 付费粉丝数 */
    private Integer uniquePayers;
    
    /** 人均粉丝价值 */
    private BigDecimal fanValue;
    
    /** 打赏转化率 */
    private BigDecimal conversionRate;
    
    /** 每日收入趋势 */
    private Map<String, BigDecimal> dailyTrend;
    
    /** 7日移动平均 */
    private Map<String, BigDecimal> ma7;
    
    /** 30日移动平均 */
    private Map<String, BigDecimal> ma30;
}
