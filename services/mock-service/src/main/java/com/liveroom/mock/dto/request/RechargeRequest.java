package com.liveroom.mock.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 打赏请求（发送给观众服务）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RechargeRequest {

    @NotNull(message = "观众ID不能为空")
    private Long audienceId;

    @NotNull(message = "主播ID不能为空")
    private Long anchorId;

    @NotNull(message = "直播间ID不能为空")
    private Long liveRoomId;

    @NotNull(message = "打赏金额不能为空")
    private BigDecimal rechargeAmount;

    private Integer rechargeType;

    private String message;

    @NotNull(message = "traceId不能为空")
    private String traceId;
}
