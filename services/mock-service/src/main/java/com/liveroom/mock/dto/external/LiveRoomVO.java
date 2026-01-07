package com.liveroom.mock.dto.external;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 直播间信息 VO（来自主播服务）
 */
@Data
public class LiveRoomVO {
    private Long liveRoomId;
    private Long anchorId;
    private String anchorName;
    private String roomName;
    private String description;
    private Integer roomStatus;
    private String category;
    private String coverUrl;
    private String streamUrl;
    private Integer maxViewers;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long totalViewers;
    private BigDecimal totalEarnings;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<String> tags;
}
