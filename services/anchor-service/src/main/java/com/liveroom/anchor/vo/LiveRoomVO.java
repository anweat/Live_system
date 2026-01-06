package com.liveroom.anchor.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 直播间视图对象
 * 用于前端展示直播间信息
 * 
 * @author Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveRoomVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 直播间ID */
    private Long liveRoomId;

    /** 主播ID */
    private Long anchorId;

    /** 主播名称 */
    private String anchorName;

    /** 直播间名称 */
    private String roomName;

    /** 直播间描述 */
    private String description;

    /** 直播间状态：0-未开播、1-直播中、2-直播结束、3-被封禁 */
    private Integer roomStatus;

    /** 直播间状态描述 */
    private String roomStatusDesc;

    /** 分类标签 */
    private String category;

    /** 封面图URL */
    private String coverUrl;

    /** 直播流URL */
    private String streamUrl;

    /** 最大容纳观众数 */
    private Integer maxViewers;

    /** 当前在线观众数（实时数据，从Redis读取）*/
    private Long currentViewers;

    /** 上次开播时间 */
    private LocalDateTime startTime;

    /** 上次关播时间 */
    private LocalDateTime endTime;

    /** 累计观看人次 */
    private Long totalViewers;

    /** 本次直播总营收 */
    private BigDecimal totalEarnings;

    /** 本次直播时长（分钟）*/
    private Long liveDuration;

    /** 是否正在直播 */
    private Boolean isLive;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /**
     * 获取直播间状态描述
     */
    public String getRoomStatusDesc() {
        if (roomStatus == null) return "未知";
        switch (roomStatus) {
            case 0: return "未开播";
            case 1: return "直播中";
            case 2: return "直播结束";
            case 3: return "被封禁";
            default: return "未知";
        }
    }

    /**
     * 是否正在直播
     */
    public Boolean getIsLive() {
        return roomStatus != null && roomStatus == 1;
    }

    /**
     * 计算直播时长（分钟）
     */
    public Long getLiveDuration() {
        if (startTime == null) return 0L;
        
        LocalDateTime endTimeCalc = (roomStatus == 1) ? LocalDateTime.now() : endTime;
        if (endTimeCalc == null) return 0L;

        long minutes = java.time.Duration.between(startTime, endTimeCalc).toMinutes();
        return Math.max(0, minutes);
    }
}
