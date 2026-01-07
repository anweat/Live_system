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
 * 观众实体 - 继承自User
 * 在User基础上补充观众特有的业务属性
 * 支持注册用户和游客用户
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "audience", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_consumption_level", columnList = "consumption_level")
})
@PrimaryKeyJoinColumn(name = "user_id")
public class Audience extends User implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 消费等级：0-低消费、1-中消费、2-高消费 */
    @Column(nullable = false)
    private Integer consumptionLevel = 0;

    /** 累计打赏金额 */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalRechargeAmount = BigDecimal.ZERO;

    /** 累计消费次数 */
    @Column(nullable = false)
    private Long totalRechargeCount = 0L;

    /** 最后打赏时间 */
    private LocalDateTime lastRechargeTime;

    /** 观众关联的标签列表 (transient, 通过audience_tag表查询) */
    @Transient
    private java.util.List<common.bean.Tag> tags = new java.util.ArrayList<>();

    /** 等级：1-铁粉、2-银粉、3-金粉、4-超级粉丝 */
    @Column(nullable = false)
    private Integer vipLevel = 0;

    public void setRealName(String realName) {
        super.setUsername(realName);
    }
}
