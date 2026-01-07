package com.liveroom.mock.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模拟主播创建结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MockAnchorResult {

    private Long userId;

    private String nickname;

    private Long liveRoomId;

    private String traceId;
}
