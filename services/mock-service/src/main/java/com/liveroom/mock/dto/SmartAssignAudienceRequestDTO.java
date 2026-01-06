package com.liveroom.mock.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.*;
import java.util.List;

/**
 * 智能分配观众请求DTO（基于标签匹配）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmartAssignAudienceRequestDTO {

    @NotNull(message = "直播间ID不能为空")
    private Long liveRoomId;

    // 直播间标签（用于匹配观众）
    private List<String> liveRoomTags;

    @NotNull(message = "目标观众数量不能为空")
    @Min(value = 1, message = "至少分配1个观众")
    @Max(value = 50000, message = "单个直播间最多50000个观众")
    private Integer targetCount;

    // 标签匹配策略：exact-精确匹配所有标签，any-匹配任意标签，random-随机分配
    @Builder.Default
    private String matchStrategy = "any";

    // 消费等级分布（低/中/高的占比）
    @Builder.Default
    private Integer lowConsumptionRate = 60;

    @Builder.Default
    private Integer mediumConsumptionRate = 30;

    @Builder.Default
    private Integer highConsumptionRate = 10;

    // 是否从数据库查询Bot
    @Builder.Default
    private Boolean usePersistedBots = true;
}
