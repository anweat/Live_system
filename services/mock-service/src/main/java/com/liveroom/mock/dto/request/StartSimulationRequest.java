package com.liveroom.mock.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 启动模拟任务请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartSimulationRequest {

    @NotNull(message = "直播间ID不能为空")
    private Long liveRoomId;

    @NotNull(message = "观众数量不能为空")
    @Min(value = 1, message = "至少需要1个观众")
    @Max(value = 1000, message = "单次模拟最多1000个观众")
    private Integer audienceCount;

    @NotNull(message = "持续时间不能为空")
    @Min(value = 10, message = "持续时间至少10秒")
    @Max(value = 3600, message = "持续时间最多3600秒")
    private Integer durationSeconds;

    private Boolean simulateEnter;

    private Boolean simulateLeave;

    private Boolean simulateMessage;

    private Boolean simulateRecharge;

    @Min(value = 0, message = "打赏概率不能小于0")
    @Max(value = 100, message = "打赏概率不能大于100")
    private Integer rechargeProbability;
}
