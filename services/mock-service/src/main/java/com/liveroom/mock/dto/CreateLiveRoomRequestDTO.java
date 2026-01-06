package com.liveroom.mock.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.*;

/**
 * 创建直播间请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLiveRoomRequestDTO {

    @NotNull(message = "主播ID不能为空")
    private Long anchorId;

    @NotBlank(message = "直播间标题不能为空")
    @Size(max = 255, message = "标题不能超过255个字符")
    private String title;

    @Size(max = 1000, message = "描述不能超过1000个字符")
    private String description;

    @Size(max = 50, message = "分类不能超过50个字符")
    private String category;

    @Size(max = 200, message = "封面URL不能超过200个字符")
    private String coverUrl;

    @Min(value = 0, message = "状态值不能小于0")
    @Max(value = 2, message = "状态值不能大于2")
    @Builder.Default
    private Integer status = 1; // 0-未开播、1-直播中、2-已结束
}
