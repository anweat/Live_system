package com.liveroom.mock.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 模拟任务表
 * 记录行为模拟任务的执行情况
 */
@Entity
@Table(name = "mock_simulation_task", indexes = {
    @Index(name = "idx_live_room_id", columnList = "live_room_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_start_time", columnList = "start_time")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MockSimulationTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 任务ID（唯一）
     */
    @Column(name = "task_id", nullable = false, unique = true, length = 100)
    private String taskId;

    /**
     * 直播间ID
     */
    @Column(name = "live_room_id", nullable = false)
    private Long liveRoomId;

    /**
     * 参与观众数
     */
    @Column(name = "audience_count")
    private Integer audienceCount;

    /**
     * 持续时间（秒）
     */
    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    /**
     * 模拟配置（JSON格式：进入/离开/弹幕/打赏等）
     */
    @Column(name = "simulation_config", columnDefinition = "TEXT")
    private String simulationConfig;

    /**
     * 状态: PENDING/RUNNING/COMPLETED/FAILED/CANCELLED
     */
    @Column(name = "status", length = 20)
    private String status;

    /**
     * 进度百分比
     */
    @Column(name = "progress")
    private Integer progress;

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
     * 创建时间
     */
    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;

    /**
     * 错误信息
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @PrePersist
    protected void onCreate() {
        if (createdTime == null) {
            createdTime = LocalDateTime.now();
        }
        if (status == null) {
            status = "PENDING";
        }
        if (progress == null) {
            progress = 0;
        }
        if (audienceCount == null) {
            audienceCount = 0;
        }
        if (durationSeconds == null) {
            durationSeconds = 0;
        }
    }
}
