package com.liveroom.mock.service;

import com.liveroom.mock.config.MockProperties;
import com.liveroom.mock.dto.CreateAnchorRequestDTO;
import com.liveroom.mock.util.RandomDataGenerator;
import common.bean.Tag;
import common.exception.BusinessException;
import common.logger.TraceLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 主播模拟服务
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AnchorMockService {

    private final RandomDataGenerator randomDataGenerator;
    private final MockProperties mockProperties;
    private final TagMockService tagMockService;

    /**
     * 创建模拟主播
     */
    public MockAnchorDTO createMockAnchor(CreateAnchorRequestDTO request) {
        TraceLogger.info("AnchorMockService", "createMockAnchor", "创建模拟主播: " + request.getAnchorName());

        try {
            // 构建主播数据
            MockAnchorDTO anchor = MockAnchorDTO.builder()
                    .anchorId(generateAnchorId())
                    .anchorName(request.getAnchorName())
                    .gender(request.getGender() != null ? request.getGender() : 
                            randomDataGenerator.generateGender(mockProperties.getRandom().getGenderMaleRate()))
                    .bio(request.getBio() != null ? request.getBio() : randomDataGenerator.generateBio())
                    .avatarUrl(request.getAvatarUrl() != null ? request.getAvatarUrl() : 
                            randomDataGenerator.generateAvatarUrl())
                    .followerCount(0)
                    .status(1) // 在线
                    .registrationTime(LocalDateTime.now())
                    .build();

            // 分配标签
            if (request.getTags() != null && !request.getTags().isEmpty()) {
                anchor.setTags(request.getTags());
            } else {
                List<Tag> availableTags = tagMockService.getAvailableTags();
                int tagCount = randomDataGenerator.generateAge(
                        mockProperties.getRandom().getTagCountMin(),
                        mockProperties.getRandom().getTagCountMax()
                );
                List<Tag> selectedTags = randomDataGenerator.randomElements(availableTags, tagCount);
                List<String> tagNames = new ArrayList<>();
                for (Tag tag : selectedTags) {
                    tagNames.add(tag.getTagName());
                }
                anchor.setTags(tagNames);
            }

            TraceLogger.info("AnchorMockService", "createMockAnchor", 
                    "模拟主播创建成功，ID: " + anchor.getAnchorId());
            return anchor;

        } catch (Exception e) {
            TraceLogger.error("AnchorMockService", "createMockAnchor", "创建模拟主播失败", e);
            throw new BusinessException(500, "创建模拟主播失败: " + e.getMessage());
        }
    }

    /**
     * 批量创建随机主播
     */
    public List<MockAnchorDTO> batchCreateRandomAnchors(int count) {
        TraceLogger.info("AnchorMockService", "batchCreateRandomAnchors", "批量创建 " + count + " 个随机主播");

        List<MockAnchorDTO> anchors = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            CreateAnchorRequestDTO request = CreateAnchorRequestDTO.builder()
                    .anchorName(randomDataGenerator.generateChineseName())
                    .gender(randomDataGenerator.generateGender(mockProperties.getRandom().getGenderMaleRate()))
                    .bio(randomDataGenerator.generateBio())
                    .avatarUrl(randomDataGenerator.generateAvatarUrl())
                    .build();

            anchors.add(createMockAnchor(request));
        }

        return anchors;
    }

    /**
     * 生成主播ID
     */
    private Long generateAnchorId() {
        return System.currentTimeMillis() + (long) (Math.random() * 10000);
    }

    /**
     * 主播DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MockAnchorDTO {
        private Long anchorId;
        private String anchorName;
        private Integer gender;
        private String bio;
        private String avatarUrl;
        private Integer followerCount;
        private Integer status;
        private LocalDateTime registrationTime;
        private List<String> tags;
    }
}
