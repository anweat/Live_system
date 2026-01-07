package com.liveroom.analysis.service;

import com.liveroom.analysis.dto.TagRelationAnalysisDTO;
import common.exception.AnalysisException;
import common.logger.TraceLogger;
import common.service.DataAccessFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 标签关联分析Service
 * 基于Jaccard相似度计算标签关联度
 */
@Service
@RequiredArgsConstructor
public class TagRelationAnalysisService {

    private final DataAccessFacade dataAccessFacade;

    /**
     * 计算两个标签的关联度
     */
    @Cacheable(value = "analysis:tag-relation", key = "'relation:' + #tagId1 + ':' + #tagId2")
    public TagRelationAnalysisDTO getTagRelation(Long tagId1, Long tagId2) {
        try {
            TraceLogger.info("TagRelationAnalysisService", "getTagRelation", 
                null, "tagId1", tagId1, "tagId2", tagId2);

            // 这里需要获取标签相关的用户数据，由于common模块中没有直接的标签关联表，
            // 我们将使用anchor_tag和audience_tag表的数据进行关联分析
            // 模拟获取标签相关的用户集合
            Set<Long> usersWithTag1 = getTaggedUsers(tagId1);
            Set<Long> usersWithTag2 = getTaggedUsers(tagId2);

            if (usersWithTag1.isEmpty() || usersWithTag2.isEmpty()) {
                return buildEmptyRelation(tagId1, tagId2);
            }

            // 计算交集和并集
            Set<Long> intersection = new HashSet<>(usersWithTag1);
            intersection.retainAll(usersWithTag2);

            Set<Long> union = new HashSet<>(usersWithTag1);
            union.addAll(usersWithTag2);

            // 计算Jaccard相似度
            BigDecimal jaccardSimilarity = calculateJaccardSimilarity(
                usersWithTag1.size(), usersWithTag2.size(), intersection.size());

            // 计算共现频率
            BigDecimal cooccurrenceFrequency = calculateCooccurrenceFrequency(
                intersection.size(), Math.min(usersWithTag1.size(), usersWithTag2.size()));

            // 确定关联强度等级
            String strengthLevel = determineStrengthLevel(jaccardSimilarity, cooccurrenceFrequency);

            // 获取标签名称（这里简化处理）
            String tagName1 = "Tag" + tagId1;
            String tagName2 = "Tag" + tagId2;

            return TagRelationAnalysisDTO.builder()
                .tagId1(tagId1)
                .tagName1(tagName1)
                .tagId2(tagId2)
                .tagName2(tagName2)
                .jaccardSimilarity(jaccardSimilarity)
                .cooccurrenceFrequency(cooccurrenceFrequency)
                .strengthLevel(strengthLevel)
                .cooccurrenceCount(intersection.size())
                .tag1TotalCount(usersWithTag1.size())
                .tag2TotalCount(usersWithTag2.size())
                .intersectionCount(intersection.size())
                .unionCount(union.size())
                .build();

        } catch (Exception e) {
            TraceLogger.error("TagRelationAnalysisService", "getTagRelation", 
                null, e, "tagId1", tagId1, "tagId2", tagId2);
            throw new AnalysisException(5060, "获取标签关联度失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取与指定标签关联的用户集合
     * 这里简化实现，实际应查询anchor_tag和audience_tag表
     */
    private Set<Long> getTaggedUsers(Long tagId) {
        // 模拟数据 - 实际应查询数据库
        Set<Long> users = new HashSet<>();
        
        // 假设有一些用户与标签关联
        for (int i = 1; i <= 10; i++) {
            if (i % 2 == 0) { // 模拟标签关联
                users.add(tagId * 100L + i);
            }
        }
        
        return users;
    }

    /**
     * 计算Jaccard相似度
     * Jaccard(A, B) = |A ∩ B| / |A ∪ B|
     */
    private BigDecimal calculateJaccardSimilarity(int setSizeA, int setSizeB, int intersectionSize) {
        if (setSizeA + setSizeB - intersectionSize == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal numerator = BigDecimal.valueOf(intersectionSize);
        BigDecimal denominator = BigDecimal.valueOf(setSizeA + setSizeB - intersectionSize);
        
        return numerator.divide(denominator, 4, RoundingMode.HALF_UP)
            .multiply(new BigDecimal("100")); // 转换为百分比
    }

    /**
     * 计算共现频率
     * 共现频率 = 同时出现次数 / MIN(标签A总次数, 标签B总次数)
     */
    private BigDecimal calculateCooccurrenceFrequency(int intersectionSize, int minTagSize) {
        if (minTagSize == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal numerator = BigDecimal.valueOf(intersectionSize);
        BigDecimal denominator = BigDecimal.valueOf(minTagSize);
        
        return numerator.divide(denominator, 4, RoundingMode.HALF_UP)
            .multiply(new BigDecimal("100")); // 转换为百分比
    }

    /**
     * 确定关联强度等级
     * 强关联: Jaccard >= 0.5 或 共现频率 >= 0.6
     * 中关联: 0.3 <= Jaccard < 0.5 或 0.4 <= 共现频率 < 0.6
     * 弱关联: Jaccard < 0.3 或 共现频率 < 0.4
     */
    private String determineStrengthLevel(BigDecimal jaccard, BigDecimal frequency) {
        double jaccardValue = jaccard.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP).doubleValue();
        double frequencyValue = frequency.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP).doubleValue();

        if (jaccardValue >= 0.5 || frequencyValue >= 0.6) {
            return "强关联";
        } else if (jaccardValue >= 0.3 || frequencyValue >= 0.4) {
            return "中关联";
        } else {
            return "弱关联";
        }
    }

    /**
     * 构建空关联分析
     */
    private TagRelationAnalysisDTO buildEmptyRelation(Long tagId1, Long tagId2) {
        return TagRelationAnalysisDTO.builder()
            .tagId1(tagId1)
            .tagName1("Tag" + tagId1)
            .tagId2(tagId2)
            .tagName2("Tag" + tagId2)
            .jaccardSimilarity(BigDecimal.ZERO)
            .cooccurrenceFrequency(BigDecimal.ZERO)
            .strengthLevel("无关联")
            .cooccurrenceCount(0)
            .tag1TotalCount(0)
            .tag2TotalCount(0)
            .intersectionCount(0)
            .unionCount(0)
            .build();
    }

    /**
     * 获取指定标签的TOP关联标签
     */
    @Cacheable(value = "analysis:tag-relation", key = "'top-relation:' + #tagId + ':' + #limit")
    public List<TagRelationAnalysisDTO> getTopRelatedTags(Long tagId, int limit) {
        try {
            TraceLogger.info("TagRelationAnalysisService", "getTopRelatedTags", 
                tagId, "limit", limit);

            // 模拟获取TOP关联标签，实际应查询数据库
            List<TagRelationAnalysisDTO> results = new ArrayList<>();
            
            for (int i = 1; i <= limit; i++) {
                if (i != tagId.intValue()) { // 排除自己
                    Long relatedTagId = (long) i;
                    results.add(getTagRelation(tagId, relatedTagId));
                }
            }

            // 按Jaccard相似度排序
            results.sort((a, b) -> b.getJaccardSimilarity().compareTo(a.getJaccardSimilarity()));

            return results;

        } catch (Exception e) {
            TraceLogger.error("TagRelationAnalysisService", "getTopRelatedTags", tagId, e);
            throw new AnalysisException(5061, "获取TOP关联标签失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取标签关联热力图矩阵
     */
    @Cacheable(value = "analysis:tag-relation", key = "'heatmap:' + #tagIds")
    public Map<String, Map<String, BigDecimal>> getTagRelationHeatmap(List<Long> tagIds) {
        try {
            TraceLogger.info("TagRelationAnalysisService", "getTagRelationHeatmap", 
                null, "tagCount", tagIds.size());

            Map<String, Map<String, BigDecimal>> heatmap = new HashMap<>();

            for (Long tagId1 : tagIds) {
                Map<String, BigDecimal> row = new HashMap<>();
                for (Long tagId2 : tagIds) {
                    if (tagId1.equals(tagId2)) {
                        row.put("Tag" + tagId2, new BigDecimal("100")); // 自己与自己的关联度为100%
                    } else {
                        TagRelationAnalysisDTO relation = getTagRelation(tagId1, tagId2);
                        row.put("Tag" + tagId2, relation.getJaccardSimilarity());
                    }
                }
                heatmap.put("Tag" + tagId1, row);
            }

            return heatmap;

        } catch (Exception e) {
            TraceLogger.error("TagRelationAnalysisService", "getTagRelationHeatmap", null, e);
            throw new AnalysisException(5062, "获取标签关联热力图失败: " + e.getMessage(), e);
        }
    }
}
