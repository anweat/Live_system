package com.liveroom.mock.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模拟任务结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MockSimulationResult {

    private String taskId;

    private String status;

    private Integer audienceCount;

    private String traceId;

    private Integer progress;
}
