package common.bean.liveroom;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * 直播间实时数据 - 频繁更新的数据
 * 与 LiveRoom 一一对应（直播间ID作为主键）
 * 由直播服务定时更新
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "live_room_realtime")
public class LiveRoomRealtime implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 实时数据ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long realtimeId;

    /** 直播间ID（与live_room表一一对应） */
    @Column(name = "live_room_id", nullable = false, unique = true)
    private Long liveRoomId;

    /** 当前观众数 */
    @Column(nullable = false)
    @Builder.Default
    private Integer currentViewerCount = 0;

    /** 当前场次收益 */
    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal currentRevenueAmount = BigDecimal.ZERO;

    /** 弹幕数 */
    @Column(nullable = false)
    @Builder.Default
    private Long messageCount = 0L;

    /** 打赏数 */
    @Column(nullable = false)
    @Builder.Default
    private Long rechargeCount = 0L;

    /** 在线时长 (秒) */
    @Column(nullable = false)
    @Builder.Default
    private Long onlineDuration = 0L;

    /** 平均延迟 (ms) */
    @Column(nullable = false)
    @Builder.Default
    private Integer avgLatency = 0;

    /** 最大延迟 (ms) */
    @Column(nullable = false)
    @Builder.Default
    private Integer maxLatency = 0;

    /** 最后更新时间 */
    @Column(nullable = false)
    private LocalDateTime lastUpdateTime;

    /** 版本号（乐观锁） */
    @Version
    @Column(nullable = false)
    private Long version = 0L;
}