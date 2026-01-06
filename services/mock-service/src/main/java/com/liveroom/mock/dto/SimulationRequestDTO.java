package com.liveroom.mock.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.*;

/**
 * 模拟行为请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulationRequestDTO {

    @NotNull(message = "直播间ID不能为空")
    private Long liveRoomId;

    // 模拟的观众数量
    @Min(value = 1, message = "至少模拟1个观众")
    @Max(value = 100, message = "单次最多模拟100个观众")
    @Builder.Default
    private Integer audienceCount = 10;

    // 模拟持续时间（秒）
    @Min(value = 10, message = "模拟时间至少10秒")
    @Max(value = 3600, message = "模拟时间最多1小时")
    @Builder.Default
    private Integer durationSeconds = 300;

    // 是否模拟进入
    @Builder.Default
    private Boolean simulateEnter = true;

    // 是否模拟离开
    @Builder.Default
    private Boolean simulateLeave = true;

    // 是否模拟弹幕
    @Builder.Default
    private Boolean simulateMessage = true;

    // 是否模拟打赏
    @Builder.Default
    private Boolean simulateRecharge = true;

    // 打赏概率（0-100）
    @Min(value = 0, message = "概率不能小于0")
    @Max(value = 100, message = "概率不能大于100")
    @Builder.Default
    private Integer rechargeProbability = 20;
}
