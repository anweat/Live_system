package com.liveroom.mock.service;

import com.liveroom.mock.client.AudienceServiceClient;
import com.liveroom.mock.constant.BatchType;
import com.liveroom.mock.constant.EntityType;
import com.liveroom.mock.dto.external.AudienceVO;
import com.liveroom.mock.dto.request.BatchCreateBotsRequest;
import com.liveroom.mock.dto.request.CreateAudienceRequest;
import com.liveroom.mock.dto.response.MockBatchResult;
import com.liveroom.mock.entity.MockDataTracking;
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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 模拟观众服务
 * 负责创建模拟观众（Bot观众）
 */
@Service
@RequiredArgsConstructor
public class MockAudienceService {

    private final AudienceServiceClient audienceClient;
    private final MockDataTrackingService trackingService;
    private final MockBatchInfoService batchInfoService;
    private final RandomDataGenerator randomGenerator;

    @Value("${mock.bot.name-prefix:Bot_}")
    private String botNamePrefix;

    @Value("${mock.random.gender-male-rate:55}")
    private int defaultMalePercentage;

    @Value("${mock.random.consumption-low-rate:60}")
    private int consumptionLowRate;

    @Value("${mock.random.consumption-medium-rate:30}")
    private int consumptionMediumRate;

    @Value("${mock.random.tag-count-min:1}")
    private int tagCountMin;

    @Value("${mock.random.tag-count-max:5}")
    private int tagCountMax;

    /**
     * 批量创建Bot观众
     */
    public MockBatchResult batchCreateBots(BatchCreateBotsRequest request) {
        String batchId = TraceIdGenerator.generateBatchId(BatchType.AUDIENCE);
        int count = request.getCount();

        AppLogger.info("开始批量创建Bot观众，数量: {}, batchId: {}", count, batchId);

        // 创建批次记录
        batchInfoService.createBatchInfo(batchId, BatchType.AUDIENCE, count);

        List<Map<String, Object>> results = new ArrayList<>();
        List<MockDataTracking> trackingList = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;

        // 确定参数
        int malePercentage = request.getMalePercentage() != null ? 
                request.getMalePercentage() : defaultMalePercentage;
        int minAge = request.getMinAge() != null ? request.getMinAge() : 18;
        int maxAge = request.getMaxAge() != null ? request.getMaxAge() : 50;
        boolean assignTags = Boolean.TRUE.equals(request.getAssignRandomTags());
        boolean assignConsumption = Boolean.TRUE.equals(request.getAssignConsumptionLevel());

        for (int i = 0; i < count; i++) {
            String traceId = TraceIdGenerator.generate("MOCK_AUDIENCE");

            try {
                TraceLogger.info("mock_audience", "create", traceId);

                // 1. 准备观众数据
                CreateAudienceRequest audienceRequest = CreateAudienceRequest.builder()
                        .nickname(randomGenerator.generateBotNickname(botNamePrefix))
                        .gender(randomGenerator.generateGender(malePercentage))
                        .age(randomGenerator.generateAge(minAge, maxAge))
                        .avatarUrl(randomGenerator.generateAvatarUrl())
                        .tags(assignTags ? randomGenerator.generateRandomTags(tagCountMin, tagCountMax) : null)
                        .build();

                // 2. 调用观众服务创建观众
                BaseResponse<AudienceVO> response = audienceClient.createAudience(audienceRequest);

                if (!response.isSuccess() || response.getData() == null) {
                    throw new BusinessException(ErrorConstants.SERVICE_UNAVAILABLE,
                            "创建观众失败: " + response.getMessage());
                }

                AudienceVO audience = response.getData();
                Long userId = audience.getUserId();

                // 3. 构建追踪记录
                MockDataTracking tracking = MockDataTracking.builder()
                        .entityType(EntityType.AUDIENCE)
                        .entityId(userId)
                        .traceId(traceId)
                        .batchId(batchId)
                        .build();
                trackingList.add(tracking);

                Map<String, Object> result = new HashMap<>();
                result.put("userId", userId);
                result.put("nickname", audience.getNickname());
                result.put("traceId", traceId);
                results.add(result);

                successCount++;

                TraceLogger.info("mock_audience", "create_success", traceId, 
                        "userId", userId);

            } catch (Exception e) {
                failCount++;
                AppLogger.error("批量创建观众失败，索引: {}", e, i);
                TraceLogger.error("mock_audience", "create", traceId, e);
            }
        }

        // 4. 批量保存追踪记录
        if (!trackingList.isEmpty()) {
            trackingService.trackEntitiesBatch(trackingList);
        }

        // 5. 更新批次信息
        batchInfoService.updateBatchInfo(batchId, successCount, failCount);

        AppLogger.info("批量创建Bot观众完成，成功: {}, 失败: {}", successCount, failCount);

        return MockBatchResult.builder()
                .batchId(batchId)
                .totalCount(count)
                .successCount(successCount)
                .failCount(failCount)
                .results(results)
                .build();
    }

    /**
     * 获取可用的Bot观众ID列表
     */
    public List<Long> getAvailableBotIds(int limit) {
        List<Long> audienceIds = trackingService.getEntityIdsByType(EntityType.AUDIENCE);
        
        if (audienceIds.isEmpty()) {
            throw new BusinessException(ErrorConstants.BUSINESS_ERROR, 
                    "没有可用的Bot观众，请先创建Bot观众");
        }

        if (limit > 0 && audienceIds.size() > limit) {
            return audienceIds.subList(0, limit);
        }

        return audienceIds;
    }

    /**
     * 查询Bot观众数量
     */
    public long countBots() {
        return trackingService.getEntityIdsByType(EntityType.AUDIENCE).size();
    }
}
