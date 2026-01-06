package com.liveroom.analysis.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 排行榜VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankingVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 排行榜类型
     */
    private String rankingType;

    /**
     * 时间周期
     */
    private String period;

    /**
     * 排行榜数据
     */
    private List<RankingItem> rankings;

    /**
     * 排行榜项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RankingItem implements Serializable {
        /**
         * 排名
         */
        private Integer rank;

        /**
         * 用户ID（主播或观众）
         */
        private Long userId;

        /**
         * 用户名称
         */
        private String userName;

        /**
         * 头像URL
         */
        private String avatarUrl;

        /**
         * 指标值（收入/消费金额）
         */
        private BigDecimal value;

        /**
         * 次要指标（打赏次数/粉丝数等）
         */
        private Integer secondaryValue;

        /**
         * 环比变化
         */
        private Integer rankChange;

        /**
         * 标签
         */
        private List<String> tags;
    }
}
