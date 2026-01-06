package com.liveroom.mock.service;

import com.liveroom.mock.config.MockProperties;
import com.liveroom.mock.dto.*;
import com.liveroom.mock.service.AnchorMockService.MockAnchorDTO;
import com.liveroom.mock.service.AudienceMockService.MockAudienceDTO;
import com.liveroom.mock.service.LiveRoomMockService.MockLiveRoomDTO;
import com.liveroom.mock.service.BehaviorSimulationService.SimulationResult;
import common.exception.BusinessException;
import common.logger.TraceLogger;
import lombok.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 批量测试服务
 * 支持大规模直播间和观众的测试场景
 */
@Service
@RequiredArgsConstructor
public class BatchTestService {

    private final AnchorMockService anchorMockService;
    private final LiveRoomMockService liveRoomMockService;
    private final AudienceMockService audienceMockService;
    private final BotPersistenceService botPersistenceService;
    private final LiveRoomPersistenceService liveRoomPersistenceService;
    private final SmartAudienceAssignmentService smartAudienceAssignmentService;
    private final BehaviorSimulationService behaviorSimulationService;
    private final MockProperties mockProperties;

    // 使用更大的线程池支持高并发
    private final ExecutorService batchExecutor = Executors.newFixedThreadPool(50);
    
    // 存储批量测试任务
    private final Map<String, BatchTestTask> runningTasks = new ConcurrentHashMap<>();

    /**
     * 批量创建直播间并分配观众
     */
    @Async
    public BatchTestResult batchCreateLiveRoomsWithAudience(BatchLiveRoomTestRequestDTO request) {
        String taskId = UUID.randomUUID().toString();
        TraceLogger.info("BatchTestService", "batchCreateLiveRoomsWithAudience", 
                String.format("启动批量测试任务 %s: %d个直播间, 每间%d观众, 总计%d观众",
                        taskId, request.getLiveRoomCount(), request.getAudiencePerRoom(),
                        request.getLiveRoomCount() * request.getAudiencePerRoom()));

        BatchTestTask task = new BatchTestTask();
        task.setTaskId(taskId);
        task.setStartTime(LocalDateTime.now());
        task.setStatus("RUNNING");
        task.setTotalLiveRooms(request.getLiveRoomCount());
        task.setTotalAudiences(request.getLiveRoomCount() * request.getAudiencePerRoom());
        runningTasks.put(taskId, task);

        BatchTestResult result = new BatchTestResult();
        result.setTaskId(taskId);
        result.setStartTime(LocalDateTime.now());

        try {
            // 1. 预创建Bot（如果需要）
            if (request.getPreCreateBots() > 0) {
                TraceLogger.info("BatchTestService", "batchCreateLiveRoomsWithAudience", 
                        "预创建 " + request.getPreCreateBots() + " 个Bot");
                
                List<MockAudienceDTO> bots = audienceMockService.batchCreateBots(
                        BatchCreateBotsRequestDTO.builder()
                                .count(request.getPreCreateBots())
                                .assignRandomTags(true)
                                .assignConsumptionLevel(true)
                                .build()
                );
                
                if (request.getPersistData()) {
                    botPersistenceService.saveBots(bots);
                }
                result.setPreCreatedBots(bots.size());
            }

            // 2. 批量创建主播和直播间
            List<MockLiveRoomDTO> liveRooms = new ArrayList<>();
            for (int i = 0; i < request.getLiveRoomCount(); i++) {
                MockAnchorDTO anchor = anchorMockService.createMockAnchor(
                        CreateAnchorRequestDTO.builder()
                                .anchorName("测试主播_" + (i + 1))
                                .build()
                );
                
                MockLiveRoomDTO liveRoom = liveRoomMockService.createDefaultLiveRoom(anchor);
                liveRooms.add(liveRoom);
            }

            if (request.getPersistData()) {
                liveRoomPersistenceService.saveLiveRooms(liveRooms);
            }

            result.setCreatedLiveRooms(liveRooms.size());
            task.setCreatedLiveRooms(liveRooms.size());

            // 3. 并发为每个直播间分配观众
            List<Future<AudienceAssignmentResult>> futures = new ArrayList<>();
            for (MockLiveRoomDTO liveRoom : liveRooms) {
                Future<AudienceAssignmentResult> future = batchExecutor.submit(() -> 
                        assignAudienceToLiveRoom(liveRoom, request)
                );
                futures.add(future);
            }

            // 4. 等待所有分配完成并统计
            int totalAssigned = 0;
            for (Future<AudienceAssignmentResult> future : futures) {
                try {
                    AudienceAssignmentResult assignResult = future.get(30, TimeUnit.SECONDS);
                    totalAssigned += assignResult.getAssignedCount();
                } catch (Exception e) {
                    TraceLogger.error("BatchTestService", "batchCreateLiveRoomsWithAudience", 
                            "分配观众失败", e);
                }
            }

            result.setTotalAssignedAudiences(totalAssigned);
            task.setAssignedAudiences(totalAssigned);

            // 5. 启动行为模拟（如果需要）
            if (request.getSimulateBehavior()) {
                TraceLogger.info("BatchTestService", "batchCreateLiveRoomsWithAudience", 
                        "启动行为模拟");
                
                List<Future<SimulationResult>> simFutures = new ArrayList<>();
                for (MockLiveRoomDTO liveRoom : liveRooms) {
                    Future<SimulationResult> future = batchExecutor.submit(() -> {
                        SimulationRequestDTO simRequest = SimulationRequestDTO.builder()
                                .liveRoomId(liveRoom.getLiveRoomId())
                                .audienceCount(request.getAudiencePerRoom())
                                .durationSeconds(request.getDurationSeconds())
                                .simulateEnter(true)
                                .simulateLeave(false)
                                .simulateMessage(true)
                                .simulateRecharge(true)
                                .rechargeProbability(request.getRechargeProbability())
                                .build();
                        
                        return behaviorSimulationService.startSimulation(liveRoom, simRequest);
                    });
                    simFutures.add(future);
                }

                // 等待所有模拟启动
                for (Future<SimulationResult> future : simFutures) {
                    future.get(10, TimeUnit.SECONDS);
                }
            }

            result.setStatus("COMPLETED");
            result.setEndTime(LocalDateTime.now());
            task.setStatus("COMPLETED");
            task.setEndTime(LocalDateTime.now());

            TraceLogger.info("BatchTestService", "batchCreateLiveRoomsWithAudience", 
                    String.format("批量测试任务完成: 创建%d个直播间, 分配%d个观众",
                            result.getCreatedLiveRooms(), result.getTotalAssignedAudiences()));

        } catch (Exception e) {
            TraceLogger.error("BatchTestService", "batchCreateLiveRoomsWithAudience", 
                    "批量测试任务失败", e);
            result.setStatus("FAILED");
            result.setErrorMessage(e.getMessage());
            task.setStatus("FAILED");
        }

        return result;
    }

    /**
     * 为单个直播间分配观众
     */
    private AudienceAssignmentResult assignAudienceToLiveRoom(
            MockLiveRoomDTO liveRoom, BatchLiveRoomTestRequestDTO request) {
        
        AudienceAssignmentResult result = new AudienceAssignmentResult();
        result.setLiveRoomId(liveRoom.getLiveRoomId());

        try {
            List<MockAudienceDTO> assignedAudiences;

            if (request.getUseTagMatching()) {
                // 使用智能分配
                assignedAudiences = smartAudienceAssignmentService.assignAudiencesByTags(
                        liveRoom, request.getAudiencePerRoom()
                );
            } else {
                // 随机分配
                if (request.getPersistData()) {
                    assignedAudiences = botPersistenceService.findRandomBots(request.getAudiencePerRoom());
                } else {
                    // 创建临时Bot
                    assignedAudiences = audienceMockService.batchCreateBots(
                            BatchCreateBotsRequestDTO.builder()
                                    .count(request.getAudiencePerRoom())
                                    .build()
                    );
                }
            }

            result.setAssignedCount(assignedAudiences.size());
            result.setStatus("SUCCESS");

            // 更新直播间观众数
            if (request.getPersistData()) {
                liveRoomPersistenceService.updateAudienceCount(
                        liveRoom.getLiveRoomId(), 
                        assignedAudiences.size(), 
                        assignedAudiences.size()
                );
            }

        } catch (Exception e) {
            TraceLogger.error("BatchTestService", "assignAudienceToLiveRoom", 
                    "分配观众失败: " + liveRoom.getLiveRoomId(), e);
            result.setStatus("FAILED");
            result.setAssignedCount(0);
        }

        return result;
    }

    /**
     * 获取任务状态
     */
    public BatchTestTask getTaskStatus(String taskId) {
        return runningTasks.get(taskId);
    }

    /**
     * 停止批量测试任务
     */
    public void stopTask(String taskId) {
        BatchTestTask task = runningTasks.get(taskId);
        if (task != null) {
            task.setStatus("STOPPED");
            task.setEndTime(LocalDateTime.now());
        }
    }

    /**
     * 批量测试任务
     */
    @Data
    public static class BatchTestTask {
        private String taskId;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String status;
        private Integer totalLiveRooms;
        private Integer createdLiveRooms;
        private Integer totalAudiences;
        private Integer assignedAudiences;
    }

    /**
     * 批量测试结果
     */
    @Data
    public static class BatchTestResult {
        private String taskId;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String status;
        private Integer preCreatedBots;
        private Integer createdLiveRooms;
        private Integer totalAssignedAudiences;
        private String errorMessage;
    }

    /**
     * 观众分配结果
     */
    @Data
    private static class AudienceAssignmentResult {
        private Long liveRoomId;
        private Integer assignedCount;
        private String status;
    }
}
