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
 * 数据同步进度表实体
 * 用于记录打赏数据同步至财务服务等外部服务的进度
 * 支持断点续传和失败重试
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "sync_progress", indexes = {
        @Index(name = "idx_sync_type", columnList = "sync_type"),
        @Index(name = "idx_source_service", columnList = "source_service"),
        @Index(name = "idx_last_sync_time", columnList = "last_sync_time"),
        @Index(name = "idx_status", columnList = "status")
})
public class SyncProgress implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long progressId;

    /** 同步类型：0-打赏数据同步、1-用户数据同步 */
    @Column(nullable = false)
    @Builder.Default
    private Integer syncType = 0;

    /** 数据源服务 */
    @Column(nullable = false, length = 50)
    private String sourceService;

    /** 目标服务 */
    @Column(nullable = false, length = 50)
    private String targetService;

    /** 最后同步的打赏记录ID（用于断点续传） */
    @Column(nullable = false)
    @Builder.Default
    private Long lastSyncRechargeId = 0L;

    /** 累计同步笔数 */
    @Column(nullable = false)
    @Builder.Default
    private Long totalSyncedCount = 0L;

    /** 累计同步金额 */
    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalSyncedAmount = BigDecimal.ZERO;

    /** 最后同步时间 */
    private LocalDateTime lastSyncTime;

    /** 下次同步时间 */
    private LocalDateTime nextSyncTime;

    /** 同步状态：0-待同步、1-同步中、2-已同步、3-失败 */
    @Column(nullable = false)
    @Builder.Default
    private Integer syncStatus = 0;

    /** 错误信息（同步失败时记录原因） */
    @Column(length = 500)
    private String errorMessage;

    /** 同步间隔(秒) */
    @Column(nullable = false)
    @Builder.Default
    private Integer syncIntervalSeconds = 300;

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
