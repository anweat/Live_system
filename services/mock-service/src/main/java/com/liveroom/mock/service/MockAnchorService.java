package com.liveroom.mock.service;

import com.liveroom.mock.client.AnchorServiceClient;
import com.liveroom.mock.constant.BatchType;
import com.liveroom.mock.constant.EntityType;
import com.liveroom.mock.dto.external.AnchorVO;
import com.liveroom.mock.dto.external.LiveRoomVO;
import com.liveroom.mock.dto.request.CreateAnchorRequest;
import com.liveroom.mock.dto.request.CreateMockAnchorRequest;
import com.liveroom.mock.dto.response.MockAnchorResult;
import com.liveroom.mock.dto.response.MockBatchResult;
import com.liveroom.mock.util.RandomDataGenerator;
import com.liveroom.mock.util.TraceIdGenerator;
import common.constant.ErrorConstants;
import common.exception.BusinessException;
import common.exception.SystemException;
import common.logger.AppLogger;
import common.logger.TraceLogger;
import common.response.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 模拟主播服务
 * 负责创建模拟主播和直播间
 */
@Service
@RequiredArgsConstructor
public class MockAnchorService {

    private final AnchorServiceClient anchorClient;
    private final MockDataTrackingService trackingService;
    private final MockBatchInfoService batchInfoService;
    private final RandomDataGenerator randomGenerator;

    @Value("${mock.random.gender-male-rate:55}")
    private int malePercentage;

    @Value("${mock.random.tag-count-min:1}")
    private int tagCountMin;

    @Value("${mock.random.tag-count-max:5}")
    private int tagCountMax;

    /**
     * 创建单个模拟主播
     */
    public MockAnchorResult createMockAnchor(CreateMockAnchorRequest request) {
        String traceId = TraceIdGenerator.generate("MOCK_ANCHOR");

        try {
            TraceLogger.info("mock_anchor", "create", traceId);

            // 1. 准备主播数据
            CreateAnchorRequest anchorRequest = buildAnchorRequest(request);

            // 2. 调用主播服务创建主播
            BaseResponse<AnchorVO> response = anchorClient.createAnchor(anchorRequest);

            if (!response.isSuccess() || response.getData() == null) {
                throw new BusinessException(ErrorConstants.SERVICE_UNAVAILABLE,
                        "创建主播失败: " + response.getMessage());
            }

            AnchorVO anchor = response.getData();
            Long userId = anchor.getUserId();

            // 3. 追踪主播ID
            trackingService.trackEntity(EntityType.ANCHOR, userId, traceId, null);

            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("userId", String.valueOf(userId));
            resultMap.put("nickname", anchor.getNickname());

            // 4. 获取直播间信息
            Long liveRoomId = anchor.getLiveRoomId();
            if (liveRoomId != null) {
                trackingService.trackEntity(EntityType.LIVE_ROOM, liveRoomId, traceId, null);
                resultMap.put("liveRoomId", String.valueOf(liveRoomId));
            }

            TraceLogger.info("mock_anchor", "create_success", traceId, 
                    "userId", userId, "nickname", anchor.getNickname());

            return MockAnchorResult.builder()
                    .userId(userId)
                    .nickname(anchor.getNickname())
                    .liveRoomId(liveRoomId)
                    .traceId(traceId)
                    .build();

        } catch (BusinessException e) {
            TraceLogger.error("mock_anchor", "create", traceId, e);
            throw e;
        } catch (Exception e) {
            TraceLogger.error("mock_anchor", "create", traceId, e);
            throw new SystemException(ErrorConstants.SYSTEM_ERROR, "创建模拟主播异常", e);
        }
    }

    /**
     * 批量创建模拟主播
     */
    public MockBatchResult batchCreateMockAnchors(int count) {
        String batchId = TraceIdGenerator.generateBatchId(BatchType.ANCHOR);

        AppLogger.info("开始批量创建模拟主播，数量: {}, batchId: {}", count, batchId);

        // 创建批次记录
        batchInfoService.createBatchInfo(batchId, BatchType.ANCHOR, count);

        List<MockAnchorResult> results = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;

        for (int i = 0; i < count; i++) {
            try {
                CreateMockAnchorRequest request = generateRandomAnchorRequest();
                MockAnchorResult result = createMockAnchor(request);
                
                // 更新批次ID
                trackingService.trackEntity(EntityType.ANCHOR, result.getUserId(), 
                        result.getTraceId(), batchId);
                if (result.getLiveRoomId() != null) {
                    trackingService.trackEntity(EntityType.LIVE_ROOM, result.getLiveRoomId(), 
                            result.getTraceId(), batchId);
                }

                results.add(result);
                successCount++;

            } catch (Exception e) {
                failCount++;
                AppLogger.error("批量创建主播失败，索引: {}", e, i);
            }
        }

        // 更新批次信息
        batchInfoService.updateBatchInfo(batchId, successCount, failCount);

        AppLogger.info("批量创建主播完成，成功: {}, 失败: {}", successCount, failCount);

        return MockBatchResult.builder()
                .batchId(batchId)
                .totalCount(count)
                .successCount(successCount)
                .failCount(failCount)
                .results(results)
                .build();
    }

    /**
     * 构建主播创建请求
     */
    private CreateAnchorRequest buildAnchorRequest(CreateMockAnchorRequest request) {
        CreateAnchorRequest.CreateAnchorRequestBuilder builder = CreateAnchorRequest.builder();

        // 昵称
        if (request.getAnchorName() != null && !request.getAnchorName().isEmpty()) {
            builder.nickname(request.getAnchorName());
        } else {
            builder.nickname(randomGenerator.generateAnchorNickname());
        }

        // 性别
        if (request.getGender() != null) {
            builder.gender(request.getGender());
        } else {
            builder.gender(randomGenerator.generateGender(malePercentage));
        }

        // 年龄
        builder.age(randomGenerator.generateAge(18, 40));

        // 头像
        builder.avatarUrl(randomGenerator.generateAvatarUrl());

        // 简介
        if (request.getBio() != null && !request.getBio().isEmpty()) {
            builder.bio(request.getBio());
        } else {
            builder.bio(randomGenerator.generateBio());
        }

        // 标签
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            builder.tags(request.getTags());
        } else {
            builder.tags(randomGenerator.generateRandomTags(tagCountMin, tagCountMax));
        }

        return builder.build();
    }

    /**
     * 生成随机主播请求
     */
    private CreateMockAnchorRequest generateRandomAnchorRequest() {
        return CreateMockAnchorRequest.builder()
                .anchorName(randomGenerator.generateAnchorNickname())
                .gender(randomGenerator.generateGender(malePercentage))
                .bio(randomGenerator.generateBio())
                .tags(randomGenerator.generateRandomTags(tagCountMin, tagCountMax))
                .build();
    }
}
