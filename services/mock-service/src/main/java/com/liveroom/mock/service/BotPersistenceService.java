package com.liveroom.mock.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liveroom.mock.entity.MockUser;
import com.liveroom.mock.repository.MockUserRepository;
import com.liveroom.mock.service.AudienceMockService.MockAudienceDTO;
import common.logger.TraceLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Bot持久化服务
 * 负责Bot数据的数据库存储和查询
 */
@Service
@RequiredArgsConstructor
@Transactional
public class BotPersistenceService {

    private final MockUserRepository mockUserRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 批量保存Bot到数据库
     */
    public List<Long> saveBots(List<MockAudienceDTO> bots) {
        TraceLogger.info("BotPersistenceService", "saveBots", "批量保存 " + bots.size() + " 个Bot到数据库");

        List<MockUser> entities = bots.stream().map(this::convertToEntity).collect(Collectors.toList());
        List<MockUser> saved = mockUserRepository.saveAll(entities);
        
        return saved.stream().map(MockUser::getId).collect(Collectors.toList());
    }

    /**
     * 根据标签筛选Bot
     */
    public List<MockAudienceDTO> findBotsByTag(String tag, int limit) {
        TraceLogger.info("BotPersistenceService", "findBotsByTag", 
                "根据标签 '" + tag + "' 筛选Bot，限制 " + limit + " 个");

        List<MockUser> users = mockUserRepository.findBotsByTag(tag);
        
        if (users.size() > limit) {
            users = users.subList(0, limit);
        }
        
        return users.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * 根据多个标签筛选Bot（任意匹配）
     */
    public List<MockAudienceDTO> findBotsByTags(List<String> tags, int limit) {
        TraceLogger.info("BotPersistenceService", "findBotsByTags", 
                "根据标签组 " + tags + " 筛选Bot，限制 " + limit + " 个");

        if (tags.isEmpty()) {
            return findRandomBots(limit);
        }

        // 扩展查询以支持更多标签
        List<MockUser> allUsers = new ArrayList<>();
        for (String tag : tags) {
            List<MockUser> users = mockUserRepository.findBotsByTag(tag);
            allUsers.addAll(users);
        }

        // 去重
        allUsers = allUsers.stream().distinct().collect(Collectors.toList());

        if (allUsers.size() > limit) {
            allUsers = allUsers.subList(0, limit);
        }

        return allUsers.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * 根据标签和消费等级筛选Bot
     */
    public List<MockAudienceDTO> findBotsByTagAndLevel(String tag, Integer level, int limit) {
        TraceLogger.info("BotPersistenceService", "findBotsByTagAndLevel", 
                String.format("根据标签 '%s' 和消费等级 %d 筛选Bot，限制 %d 个", tag, level, limit));

        List<MockUser> users = mockUserRepository.findBotsByTagAndLevel(tag, level);
        
        if (users.size() > limit) {
            users = users.subList(0, limit);
        }
        
        return users.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * 查询随机Bot
     */
    public List<MockAudienceDTO> findRandomBots(int limit) {
        TraceLogger.info("BotPersistenceService", "findRandomBots", "查询 " + limit + " 个随机Bot");

        List<MockUser> users = mockUserRepository.findRandomBots(limit);
        return users.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * 根据消费等级查询Bot
     */
    public List<MockAudienceDTO> findBotsByConsumptionLevel(Integer level, int limit) {
        List<MockUser> users = mockUserRepository.findByIsBotTrueAndConsumptionLevel(level);
        
        if (users.size() > limit) {
            users = users.subList(0, limit);
        }
        
        return users.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * 获取所有Bot
     */
    public List<MockAudienceDTO> getAllBots(int page, int size) {
        List<MockUser> users = mockUserRepository.findByIsBotTrue(PageRequest.of(page, size)).getContent();
        return users.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * 统计Bot总数
     */
    public long countBots() {
        return mockUserRepository.countBots();
    }

    /**
     * 统计各消费等级Bot数量
     */
    public List<Object[]> countBotsByLevel() {
        return mockUserRepository.countBotsByConsumptionLevel();
    }

    /**
     * 转换DTO为实体
     */
    private MockUser convertToEntity(MockAudienceDTO dto) {
        try {
            String tagsJson = dto.getTags() != null ? objectMapper.writeValueAsString(dto.getTags()) : "[]";
            
            return MockUser.builder()
                    .nickname(dto.getNickname())
                    .gender(dto.getGender())
                    .age(dto.getAge())
                    .avatarUrl(dto.getAvatarUrl())
                    .isBot(dto.getIsBot())
                    .consumptionLevel(dto.getConsumptionLevel())
                    .tags(tagsJson)
                    .status(1)
                    .build();
        } catch (Exception e) {
            TraceLogger.error("BotPersistenceService", "convertToEntity", "转换失败", e);
            throw new RuntimeException("转换失败", e);
        }
    }

    /**
     * 转换实体为DTO
     */
    private MockAudienceDTO convertToDTO(MockUser entity) {
        try {
            List<String> tags = entity.getTags() != null ? 
                    objectMapper.readValue(entity.getTags(), List.class) : new ArrayList<>();
            
            return MockAudienceDTO.builder()
                    .audienceId(entity.getId())
                    .nickname(entity.getNickname())
                    .gender(entity.getGender())
                    .age(entity.getAge())
                    .avatarUrl(entity.getAvatarUrl())
                    .isBot(entity.getIsBot())
                    .consumptionLevel(entity.getConsumptionLevel())
                    .tags(tags)
                    .registrationTime(entity.getCreateTime())
                    .build();
        } catch (Exception e) {
            TraceLogger.error("BotPersistenceService", "convertToDTO", "转换失败", e);
            throw new RuntimeException("转换失败", e);
        }
    }
}
