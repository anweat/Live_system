package com.liveroom.audience.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import common.dto.BaseDTO;

/**
 * 观众信息传输对象
 * 用于接收和返回观众相关数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AudienceDTO extends BaseDTO {

    /** 用户ID */
    private Long userId;

    /** 昵称 */
    @NotBlank(message = "昵称不能为空")
    @Size(min = 2, max = 50, message = "昵称长度在2-50之间")
    private String nickname;

    /** 真实姓名 */
    private String realName;

    /** 用户类型：0-游客、1-注册用户、2-会员用户 */
    private Integer userType;

    /** 性别：0-未知、1-男、2-女 */
    private Integer gender;


    /** 消费等级：0-低、1-中、2-高 */
    private Integer consumptionLevel;

    /** 累计打赏金额 */
    private BigDecimal totalRechargeAmount;

    /** 累计打赏次数 */
    private Long totalRechargeCount;

    /** 最后打赏时间 */
    private LocalDateTime lastRechargeTime;

    /** 粉丝等级：0-普通、1-铁粉、2-银粉、3-金粉、4-超级粉丝 */
    private Integer vipLevel;

    /** 账户状态：0-正常、1-禁用、2-禁言、3-封禁 */
    private Integer status;

    /** 关注主播数 */
    private Long followingCount;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
