package common.bean.user;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 主播实体 - 继承自User
 * 在User基础上补充主播特有的业务属性
 * 主播一一对应一个直播间
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "anchor", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_live_room_id", columnList = "live_room_id")
})
@PrimaryKeyJoinColumn(name = "user_id")
public class Anchor extends User implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主播等级 */
    @Column(nullable = false)
    private Integer anchorLevel = 0;

    /** 可提取余额 */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal availableAmount = BigDecimal.ZERO;

    /** 直播间ID */
    @Column(nullable = false)
    private Long liveRoomId;

    /** 点赞数 */
    @Column(nullable = false)
    private Long likeCount = 0L;

    /** 粉丝数 */
    @Column(nullable = false)
    private Long fanCount = 0L;

    /** 累计收益 */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalEarnings = BigDecimal.ZERO;

    /** 当前分成比例(%) */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal currentCommissionRate = new BigDecimal("50.00");

    /** 封禁截止时间(null表示未被封禁) */
    private LocalDateTime bannedUntil;

    /** 主播关联的标签列表 (transient, 通过anchor_tag表查询) */
    @Transient
    private java.util.List<common.bean.Tag> tags = new java.util.ArrayList<>();
}
