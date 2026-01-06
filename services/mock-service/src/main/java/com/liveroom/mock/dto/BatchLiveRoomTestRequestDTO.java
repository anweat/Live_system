package com.liveroom.mock.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.*;
import java.util.List;

/**
 * 批量直播间测试请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchLiveRoomTestRequestDTO {

    @NotNull(message = "直播间数量不能为空")
    @Min(value = 1, message = "至少创建1个直播间")
    @Max(value = 500, message = "单次最多创建500个直播间")
    private Integer liveRoomCount;

    @NotNull(message = "每个直播间的观众数量不能为空")
    @Min(value = 1, message = "每个直播间至少1个观众")
    @Max(value = 10000, message = "每个直播间最多10000个观众")
    private Integer audiencePerRoom;

    // 是否保存到数据库
    @Builder.Default
    private Boolean persistData = true;

    // 是否使用标签匹配
    @Builder.Default
    private Boolean useTagMatching = true;

    // 模拟持续时间（秒）
    @Min(value = 60, message = "模拟时间至少60秒")
    @Builder.Default
    private Integer durationSeconds = 300;

    // 是否模拟行为
    @Builder.Default
    private Boolean simulateBehavior = true;

    // 打赏概率
    @Min(value = 0)
    @Max(value = 100)
    @Builder.Default
    private Integer rechargeProbability = 15;

    // 预创建Bot数量（如果数据库Bot不足）
    @Builder.Default
    private Integer preCreateBots = 0;
}
