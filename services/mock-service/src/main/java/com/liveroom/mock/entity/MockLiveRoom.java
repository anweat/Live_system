package com.liveroom.mock.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Mock直播间实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mock_live_room", indexes = {
        @Index(name = "idx_anchor_id", columnList = "anchor_id"),
        @Index(name = "idx_status", columnList = "status")
})
public class MockLiveRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long anchorId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(length = 50)
    private String category;

    @Column(length = 500)
    private String tags; // JSON格式存储标签

    @Column(length = 200)
    private String coverUrl;

    @Column(nullable = false)
    @Builder.Default
    private Integer status = 1; // 0-未开播、1-直播中、2-已结束

    @Column(nullable = false)
    @Builder.Default
    private Integer currentAudienceCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalAudienceCount = 0;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createTime;
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
