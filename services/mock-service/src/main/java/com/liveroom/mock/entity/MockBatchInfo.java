package com.liveroom.mock.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 批次信息表
 * 记录批量创建任务的元信息
 */
@Entity
@Table(name = "mock_batch_info", indexes = {
    @Index(name = "idx_batch_type", columnList = "batch_type"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_start_time", columnList = "start_time")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MockBatchInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 批次ID（唯一）
     */
    @Column(name = "batch_id", nullable = false, unique = true, length = 100)
    private String batchId;

    /**
     * 批次类型: ANCHOR/AUDIENCE/SIMULATION
     */
    @Column(name = "batch_type", nullable = false, length = 50)
    private String batchType;

    /**
     * 总数量
     */
    @Column(name = "total_count")
    private Integer totalCount;

    /**
     * 成功数量
     */
    @Column(name = "success_count")
    private Integer successCount;

    /**
     * 失败数量
     */
    @Column(name = "fail_count")
    private Integer failCount;

    /**
     * 状态: RUNNING/SUCCESS/PARTIAL/FAILED
     */
    @Column(name = "status", length = 20)
    private String status;

    /**
     * 开始时间
     */
    @Column(name = "start_time")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    /**
     * 错误信息
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * 创建者
     */
    @Column(name = "created_by", length = 50)
    private String createdBy;

    @PrePersist
    protected void onCreate() {
        if (startTime == null) {
            startTime = LocalDateTime.now();
        }
        if (status == null) {
            status = "RUNNING";
        }
        if (totalCount == null) {
            totalCount = 0;
        }
        if (successCount == null) {
            successCount = 0;
        }
        if (failCount == null) {
            failCount = 0;
        }
        if (createdBy == null) {
            createdBy = "SYSTEM";
        }
    }
}
