package com.liveroom.mock.service;

import com.liveroom.mock.client.AnchorServiceClient;
import com.liveroom.mock.client.AudienceServiceClient;
import com.liveroom.mock.client.RedisServiceClient;
import com.liveroom.mock.constant.EntityType;
import com.liveroom.mock.constant.TaskStatus;
import com.liveroom.mock.dto.external.RechargeVO;
import com.liveroom.mock.dto.request.DanmakuRequest;
import com.liveroom.mock.dto.request.RechargeRequest;
import com.liveroom.mock.dto.request.StartSimulationRequest;
import com.liveroom.mock.dto.request.ViewerEnterRequest;
import com.liveroom.mock.dto.response.MockSimulationResult;
import com.liveroom.mock.entity.MockSimulationTask;
import com.liveroom.mock.repository.MockSimulationTaskRepository;
import com.liveroom.mock.util.RandomDataGenerator;
import com.liveroom.mock.util.TraceIdGenerator;
import common.constant.ErrorConstants;
import common.exception.BusinessException;
import common.exception.SystemException;
import common.logger.AppLogger;
import common.logger.TraceLogger;
import common.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 模拟行为服务
 * 负责模拟观众的各种行为（进入、离开、弹幕、打赏等）
 */
@Service
@RequiredArgsConstructor
public class MockSimulationService {

    private final AnchorServiceClient anchorClient;
    private final AudienceServiceClient audienceClient;
    private final RedisServiceClient redisClient;
    private final MockAudienceService audienceService;
    private final MockDataTrackingService trackingService;
    private final MockSimulationTaskRepository taskRepository;
    private final RandomDataGenerator randomGenerator;

    @Value("${mock.simulation.enter-interval-min:1000}")
    private long enterIntervalMin;

    @Value("${mock.simulation.enter-interval-max:5000}")
    private long enterIntervalMax;

    @Value("${mock.simulation.message-interval-min:3000}")
    private long messageIntervalMin;

    @Value("${mock.simulation.message-interval-max:10000}")
    private long messageIntervalMax;

    @Value("${mock.simulation.recharge-interval-min:10000}")
    private long rechargeIntervalMin;

    @Value("${mock.simulation.recharge-interval-max:30000}")
    private long rechargeIntervalMax;

    @Value("${mock.random.recharge-min:1.0}")
    private double rechargeMin;

    @Value("${mock.random.recharge-max:1000.0}")
    private double rechargeMax;

    /**
     * 启动模拟任务
     */
    public MockSimulationResult startSimulation(StartSimulationRequest request) {
        String taskId = TraceIdGenerator.generateTaskId();
        String traceId = TraceIdGenerator.generate("SIMULATION");

        TraceLogger.info("simulation", "start", traceId);

        try {
            // 1. 验证直播间
            Long liveRoomId = request.getLiveRoomId();
            
            // 2. 获取可用的Bot观众
            List<Long> audienceIds = audienceService.getAvailableBotIds(request.getAudienceCount());

            if (audienceIds.size() < request.getAudienceCount()) {
                throw new BusinessException(ErrorConstants.BUSINESS_ERROR,
                        String.format("可用Bot观众不足，需要: %d, 实际: %d", 
                                request.getAudienceCount(), audienceIds.size()));
            }

            // 3. 创建模拟任务记录
            MockSimulationTask task = MockSimulationTask.builder()
                    .taskId(taskId)
                    .liveRoomId(liveRoomId)
                    .audienceCount(request.getAudienceCount())
                    .durationSeconds(request.getDurationSeconds())
                    .status(TaskStatus.PENDING)
                    .build();
            taskRepository.save(task);

            // 4. 异步执行模拟任务
            executeSimulationAsync(task, audienceIds, request);

            TraceLogger.info("simulation", "start_success", traceId,
                    "taskId", taskId, "audienceCount", audienceIds.size());

            return MockSimulationResult.builder()
                    .taskId(taskId)
                    .status(TaskStatus.RUNNING)
                    .audienceCount(audienceIds.size())
                    .traceId(traceId)
                    .progress(0)
                    .build();

        } catch (BusinessException e) {
            TraceLogger.error("simulation", "start", traceId, e);
            throw e;
        } catch (Exception e) {
            TraceLogger.error("simulation", "start", traceId, e);
            throw new SystemException(ErrorConstants.SYSTEM_ERROR, "启动模拟任务失败", e);
        }
    }

    /**
     * 异步执行模拟任务
     */
    @Async
    public void executeSimulationAsync(MockSimulationTask task, List<Long> audienceIds,
                                       StartSimulationRequest config) {
        String taskId = task.getTaskId();
        Long liveRoomId = task.getLiveRoomId();

        try {
            AppLogger.info("开始执行模拟任务: {}, 直播间: {}, 观众数: {}", 
                    taskId, liveRoomId, audienceIds.size());

            // 更新任务状态为运行中
            updateTaskStatus(task.getId(), TaskStatus.RUNNING, 0, null);

            long startTime = System.currentTimeMillis();
            long duration = config.getDurationSeconds() * 1000L;

            // 1. 观众进入直播间
            if (Boolean.TRUE.equals(config.getSimulateEnter())) {
                simulateViewerEnter(liveRoomId, audienceIds);
                updateTaskProgress(task.getId(), 20);
            }

            // 2. 持续模拟弹幕和打赏
            long elapsedTime = 0;
            while (elapsedTime < duration) {
                // 模拟弹幕
                if (Boolean.TRUE.equals(config.getSimulateMessage())) {
                    simulateDanmaku(liveRoomId, audienceIds);
                }

                // 模拟打赏
                if (Boolean.TRUE.equals(config.getSimulateRecharge())) {
                    int probability = config.getRechargeProbability() != null ? 
                            config.getRechargeProbability() : 20;
                    simulateRecharge(liveRoomId, audienceIds, probability);
                }

                // 计算进度（20%-90%）
                elapsedTime = System.currentTimeMillis() - startTime;
                int progress = 20 + (int) ((elapsedTime * 70) / duration);
                updateTaskProgress(task.getId(), Math.min(progress, 90));

                // 休眠一段时间
                Thread.sleep(randomGenerator.randomLong(5000, 10000));
            }

            // 3. 观众离开直播间
            if (Boolean.TRUE.equals(config.getSimulateLeave())) {
                simulateViewerLeave(liveRoomId, audienceIds);
            }

            // 4. 更新任务状态为完成
            updateTaskStatus(task.getId(), TaskStatus.COMPLETED, 100, null);

            AppLogger.info("模拟任务执行完成: {}", taskId);

        } catch (Exception e) {
            AppLogger.error("模拟任务执行失败: {}", e, taskId);
            updateTaskStatus(task.getId(), TaskStatus.FAILED, 0, e.getMessage());
        }
    }

    /**
     * 模拟观众进入直播间
     */
    private void simulateViewerEnter(Long liveRoomId, List<Long> audienceIds) {
        AppLogger.info("模拟观众进入直播间，liveRoomId: {}, 观众数: {}", liveRoomId, audienceIds.size());

        for (Long audienceId : audienceIds) {
            try {
                ViewerEnterRequest request = ViewerEnterRequest.builder()
                        .liveRoomId(liveRoomId)
                        .audienceId(audienceId)
                        .nickname("Bot_" + audienceId)
                        .build();

                anchorClient.viewerEnter(request);

                // 随机间隔
                Thread.sleep(randomGenerator.randomLong(enterIntervalMin, enterIntervalMax));

            } catch (Exception e) {
                AppLogger.warn("模拟观众进入失败，audienceId: {}", audienceId, e);
            }
        }
    }

    /**
     * 模拟观众离开直播间
     */
    private void simulateViewerLeave(Long liveRoomId, List<Long> audienceIds) {
        AppLogger.info("模拟观众离开直播间，liveRoomId: {}, 观众数: {}", liveRoomId, audienceIds.size());

        for (Long audienceId : audienceIds) {
            try {
                ViewerEnterRequest request = ViewerEnterRequest.builder()
                        .liveRoomId(liveRoomId)
                        .audienceId(audienceId)
                        .nickname("Bot_" + audienceId)
                        .build();

                anchorClient.viewerLeave(request);

                // 随机间隔
                Thread.sleep(randomGenerator.randomLong(100, 500));

            } catch (Exception e) {
                AppLogger.warn("模拟观众离开失败，audienceId: {}", audienceId, e);
            }
        }
    }

    /**
     * 模拟发送弹幕
     */
    private void simulateDanmaku(Long liveRoomId, List<Long> audienceIds) {
        // 随机选择部分观众发送弹幕
        int sendCount = Math.max(1, audienceIds.size() / 5);

        for (int i = 0; i < sendCount; i++) {
            try {
                Long audienceId = audienceIds.get(randomGenerator.randomInt(0, audienceIds.size() - 1));

                DanmakuRequest request = DanmakuRequest.builder()
                        .liveRoomId(liveRoomId)
                        .audienceId(audienceId)
                        .content(randomGenerator.generateDanmaku())
                        .nickname("Bot_" + audienceId)
                        .build();

                anchorClient.sendDanmaku(request);

                Thread.sleep(randomGenerator.randomLong(messageIntervalMin, messageIntervalMax));

            } catch (Exception e) {
                AppLogger.warn("模拟发送弹幕失败", e);
            }
        }
    }

    /**
     * 模拟打赏
     */
    private void simulateRecharge(Long liveRoomId, List<Long> audienceIds, int probability) {
        for (Long audienceId : audienceIds) {
            // 按概率决定是否打赏
            if (randomGenerator.randomInt(0, 100) >= probability) {
                continue;
            }

            String traceId = TraceIdGenerator.generate("RECHARGE");

            try {
                // 幂等性检查
                BaseResponse<Boolean> idempotencyCheck = redisClient.checkIdempotency(traceId, 3600L);

                if (Boolean.TRUE.equals(idempotencyCheck.getData())) {
                    BigDecimal amount = randomGenerator.generateRechargeAmount(rechargeMin, rechargeMax);

                    RechargeRequest request = RechargeRequest.builder()
                            .audienceId(audienceId)
                            .liveRoomId(liveRoomId)
                            .anchorId(0L) // 实际会从直播间获取
                            .rechargeAmount(amount)
                            .rechargeType(0)
                            .traceId(traceId)
                            .build();

                    BaseResponse<RechargeVO> response = audienceClient.recharge(request);

                    if (response.isSuccess() && response.getData() != null) {
                        // 追踪打赏记录
                        trackingService.trackEntity(EntityType.RECHARGE,
                                response.getData().getRechargeId(), traceId, null);
                    }
                }

                Thread.sleep(randomGenerator.randomLong(rechargeIntervalMin, rechargeIntervalMax));

            } catch (Exception e) {
                AppLogger.warn("模拟打赏失败，audienceId: {}", audienceId, e);
            }
        }
    }

    /**
     * 更新任务状态
     */
    private void updateTaskStatus(Long taskId, String status, Integer progress, String errorMessage) {
        try {
            MockSimulationTask task = taskRepository.findById(taskId).orElse(null);
            if (task != null) {
                task.setStatus(status);
                task.setProgress(progress);
                
                if (TaskStatus.RUNNING.equals(status) && task.getStartTime() == null) {
                    task.setStartTime(LocalDateTime.now());
                }
                
                if (TaskStatus.COMPLETED.equals(status) || TaskStatus.FAILED.equals(status)) {
                    task.setEndTime(LocalDateTime.now());
                }
                
                if (errorMessage != null) {
                    task.setErrorMessage(errorMessage);
                }
                
                taskRepository.save(task);
            }
        } catch (Exception e) {
            AppLogger.error("更新任务状态失败，taskId: {}", e, taskId);
        }
    }

    /**
     * 更新任务进度
     */
    private void updateTaskProgress(Long taskId, Integer progress) {
        updateTaskStatus(taskId, TaskStatus.RUNNING, progress, null);
    }

    /**
     * 查询任务状态
     */
    public MockSimulationTask getTaskStatus(String taskId) {
        return taskRepository.findByTaskId(taskId).orElse(null);
    }
}
