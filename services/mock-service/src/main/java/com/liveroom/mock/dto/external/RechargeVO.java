package com.liveroom.mock.dto.external;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 打赏记录 VO（来自观众服务）
 */
@Data
public class RechargeVO {
    private Long rechargeId;
    private Long liveRoomId;
    private Long anchorId;
    private String anchorName;
    private Long audienceId;
    private String audienceNickname;
    private BigDecimal rechargeAmount;
    private LocalDateTime rechargeTime;
    private String traceId;
    private Integer rechargeType;
    private String message;
    private Integer status;
    private Long settlementId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
