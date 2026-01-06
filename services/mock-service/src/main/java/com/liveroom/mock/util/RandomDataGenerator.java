package com.liveroom.mock.util;

import com.github.javafaker.Faker;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 随机数据生成工具
 */
@Component
public class RandomDataGenerator {

    private static final Faker faker = new Faker(Locale.CHINA);
    
    // 预定义的直播间标题模板
    private static final List<String> TITLE_TEMPLATES = Arrays.asList(
            "{name}的直播间",
            "一起{activity}吧~",
            "{adjective}的{name}",
            "今天{activity}啦",
            "{name}带你{activity}"
    );

    // 活动关键词
    private static final List<String> ACTIVITIES = Arrays.asList(
            "唱歌", "跳舞", "聊天", "玩游戏", "学习", "做饭", "运动", "画画", "看书", "旅游"
    );

    // 形容词
    private static final List<String> ADJECTIVES = Arrays.asList(
            "可爱", "帅气", "温柔", "活泼", "开朗", "幽默", "努力", "认真", "快乐", "热情"
    );

    // 弹幕模板
    private static final List<String> MESSAGE_TEMPLATES = Arrays.asList(
            "主播好棒！", "666", "支持主播！", "来了来了", "厉害了",
            "好看！", "加油！", "哈哈哈", "学到了", "太强了",
            "关注主播了", "打卡", "前排", "沙发", "每天都来",
            "主播辛苦了", "给力", "赞", "不错不错", "继续继续"
    );

    // 中文姓氏
    private static final List<String> SURNAMES = Arrays.asList(
            "王", "李", "张", "刘", "陈", "杨", "黄", "赵", "吴", "周",
            "徐", "孙", "马", "朱", "胡", "郭", "何", "高", "林", "罗"
    );

    // 中文名字（双字）
    private static final List<String> GIVEN_NAMES = Arrays.asList(
            "伟", "芳", "娜", "敏", "静", "丽", "强", "磊", "军", "洋",
            "勇", "艳", "杰", "娟", "涛", "明", "超", "秀英", "霞", "平"
    );

    /**
     * 生成随机中文名字
     */
    public String generateChineseName() {
        String surname = SURNAMES.get(ThreadLocalRandom.current().nextInt(SURNAMES.size()));
        String givenName = GIVEN_NAMES.get(ThreadLocalRandom.current().nextInt(GIVEN_NAMES.size()));
        return surname + givenName;
    }

    /**
     * 生成Bot名称
     */
    public String generateBotName(String prefix) {
        return prefix + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * 生成性别（0-女，1-男）
     */
    public int generateGender(int malePercentage) {
        return ThreadLocalRandom.current().nextInt(100) < malePercentage ? 1 : 0;
    }

    /**
     * 生成年龄
     */
    public int generateAge(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    /**
     * 生成消费等级（0-低、1-中、2-高）
     */
    public int generateConsumptionLevel(int lowRate, int mediumRate, int highRate) {
        int random = ThreadLocalRandom.current().nextInt(100);
        if (random < lowRate) {
            return 0;
        } else if (random < lowRate + mediumRate) {
            return 1;
        } else {
            return 2;
        }
    }

    /**
     * 生成头像URL
     */
    public String generateAvatarUrl() {
        int avatarId = ThreadLocalRandom.current().nextInt(1, 100);
        return "https://api.dicebear.com/7.x/avatars/svg?seed=" + avatarId;
    }

    /**
     * 生成个人简介
     */
    public String generateBio() {
        return faker.lorem().sentence(10);
    }

    /**
     * 生成直播间标题
     */
    public String generateLiveRoomTitle(String anchorName) {
        String template = TITLE_TEMPLATES.get(ThreadLocalRandom.current().nextInt(TITLE_TEMPLATES.size()));
        template = template.replace("{name}", anchorName);
        template = template.replace("{activity}", ACTIVITIES.get(ThreadLocalRandom.current().nextInt(ACTIVITIES.size())));
        template = template.replace("{adjective}", ADJECTIVES.get(ThreadLocalRandom.current().nextInt(ADJECTIVES.size())));
        return template;
    }

    /**
     * 生成直播间描述
     */
    public String generateLiveRoomDescription() {
        return faker.lorem().sentence(20);
    }

    /**
     * 生成直播间分类
     */
    public String generateCategory() {
        List<String> categories = Arrays.asList("娱乐", "游戏", "音乐", "舞蹈", "美食", "运动", "学习", "聊天", "才艺", "其他");
        return categories.get(ThreadLocalRandom.current().nextInt(categories.size()));
    }

    /**
     * 生成封面URL
     */
    public String generateCoverUrl() {
        int coverId = ThreadLocalRandom.current().nextInt(1, 50);
        return "https://picsum.photos/640/360?random=" + coverId;
    }

    /**
     * 生成弹幕消息
     */
    public String generateMessage() {
        return MESSAGE_TEMPLATES.get(ThreadLocalRandom.current().nextInt(MESSAGE_TEMPLATES.size()));
    }

    /**
     * 生成打赏金额
     */
    public double generateRechargeAmount(double min, double max) {
        double amount = ThreadLocalRandom.current().nextDouble(min, max);
        return Math.round(amount * 100.0) / 100.0;
    }

    /**
     * 随机选择元素
     */
    public <T> T randomElement(List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }

    /**
     * 随机选择多个元素
     */
    public <T> List<T> randomElements(List<T> list, int count) {
        if (list == null || list.isEmpty() || count <= 0) {
            return Collections.emptyList();
        }
        
        count = Math.min(count, list.size());
        List<T> shuffled = new ArrayList<>(list);
        Collections.shuffle(shuffled);
        return shuffled.subList(0, count);
    }

    /**
     * 生成随机延迟时间（毫秒）
     */
    public long generateDelay(long min, long max) {
        return ThreadLocalRandom.current().nextLong(min, max + 1);
    }

    /**
     * 判断概率事件是否发生
     */
    public boolean happens(int probability) {
        return ThreadLocalRandom.current().nextInt(100) < probability;
    }
}
