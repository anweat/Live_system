package com.liveroom.audience.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import common.dto.BaseDTO;

/**
 * 打赏记录传输对象
 * 用于接收和返回打赏相关数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RechargeDTO extends BaseDTO {

    /** 打赏ID */
    private Long rechargeId;

    /** 直播间ID */
    @NotNull(message = "直播间ID不能为空")
    private Long liveRoomId;

    /** 主播ID */
    @NotNull(message = "主播ID不能为空")
    private Long anchorId;

    /** 主播名称 */
    private String anchorName;

    /** 观众ID */
    @NotNull(message = "观众ID不能为空")
    private Long audienceId;

    /** 观众昵称 */
    @NotBlank(message = "观众昵称不能为空")
    private String audienceNickname;

    /** 打赏金额 */
    @NotNull(message = "打赏金额不能为空")
    @DecimalMin(value = "0.01", message = "打赏金额必须大于0")
    @DecimalMax(value = "999999.99", message = "打赏金额不能超过999999.99")
    private BigDecimal rechargeAmount;

    /** 打赏时间 */
    private LocalDateTime rechargeTime;

    /** 链路追踪ID（用于幂等性控制） */
    private String traceId;

    /** 打赏类型：0-普通、1-礼物等 */
    @NotNull(message = "打赏类型不能为空")
    private Integer rechargeType;

    /** 打赏消息 */
    @Size(max = 500, message = "打赏消息不能超过500字符")
    private String message;

    /** 处理状态：0-已入账、1-待结算、2-已结算、3-已退款 */
    private Integer status;

    /** 结算ID */
    private Long settlementId;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
