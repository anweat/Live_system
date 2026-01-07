package com.liveroom.mock.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量创建结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MockBatchResult {

    private String batchId;

    private Integer totalCount;

    private Integer successCount;

    private Integer failCount;

    private List<?> results;
}
