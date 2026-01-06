package com.liveroom.mock.service;

import com.liveroom.mock.dto.CreateLiveRoomRequestDTO;
import com.liveroom.mock.service.AnchorMockService.MockAnchorDTO;
import com.liveroom.mock.util.RandomDataGenerator;
import common.exception.BusinessException;
import common.logger.TraceLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

/**
 * 直播间模拟服务
 */
@Service
@RequiredArgsConstructor
@Transactional
public class LiveRoomMockService {

    private final RandomDataGenerator randomDataGenerator;

    /**
     * 创建模拟直播间
     */
    public MockLiveRoomDTO createMockLiveRoom(CreateLiveRoomRequestDTO request) {
        TraceLogger.info("LiveRoomMockService", "createMockLiveRoom", 
                "为主播ID " + request.getAnchorId() + " 创建直播间");

        try {
            MockLiveRoomDTO liveRoom = MockLiveRoomDTO.builder()
                    .liveRoomId(generateLiveRoomId())
                    .anchorId(request.getAnchorId())
                    .title(request.getTitle())
                    .description(request.getDescription() != null ? request.getDescription() : 
                            randomDataGenerator.generateLiveRoomDescription())
                    .category(request.getCategory() != null ? request.getCategory() : 
                            randomDataGenerator.generateCategory())
                    .coverUrl(request.getCoverUrl() != null ? request.getCoverUrl() : 
                            randomDataGenerator.generateCoverUrl())
                    .status(request.getStatus() != null ? request.getStatus() : 1)
                    .currentAudienceCount(0)
                    .totalAudienceCount(0)
                    .startTime(LocalDateTime.now())
                    .build();

            TraceLogger.info("LiveRoomMockService", "createMockLiveRoom", 
                    "直播间创建成功，ID: " + liveRoom.getLiveRoomId());
            return liveRoom;

        } catch (Exception e) {
            TraceLogger.error("LiveRoomMockService", "createMockLiveRoom", "创建直播间失败", e);
            throw new BusinessException(500, "创建直播间失败: " + e.getMessage());
        }
    }

    /**
     * 为主播创建默认直播间
     */
    public MockLiveRoomDTO createDefaultLiveRoom(MockAnchorDTO anchor) {
        CreateLiveRoomRequestDTO request = CreateLiveRoomRequestDTO.builder()
                .anchorId(anchor.getAnchorId())
                .title(randomDataGenerator.generateLiveRoomTitle(anchor.getAnchorName()))
                .description(randomDataGenerator.generateLiveRoomDescription())
                .category(randomDataGenerator.generateCategory())
                .coverUrl(randomDataGenerator.generateCoverUrl())
                .status(1)
                .build();

        return createMockLiveRoom(request);
    }

    /**
     * 生成直播间ID
     */
    private Long generateLiveRoomId() {
        return System.currentTimeMillis() + (long) (Math.random() * 10000);
    }

    /**
     * 直播间DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MockLiveRoomDTO {
        private Long liveRoomId;
        private Long anchorId;
        private String title;
        private String description;
        private String category;
        private String coverUrl;
        private Integer status;
        private Integer currentAudienceCount;
        private Integer totalAudienceCount;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
    }
}
