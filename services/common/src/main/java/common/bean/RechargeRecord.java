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
 * 打赏记录表（财务服务DB2副本）
 * 用于财务结算、统计分析、对账审计
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "recharge_record", indexes = {
        @Index(name = "idx_trace_id", columnList = "trace_id"),
        @Index(name = "idx_anchor_id", columnList = "anchor_id"),
        @Index(name = "idx_audience_id", columnList = "audience_id"),
        @Index(name = "idx_recharge_time", columnList = "recharge_time"),
        @Index(name = "idx_sync_batch_id", columnList = "sync_batch_id"),
        @Index(name = "idx_settlement_status", columnList = "settlement_status")
})
public class RechargeRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recordId;

    /** 原始打赏记录ID（来自观众服务） */
    @Column(nullable = false)
    private Long originalRechargeId;

    /** traceId（唯一标识，用于幂等性） */
    @Column(nullable = false, unique = true, length = 64)
    private String traceId;

    /** 主播ID */
    @Column(nullable = false)
    private Long anchorId;

    /** 主播名称 */
    @Column(length = 128)
    private String anchorName;

    /** 观众ID */
    @Column(nullable = false)
    private Long audienceId;

    /** 观众名称 */
    @Column(length = 128)
    private String audienceName;

    /** 打赏金额 */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal rechargeAmount;

    /** 打赏时间（原始时间） */
    @Column(nullable = false)
    private LocalDateTime rechargeTime;

    /** 打赏类型：0-普通打赏、1-礼物、2-跳过广告 */
    @Column(nullable = false)
    @Builder.Default
    private Integer rechargeType = 0;

    /** 直播间ID */
    private Long liveRoomId;

    /** 同步批次ID */
    @Column(length = 64)
    private String syncBatchId;

    /** 结算状态：0-待结算、1-已结算、2-已提现 */
    @Column(nullable = false)
    @Builder.Default
    private Integer settlementStatus = 0;

    /** 结算时间 */
    private LocalDateTime settlementTime;

    /** 应用的分成比例 */
    @Column(precision = 5, scale = 2)
    private Double appliedCommissionRate;

    /** 结算金额（打赏金额 * 分成比例） */
    @Column(precision = 15, scale = 2)
    private BigDecimal settlementAmount;

    /** 数据来源：audience-service */
    @Column(length = 50)
    private String sourceService;

    /** 接收时间（同步到财务服务的时间） */
    @Column(nullable = false)
    private LocalDateTime receivedTime;

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
        if (receivedTime == null) {
            receivedTime = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
