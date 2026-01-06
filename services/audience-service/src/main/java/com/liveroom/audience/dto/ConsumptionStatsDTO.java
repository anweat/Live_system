package com.liveroom.audience.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消费统计传输对象
 * 用于返回观众的消费相关统计数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsumptionStatsDTO {

    /** 观众ID */
    private Long audienceId;

    /** 观众昵称 */
    private String audienceNickname;

    /** 累计打赏金额 */
    private BigDecimal totalRechargeAmount;

    /** 累计打赏次数 */
    private Long totalRechargeCount;

    /** 消费等级：0-低、1-中、2-高 */
    private Integer consumptionLevel;

    /** 粉丝等级：0-普通、1-铁粉、2-银粉、3-金粉、4-超级粉丝 */
    private Integer vipLevel;

    /** 消费等级描述 */
    private String consumptionLevelDesc;

    /** 粉丝等级描述 */
    private String vipLevelDesc;
}
