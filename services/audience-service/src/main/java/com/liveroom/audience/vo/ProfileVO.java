package com.liveroom.audience.vo;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户消费画像视图对象
 * 用于返回用户的详细消费分析信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileVO {

    /** 观众ID */
    private Long audienceId;

    /** 观众昵称 */
    private String audienceNickname;

    /** 消费等级 */
    private Integer consumptionLevel;

    /** 消费等级描述 */
    private String consumptionLevelDescription;

    /** 累计消费金额 */
    private BigDecimal totalRechargeAmount;

    /** 消费分位数（0-1） */
    private Double percentile;

    /** 粉丝等级 */
    private Integer vipLevel;

    /** 粉丝等级描述 */
    private String vipLevelDescription;

    /** 用户标签列表 */
    private List<String> tags;

    /** 关注的主播ID列表 */
    private List<Long> followingAnchorIds;
}
