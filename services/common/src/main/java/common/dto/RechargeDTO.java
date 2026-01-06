package common.dto;

import java.math.BigDecimal;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 充值请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RechargeDTO extends BaseDTO {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /** 充值金额 */
    @NotNull(message = "充值金额不能为空")
    @Min(value = 1, message = "充值金额最少为0.01元")
    @Max(value = 9999999, message = "充值金额最多为99999.99元")
    private BigDecimal amount;

    /** 充值类型: NORMAL(0), GIFT(1), SKIP_AD(2) */
    @NotNull(message = "充值类型不能为空")
    @Min(value = 0)
    @Max(value = 2)
    private Integer rechargeType;

    /** 支付渠道: ALIPAY, WECHAT, CARD等 */
    @NotBlank(message = "支付渠道不能为空")
    private String paymentChannel;

    /** 第三方订单号（可选） */
    private String thirdOrderNo;

    /** 备注信息（可选） */
    private String remark;

    /**
     * 验证充值DTO的有效性
     */
    public boolean validate() {
        if (userId == null || userId <= 0) {
            return false;
        }
        if (amount == null || amount.compareTo(new BigDecimal("0.01")) < 0
                || amount.compareTo(new BigDecimal("99999.99")) > 0) {
            return false;
        }
        if (rechargeType == null || rechargeType < 0 || rechargeType > 2) {
            return false;
        }
        if (paymentChannel == null || paymentChannel.trim().isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * 获取标准化的金额（保留2位小数）
     */
    public BigDecimal getNormalizedAmount() {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        return amount.setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
