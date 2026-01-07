package common.bean.user;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import javax.persistence.*;

/**
 * 用户基础实体
 * 主播(Anchor)、观众(Audience) 继承此类
 * 游客直接使用User实体，userType=0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "user", indexes = {
        @Index(name = "idx_username", columnList = "username"),
        @Index(name = "idx_email", columnList = "email"),
        @Index(name = "idx_user_type", columnList = "user_type")
})
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "user_type", discriminatorType = DiscriminatorType.INTEGER)
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    /** 用户类型：0-游客、1-注册用户 */
    @Column(name = "user_type", insertable = false, updatable = false)
    private Integer userType;

    /** 用户性别：0-未知、1-男、2-女 */
    @Column(nullable = false)
    private Integer gender = 0;

    /** 用户年龄 */
    private Integer age;

    /** 用户名 (注册用户使用) */
    @Column(length = 64)
    private String username;

    /** 昵称/姓名 */
    @Column(nullable = false, length = 128)
    private String nickname;

    /** 密码哈希值 (注册用户使用) */
    @Column(length = 256)
    private String passwordHash;

    /** 邮箱地址 (注册用户) */
    @Column(length = 128)
    private String email;

    /** 电话号码 (注册用户) */
    @Column(length = 20)
    private String phoneNumber;

    /** 头像URL */
    @Column(length = 500)
    private String avatarUrl;

    /** 个人简介 */
    @Column(length = 500)
    private String bio;

    /** 账户状态：0-正常、1-禁用、2-禁言、3-封禁 */
    @Column(nullable = false)
    private Integer accountStatus = 0;

    /** 是否删除：0-未删除、1-已删除 */
    @Column(nullable = false)
    private Integer isDeleted = 0;

    /** 创建时间 */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;

    /** 更新时间 */
    @Column(nullable = false)
    private LocalDateTime updateTime;

    /**
     * 设置账户状态
     * @param i 账户状态
     */
    public void setStatus(int i) {
        this.accountStatus = i;
    }
}
