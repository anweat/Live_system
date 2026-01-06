package com.liveroom.mock.service;

import com.liveroom.mock.config.MockProperties;
import com.liveroom.mock.dto.BatchCreateBotsRequestDTO;
import com.liveroom.mock.dto.CreateAudienceRequestDTO;
import com.liveroom.mock.util.RandomDataGenerator;
import common.bean.Tag;
import common.exception.BusinessException;
import common.logger.TraceLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 观众模拟服务
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AudienceMockService {

    private final RandomDataGenerator randomDataGenerator;
    private final MockProperties mockProperties;
    private final TagMockService tagMockService;

    /**
     * 创建模拟观众
     */
    public MockAudienceDTO createMockAudience(CreateAudienceRequestDTO request) {
        TraceLogger.info("AudienceMockService", "createMockAudience", "创建模拟观众: " + request.getNickname());

        try {
            MockAudienceDTO audience = MockAudienceDTO.builder()
                    .audienceId(generateAudienceId())
                    .nickname(request.getNickname())
                    .gender(request.getGender() != null ? request.getGender() : 
                            randomDataGenerator.generateGender(mockProperties.getRandom().getGenderMaleRate()))
                    .age(request.getAge() != null ? request.getAge() : 
                            randomDataGenerator.generateAge(18, 50))
                    .avatarUrl(request.getAvatarUrl() != null ? request.getAvatarUrl() : 
                            randomDataGenerator.generateAvatarUrl())
                    .isBot(request.getIsBot() != null ? request.getIsBot() : false)
                    .balance(BigDecimal.ZERO)
                    .consumptionLevel(request.getConsumptionLevel() != null ? request.getConsumptionLevel() : 
                            randomDataGenerator.generateConsumptionLevel(
                                mockProperties.getRandom().getConsumptionLowRate(),
                                mockProperties.getRandom().getConsumptionMediumRate(),
                                mockProperties.getRandom().getConsumptionHighRate()
                            ))
                    .registrationTime(LocalDateTime.now())
                    .build();

            // 分配标签
            if (request.getTags() != null && !request.getTags().isEmpty()) {
                audience.setTags(request.getTags());
            } else if (request.getIsBot() == null || !request.getIsBot()) {
                assignRandomTags(audience);
            }

            return audience;

        } catch (Exception e) {
            TraceLogger.error("AudienceMockService", "createMockAudience", "创建模拟观众失败", e);
            throw new BusinessException(500, "创建模拟观众失败: " + e.getMessage());
        }
    }

    /**
     * 批量创建Bot观众
     */
    public List<MockAudienceDTO> batchCreateBots(BatchCreateBotsRequestDTO request) {
        TraceLogger.info("AudienceMockService", "batchCreateBots", "批量创建 " + request.getCount() + " 个Bot观众");

        List<MockAudienceDTO> bots = new ArrayList<>();
        int malePercentage = request.getMalePercentage() != null ? 
                request.getMalePercentage() : mockProperties.getRandom().getGenderMaleRate();

        for (int i = 0; i < request.getCount(); i++) {
            CreateAudienceRequestDTO audienceRequest = CreateAudienceRequestDTO.builder()
                    .nickname(randomDataGenerator.generateBotName(mockProperties.getBot().getNamePrefix()))
                    .gender(randomDataGenerator.generateGender(malePercentage))
                    .age(randomDataGenerator.generateAge(request.getMinAge(), request.getMaxAge()))
                    .avatarUrl(randomDataGenerator.generateAvatarUrl())
                    .isBot(true)
                    .build();

            if (request.getAssignConsumptionLevel()) {
                audienceRequest.setConsumptionLevel(
                        randomDataGenerator.generateConsumptionLevel(
                                mockProperties.getRandom().getConsumptionLowRate(),
                                mockProperties.getRandom().getConsumptionMediumRate(),
                                mockProperties.getRandom().getConsumptionHighRate()
                        )
                );
            }

            MockAudienceDTO bot = createMockAudience(audienceRequest);

            if (request.getAssignRandomTags()) {
                assignRandomTags(bot);
            }

            bots.add(bot);
        }

        return bots;
    }

    /**
     * 为观众分配随机标签
     */
    private void assignRandomTags(MockAudienceDTO audience) {
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
        audience.setTags(tagNames);
    }

    /**
     * 生成观众ID
     */
    private Long generateAudienceId() {
        return System.currentTimeMillis() + (long) (Math.random() * 10000);
    }

    /**
     * 观众DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MockAudienceDTO {
        private Long audienceId;
        private String nickname;
        private Integer gender;
        private Integer age;
        private String avatarUrl;
        private Boolean isBot;
        private BigDecimal balance;
        private Integer consumptionLevel;
        private LocalDateTime registrationTime;
        private List<String> tags;
    }
}
