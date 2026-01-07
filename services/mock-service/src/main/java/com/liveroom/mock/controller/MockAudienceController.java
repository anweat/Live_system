package com.liveroom.mock.controller;

import com.liveroom.mock.dto.request.BatchCreateBotsRequest;
import com.liveroom.mock.dto.response.MockBatchResult;
import com.liveroom.mock.service.MockAudienceService;
import common.response.BaseResponse;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 模拟观众 Controller
 */
@RestController
@RequestMapping("/api/v1/mock/audience")
@AllArgsConstructor
@Validated
public class MockAudienceController {

    private final MockAudienceService audienceService;

    /**
     * 批量创建Bot观众
     */
    @PostMapping("/batch-create-bots")
    public BaseResponse<MockBatchResult> batchCreateBots(@Valid @RequestBody BatchCreateBotsRequest request) {
        MockBatchResult result = audienceService.batchCreateBots(request);
        return BaseResponse.success("批量创建Bot观众任务已启动", result);
    }

    /**
     * 查询可用Bot观众数量
     */
    @GetMapping("/bots/count")
    public BaseResponse<Map<String, Object>> countBots() {
        long count = audienceService.countBots();
        Map<String, Object> data = new HashMap<>();
        data.put("count", count);
        return BaseResponse.success("查询成功", data);
    }

    /**
     * 获取可用Bot观众ID列表
     */
    @GetMapping("/bots/available")
    public BaseResponse<List<Long>> getAvailableBotIds(
            @RequestParam(defaultValue = "0") Integer limit) {
        List<Long> botIds = audienceService.getAvailableBotIds(limit);
        return BaseResponse.success("查询成功", botIds);
    }
}
