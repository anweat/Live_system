package com.liveroom.finance.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 小时统计VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HourlyStatisticsVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主播ID
     */
    private Long anchorId;
    
    /**
     * 统计小时（0-23）
     */
    private Integer statisticsHour;
    
    /**
     * 打赏次数
     */
    private Long rechargeCount;
    
    /**
     * 总金额
     */
    private BigDecimal totalAmount;
}
