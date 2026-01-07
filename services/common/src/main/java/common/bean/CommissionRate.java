package common.bean;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;

/**
 * 分成比例表实体
 * 记录主播的分成比例及其变化历史
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "commission_rate", indexes = {
        @Index(name = "idx_anchor_id", columnList = "anchor_id"),
        @Index(name = "idx_effective_time", columnList = "effective_time"),
        @Index(name = "idx_status", columnList = "status")
})
public class CommissionRate implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commissionRateId;

    /** 主播ID */
    @Column(nullable = false)
    private Long anchorId;

    /** 主播名称 */
    @Column(nullable = false, length = 128)
    private String anchorName;

    /** 分成比例 (百分比) */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal commissionRate;

    /** 生效时间 */
    @Column(nullable = false)
    private LocalDateTime effectiveTime;

    /** 过期时间 (为null表示未过期) */
    private LocalDateTime expireTime;

    /** 状态：0-未启用、1-启用、2-已过期 */
    @Column(nullable = false)
    private Integer status;

    /** 操作备注 */
    @Column(length = 500)
    private String remark;

    /** 创建时间 */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;

    /** 更新时间 */
    @Column(nullable = false)
    private LocalDateTime updateTime;
}
