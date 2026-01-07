package com.liveroom.mock.controller;

import com.liveroom.mock.dto.request.StartSimulationRequest;
import com.liveroom.mock.dto.response.MockSimulationResult;
import com.liveroom.mock.entity.MockSimulationTask;
import com.liveroom.mock.service.MockSimulationService;
import common.response.BaseResponse;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 模拟任务 Controller
 */
@RestController
@RequestMapping("/api/v1/mock/simulation")
@AllArgsConstructor
@Validated
public class MockSimulationController {

    private final MockSimulationService simulationService;

    /**
     * 启动行为模拟
     */
    @PostMapping("/start")
    public BaseResponse<MockSimulationResult> startSimulation(@Valid @RequestBody StartSimulationRequest request) {
        MockSimulationResult result = simulationService.startSimulation(request);
        return BaseResponse.success("模拟任务已启动", result);
    }

    /**
     * 查询模拟任务状态
     */
    @GetMapping("/task/{taskId}")
    public BaseResponse<MockSimulationTask> getTaskStatus(@PathVariable String taskId) {
        MockSimulationTask task = simulationService.getTaskStatus(taskId);
        if (task == null) {
            return BaseResponse.error(404, "任务不存在");
        }
        return BaseResponse.success("查询成功", task);
    }
}
