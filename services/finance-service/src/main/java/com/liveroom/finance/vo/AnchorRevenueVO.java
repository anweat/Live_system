package com.liveroom.finance.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 主播收入统计VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnchorRevenueVO implements Serializable {
    
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
     * 总收入金额
     */
    private BigDecimal totalAmount;
    
    /**
     * 打赏次数
     */
    private Long totalCount;
    
    /**
     * 排名（可选）
     */
    private Integer rank;
    
    /**
     * 统计开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 统计结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 查询时间
     */
    private LocalDateTime queryTime;
}
