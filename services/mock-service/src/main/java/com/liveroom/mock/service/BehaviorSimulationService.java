package com.liveroom.mock.service;

import com.liveroom.mock.config.MockProperties;
import com.liveroom.mock.dto.SimulationRequestDTO;
import com.liveroom.mock.service.AudienceMockService.MockAudienceDTO;
import com.liveroom.mock.service.LiveRoomMockService.MockLiveRoomDTO;
import com.liveroom.mock.util.RandomDataGenerator;
import common.exception.BusinessException;
import common.logger.TraceLogger;
import lombok.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

/**
 * 行为模拟服务
 * 模拟观众进入、离开、发送弹幕、打赏等行为
 */
@Service
@RequiredArgsConstructor
public class BehaviorSimulationService {

    private final MockProperties mockProperties;
    private final RandomDataGenerator randomDataGenerator;
    private final AudienceMockService audienceMockService;
    
    // 存储正在进行的模拟任务
    private final Map<String, SimulationTask> runningTasks = new ConcurrentHashMap<>();
    
    // 线程池
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    /**
     * 启动行为模拟
     */
    @Async
    public SimulationResult startSimulation(MockLiveRoomDTO liveRoom, SimulationRequestDTO request) {
        String taskId = UUID.randomUUID().toString();
        TraceLogger.info("BehaviorSimulationService", "startSimulation", 
                "启动行为模拟，任务ID: " + taskId + ", 直播间ID: " + liveRoom.getLiveRoomId());

        SimulationTask task = new SimulationTask();
        task.setTaskId(taskId);
        task.setLiveRoomId(liveRoom.getLiveRoomId());
        task.setStartTime(LocalDateTime.now());
        task.setStatus("RUNNING");
        runningTasks.put(taskId, task);

        SimulationResult result = new SimulationResult();
        result.setTaskId(taskId);
        result.setLiveRoomId(liveRoom.getLiveRoomId());

        try {
            // 创建Bot观众
            List<MockAudienceDTO> bots = audienceMockService.batchCreateBots(
                    com.liveroom.mock.dto.BatchCreateBotsRequestDTO.builder()
                            .count(request.getAudienceCount())
                            .assignRandomTags(true)
                            .assignConsumptionLevel(true)
                            .build()
            );

            result.setCreatedBots(bots.size());
            task.setAudiences(bots);

            // 计算模拟结束时间
            long endTime = System.currentTimeMillis() + (request.getDurationSeconds() * 1000L);

            // 为每个Bot启动行为模拟
            List<Future<BotBehaviorStats>> futures = new ArrayList<>();
            for (MockAudienceDTO bot : bots) {
                Future<BotBehaviorStats> future = executorService.submit(() -> 
                        simulateBotBehavior(bot, liveRoom, request, endTime, task)
                );
                futures.add(future);
            }

            // 等待所有模拟完成
            int totalEnters = 0, totalLeaves = 0, totalMessages = 0, totalRecharges = 0;
            BigDecimal totalRechargeAmount = BigDecimal.ZERO;

            for (Future<BotBehaviorStats> future : futures) {
                try {
                    BotBehaviorStats stats = future.get();
                    totalEnters += stats.getEnterCount();
                    totalLeaves += stats.getLeaveCount();
                    totalMessages += stats.getMessageCount();
                    totalRecharges += stats.getRechargeCount();
                    totalRechargeAmount = totalRechargeAmount.add(stats.getTotalRechargeAmount());
                } catch (Exception e) {
                    TraceLogger.error("BehaviorSimulationService", "startSimulation", 
                            "等待Bot行为统计失败", e);
                }
            }

            result.setTotalEnters(totalEnters);
            result.setTotalLeaves(totalLeaves);
            result.setTotalMessages(totalMessages);
            result.setTotalRecharges(totalRecharges);
            result.setTotalRechargeAmount(totalRechargeAmount);
            result.setStatus("COMPLETED");

            task.setStatus("COMPLETED");
            task.setEndTime(LocalDateTime.now());

            TraceLogger.info("BehaviorSimulationService", "startSimulation", 
                    String.format("模拟完成，任务ID: %s, 进入: %d, 离开: %d, 弹幕: %d, 打赏: %d次/%.2f元",
                            taskId, totalEnters, totalLeaves, totalMessages, totalRecharges, 
                            totalRechargeAmount.doubleValue()));

        } catch (Exception e) {
            TraceLogger.error("BehaviorSimulationService", "startSimulation", "行为模拟失败", e);
            result.setStatus("FAILED");
            result.setErrorMessage(e.getMessage());
            task.setStatus("FAILED");
        }

        return result;
    }

    /**
     * 模拟单个Bot的行为
     */
    private BotBehaviorStats simulateBotBehavior(
            MockAudienceDTO bot, 
            MockLiveRoomDTO liveRoom,
            SimulationRequestDTO request,
            long endTime,
            SimulationTask task) {

        BotBehaviorStats stats = new BotBehaviorStats();

        try {
            // 1. 模拟进入
            if (request.getSimulateEnter()) {
                Thread.sleep(randomDataGenerator.generateDelay(
                        mockProperties.getSimulation().getEnterIntervalMin(),
                        mockProperties.getSimulation().getEnterIntervalMax()
                ));
                simulateEnter(bot, liveRoom);
                stats.incrementEnter();
            }

            // 2. 在直播间内的行为循环
            while (System.currentTimeMillis() < endTime && "RUNNING".equals(task.getStatus())) {
                // 模拟发送弹幕
                if (request.getSimulateMessage() && randomDataGenerator.happens(80)) {
                    Thread.sleep(randomDataGenerator.generateDelay(
                            mockProperties.getSimulation().getMessageIntervalMin(),
                            mockProperties.getSimulation().getMessageIntervalMax()
                    ));
                    simulateMessage(bot, liveRoom);
                    stats.incrementMessage();
                }

                // 模拟打赏
                if (request.getSimulateRecharge() && 
                    randomDataGenerator.happens(request.getRechargeProbability())) {
                    Thread.sleep(randomDataGenerator.generateDelay(
                            mockProperties.getSimulation().getRechargeIntervalMin(),
                            mockProperties.getSimulation().getRechargeIntervalMax()
                    ));
                    BigDecimal amount = simulateRecharge(bot, liveRoom);
                    stats.incrementRecharge(amount);
                }

                // 短暂休眠
                Thread.sleep(1000);
            }

            // 3. 模拟离开
            if (request.getSimulateLeave()) {
                Thread.sleep(randomDataGenerator.generateDelay(
                        mockProperties.getSimulation().getLeaveIntervalMin(),
                        mockProperties.getSimulation().getLeaveIntervalMax()
                ));
                simulateLeave(bot, liveRoom);
                stats.incrementLeave();
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            TraceLogger.warn("BehaviorSimulationService", "simulateBotBehavior", 
                    "Bot行为模拟被中断: " + bot.getNickname());
        } catch (Exception e) {
            TraceLogger.error("BehaviorSimulationService", "simulateBotBehavior", 
                    "Bot行为模拟异常: " + bot.getNickname(), e);
        }

        return stats;
    }

    /**
     * 模拟进入直播间
     */
    private void simulateEnter(MockAudienceDTO audience, MockLiveRoomDTO liveRoom) {
        TraceLogger.debug("BehaviorSimulationService", "simulateEnter", 
                audience.getNickname() + " 进入直播间 " + liveRoom.getLiveRoomId());
        // 这里可以调用实际的进入直播间API
    }

    /**
     * 模拟离开直播间
     */
    private void simulateLeave(MockAudienceDTO audience, MockLiveRoomDTO liveRoom) {
        TraceLogger.debug("BehaviorSimulationService", "simulateLeave", 
                audience.getNickname() + " 离开直播间 " + liveRoom.getLiveRoomId());
        // 这里可以调用实际的离开直播间API
    }

    /**
     * 模拟发送弹幕
     */
    private void simulateMessage(MockAudienceDTO audience, MockLiveRoomDTO liveRoom) {
        String message = randomDataGenerator.generateMessage();
        TraceLogger.debug("BehaviorSimulationService", "simulateMessage", 
                String.format("%s 在直播间 %d 发送弹幕: %s", 
                        audience.getNickname(), liveRoom.getLiveRoomId(), message));
        // 这里可以调用实际的发送弹幕API
    }

    /**
     * 模拟打赏
     */
    private BigDecimal simulateRecharge(MockAudienceDTO audience, MockLiveRoomDTO liveRoom) {
        double amount = randomDataGenerator.generateRechargeAmount(
                mockProperties.getRandom().getRechargeMin(),
                mockProperties.getRandom().getRechargeMax()
        );
        BigDecimal rechargeAmount = BigDecimal.valueOf(amount);
        
        TraceLogger.info("BehaviorSimulationService", "simulateRecharge", 
                String.format("%s 在直播间 %d 打赏 %.2f 元", 
                        audience.getNickname(), liveRoom.getLiveRoomId(), amount));
        // 这里可以调用实际的打赏API
        
        return rechargeAmount;
    }

    /**
     * 停止模拟任务
     */
    public void stopSimulation(String taskId) {
        SimulationTask task = runningTasks.get(taskId);
        if (task != null) {
            task.setStatus("STOPPED");
            task.setEndTime(LocalDateTime.now());
            TraceLogger.info("BehaviorSimulationService", "stopSimulation", "停止模拟任务: " + taskId);
        }
    }

    /**
     * 获取任务状态
     */
    public SimulationTask getTaskStatus(String taskId) {
        return runningTasks.get(taskId);
    }

    /**
     * 模拟任务
     */
    @Data
    public static class SimulationTask {
        private String taskId;
        private Long liveRoomId;
        private String status;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private List<MockAudienceDTO> audiences;
    }

    /**
     * 模拟结果
     */
    @Data
    public static class SimulationResult {
        private String taskId;
        private Long liveRoomId;
        private Integer createdBots;
        private Integer totalEnters;
        private Integer totalLeaves;
        private Integer totalMessages;
        private Integer totalRecharges;
        private BigDecimal totalRechargeAmount;
        private String status;
        private String errorMessage;
    }

    /**
     * Bot行为统计
     */
    @Data
    private static class BotBehaviorStats {
        private int enterCount = 0;
        private int leaveCount = 0;
        private int messageCount = 0;
        private int rechargeCount = 0;
        private BigDecimal totalRechargeAmount = BigDecimal.ZERO;

        public void incrementEnter() {
            enterCount++;
        }

        public void incrementLeave() {
            leaveCount++;
        }

        public void incrementMessage() {
            messageCount++;
        }

        public void incrementRecharge(BigDecimal amount) {
            rechargeCount++;
            totalRechargeAmount = totalRechargeAmount.add(amount);
        }
    }
}
