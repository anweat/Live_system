package com.liveroom.mock.controller;

import com.liveroom.mock.dto.request.CreateMockAnchorRequest;
import com.liveroom.mock.dto.response.MockAnchorResult;
import com.liveroom.mock.dto.response.MockBatchResult;
import com.liveroom.mock.service.MockAnchorService;
import common.response.BaseResponse;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * 模拟主播 Controller
 */
@RestController
@RequestMapping("/api/v1/mock/anchor")
@AllArgsConstructor
@Validated
public class MockAnchorController {

    private final MockAnchorService anchorService;

    /**
     * 创建单个模拟主播
     */
    @PostMapping("/create")
    public BaseResponse<MockAnchorResult> createMockAnchor(@Valid @RequestBody CreateMockAnchorRequest request) {
        MockAnchorResult result = anchorService.createMockAnchor(request);
        return BaseResponse.success("创建模拟主播成功", result);
    }

    /**
     * 批量创建模拟主播
     */
    @PostMapping("/batch-create")
    public BaseResponse<MockBatchResult> batchCreateMockAnchors(
            @RequestParam @Min(1) @Max(500) Integer count) {
        MockBatchResult result = anchorService.batchCreateMockAnchors(count);
        return BaseResponse.success("批量创建主播任务已启动", result);
    }
}
