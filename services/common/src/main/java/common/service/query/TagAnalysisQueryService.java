package common.service.query;

import common.logger.TraceLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 标签分析查询Service
 *
 * 专门用于处理标签相关的数据分析
 * 包括：
 * - 标签关联度计算（Jaccard相似度）
 * - 标签共现频率分析
 * - 标签热力图矩阵生成
 * - 标签推荐
 *
 * @author Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TagAnalysisQueryService {

    /**
     * 计算两个标签的Jaccard相似度
     *
     * Jaccard(A, B) = |A ∩ B| / |A ∪ B|
     *
     * @param tagAUsers 拥有标签A的用户集合
     * @param tagBUsers 拥有标签B的用户集合
     * @return Jaccard相似度 (0-1之间)
     */
    public BigDecimal calculateJaccardSimilarity(Set<Long> tagAUsers, Set<Long> tagBUsers) {
        if (tagAUsers.isEmpty() && tagBUsers.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // 交集
        Set<Long> intersection = new HashSet<>(tagAUsers);
        intersection.retainAll(tagBUsers);

        // 并集
        Set<Long> union = new HashSet<>(tagAUsers);
        union.addAll(tagBUsers);

        if (union.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(intersection.size())
            .divide(BigDecimal.valueOf(union.size()), 4, RoundingMode.HALF_UP);
    }

    /**
     * 计算标签共现频率
     *
     * 共现频率 = 同时出现次数 / MIN(标签A总次数, 标签B总次数)
     *
     * @param cooccurrenceCount 标签A和B同时出现的次数
     * @param tagACount 标签A的总次数
     * @param tagBCount 标签B的总次数
     * @return 共现频率 (0-1之间)
     */
    public BigDecimal calculateCooccurrenceRate(int cooccurrenceCount, int tagACount, int tagBCount) {
        int minCount = Math.min(tagACount, tagBCount);
        if (minCount == 0) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(cooccurrenceCount)
            .divide(BigDecimal.valueOf(minCount), 4, RoundingMode.HALF_UP);
    }

    /**
     * 评估标签关联强度等级
     *
     * @param jaccardScore Jaccard相似度
     * @param cooccurrenceRate 共现频率
     * @return 关联强度等级: 3=强关联, 2=中关联, 1=弱关联, 0=无关联
     */
    public int evaluateRelationStrength(BigDecimal jaccardScore, BigDecimal cooccurrenceRate) {
        // 强关联: Jaccard >= 0.5 或 共现频率 >= 0.6
        if (jaccardScore.compareTo(BigDecimal.valueOf(0.5)) >= 0 ||
            cooccurrenceRate.compareTo(BigDecimal.valueOf(0.6)) >= 0) {
            return 3;
        }

        // 中关联: 0.3 <= Jaccard < 0.5 或 0.4 <= 共现频率 < 0.6
        if (jaccardScore.compareTo(BigDecimal.valueOf(0.3)) >= 0 ||
            cooccurrenceRate.compareTo(BigDecimal.valueOf(0.4)) >= 0) {
            return 2;
        }

        // 弱关联: Jaccard < 0.3 或 共现频率 < 0.4
        if (jaccardScore.compareTo(BigDecimal.valueOf(0.1)) >= 0 ||
            cooccurrenceRate.compareTo(BigDecimal.valueOf(0.1)) >= 0) {
            return 1;
        }

        return 0; // 无关联
    }

    /**
     * 生成标签热力图矩阵
     *
     * @param tagRelations 标签关联数据列表
     * @return 热力图矩阵数据
     */
    @Transactional(readOnly = true)
    public TagHeatmapMatrix generateHeatmapMatrix(List<TagRelation> tagRelations) {
        TraceLogger.info("TagAnalysisQueryService", "generateHeatmapMatrix",
            String.format("生成标签热力图矩阵，关联数据量: %d", tagRelations.size()));

        // 获取所有唯一标签
        Set<Long> tagIds = new HashSet<>();
        tagRelations.forEach(relation -> {
            tagIds.add(relation.getTagId1());
            tagIds.add(relation.getTagId2());
        });

        List<Long> sortedTagIds = new ArrayList<>(tagIds);
        Collections.sort(sortedTagIds);

        int size = sortedTagIds.size();
        BigDecimal[][] matrix = new BigDecimal[size][size];

        // 初始化矩阵
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i == j) {
                    matrix[i][j] = BigDecimal.valueOf(100); // 自身完全关联
                } else {
                    matrix[i][j] = BigDecimal.ZERO;
                }
            }
        }

        // 填充关联度数据
        Map<Long, Integer> tagIndexMap = new HashMap<>();
        for (int i = 0; i < sortedTagIds.size(); i++) {
            tagIndexMap.put(sortedTagIds.get(i), i);
        }

        tagRelations.forEach(relation -> {
            Integer index1 = tagIndexMap.get(relation.getTagId1());
            Integer index2 = tagIndexMap.get(relation.getTagId2());

            if (index1 != null && index2 != null) {
                BigDecimal score = relation.getRelationScore();
                matrix[index1][index2] = score;
                matrix[index2][index1] = score; // 对称矩阵
            }
        });

        return TagHeatmapMatrix.builder()
            .tagIds(sortedTagIds)
            .matrix(matrix)
            .size(size)
            .build();
    }

    /**
     * 获取与指定标签最相关的标签列表
     *
     * @param tagId 标签ID
     * @param tagRelations 所有标签关联数据
     * @param limit 返回数量
     * @return 相关标签列表
     */
    public List<RelatedTag> getRelatedTags(Long tagId, List<TagRelation> tagRelations, int limit) {
        TraceLogger.info("TagAnalysisQueryService", "getRelatedTags",
            String.format("获取标签%d的相关标签TOP%d", tagId, limit));

        return tagRelations.stream()
            .filter(relation -> relation.getTagId1().equals(tagId) || relation.getTagId2().equals(tagId))
            .map(relation -> {
                Long relatedTagId = relation.getTagId1().equals(tagId) ?
                    relation.getTagId2() : relation.getTagId1();

                return RelatedTag.builder()
                    .tagId(relatedTagId)
                    .relationScore(relation.getRelationScore())
                    .cooccurrenceCount(relation.getCooccurrenceCount())
                    .strengthLevel(relation.getStrengthLevel())
                    .build();
            })
            .sorted((a, b) -> b.getRelationScore().compareTo(a.getRelationScore()))
            .limit(limit)
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 标签关联数据DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TagRelation {
        private Long tagId1;
        private String tagName1;
        private Long tagId2;
        private String tagName2;
        private BigDecimal relationScore; // 关联度评分 (0-100)
        private Integer cooccurrenceCount; // 共现次数
        private Integer strengthLevel; // 关联强度等级 (0-3)
    }

    /**
     * 标签热力图矩阵DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class TagHeatmapMatrix {
        private List<Long> tagIds; // 标签ID列表
        private BigDecimal[][] matrix; // 关联度矩阵
        private Integer size; // 矩阵大小
        private Map<Long, String> tagNameMap; // 标签ID到名称的映射
    }

    /**
     * 相关标签DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class RelatedTag {
        private Long tagId;
        private String tagName;
        private BigDecimal relationScore;
        private Integer cooccurrenceCount;
        private Integer strengthLevel;
        private String strengthDesc; // 关联强度描述
    }
}
