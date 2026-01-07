package com.liveroom.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 排行榜数据项DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankingItemDTO {
    
    /** 排名 */
    private Integer rank;
    
    /** 用户ID（主播或观众） */
    private Long userId;
    
    /** 用户名称 */
    private String userName;
    
    /** 数值（收入或消费金额） */
    private BigDecimal value;
    
    /** 次数（打赏次数） */
    private Integer count;
    
    /** 额外信息 */
    private String extra;
}
