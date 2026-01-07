package com.liveroom.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户画像DTO（基于RFM模型）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPortraitDTO {
    
    /** 用户ID */
    private Long userId;
    
    /** 用户名称 */
    private String userName;
    
    /** R - 最近消费（天数） */
    private Integer recencyDays;
    
    /** R得分 (1-5) */
    private Integer recencyScore;
    
    /** F - 消费频次 */
    private Integer frequency;
    
    /** F得分 (1-5) */
    private Integer frequencyScore;
    
    /** M - 消费金额 */
    private BigDecimal monetary;
    
    /** M得分 (1-5) */
    private Integer monetaryScore;
    
    /** RFM综合得分 */
    private BigDecimal rfmScore;
    
    /** 用户等级 (高价值/中价值/低价值) */
    private String userLevel;
    
    /** 消费分层 (0=低消费, 1=中消费, 2=高消费) */
    private Integer consumptionLevel;
    
    /** 活跃度评分 (0-100) */
    private BigDecimal activityScore;
    
    /** 预测LTV */
    private BigDecimal predictedLTV;
    
    /** 最后消费时间 */
    private LocalDateTime lastRechargeTime;
    
    /** 首次消费时间 */
    private LocalDateTime firstRechargeTime;
    
    /** 累计消费金额 */
    private BigDecimal totalAmount;
    
    /** 平均单笔金额 */
    private BigDecimal avgAmount;
    
    /** 消费的主播数量 */
    private Integer anchorCount;
}
