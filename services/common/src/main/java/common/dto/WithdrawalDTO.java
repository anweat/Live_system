package common.dto;

import java.math.BigDecimal;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 提现请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WithdrawalDTO extends BaseDTO {

    private static final long serialVersionUID = 1L;

    /** 主播ID */
    @NotNull(message = "主播ID不能为空")
    private Long anchorId;

    /** 提现金额 */
    @NotNull(message = "提现金额不能为空")
    @Min(value = 100, message = "提现金额最少为1.00元")
    @Max(value = 9999999, message = "提现金额最多为99999.99元")
    private BigDecimal amount;

    /** 提现方式: ALIPAY, WECHAT, BANK_TRANSFER等 */
    @NotBlank(message = "提现方式不能为空")
    private String withdrawalMethod;

    /** 银行卡号或支付宝账号 */
    @NotBlank(message = "账户信息不能为空")
    private String accountNumber;

    /** 账户持有人姓名 */
    @NotBlank(message = "账户持有人姓名不能为空")
    private String accountName;

    /** 身份证号（用于验证） */
    @NotBlank(message = "身份证号不能为空")
    @Pattern(regexp = "^[0-9]{17}[0-9X]$", message = "身份证号格式不正确")
    private String idCard;

    /** 联系电话 */
    @NotBlank(message = "联系电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "电话号码格式不正确")
    private String phoneNumber;

    /** 备注信息（可选） */
    private String remark;

    /**
     * 验证提现DTO的有效性
     */
    public boolean validate() {
        if (anchorId == null || anchorId <= 0) {
            return false;
        }
        if (amount == null || amount.compareTo(new BigDecimal("1")) < 0
                || amount.compareTo(new BigDecimal("99999.99")) > 0) {
            return false;
        }
        if (withdrawalMethod == null || withdrawalMethod.trim().isEmpty()) {
            return false;
        }
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            return false;
        }
        if (accountName == null || accountName.trim().isEmpty()) {
            return false;
        }
        if (idCard == null || !idCard.matches("^[0-9]{17}[0-9X]$")) {
            return false;
        }
        if (phoneNumber == null || !phoneNumber.matches("^1[3-9]\\d{9}$")) {
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
