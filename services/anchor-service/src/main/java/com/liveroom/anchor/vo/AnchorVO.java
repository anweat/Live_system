package com.liveroom.anchor.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 主播视图对象
 * 用于前端展示主播信息
 * 
 * @author Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnchorVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 用户ID（主键）*/
    private Long userId;

    /** 昵称 */
    private String nickname;

    /** 真实姓名（脱敏）*/
    private String realName;

    /** 性别：0-未知、1-男、2-女 */
    private Integer gender;

    /** 性别描述 */
    private String genderDesc;

    /** 个性签名 */
    private String signature;

    /** IP地理位置 */
    private String ipLocation;

    /** 主播等级：0-普通、1-优质、2-顶级 */
    private Integer anchorLevel;

    /** 主播等级描述 */
    private String anchorLevelDesc;

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

    /** 认证状态描述 */
    private String verificationStatusDesc;

    /** 封禁截止时间 */
    private LocalDateTime bannedUntil;

    /** 是否被封禁 */
    private Boolean isBanned;

    /** 状态：0-禁用、1-启用 */
    private Integer status;

    /** 状态描述 */
    private String statusDesc;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /**
     * 获取性别描述
     */
    public String getGenderDesc() {
        if (gender == null) return "未知";
        switch (gender) {
            case 1: return "男";
            case 2: return "女";
            default: return "未知";
        }
    }

    /**
     * 获取主播等级描述
     */
    public String getAnchorLevelDesc() {
        if (anchorLevel == null) return "普通";
        switch (anchorLevel) {
            case 1: return "优质";
            case 2: return "顶级";
            default: return "普通";
        }
    }

    /**
     * 获取认证状态描述
     */
    public String getVerificationStatusDesc() {
        if (verificationStatus == null) return "未认证";
        switch (verificationStatus) {
            case 1: return "已认证";
            case 2: return "认证中";
            default: return "未认证";
        }
    }

    /**
     * 获取状态描述
     */
    public String getStatusDesc() {
        return (status != null && status == 1) ? "启用" : "禁用";
    }

    /**
     * 是否被封禁
     */
    public Boolean getIsBanned() {
        return bannedUntil != null && bannedUntil.isAfter(LocalDateTime.now());
    }
}
