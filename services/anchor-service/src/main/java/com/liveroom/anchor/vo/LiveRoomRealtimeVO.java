package com.liveroom.anchor.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 直播间实时统计视图对象
 * 用于展示直播间的实时数据
 * 
 * @author Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveRoomRealtimeVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 直播间ID */
    private Long liveRoomId;

    /** 主播ID */
    private Long anchorId;

    /** 主播名称 */
    private String anchorName;

    /** 当前在线观众数 */
    private Long currentViewers;

    /** 累计观看人次 */
    private Long totalViewers;

    /** 本次直播总营收 */
    private BigDecimal totalEarnings;

    /** 直播间状态：0-未开播、1-直播中、2-直播结束、3-被封禁 */
    private Integer roomStatus;

    /** 开播时间 */
    private LocalDateTime startTime;

    /** 直播时长（分钟）*/
    private Long liveDuration;

    /** 数据查询时间 */
    private LocalDateTime queryTime;

    /**
     * 计算直播时长（分钟）
     */
    public Long getLiveDuration() {
        if (startTime == null || roomStatus != 1) return 0L;
        
        long minutes = java.time.Duration.between(startTime, LocalDateTime.now()).toMinutes();
        return Math.max(0, minutes);
    }
}
