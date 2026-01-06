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
 * 结算表实体
 * 记录主播的结算金额、已提取金额等信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "settlement", indexes = {
        @Index(name = "idx_anchor_id", columnList = "anchor_id"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_next_settlement_time", columnList = "next_settlement_time")
})
public class Settlement implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long settlementId;

    /** 主播ID（一一对应） */
    @Column(nullable = false, unique = true)
    private Long anchorId;

    /** 主播名称 */
    @Column(nullable = false, length = 128)
    private String anchorName;

    /** 结算金额（应付） */
    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal settlementAmount = BigDecimal.ZERO;

    /** 已提取金额 */
    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal withdrawnAmount = BigDecimal.ZERO;

    /** 可提取金额 */
    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal availableAmount = BigDecimal.ZERO;

    /** 结算周期（天数） */
    @Column(nullable = false)
    @Builder.Default
    private Integer settlementCycle = 1;

    /** 上次结算时间 */
    private LocalDateTime lastSettlementTime;

    /** 下次结算时间 */
    private LocalDateTime nextSettlementTime;

    /** 状态：0-正常、1-冻结、2-禁提 */
    @Column(nullable = false)
    @Builder.Default
    private Integer status = 0;

    /** 创建时间 */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;

    /** 更新时间 */
    @Column(nullable = false)
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
        if (updateTime == null) {
            updateTime = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
