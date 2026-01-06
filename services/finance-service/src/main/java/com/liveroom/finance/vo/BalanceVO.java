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
 * 主播余额VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BalanceVO implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 主播ID
     */
    private Long anchorId;

    /**
     * 主播名称
     */
    private String anchorName;

    /**
     * 结算总金额（应付）
     */
    private BigDecimal settlementAmount;

    /**
     * 已提取金额
     */
    private BigDecimal withdrawnAmount;

    /**
     * 可提取金额
     */
    private BigDecimal availableAmount;

    /**
     * 当前分成比例
     */
    private Double currentCommissionRate;

    /**
     * 账户状态：0-正常、1-冻结、2-禁提
     */
    private Integer status;

    /**
     * 账户状态描述
     */
    private String statusDesc;

    /**
     * 上次结算时间
     */
    private LocalDateTime lastSettlementTime;

    /**
     * 下次结算时间
     */
    private LocalDateTime nextSettlementTime;

    /**
     * 查询时间
     */
    private LocalDateTime queryTime;
}
