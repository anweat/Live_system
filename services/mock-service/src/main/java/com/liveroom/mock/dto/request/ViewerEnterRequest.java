package com.liveroom.mock.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * 观众进入直播间请求（发送给主播服务）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViewerEnterRequest {

    @NotNull(message = "直播间ID不能为空")
    private Long liveRoomId;

    @NotNull(message = "观众ID不能为空")
    private Long audienceId;

    private String nickname;
}
