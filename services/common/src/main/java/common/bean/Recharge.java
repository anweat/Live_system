package common.bean;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import common.bean.user.Audience;
import common.bean.liveroom.LiveRoom;

/**
 * 打赏记录表实体
 * 记录每一笔打赏的详细信息，支持幂等性控制
 * 支持多维度查询：按直播间、按主播、按观众
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "recharge", indexes = {
        @Index(name = "idx_live_room_id", columnList = "live_room_id"),
        @Index(name = "idx_anchor_id", columnList = "anchor_id"),
        @Index(name = "idx_audience_id", columnList = "audience_id"),
        @Index(name = "idx_recharge_time", columnList = "recharge_time"),
        @Index(name = "idx_trace_id", columnList = "trace_id")
})
public class Recharge implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rechargeId;

    /** 直播间ID - 用于快速查询该场直播的所有打赏 */
    @Column(nullable = false)
    private Long liveRoomId;

    /** 主播ID - 用于查询主播的所有打赏记录 */
    @Column(nullable = false)
    private Long anchorId;

    /** 主播名称 */
    @Column(nullable = false, length = 128)
    private String anchorName;

    /** 观众ID（打赏者） - 用于查询观众的打赏历史 */
    @Column(nullable = false)
    private Long audienceId;

    /** 观众昵称 */
    @Column(nullable = false, length = 128)
    private String audienceNickname;

    /** 打赏金额 */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal rechargeAmount;

    /** 打赏时间 */
    @Column(nullable = false)
    private LocalDateTime rechargeTime;

    /** traceId (用于幂等性控制和链路追踪) */
    @Column(unique = true, length = 64)
    private String traceId;

    /** 打赏类型：0-普通打赏、1-礼物、2-跳过广告等 */
    @Column(nullable = false)
    private Integer rechargeType;

    /** 打赏消息 */
    @Column(length = 500)
    private String message;

    /** 处理状态：0-已入账、1-待结算、2-已结算、3-已退款 */
    @Column(nullable = false)
    @Builder.Default
    private Integer status = 0;

    /** 对应的结算ID */
    private Long settlementId;

    /** 创建时间 */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;

    /** 更新时间 */
    @Column(nullable = false)
    private LocalDateTime updateTime;
}
