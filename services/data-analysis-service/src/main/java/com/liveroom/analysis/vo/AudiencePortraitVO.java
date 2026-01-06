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
 * 观众画像VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AudiencePortraitVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 观众ID
     */
    private Long audienceId;

    /**
     * 观众名称
     */
    private String audienceName;

    /**
     * 累计消费金额
     */
    private BigDecimal totalAmount;

    /**
     * 消费次数
     */
    private Integer rechargeCount;

    /**
     * 消费分位数（0-100）
     */
    private Integer percentile;

    /**
     * 消费等级（0-低/1-中/2-高）
     */
    private Integer consumptionLevel;

    /**
     * 消费等级描述
     */
    private String consumptionLevelDesc;

    /**
     * RFM评分
     */
    private RFMScore rfmScore;

    /**
     * 活跃度等级（0-低/1-中/2-高）
     */
    private Integer activityLevel;

    /**
     * 活跃度描述
     */
    private String activityLevelDesc;

    /**
     * 预测LTV
     */
    private BigDecimal predictedLtv;

    /**
     * 留存天数
     */
    private Integer retentionDays;

    /**
     * 最后消费时间
     */
    private String lastRechargeTime;

    /**
     * 首次消费时间
     */
    private String firstRechargeTime;

    /**
     * 最喜欢的主播列表
     */
    private List<FavoriteAnchor> favoriteAnchors;

    /**
     * 偏好分类
     */
    private List<String> favoriteCategories;

    /**
     * 画像标签
     */
    private List<String> tags;

    /**
     * RFM评分模型
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RFMScore implements Serializable {
        /**
         * R-最近消费得分（1-5分）
         */
        private Integer recencyScore;

        /**
         * F-消费频率得分（1-5分）
         */
        private Integer frequencyScore;

        /**
         * M-消费金额得分（1-5分）
         */
        private Integer monetaryScore;

        /**
         * 综合得分
         */
        private BigDecimal totalScore;

        /**
         * 用户价值等级（高/中/低）
         */
        private String valueLevel;
    }

    /**
     * 最喜欢的主播
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FavoriteAnchor implements Serializable {
        private Long anchorId;
        private String anchorName;
        private BigDecimal totalAmount;
        private Integer rechargeCount;
    }
}
