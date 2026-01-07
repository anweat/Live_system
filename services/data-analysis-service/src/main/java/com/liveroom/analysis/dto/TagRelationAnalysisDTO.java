package com.liveroom.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 标签关联分析DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagRelationAnalysisDTO {
    
    /** 标签ID1 */
    private Long tagId1;
    
    /** 标签名称1 */
    private String tagName1;
    
    /** 标签ID2 */
    private Long tagId2;
    
    /** 标签名称2 */
    private String tagName2;
    
    /** Jaccard相似度 */
    private BigDecimal jaccardSimilarity;
    
    /** 共现频率 */
    private BigDecimal cooccurrenceFrequency;
    
    /** 关联强度等级 (强关联/中关联/弱关联) */
    private String strengthLevel;
    
    /** 共现次数 */
    private Integer cooccurrenceCount;
    
    /** 标签1总出现次数 */
    private Integer tag1TotalCount;
    
    /** 标签2总出现次数 */
    private Integer tag2TotalCount;
    
    /** 交集用户数 */
    private Integer intersectionCount;
    
    /** 并集用户数 */
    private Integer unionCount;
}
