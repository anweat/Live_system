package com.liveroom.anchor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 主播数据传输对象
 * 用于前后端数据传输
 * 
 * @author Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnchorDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 用户ID（主键）*/
    private Long userId;

    /** 昵称 */
    @NotBlank(message = "昵称不能为空")
    @Size(min = 2, max = 32, message = "昵称长度为2-32个字符")
    private String nickname;

    /** 真实姓名 */
    @Size(max = 32, message = "真实姓名最长32个字符")
    private String realName;

    /** 性别：0-未知、1-男、2-女 */
    @Min(value = 0, message = "性别参数错误")
    @Max(value = 2, message = "性别参数错误")
    private Integer gender;

    /** 出生日期 */
    private LocalDate birthDate;

    /** 个性签名 */
    @Size(max = 500, message = "个性签名最长500个字符")
    private String signature;

    /** IP地理位置 */
    private String ipLocation;

    /** 主播等级：0-普通、1-优质、2-顶级 */
    private Integer anchorLevel;

    /** 可提取余额 */
    private BigDecimal availableAmount;

    /** 直播间ID */
    private Long liveRoomId;

    /** 点赞数 */
    private Long likeCount;

    /** 粉丝数 */
    private Long fanCount;

    /** 累计收益 */
    private BigDecimal totalEarnings;

    /** 当前分成比例(%) */
    private BigDecimal currentCommissionRate;

    /** 认证状态：0-未认证、1-已认证、2-认证中 */
    private Integer verificationStatus;

    /** 封禁截止时间 */
    private LocalDateTime bannedUntil;

    /** 状态：0-禁用、1-启用 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
