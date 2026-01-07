package com.liveroom.mock.dto.external;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 主播信息 VO（来自主播服务）
 */
@Data
public class AnchorVO {
    private Long userId;
    private String username;
    private String nickname;
    private String avatarUrl;
    private Integer gender;
    private Integer age;
    private String bio;
    private Integer anchorLevel;
    private BigDecimal availableAmount;
    private Long liveRoomId;
    private Long likeCount;
    private Long fanCount;
    private BigDecimal totalEarnings;
    private BigDecimal currentCommissionRate;
    private Integer accountStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<String> tags;
}
