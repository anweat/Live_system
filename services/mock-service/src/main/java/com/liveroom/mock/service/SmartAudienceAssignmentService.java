package com.liveroom.mock.service;

import com.liveroom.mock.dto.SmartAssignAudienceRequestDTO;
import com.liveroom.mock.service.AudienceMockService.MockAudienceDTO;
import com.liveroom.mock.service.LiveRoomMockService.MockLiveRoomDTO;
import com.liveroom.mock.util.RandomDataGenerator;
import common.logger.TraceLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 智能观众分配服务
 * 基于标签匹配为直播间分配合适的观众
 */
@Service
@RequiredArgsConstructor
public class SmartAudienceAssignmentService {

    private final BotPersistenceService botPersistenceService;
    private final AudienceMockService audienceMockService;
    private final RandomDataGenerator randomDataGenerator;

    /**
     * 根据标签为直播间智能分配观众
     */
    public List<MockAudienceDTO> assignAudiencesByTags(MockLiveRoomDTO liveRoom, Integer targetCount) {
        TraceLogger.info("SmartAudienceAssignmentService", "assignAudiencesByTags", 
                String.format("为直播间 %d 智能分配 %d 个观众", liveRoom.getLiveRoomId(), targetCount));

        // 解析直播间标签（从分类推断）
        List<String> roomTags = inferTagsFromCategory(liveRoom.getCategory());
        
        return assignByTags(roomTags, targetCount, "any", true);
    }

    /**
     * 根据请求智能分配观众
     */
    public List<MockAudienceDTO> smartAssign(SmartAssignAudienceRequestDTO request) {
        TraceLogger.info("SmartAudienceAssignmentService", "smartAssign", 
                String.format("智能分配观众: 策略=%s, 数量=%d", 
                        request.getMatchStrategy(), request.getTargetCount()));

        List<String> tags = request.getLiveRoomTags() != null ? 
                request.getLiveRoomTags() : Collections.emptyList();

        return assignByTags(tags, request.getTargetCount(), 
                request.getMatchStrategy(), request.getUsePersistedBots());
    }

    /**
     * 核心分配逻辑
     */
    private List<MockAudienceDTO> assignByTags(List<String> tags, Integer targetCount, 
                                                 String strategy, Boolean usePersistedBots) {
        List<MockAudienceDTO> assignedAudiences = new ArrayList<>();

        if (usePersistedBots) {
            // 从数据库查询Bot
            if (tags.isEmpty() || "random".equals(strategy)) {
                // 随机分配
                assignedAudiences = botPersistenceService.findRandomBots(targetCount);
            } else {
                // 标签匹配
                assignedAudiences = botPersistenceService.findBotsByTags(tags, targetCount);
            }

            // 如果数据库Bot不足，补充创建
            if (assignedAudiences.size() < targetCount) {
                int shortage = targetCount - assignedAudiences.size();
                TraceLogger.warn("SmartAudienceAssignmentService", "assignByTags", 
                        String.format("数据库Bot不足，需补充创建 %d 个", shortage));
                
                List<MockAudienceDTO> additionalBots = createAdditionalBots(shortage, tags);
                assignedAudiences.addAll(additionalBots);
            }
        } else {
            // 直接创建临时Bot
            assignedAudiences = createBotsWithTags(targetCount, tags);
        }

        TraceLogger.info("SmartAudienceAssignmentService", "assignByTags", 
                String.format("分配完成: 共%d个观众", assignedAudiences.size()));

        return assignedAudiences;
    }

    /**
     * 创建带标签的Bot
     */
    private List<MockAudienceDTO> createBotsWithTags(int count, List<String> tags) {
        List<MockAudienceDTO> bots = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            MockAudienceDTO bot = MockAudienceDTO.builder()
                    .audienceId(System.currentTimeMillis() + i)
                    .nickname("Bot_" + UUID.randomUUID().toString().substring(0, 8))
                    .gender(randomDataGenerator.generateGender(55))
                    .age(randomDataGenerator.generateAge(18, 45))
                    .avatarUrl(randomDataGenerator.generateAvatarUrl())
                    .isBot(true)
                    .consumptionLevel(randomDataGenerator.generateConsumptionLevel(60, 30, 10))
                    .tags(tags.isEmpty() ? randomDataGenerator.randomElements(getAllTags(), 3) : 
                          randomDataGenerator.randomElements(tags, Math.min(3, tags.size())))
                    .build();
            bots.add(bot);
        }
        
        return bots;
    }

    /**
     * 补充创建Bot
     */
    private List<MockAudienceDTO> createAdditionalBots(int count, List<String> tags) {
        return createBotsWithTags(count, tags);
    }

    /**
     * 从分类推断标签
     */
    private List<String> inferTagsFromCategory(String category) {
        Map<String, List<String>> categoryTagMap = new HashMap<>();
        categoryTagMap.put("娱乐", Arrays.asList("才艺", "搞笑", "聊天"));
        categoryTagMap.put("游戏", Arrays.asList("游戏", "电竞", "手游"));
        categoryTagMap.put("音乐", Arrays.asList("唱歌", "音乐", "乐器"));
        categoryTagMap.put("舞蹈", Arrays.asList("跳舞", "舞蹈", "才艺"));
        categoryTagMap.put("美食", Arrays.asList("美食", "做菜", "吃播"));
        categoryTagMap.put("运动", Arrays.asList("运动", "健身", "户外"));
        categoryTagMap.put("学习", Arrays.asList("学习", "知识", "教育"));
        categoryTagMap.put("聊天", Arrays.asList("聊天", "交友", "唠嗑"));
        
        return categoryTagMap.getOrDefault(category, Arrays.asList("颜值", "才艺", "聊天"));
    }

    /**
     * 获取所有可用标签
     */
    private List<String> getAllTags() {
        return Arrays.asList("颜值", "才艺", "唱歌", "跳舞", "游戏", "聊天", "搞笑", "美食",
                "运动", "学习", "旅游", "萌宠", "化妆", "穿搭", "手工", "绘画",
                "乐器", "电竞", "户外", "健身", "瑜伽", "美妆", "时尚", "音乐");
    }
}
