package com.liveroom.mock.controller;

import com.liveroom.mock.dto.*;
import com.liveroom.mock.service.*;
import com.liveroom.mock.service.AnchorMockService.MockAnchorDTO;
import com.liveroom.mock.service.AudienceMockService.MockAudienceDTO;
import com.liveroom.mock.service.LiveRoomMockService.MockLiveRoomDTO;
import com.liveroom.mock.service.BehaviorSimulationService.SimulationResult;
import common.response.BaseResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mock Service 主控制器
 */
@Api(tags = "Mock Service - 模拟测试服务")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MockController {

    private final AnchorMockService anchorMockService;
    private final AudienceMockService audienceMockService;
    private final LiveRoomMockService liveRoomMockService;
    private final BehaviorSimulationService behaviorSimulationService;
    private final BatchTestService batchTestService;
    private final BotPersistenceService botPersistenceService;
    private final SmartAudienceAssignmentService smartAudienceAssignmentService;

    // ==================== 主播相关 ====================

    @ApiOperation("创建模拟主播")
    @PostMapping("/anchor/create")
    public BaseResponse<MockAnchorDTO> createAnchor(@Validated @RequestBody CreateAnchorRequestDTO request) {
        MockAnchorDTO anchor = anchorMockService.createMockAnchor(request);
        return BaseResponse.success(anchor);
    }

    @ApiOperation("批量创建随机主播")
    @PostMapping("/anchor/batch-create")
    public BaseResponse<List<MockAnchorDTO>> batchCreateAnchors(@RequestParam(defaultValue = "10") Integer count) {
        List<MockAnchorDTO> anchors = anchorMockService.batchCreateRandomAnchors(count);
        return BaseResponse.success(anchors);
    }

    // ==================== 直播间相关 ====================

    @ApiOperation("创建直播间")
    @PostMapping("/live-room/create")
    public BaseResponse<MockLiveRoomDTO> createLiveRoom(@Validated @RequestBody CreateLiveRoomRequestDTO request) {
        MockLiveRoomDTO liveRoom = liveRoomMockService.createMockLiveRoom(request);
        return BaseResponse.success(liveRoom);
    }

    @ApiOperation("为主播创建默认直播间")
    @PostMapping("/live-room/create-default")
    public BaseResponse<MockLiveRoomDTO> createDefaultLiveRoom(@RequestParam Long anchorId, 
                                                                 @RequestParam String anchorName) {
        MockAnchorDTO anchor = MockAnchorDTO.builder()
                .anchorId(anchorId)
                .anchorName(anchorName)
                .build();
        MockLiveRoomDTO liveRoom = liveRoomMockService.createDefaultLiveRoom(anchor);
        return BaseResponse.success(liveRoom);
    }

    // ==================== 观众相关 ====================

    @ApiOperation("创建模拟观众")
    @PostMapping("/audience/create")
    public BaseResponse<MockAudienceDTO> createAudience(@Validated @RequestBody CreateAudienceRequestDTO request) {
        MockAudienceDTO audience = audienceMockService.createMockAudience(request);
        return BaseResponse.success(audience);
    }

    @ApiOperation("批量创建Bot观众")
    @PostMapping("/audience/batch-create-bots")
    public BaseResponse<List<MockAudienceDTO>> batchCreateBots(@Validated @RequestBody BatchCreateBotsRequestDTO request) {
        List<MockAudienceDTO> bots = audienceMockService.batchCreateBots(request);
        return BaseResponse.success(bots);
    }

    @ApiOperation("批量创建Bot并保存到数据库")
    @PostMapping("/audience/batch-create-and-persist")
    public BaseResponse<Map<String, Object>> batchCreateAndPersist(@Validated @RequestBody BatchCreateBotsRequestDTO request) {
        List<MockAudienceDTO> bots = audienceMockService.batchCreateBots(request);
        List<Long> ids = botPersistenceService.saveBots(bots);
        
        Map<String, Object> result = new HashMap<>();
        result.put("createdCount", bots.size());
        result.put("savedIds", ids);
        result.put("bots", bots);
        return BaseResponse.success(result);
    }

    @ApiOperation("从数据库查询Bot（根据标签）")
    @GetMapping("/audience/find-by-tag")
    public BaseResponse<List<MockAudienceDTO>> findBotsByTag(
            @RequestParam String tag, 
            @RequestParam(defaultValue = "100") Integer limit) {
        List<MockAudienceDTO> bots = botPersistenceService.findBotsByTag(tag, limit);
        return BaseResponse.success(bots);
    }

    @ApiOperation("从数据库查询随机Bot")
    @GetMapping("/audience/find-random")
    public BaseResponse<List<MockAudienceDTO>> findRandomBots(
            @RequestParam(defaultValue = "100") Integer limit) {
        List<MockAudienceDTO> bots = botPersistenceService.findRandomBots(limit);
        return BaseResponse.success(bots);
    }

    @ApiOperation("统计数据库中Bot数量")
    @GetMapping("/audience/c批量测试相关 ====================

    @ApiOperation("批量创建直播间并分配观众（支持大规模测试）")
    @PostMapping("/batch/create-live-rooms")
    public BaseResponse<BatchTestService.BatchTestResult> batchCreateLiveRooms(
            @Validated @RequestBody BatchLiveRoomTestRequestDTO request) {
        BatchTestService.BatchTestResult result = batchTestService.batchCreateLiveRoomsWithAudience(request);
        return BaseResponse.success(result);
    }

    @ApiOperation("查询批量测试任务状态")
    @GetMapping("/batch/task-status/{taskId}")
    public BaseResponse<BatchTestService.BatchTestTask> getBatchTaskStatus(@PathVariable String taskId) {
        BatchTestService.BatchTestTask task = batchTestService.getTaskStatus(taskId);
        return BaseResponse.success(task);
    }

    @ApiOperation("停止批量测试任务")
    @PostMapping("/batch/stop-task/{taskId}")
    public BaseResponse<String> stopBatchTask(@PathVariable String taskId) {
        batchTestService.stopTask(taskId);
        return BaseResponse.success("批量测试任务已停止");
    }

    @ApiOperation("智能分配观众（基于标签匹配）")
    @PostMapping("/batch/smart-assign")
    public BaseResponse<List<MockAudienceDTO>> smartAssignAudience(
            @Validated @RequestBody SmartAssignAudienceRequestDTO request) {
        List<MockAudienceDTO> audiences = smartAudienceAssignmentService.smartAssign(request);
        return BaseResponse.success(audiences);
    }

    // ==================== ount-bots")
    public BaseResponse<Map<String, Object>> countBots() {
        long totalBots = botPersistenceService.countBots();
        List<Object[]> levelStats = botPersistenceService.countBotsByLevel();
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalBots", totalBots);
        result.put("levelStatistics", levelStats);
        return BaseResponse.success(result);
    }

    // ==================== 行为模拟相关 ====================

    @ApiOperation("启动行为模拟")
    @PostMapping("/simulation/start")
    public BaseResponse<SimulationResult> startSimulation(@Validated @RequestBody SimulationRequestDTO request) {
        // 创建临时直播间对象用于模拟
        MockLiveRoomDTO liveRoom = MockLiveRoomDTO.builder()
                .liveRoomId(request.getLiveRoomId())
                .anchorId(1L)
                .title("模拟直播间")
                .status(1)
                .build();
        
        SimulationResult result = behaviorSimulationService.startSimulation(liveRoom, request);
        return BaseResponse.success(result);
    }

    @ApiOperation("停止行为模拟")
    @PostMapping("/simulation/stop/{taskId}")
    public BaseResponse<String> stopSimulation(@PathVariable String taskId) {
        behaviorSimulationService.stopSimulation(taskId);
        return BaseResponse.success("模拟任务已停止");
    }

    @ApiOperation("查询模拟任务状态")
    @GetMapping("/simulation/status/{taskId}")
    public BaseResponse<BehaviorSimulationService.SimulationTask> getSimulationStatus(@PathVariable String taskId) {
        BehaviorSimulationService.SimulationTask task = behaviorSimulationService.getTaskStatus(taskId);
        return BaseResponse.success(task);
    }

    // ==================== 快捷操作 ====================

    @ApiOperation("一键创建完整场景：主播+直播间+观众+模拟")
    @PostMapping("/quick/complete-scenario")
    public BaseResponse<CompleteScenarioResult> createCompleteScenario(
            @RequestParam(defaultValue = "测试主播") String anchorName,
            @RequestParam(defaultValue = "30") Integer botCount,
            @RequestParam(defaultValue = "300") Integer simulationSeconds) {

        // 1. 创建主播
        MockAnchorDTO anchor = anchorMockService.createMockAnchor(
                CreateAnchorRequestDTO.builder()
                        .anchorName(anchorName)
                        .build()
        );

        // 2. 创建直播间
        MockLiveRoomDTO liveRoom = liveRoomMockService.createDefaultLiveRoom(anchor);

        // 3. 创建Bot观众
        List<MockAudienceDTO> bots = audienceMockService.batchCreateBots(
                BatchCreateBotsRequestDTO.builder()
                        .count(botCount)
                        .assignRandomTags(true)
                        .assignConsumptionLevel(true)
                        .build()
        );

        // 4. 启动行为模拟
        SimulationResult simulationResult = behaviorSimulationService.startSimulation(
                liveRoom,
                SimulationRequestDTO.builder()
                        .liveRoomId(liveRoom.getLiveRoomId())
                        .audienceCount(botCount)
                        .durationSeconds(simulationSeconds)
                        .simulateEnter(true)
                        .simulateLeave(true)
                        .simulateMessage(true)
                        .simulateRecharge(true)
                        .rechargeProbability(20)
                        .build()
        );

        CompleteScenarioResult result = new CompleteScenarioResult();
        result.setAnchor(anchor);
        result.setLiveRoom(liveRoom);
        result.setBotCount(bots.size());
        result.setSimulationResult(simulationResult);

        return BaseResponse.success(result);
    }

    @ApiOperation("超大规模测试：100+直播间 + 300K观众")
    @PostMapping("/quick/massive-test")
    public BaseResponse<BatchTestService.BatchTestResult> massiveTest(
            @RequestParam(defaultValue = "100") Integer liveRoomCount,
            @RequestParam(defaultValue = "3000") Integer audiencePerRoom,
            @RequestParam(defaultValue = "60000") Integer preCreateBots) {
        
        BatchLiveRoomTestRequestDTO request = BatchLiveRoomTestRequestDTO.builder()
                .liveRoomCount(liveRoomCount)
                .audiencePerRoom(audiencePerRoom)
                .preCreateBots(preCreateBots)
                .persistData(true)
                .useTagMatching(true)
                .durationSeconds(600)
                .simulateBehavior(true)
                .rechargeProbability(15)
                .build();
        
        BatchTestService.BatchTestResult result = batchTestService.batchCreateLiveRoomsWithAudience(request);
        return BaseResponse.success(result);
    }

    /**
     * 完整场景结果
     */
    @lombok.Data
    public static class CompleteScenarioResult {
        private MockAnchorDTO anchor;
        private MockLiveRoomDTO liveRoom;
        private Integer botCount;
        private SimulationResult simulationResult;
    }
}
