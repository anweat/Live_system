package common.bean;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;

/**
 * 提现表实体
 * 记录主播的提现申请和处理流程
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "withdrawal", indexes = {
        @Index(name = "idx_anchor_id", columnList = "anchor_id"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_applied_time", columnList = "applied_time")
})
public class Withdrawal implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long withdrawalId;

    /** 主播ID */
    @Column(nullable = false)
    private Long anchorId;

    /** 主播名称 */
    @Column(nullable = false, length = 128)
    private String anchorName;

    /** 提现金额 */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal withdrawalAmount;

    /** 提现方式：0-银行卡、1-支付宝、2-微信 */
    @Column(nullable = false)
    private Integer withdrawalType;

    /** 开户行 */
    @Column(length = 100)
    private String bankName;

    /** 银行卡号(加密存储) */
    @Column(length = 255)
    private String bankCardEncrypted;

    /** 账户持有人名称 */
    @Column(length = 128)
    private String accountHolder;

    /** 提现申请时间 */
    @Column(nullable = false)
    private LocalDateTime appliedTime;

    /** 提现处理时间 */
    private LocalDateTime processedTime;

    /** 提现状态：0-申请中、1-处理中、2-已打款、3-失败、4-已拒绝 */
    @Column(nullable = false)
    private Integer status;

    /** 拒绝原因 */
    @Column(length = 500)
    private String rejectReason;

    /** 转账流水号 */
    @Column(length = 100)
    private String transferSerialNumber;

    /** traceId (幂等性控制) */
    @Column(length = 64)
    private String traceId;

    /** 版本号，用于乐观锁 */
    @Version
    @Column(nullable = false)
    @Builder.Default
    private Integer version = 0;

    /** 创建时间 */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;

    /** 更新时间 */
    @Column(nullable = false)
    private LocalDateTime updateTime;
}
