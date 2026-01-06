package com.liveroom.finance.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * TOP观众VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopAudienceVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主播ID
     */
    private Long anchorId;
    
    /**
     * 观众ID
     */
    private Long audienceId;
    
    /**
     * 观众名称
     */
    private String audienceName;
    
    /**
     * 总打赏金额
     */
    private BigDecimal totalAmount;
    
    /**
     * 打赏次数
     */
    private Long rechargeCount;
    
    /**
     * 排名
     */
    private Integer rank;
}
