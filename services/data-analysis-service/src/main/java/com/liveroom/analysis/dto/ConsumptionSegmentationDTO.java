package com.liveroom.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 消费分层数据DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsumptionSegmentationDTO {
    
    /** 高消费用户数 */
    private Integer highLevelCount;
    
    /** 中消费用户数 */
    private Integer mediumLevelCount;
    
    /** 低消费用户数 */
    private Integer lowLevelCount;
    
    /** 高消费总金额 */
    private BigDecimal highLevelAmount;
    
    /** 中消费总金额 */
    private BigDecimal mediumLevelAmount;
    
    /** 低消费总金额 */
    private BigDecimal lowLevelAmount;
    
    /** 各层级占比 */
    private Map<String, BigDecimal> percentages;
}
