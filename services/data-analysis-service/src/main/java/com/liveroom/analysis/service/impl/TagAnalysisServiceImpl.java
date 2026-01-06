package com.liveroom.analysis.service.impl;

import com.liveroom.analysis.service.TagAnalysisService;
import com.liveroom.analysis.vo.TagHeatmapVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 标签分析Service实现
 */
@Slf4j
@Service
public class TagAnalysisServiceImpl implements TagAnalysisService {

    @Autowired
    @Qualifier("db1JdbcTemplate")
    private JdbcTemplate db1JdbcTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_KEY_PREFIX = "analysis:tag:heatmap:";
    private static final long CACHE_TTL = 604800; // 7天

    @Override
    public TagHeatmapVO getTagHeatmap(Integer limit) {
        // 尝试从缓存获取
        String cacheKey = CACHE_KEY_PREFIX + "matrix:" + limit;
        TagHeatmapVO cached = (TagHeatmapVO) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.info("从缓存获取标签热力图");
            return cached;
        }

        // 获取热门标签
        List<TagHeatmapVO.TagDetail> tagDetails = getTopTags(limit);
        List<String> tags = tagDetails.stream()
                .map(TagHeatmapVO.TagDetail::getTagName)
                .collect(Collectors.toList());
        
        List<Long> tagIds = tagDetails.stream()
                .map(TagHeatmapVO.TagDetail::getTagId)
                .collect(Collectors.toList());

        // 构建用户-标签关系Map
        Map<Long, Set<Long>> userTagMap = buildUserTagMap(tagIds);

        // 计算标签关联度矩阵
        List<List<BigDecimal>> matrix = calculateTagRelationMatrix(tagIds, userTagMap);

        // 获取最强关联标签对
        List<TagHeatmapVO.TagRelation> topRelations = getTopRelations(tags, matrix, 10);

        TagHeatmapVO heatmap = TagHeatmapVO.builder()
                .tags(tags)
                .matrix(matrix)
                .tagDetails(tagDetails)
                .topRelations(topRelations)
                .build();

        // 缓存结果
        redisTemplate.opsForValue().set(cacheKey, heatmap, CACHE_TTL, TimeUnit.SECONDS);

        return heatmap;
    }

    @Override
    public Map<String, Object> getRelatedTags(String tagName, Integer limit) {
        // 查询标签ID
        String sql1 = "SELECT tag_id FROM tag WHERE tag_name = ?";
        Long tagId;
        try {
            tagId = db1JdbcTemplate.queryForObject(sql1, Long.class, tagName);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("tagName", tagName);
            result.put("relatedTags", Collections.emptyList());
            return result;
        }

        // 查询关联标签
        String sql2 = "SELECT " +
                "    t.tag_name, " +
                "    tr.relation_score, " +
                "    tr.cooccurrence_count, " +
                "    tr.strength_level " +
                "FROM tag_relation tr " +
                "INNER JOIN tag t ON (tr.tag_id2 = t.tag_id) " +
                "WHERE tr.tag_id1 = ? " +
                "ORDER BY tr.relation_score DESC " +
                "LIMIT ?";

        List<Map<String, Object>> relatedTags = db1JdbcTemplate.query(sql2, (rs, rowNum) -> {
            Map<String, Object> tag = new HashMap<>();
            tag.put("tagName", rs.getString("tag_name"));
            tag.put("relationScore", rs.getBigDecimal("relation_score"));
            tag.put("cooccurrenceCount", rs.getInt("cooccurrence_count"));
            tag.put("strengthLevel", rs.getInt("strength_level"));
            tag.put("strengthDesc", getStrengthDesc(rs.getInt("strength_level")));
            return tag;
        }, tagId, limit);

        Map<String, Object> result = new HashMap<>();
        result.put("tagName", tagName);
        result.put("tagId", tagId);
        result.put("relatedTags", relatedTags);

        return result;
    }

    @Override
    public Map<String, Object> getHotTags(Integer limit) {
        String cacheKey = CACHE_KEY_PREFIX + "hot:" + limit;
        @SuppressWarnings("unchecked")
        Map<String, Object> cached = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 统计各标签的使用次数
        String sql = "SELECT " +
                "    t.tag_id, " +
                "    t.tag_name, " +
                "    COUNT(DISTINCT at.anchor_id) as anchor_count, " +
                "    COUNT(DISTINCT aut.audience_id) as audience_count, " +
                "    (COUNT(DISTINCT at.anchor_id) + COUNT(DISTINCT aut.audience_id)) as total_count " +
                "FROM tag t " +
                "LEFT JOIN anchor_tag at ON t.tag_id = at.tag_id " +
                "LEFT JOIN audience_tag aut ON t.tag_id = aut.tag_id " +
                "GROUP BY t.tag_id, t.tag_name " +
                "ORDER BY total_count DESC " +
                "LIMIT ?";

        List<Map<String, Object>> hotTags = db1JdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> tag = new HashMap<>();
            tag.put("tagId", rs.getLong("tag_id"));
            tag.put("tagName", rs.getString("tag_name"));
            tag.put("anchorCount", rs.getInt("anchor_count"));
            tag.put("audienceCount", rs.getInt("audience_count"));
            tag.put("totalCount", rs.getInt("total_count"));
            
            // 计算热度得分
            int totalCount = rs.getInt("total_count");
            BigDecimal hotScore = BigDecimal.valueOf(Math.log(totalCount + 1) * 10)
                    .setScale(2, RoundingMode.HALF_UP);
            tag.put("hotScore", hotScore);
            
            return tag;
        }, limit);

        Map<String, Object> result = new HashMap<>();
        result.put("hotTags", hotTags);
        result.put("totalCount", hotTags.size());

        redisTemplate.opsForValue().set(cacheKey, result, 3600, TimeUnit.SECONDS);
        return result;
    }

    /**
     * 获取热门标签详情
     */
    private List<TagHeatmapVO.TagDetail> getTopTags(Integer limit) {
        String sql = "SELECT " +
                "    t.tag_id, " +
                "    t.tag_name, " +
                "    (SELECT COUNT(*) FROM anchor_tag WHERE tag_id = t.tag_id) + " +
                "    (SELECT COUNT(*) FROM audience_tag WHERE tag_id = t.tag_id) as use_count " +
                "FROM tag t " +
                "ORDER BY use_count DESC " +
                "LIMIT ?";

        return db1JdbcTemplate.query(sql, (rs, rowNum) -> {
            int useCount = rs.getInt("use_count");
            BigDecimal hotScore = BigDecimal.valueOf(Math.log(useCount + 1) * 10)
                    .setScale(2, RoundingMode.HALF_UP);

            return TagHeatmapVO.TagDetail.builder()
                    .tagId(rs.getLong("tag_id"))
                    .tagName(rs.getString("tag_name"))
                    .useCount(useCount)
                    .hotScore(hotScore)
                    .build();
        }, limit);
    }

    /**
     * 构建用户-标签关系Map
     */
    private Map<Long, Set<Long>> buildUserTagMap(List<Long> tagIds) {
        Map<Long, Set<Long>> userTagMap = new HashMap<>();

        // 查询主播标签
        String sql1 = "SELECT anchor_id as user_id, tag_id " +
                "FROM anchor_tag " +
                "WHERE tag_id IN (" + String.join(",", Collections.nCopies(tagIds.size(), "?")) + ")";

        db1JdbcTemplate.query(sql1, rs -> {
            Long userId = rs.getLong("user_id");
            Long tagId = rs.getLong("tag_id");
            userTagMap.computeIfAbsent(userId, k -> new HashSet<>()).add(tagId);
        }, tagIds.toArray());

        // 查询观众标签
        String sql2 = "SELECT audience_id as user_id, tag_id " +
                "FROM audience_tag " +
                "WHERE tag_id IN (" + String.join(",", Collections.nCopies(tagIds.size(), "?")) + ")";

        db1JdbcTemplate.query(sql2, rs -> {
            Long userId = rs.getLong("user_id");
            Long tagId = rs.getLong("tag_id");
            userTagMap.computeIfAbsent(userId, k -> new HashSet<>()).add(tagId);
        }, tagIds.toArray());

        return userTagMap;
    }

    /**
     * 计算标签关联度矩阵（Jaccard相似度）
     */
    private List<List<BigDecimal>> calculateTagRelationMatrix(
            List<Long> tagIds, Map<Long, Set<Long>> userTagMap) {
        
        int n = tagIds.size();
        List<List<BigDecimal>> matrix = new ArrayList<>();

        // 统计每个标签的用户集合
        Map<Long, Set<Long>> tagUserMap = new HashMap<>();
        for (Long tagId : tagIds) {
            tagUserMap.put(tagId, new HashSet<>());
        }

        for (Map.Entry<Long, Set<Long>> entry : userTagMap.entrySet()) {
            Long userId = entry.getKey();
            Set<Long> tags = entry.getValue();
            for (Long tagId : tags) {
                if (tagUserMap.containsKey(tagId)) {
                    tagUserMap.get(tagId).add(userId);
                }
            }
        }

        // 计算Jaccard相似度矩阵
        for (int i = 0; i < n; i++) {
            List<BigDecimal> row = new ArrayList<>();
            Long tagId1 = tagIds.get(i);
            Set<Long> users1 = tagUserMap.get(tagId1);

            for (int j = 0; j < n; j++) {
                if (i == j) {
                    row.add(BigDecimal.valueOf(100)); // 自身完全相关
                } else {
                    Long tagId2 = tagIds.get(j);
                    Set<Long> users2 = tagUserMap.get(tagId2);

                    // 计算交集
                    Set<Long> intersection = new HashSet<>(users1);
                    intersection.retainAll(users2);

                    // 计算并集
                    Set<Long> union = new HashSet<>(users1);
                    union.addAll(users2);

                    // Jaccard相似度
                    BigDecimal jaccard = union.isEmpty() ? BigDecimal.ZERO :
                            BigDecimal.valueOf(intersection.size() * 100.0 / union.size())
                                    .setScale(2, RoundingMode.HALF_UP);

                    row.add(jaccard);
                }
            }
            matrix.add(row);
        }

        return matrix;
    }

    /**
     * 获取最强关联标签对
     */
    private List<TagHeatmapVO.TagRelation> getTopRelations(
            List<String> tags, List<List<BigDecimal>> matrix, Integer limit) {
        
        List<TagHeatmapVO.TagRelation> relations = new ArrayList<>();

        for (int i = 0; i < tags.size(); i++) {
            for (int j = i + 1; j < tags.size(); j++) {
                BigDecimal score = matrix.get(i).get(j);
                
                // 只保留有意义的关联（得分 > 5%）
                if (score.compareTo(BigDecimal.valueOf(5)) > 0) {
                    int strengthLevel = getStrengthLevel(score);
                    
                    relations.add(TagHeatmapVO.TagRelation.builder()
                            .tag1(tags.get(i))
                            .tag2(tags.get(j))
                            .relationScore(score)
                            .cooccurrenceCount(0) // 简化，实际应查询
                            .strengthLevel(strengthLevel)
                            .strengthDesc(getStrengthDesc(strengthLevel))
                            .build());
                }
            }
        }

        // 按关联度排序，取TOP N
        return relations.stream()
                .sorted((a, b) -> b.getRelationScore().compareTo(a.getRelationScore()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 获取关联强度等级
     */
    private int getStrengthLevel(BigDecimal score) {
        if (score.compareTo(BigDecimal.valueOf(50)) >= 0) return 3; // 强关联
        else if (score.compareTo(BigDecimal.valueOf(30)) >= 0) return 2; // 中关联
        else return 1; // 弱关联
    }

    /**
     * 获取关联强度描述
     */
    private String getStrengthDesc(int level) {
        return switch (level) {
            case 3 -> "强关联";
            case 2 -> "中关联";
            default -> "弱关联";
        };
    }
}
