package com.liveroom.mock.service;

import common.bean.Tag;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 标签模拟服务
 */
@Service
public class TagMockService {

    // 预定义的标签列表
    private static final List<String> PREDEFINED_TAGS = Arrays.asList(
            "颜值", "才艺", "唱歌", "跳舞", "游戏", "聊天", "搞笑", "美食",
            "运动", "学习", "旅游", "萌宠", "化妆", "穿搭", "手工", "绘画",
            "乐器", "电竞", "户外", "健身", "瑜伽", "美妆", "时尚", "音乐"
    );

    /**
     * 获取可用标签列表
     */
    public List<Tag> getAvailableTags() {
        List<Tag> tags = new ArrayList<>();
        long baseId = 1000L;
        
        for (String tagName : PREDEFINED_TAGS) {
            Tag tag = new Tag();
            tag.setTagId(baseId++);
            tag.setTagName(tagName);
            tag.setTagType(0); // 内容标签
            tag.setWeight(100);
            tags.add(tag);
        }
        
        return tags;
    }

    /**
     * 获取标签名称列表
     */
    public List<String> getTagNames() {
        return new ArrayList<>(PREDEFINED_TAGS);
    }
}
