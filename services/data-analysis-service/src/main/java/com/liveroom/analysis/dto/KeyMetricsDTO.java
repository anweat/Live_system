package com.liveroom.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 关键指标DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeyMetricsDTO {
    
    /** GMV（总流水） */
    private BigDecimal gmv;
    
    /** 付费用户数 */
    private Integer payingUserCount;
    
    /** 总用户数 */
    private Integer totalUserCount;
    
    /** 付费率 */
    private BigDecimal paymentRate;
    
    /** ARPU（平均每用户收入） */
    private BigDecimal arpu;
    
    /** ARPPU（平均每付费用户收入） */
    private BigDecimal arppu;
    
    /** 平均单笔金额 */
    private BigDecimal avgOrderAmount;
    
    /** 总订单数 */
    private Integer totalOrders;
}
