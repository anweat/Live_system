package com.liveroom.mock.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Mock用户实体（用于持久化Bot数据）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mock_user", indexes = {
        @Index(name = "idx_is_bot", columnList = "is_bot"),
        @Index(name = "idx_consumption_level", columnList = "consumption_level"),
        @Index(name = "idx_gender", columnList = "gender")
})
public class MockUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128)
    private String nickname;

    @Column(nullable = false)
    private Integer gender; // 0-女，1-男

    private Integer age;

    @Column(length = 200)
    private String avatarUrl;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isBot = false;

    @Column(nullable = false)
    @Builder.Default
    private Integer consumptionLevel = 0; // 0-低、1-中、2-高

    @Column(length = 500)
    private String tags; // JSON格式存储标签，如：["游戏","音乐"]

    @Column(nullable = false)
    @Builder.Default
    private Integer status = 1; // 0-禁用、1-启用

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
