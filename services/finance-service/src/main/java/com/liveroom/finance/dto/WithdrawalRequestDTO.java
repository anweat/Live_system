package com.liveroom.finance.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 提现请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WithdrawalRequestDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 主播ID
     */
    @NotNull(message = "主播ID不能为空")
    private Long anchorId;

    /**
     * 主播名称
     */
    @NotBlank(message = "主播名称不能为空")
    private String anchorName;

    /**
     * 提现金额
     */
    @NotNull(message = "提现金额不能为空")
    @DecimalMin(value = "1.00", message = "提现金额最少为1.00元")
    @DecimalMax(value = "99999.99", message = "提现金额最多为99999.99元")
    private BigDecimal amount;

    /**
     * 提现方式：0-银行卡、1-支付宝、2-微信
     */
    @NotNull(message = "提现方式不能为空")
    @Min(value = 0, message = "提现方式取值范围0-2")
    @Max(value = 2, message = "提现方式取值范围0-2")
    private Integer withdrawalType;

    /**
     * 银行卡号或支付宝账号
     */
    @NotBlank(message = "账户信息不能为空")
    private String accountNumber;

    /**
     * 开户行（银行卡提现时必填）
     */
    private String bankName;

    /**
     * 账户持有人姓名
     */
    @NotBlank(message = "账户持有人姓名不能为空")
    private String accountHolder;

    /**
     * traceId（用于幂等性）
     */
    @NotBlank(message = "traceId不能为空")
    private String traceId;

    /**
     * 备注
     */
    private String remark;

    /**
     * 验证提现请求
     */
    public boolean validate() {
        if (anchorId == null || anchorId <= 0) {
            return false;
        }
        if (amount == null || amount.compareTo(new BigDecimal("1.00")) < 0
                || amount.compareTo(new BigDecimal("99999.99")) > 0) {
            return false;
        }
        if (withdrawalType == null || withdrawalType < 0 || withdrawalType > 2) {
            return false;
        }
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            return false;
        }
        if (accountHolder == null || accountHolder.trim().isEmpty()) {
            return false;
        }
        if (traceId == null || traceId.trim().isEmpty()) {
            return false;
        }
        // 银行卡提现必须填写开户行
        if (withdrawalType == 0 && (bankName == null || bankName.trim().isEmpty())) {
            return false;
        }
        return true;
    }
}
