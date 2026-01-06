package com.liveroom.anchor.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 打赏记录视图对象
 * 
 * @author Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RechargeVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 打赏ID */
    private Long rechargeId;

    /** 直播间ID */
    private Long liveRoomId;

    /** 直播间名称 */
    private String roomName;

    /** 主播ID */
    private Long anchorId;

    /** 主播名称 */
    private String anchorName;

    /** 观众ID */
    private Long audienceId;

    /** 观众昵称 */
    private String audienceNickname;

    /** 打赏金额 */
    private BigDecimal rechargeAmount;

    /** 打赏类型：0-普通、1-礼物、2-特殊 */
    private Integer rechargeType;

    /** 打赏类型描述 */
    private String rechargeTypeDesc;

    /** 打赏消息 */
    private String message;

    /** 链路追踪ID */
    private String traceId;

    /** 状态：0-已入账、1-待结算、2-已结算、3-已退款 */
    private Integer status;

    /** 状态描述 */
    private String statusDesc;

    /** 结算ID */
    private Long settlementId;

    /** 打赏时间 */
    private LocalDateTime rechargeTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /**
     * 获取打赏类型描述
     */
    public String getRechargeTypeDesc() {
        if (rechargeType == null) return "未知";
        switch (rechargeType) {
            case 0: return "普通打赏";
            case 1: return "礼物打赏";
            case 2: return "特殊打赏";
            default: return "未知类型";
        }
    }

    /**
     * 获取状态描述
     */
    public String getStatusDesc() {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "已入账";
            case 1: return "待结算";
            case 2: return "已结算";
            case 3: return "已退款";
            default: return "未知状态";
        }
    }

    /**
     * TOP10打赏观众视图对象
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Top10AudienceVO implements Serializable {
        private static final long serialVersionUID = 1L;

        /** 排名 */
        private Integer rank;

        /** 观众ID */
        private Long audienceId;

        /** 观众昵称 */
        private String audienceNickname;

        /** 观众头像 */
        private String avatarUrl;

        /** 累计打赏金额 */
        private BigDecimal totalRechargeAmount;

        /** 打赏次数 */
        private Long rechargeCount;

        /** 最后打赏时间 */
        private LocalDateTime lastRechargeTime;

        /** VIP等级 */
        private Integer vipLevel;
    }

    /**
     * 打赏统计汇总VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RechargeSummaryVO implements Serializable {
        private static final long serialVersionUID = 1L;

        /** 主播ID */
        private Long anchorId;

        /** 打赏总额 */
        private BigDecimal totalAmount;

        /** 打赏次数 */
        private Long totalCount;

        /** 打赏观众数 */
        private Long audienceCount;

        /** 查询开始时间 */
        private LocalDateTime startTime;

        /** 查询结束时间 */
        private LocalDateTime endTime;
    }
}
