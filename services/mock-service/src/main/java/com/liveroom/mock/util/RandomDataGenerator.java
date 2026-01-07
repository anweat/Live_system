package com.liveroom.mock.util;

import com.github.javafaker.Faker;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

/**
 * 随机数据生成器
 * 用于生成测试数据
 */
@Component
public class RandomDataGenerator {

    private static final Faker faker = new Faker(Locale.SIMPLIFIED_CHINESE);
    private static final Random random = new Random();

    // 主播名称前缀
    private static final List<String> ANCHOR_PREFIXES = Arrays.asList(
        "主播", "美女", "小", "大", "萌", "甜", "酷", ""
    );

    // 直播间标题模板
    private static final List<String> ROOM_TITLE_TEMPLATES = Arrays.asList(
        "{name}的直播间",
        "欢迎来到{name}的直播间",
        "{name}和你聊聊天",
        "{name}今天也要加油鸭",
        "陪你一起{activity}",
        "{name}的{activity}时间"
    );

    // 活动类型
    private static final List<String> ACTIVITIES = Arrays.asList(
        "唱歌", "跳舞", "聊天", "游戏", "画画", "做饭", "运动", "学习"
    );

    // 直播分类
    private static final List<String> CATEGORIES = Arrays.asList(
        "娱乐", "游戏", "音乐", "舞蹈", "户外", "美食", "运动", "学习", "聊天"
    );

    // 标签库
    private static final List<String> TAGS = Arrays.asList(
        "唱歌", "跳舞", "颜值", "才艺", "搞笑", "萌妹", "御姐", "小哥哥",
        "游戏", "电竞", "音乐", "舞蹈", "美食", "旅游", "户外", "运动",
        "学习", "知识", "情感", "励志", "正能量", "二次元", "Cosplay"
    );

    // 弹幕模板
    private static final List<String> DANMAKU_TEMPLATES = Arrays.asList(
        "666", "主播好棒！", "哇哦", "厉害了", "支持主播",
        "来了来了", "前排围观", "打卡", "主播加油", "太好看了",
        "哈哈哈", "笑死我了", "真有意思", "学到了", "涨知识了"
    );

    /**
     * 生成随机中文名字
     */
    public String generateChineseName() {
        return faker.name().lastName() + faker.name().firstName();
    }

    /**
     * 生成主播昵称
     */
    public String generateAnchorNickname() {
        String prefix = ANCHOR_PREFIXES.get(random.nextInt(ANCHOR_PREFIXES.size()));
        String name = generateChineseName();
        return prefix + name;
    }

    /**
     * 生成Bot观众昵称
     */
    public String generateBotNickname(String prefix) {
        return prefix + TraceIdGenerator.generateShortUuid();
    }

    /**
     * 生成头像URL
     */
    public String generateAvatarUrl() {
        int id = random.nextInt(1000);
        return "https://api.dicebear.com/7.x/avataaars/svg?seed=" + id;
    }

    /**
     * 生成个人简介
     */
    public String generateBio() {
        List<String> bios = Arrays.asList(
            "喜欢" + randomActivity() + "的" + (random.nextBoolean() ? "小姐姐" : "小哥哥"),
            "热爱生活，享受每一天",
            "分享快乐，传递正能量",
            "一起" + randomActivity() + "吧！",
            faker.lorem().sentence(10)
        );
        return bios.get(random.nextInt(bios.size()));
    }

    /**
     * 生成直播间标题
     */
    public String generateRoomTitle(String anchorName) {
        String template = ROOM_TITLE_TEMPLATES.get(random.nextInt(ROOM_TITLE_TEMPLATES.size()));
        return template.replace("{name}", anchorName)
                      .replace("{activity}", randomActivity());
    }

    /**
     * 生成直播间分类
     */
    public String generateCategory() {
        return CATEGORIES.get(random.nextInt(CATEGORIES.size()));
    }

    /**
     * 生成封面URL
     */
    public String generateCoverUrl() {
        int id = random.nextInt(1000);
        return "https://picsum.photos/seed/" + id + "/640/360";
    }

    /**
     * 随机选择标签
     */
    public List<String> generateRandomTags(int minCount, int maxCount) {
        int count = random.nextInt(maxCount - minCount + 1) + minCount;
        List<String> allTags = new ArrayList<>(TAGS);
        Collections.shuffle(allTags);
        return allTags.subList(0, Math.min(count, allTags.size()));
    }

    /**
     * 生成随机年龄
     */
    public int generateAge(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }

    /**
     * 生成随机性别（1-男，2-女）
     */
    public int generateGender(int malePercentage) {
        return random.nextInt(100) < malePercentage ? 1 : 2;
    }

    /**
     * 生成消费等级（0-低，1-中，2-高）
     */
    public int generateConsumptionLevel(int lowRate, int mediumRate) {
        int rand = random.nextInt(100);
        if (rand < lowRate) {
            return 0;
        } else if (rand < lowRate + mediumRate) {
            return 1;
        } else {
            return 2;
        }
    }

    /**
     * 生成打赏金额
     */
    public BigDecimal generateRechargeAmount(double min, double max) {
        double amount = min + (max - min) * random.nextDouble();
        return BigDecimal.valueOf(amount).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 生成弹幕内容
     */
    public String generateDanmaku() {
        return DANMAKU_TEMPLATES.get(random.nextInt(DANMAKU_TEMPLATES.size()));
    }

    /**
     * 随机活动
     */
    private String randomActivity() {
        return ACTIVITIES.get(random.nextInt(ACTIVITIES.size()));
    }

    /**
     * 生成随机布尔值
     */
    public boolean randomBoolean() {
        return random.nextBoolean();
    }

    /**
     * 生成随机整数（闭区间）
     */
    public int randomInt(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }

    /**
     * 生成随机长整数（闭区间）
     */
    public long randomLong(long min, long max) {
        return min + (long) (random.nextDouble() * (max - min));
    }
}
