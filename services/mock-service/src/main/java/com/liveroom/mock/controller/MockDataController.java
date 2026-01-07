package com.liveroom.mock.controller;

import com.liveroom.mock.dto.response.MockDataStatistics;
import com.liveroom.mock.entity.MockBatchInfo;
import com.liveroom.mock.service.MockBatchInfoService;
import com.liveroom.mock.service.MockDataTrackingService;
import common.response.BaseResponse;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 模拟数据管理 Controller
 */
@RestController
@RequestMapping("/api/v1/mock/data")
@AllArgsConstructor
public class MockDataController {

    private final MockDataTrackingService trackingService;
    private final MockBatchInfoService batchInfoService;

    /**
     * 查询模拟数据统计
     */
    @GetMapping("/statistics")
    public BaseResponse<MockDataStatistics> getStatistics() {
        Map<String, Long> entityTypeCount = trackingService.countByEntityType();

        MockDataStatistics statistics = MockDataStatistics.builder()
                .totalAnchors(entityTypeCount.getOrDefault("ANCHOR", 0L))
                .totalAudiences(entityTypeCount.getOrDefault("AUDIENCE", 0L))
                .totalLiveRooms(entityTypeCount.getOrDefault("LIVE_ROOM", 0L))
                .totalRecharges(entityTypeCount.getOrDefault("RECHARGE", 0L))
                .totalBatches((int) batchInfoService.findAll().size())
                .entityTypeCount(entityTypeCount)
                .build();

        return BaseResponse.success("查询成功", statistics);
    }

    /**
     * 查询所有批次信息
     */
    @GetMapping("/batches")
    public BaseResponse<List<MockBatchInfo>> getAllBatches() {
        List<MockBatchInfo> batches = batchInfoService.findAll();
        return BaseResponse.success("查询成功", batches);
    }

    /**
     * 根据批次ID查询批次信息
     */
    @GetMapping("/batch/{batchId}")
    public BaseResponse<MockBatchInfo> getBatchInfo(@PathVariable String batchId) {
        MockBatchInfo batchInfo = batchInfoService.findByBatchId(batchId);
        if (batchInfo == null) {
            return BaseResponse.error(404, "批次不存在");
        }
        return BaseResponse.success("查询成功", batchInfo);
    }

    /**
     * 根据批次类型查询
     */
    @GetMapping("/batches/type/{batchType}")
    public BaseResponse<List<MockBatchInfo>> getBatchesByType(@PathVariable String batchType) {
        List<MockBatchInfo> batches = batchInfoService.findByBatchType(batchType);
        return BaseResponse.success("查询成功", batches);
    }
}
