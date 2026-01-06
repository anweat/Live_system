package common.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 结算信息DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SettlementDTO extends BaseDTO {

    private static final long serialVersionUID = 1L;

    /** 结算ID */
    private Long settlementId;

    /** 主播ID */
    @NotNull(message = "主播ID不能为空")
    private Long anchorId;

    /** 结算周期开始日期 */
    @NotNull(message = "结算周期开始日期不能为空")
    private LocalDate settlementStartDate;

    /** 结算周期结束日期 */
    @NotNull(message = "结算周期结束日期不能为空")
    private LocalDate settlementEndDate;

    /** 直播总时长（分钟） */
    @Min(value = 0, message = "直播时长不能为负")
    private Long totalLiveMinutes;

    /** 直播总人数 */
    @Min(value = 0, message = "直播人数不能为负")
    private Long totalAudienceCount;

    /** 打赏总金额 */
    @NotNull(message = "打赏总金额不能为空")
    @Min(value = 0, message = "打赏金额不能为负")
    private BigDecimal totalRechargeAmount;

    /** 基础佣金 */
    private BigDecimal baseCommission;

    /** 佣金比例（百分比） */
    @Min(value = 0, message = "佣金比例不能为负")
    private BigDecimal commissionRate;

    /** 计算的佣金金额 */
    private BigDecimal calculatedCommission;

    /** 扣款（税费等） */
    private BigDecimal deductions;

    /** 最终结算金额 */
    private BigDecimal finalAmount;

    /**
     * 结算状态: APPLYING(0), APPROVED(1), TRANSFERRING(2), COMPLETED(3), REJECTED(4)
     */
    @Min(value = 0)
    private Integer status;

    /** 是否已支付 */
    private Boolean isPaid;

    /** 备注信息 */
    private String remark;

    /**
     * 验证结算DTO的有效性
     */
    public boolean validate() {
        if (anchorId == null || anchorId <= 0) {
            return false;
        }
        if (settlementStartDate == null || settlementEndDate == null) {
            return false;
        }
        if (settlementStartDate.isAfter(settlementEndDate)) {
            return false;
        }
        if (totalRechargeAmount == null || totalRechargeAmount.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }
        if (commissionRate != null && (commissionRate.compareTo(BigDecimal.ZERO) < 0
                || commissionRate.compareTo(new BigDecimal("100")) > 0)) {
            return false;
        }
        return true;
    }

    /**
     * 计算佣金金额
     */
    public BigDecimal calculateCommission() {
        if (totalRechargeAmount == null || commissionRate == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal rate = commissionRate.divide(new BigDecimal("100"), 4, java.math.RoundingMode.HALF_UP);
        return totalRechargeAmount.multiply(rate).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * 计算最终支付金额
     */
    public BigDecimal calculateFinalAmount() {
        BigDecimal commission = calculatedCommission != null ? calculatedCommission : calculateCommission();
        BigDecimal deduct = deductions != null ? deductions : BigDecimal.ZERO;
        return commission.subtract(deduct).setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
