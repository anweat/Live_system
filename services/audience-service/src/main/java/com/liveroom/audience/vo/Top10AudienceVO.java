package com.liveroom.audience.vo;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TOP10打赏观众视图对象
 * 用于返回主播的TOP10打赏观众排行数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Top10AudienceVO {

    /** 排名 */
    private Integer rank;

    /** 观众ID */
    private Long audienceId;

    /** 观众昵称 */
    private String audienceNickname;

    /** 累计打赏金额 */
    private BigDecimal totalRechargeAmount;

    /** 打赏次数 */
    private Long rechargeCount;

    /** 最后打赏时间 */
    private String lastRechargeTime;
}
