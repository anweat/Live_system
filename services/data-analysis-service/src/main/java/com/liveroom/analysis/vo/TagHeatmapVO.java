package com.liveroom.analysis.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 标签热力图VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagHeatmapVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 标签列表
     */
    private List<String> tags;

    /**
     * 热力图矩阵数据
     * matrix[i][j] 表示 tags[i] 与 tags[j] 的关联度
     */
    private List<List<BigDecimal>> matrix;

    /**
     * 标签详情列表
     */
    private List<TagDetail> tagDetails;

    /**
     * 最强关联标签对
     */
    private List<TagRelation> topRelations;

    /**
     * 标签详情
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TagDetail implements Serializable {
        /**
         * 标签ID
         */
        private Long tagId;

        /**
         * 标签名称
         */
        private String tagName;

        /**
         * 使用次数
         */
        private Integer useCount;

        /**
         * 热度得分
         */
        private BigDecimal hotScore;
    }

    /**
     * 标签关联关系
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TagRelation implements Serializable {
        /**
         * 标签1
         */
        private String tag1;

        /**
         * 标签2
         */
        private String tag2;

        /**
         * 关联度得分
         */
        private BigDecimal relationScore;

        /**
         * 共现次数
         */
        private Integer cooccurrenceCount;

        /**
         * 关联强度（1-弱/2-中/3-强）
         */
        private Integer strengthLevel;

        /**
         * 关联强度描述
         */
        private String strengthDesc;
    }
}
