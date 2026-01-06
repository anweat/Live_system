package com.liveroom.finance.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 结算明细VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SettlementDetailVO implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 明细ID
     */
    private Long detailId;

    /**
     * 主播ID
     */
    private Long anchorId;

    /**
     * 主播名称
     */
    private String anchorName;

    /**
     * 本期打赏总额
     */
    private BigDecimal totalRechargeAmount;

    /**
     * 本期分成比例
     */
    private Double commissionRate;

    /**
     * 本期应付金额
     */
    private BigDecimal settlementAmount;

    /**
     * 结算开始时间
     */
    private LocalDateTime settlementStartTime;

    /**
     * 结算结束时间
     */
    private LocalDateTime settlementEndTime;

    /**
     * 打赏笔数
     */
    private Integer rechargeCount;

    /**
     * 状态：0-已结算、1-已对账、2-已审核
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
