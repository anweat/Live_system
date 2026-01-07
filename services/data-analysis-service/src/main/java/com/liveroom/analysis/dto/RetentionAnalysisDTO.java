package com.liveroom.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 用户留存分析DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetentionAnalysisDTO {
    
    /** 次日留存率 */
    private BigDecimal day1Retention;
    
    /** 7日留存率 */
    private BigDecimal day7Retention;
    
    /** 30日留存率 */
    private BigDecimal day30Retention;
    
    /** 留存曲线数据 */
    private Map<Integer, BigDecimal> retentionCurve;
    
    /** 流失预警用户数 */
    private Integer highRiskUsers;
    
    /** 中风险用户数 */
    private Integer mediumRiskUsers;
    
    /** 低风险用户数 */
    private Integer lowRiskUsers;
    
    /** 流失概率分布 */
    private Map<String, Integer> churnProbabilityDistribution;
    
    /** 高风险用户列表 */
    private List<Long> highRiskUserIds;
    
    /** 留存分析时间范围 */
    private LocalDateTime analysisStartTime;
    
    /** 留存分析时间范围 */
    private LocalDateTime analysisEndTime;
    
    /** 新增用户数 */
    private Integer newUsersCount;
    
    /** 活跃用户数 */
    private Integer activeUsersCount;
}
