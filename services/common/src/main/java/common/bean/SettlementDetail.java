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
 * 结算明细表实体
 * 记录每次结算的详细信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "settlement_detail", indexes = {
        @Index(name = "idx_anchor_id", columnList = "anchor_id"),
        @Index(name = "idx_settlement_start_time", columnList = "settlement_start_time")
})
public class SettlementDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long detailId;

    /** 结算ID */
    @Column(nullable = false)
    private Long settlementId;

    /** 主播ID */
    @Column(nullable = false)
    private Long anchorId;

    /** 本期打赏总额 */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalRechargeAmount;

    /** 本期分成比例 */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal commissionRate;

    /** 本期应付金额（总额 * 比例） */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal settlementAmount;

    /** 结算开始时间 */
    @Column(nullable = false)
    private LocalDateTime settlementStartTime;

    /** 结算结束时间 */
    @Column(nullable = false)
    private LocalDateTime settlementEndTime;

    /** 打赏笔数 */
    @Column(nullable = false)
    @Builder.Default
    private Integer rechargeCount = 0;

    /** 状态：0-已结算、1-已对账、2-已审核 */
    @Column(nullable = false)
    @Builder.Default
    private Integer status = 0;

    /** 备注 */
    @Column(length = 500)
    private String remark;

    /** 创建时间 */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
    }
}
