package com.liveroom.mock.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.*;

/**
 * 调整直播间观众数量请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdjustAudienceCountRequestDTO {

    @NotNull(message = "直播间ID不能为空")
    private Long liveRoomId;

    @NotNull(message = "目标观众数量不能为空")
    @Min(value = 0, message = "观众数量不能小于0")
    private Integer targetCount;

    @Builder.Default
    private Boolean useBotsOnly = true;

    @Builder.Default
    private Boolean assignRandomTags = true;
}
