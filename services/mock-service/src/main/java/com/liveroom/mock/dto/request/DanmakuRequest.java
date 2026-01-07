package com.liveroom.mock.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 发送弹幕请求（发送给主播服务）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DanmakuRequest {

    @NotNull(message = "直播间ID不能为空")
    private Long liveRoomId;

    @NotNull(message = "观众ID不能为空")
    private Long audienceId;

    @NotBlank(message = "弹幕内容不能为空")
    private String content;

    private String nickname;
}
