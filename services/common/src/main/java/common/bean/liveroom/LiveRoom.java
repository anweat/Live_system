package common.bean.liveroom;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 直播间实体
 * 主播一一对应一个直播间
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "live_room", indexes = {
        @Index(name = "idx_anchor_id", columnList = "anchor_id"),
        @Index(name = "idx_room_status", columnList = "room_status"),
        @Index(name = "idx_start_time", columnList = "start_time")
})
public class LiveRoom implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long liveRoomId;

    /** 主播ID */
    @Column(nullable = false, unique = true)
    private Long anchorId;

    /** 主播名称 */
    @Column(nullable = false, length = 128)
    private String anchorName;

    /** 直播间名称 */
    @Column(nullable = false, length = 256)
    private String roomName;

    /** 直播间描述 */
    @Column(length = 1000)
    private String description;

    /** 直播间状态：0-未开播、1-直播中、2-直播结束、3-被封禁 */
    @Column(nullable = false)
    @Builder.Default
    private Integer roomStatus = 0;

    /** 分类标签 */
    @Column(length = 128)
    private String category;

    /** 封面图URL */
    @Column(length = 500)
    private String coverUrl;

    /** 直播流URL */
    @Column(length = 500)
    private String streamUrl;

    /** 最大容纳观众数 */
    @Column(nullable = false)
    @Builder.Default
    private Integer maxViewers = 10000;

    /** 上次开播时间 */
    private LocalDateTime startTime;

    /** 上次关播时间 */
    private LocalDateTime endTime;

    /** 累计观看人次 */
    @Column(nullable = false)
    @Builder.Default
    private Long totalViewers = 0L;

    /** 本次直播总营收 */
    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalEarnings = BigDecimal.ZERO;

    /** 创建时间 */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;

    /** 更新时间 */
    @Column(nullable = false)
    private LocalDateTime updateTime;

    /** 直播间关联的标签列表 (transient, 通过live_room_tag表查询) */
    @Transient
    private java.util.List<common.bean.Tag> tags = new java.util.ArrayList<>();
}
