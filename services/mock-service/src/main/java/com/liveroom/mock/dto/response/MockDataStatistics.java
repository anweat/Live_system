package com.liveroom.mock.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 模拟数据统计结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MockDataStatistics {

    private Long totalAnchors;

    private Long totalAudiences;

    private Long totalLiveRooms;

    private Long totalRecharges;

    private Integer totalBatches;

    private Map<String, Long> entityTypeCount;
}
