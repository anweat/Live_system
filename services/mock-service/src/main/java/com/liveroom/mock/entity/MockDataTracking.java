package com.liveroom.mock.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 模拟数据追踪表
 * 用于记录所有通过 Mock Service 创建的模拟数据的 ID，便于后续批量清理
 */
@Entity
@Table(name = "mock_data_tracking", indexes = {
    @Index(name = "idx_entity_type_id", columnList = "entity_type,entity_id"),
    @Index(name = "idx_batch_id", columnList = "batch_id"),
    @Index(name = "idx_trace_id", columnList = "trace_id"),
    @Index(name = "idx_created_time", columnList = "created_time"),
    @Index(name = "idx_is_deleted", columnList = "is_deleted")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MockDataTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 实体类型: ANCHOR/AUDIENCE/LIVE_ROOM/RECHARGE
     */
    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    /**
     * 实体ID（来自各服务的响应）
     */
    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    /**
     * 创建时的traceId
     */
    @Column(name = "trace_id", length = 100)
    private String traceId;

    /**
     * 批量创建的批次ID
     */
    @Column(name = "batch_id", length = 100)
    private String batchId;

    /**
     * 创建时的数据快照（JSON格式，可选）
     */
    @Column(name = "data_snapshot", columnDefinition = "TEXT")
    private String dataSnapshot;

    /**
     * 创建时间
     */
    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;

    /**
     * 是否已删除: 0-未删除, 1-已删除
     */
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    /**
     * 删除时间
     */
    @Column(name = "deleted_time")
    private LocalDateTime deletedTime;

    @PrePersist
    protected void onCreate() {
        if (createdTime == null) {
            createdTime = LocalDateTime.now();
        }
        if (isDeleted == null) {
            isDeleted = false;
        }
    }
}

