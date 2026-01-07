package com.liveroom.mock.dto.external;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 观众信息 VO（来自观众服务）
 */
@Data
public class AudienceVO {
    private Long userId;
    private String username;
    private String nickname;
    private String avatarUrl;
    private Integer gender;
    private Integer age;
    private String bio;
    private Integer consumptionLevel;
    private BigDecimal totalRechargeAmount;
    private Long totalRechargeCount;
    private LocalDateTime lastRechargeTime;
    private Integer vipLevel;
    private Integer accountStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<String> tags;
}
