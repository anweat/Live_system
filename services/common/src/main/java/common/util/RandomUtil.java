package common.util;

import java.util.UUID;
import java.util.Random;

/**
 * 随机数/字符串工具类
 * 
 * 功能：
 * - 生成随机字符串
 * - 生成随机数字
 * - 生成 UUID
 * - 生成验证码
 */
public class RandomUtil {

    private static final Random RANDOM = new Random();

    // 常用字符集
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()-_+=[]{}|;:',.<>?/`~";

    /**
     * 生成 UUID（无中划线）
     * 
     * @return UUID 字符串
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成 UUID（带中划线）
     * 
     * @return UUID 字符串
     */
    public static String generateUUIDWithLine() {
        return UUID.randomUUID().toString();
    }

    /**
     * 生成随机字符串（仅包含字母）
     * 
     * @param length 字符串长度
     * @return 随机字符串
     */
    public static String generateRandomString(int length) {
        return generateRandomString(length, true, false, false, false);
    }

    /**
     * 生成随机字符串（包含大小写字母和数字）
     * 
     * @param length 字符串长度
     * @return 随机字符串
     */
    public static String generateRandomAlphanumeric(int length) {
        return generateRandomString(length, true, true, true, false);
    }

    /**
     * 生成随机字符串（自定义包含字符类型）
     * 
     * @param length           字符串长度
     * @param includeLowercase 是否包含小写字母
     * @param includeUppercase 是否包含大写字母
     * @param includeDigits    是否包含数字
     * @param includeSpecial   是否包含特殊字符
     * @return 随机字符串
     */
    public static String generateRandomString(int length,
            boolean includeLowercase,
            boolean includeUppercase,
            boolean includeDigits,
            boolean includeSpecial) {
        if (length <= 0) {
            throw new IllegalArgumentException("长度必须大于 0");
        }

        StringBuilder chars = new StringBuilder();
        if (includeLowercase) {
            chars.append(LOWERCASE);
        }
        if (includeUppercase) {
            chars.append(UPPERCASE);
        }
        if (includeDigits) {
            chars.append(DIGITS);
        }
        if (includeSpecial) {
            chars.append(SPECIAL);
        }

        if (chars.length() == 0) {
            throw new IllegalArgumentException("至少需要选择一种字符类型");
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(chars.length());
            result.append(chars.charAt(index));
        }

        return result.toString();
    }

    /**
     * 生成随机数字字符串（仅包含 0-9）
     * 
     * @param length 字符串长度
     * @return 随机数字字符串
     */
    public static String generateRandomNumbers(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("长度必须大于 0");
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            result.append(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        }
        return result.toString();
    }

    /**
     * 生成随机整数（范围：0 ~ bound-1）
     * 
     * @param bound 上界（不包含）
     * @return 随机整数
     */
    public static int nextInt(int bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException("上界必须大于 0");
        }
        return RANDOM.nextInt(bound);
    }

    /**
     * 生成随机整数（范围：origin ~ bound-1）
     * 
     * @param origin 下界（包含）
     * @param bound  上界（不包含）
     * @return 随机整数
     */
    public static int nextInt(int origin, int bound) {
        if (origin >= bound) {
            throw new IllegalArgumentException("origin 必须小于 bound");
        }
        return origin + RANDOM.nextInt(bound - origin);
    }

    /**
     * 生成随机长整数
     * 
     * @return 随机长整数
     */
    public static long nextLong() {
        return RANDOM.nextLong();
    }

    /**
     * 生成随机长整数（范围：0 ~ bound-1）
     * 
     * @param bound 上界（不包含）
     * @return 随机长整数
     */
    public static long nextLong(long bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException("上界必须大于 0");
        }
        return Math.abs(RANDOM.nextLong()) % bound;
    }

    /**
     * 生成随机长整数（范围：origin ~ bound-1）
     * 
     * @param origin 下界（包含）
     * @param bound  上界（不包含）
     * @return 随机长整数
     */
    public static long nextLong(long origin, long bound) {
        if (origin >= bound) {
            throw new IllegalArgumentException("origin 必须小于 bound");
        }
        return origin + (Math.abs(RANDOM.nextLong()) % (bound - origin));
    }

    /**
     * 生成随机浮点数（范围：0.0 ~ 1.0）
     * 
     * @return 随机浮点数
     */
    public static double nextDouble() {
        return RANDOM.nextDouble();
    }

    /**
     * 生成随机浮点数（范围：0.0 ~ bound）
     * 
     * @param bound 上界
     * @return 随机浮点数
     */
    public static double nextDouble(double bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException("上界必须大于 0");
        }
        return RANDOM.nextDouble() * bound;
    }

    /**
     * 生成随机布尔值
     * 
     * @return true 或 false
     */
    public static boolean nextBoolean() {
        return RANDOM.nextBoolean();
    }

    /**
     * 生成数字验证码（6位）
     * 
     * @return 验证码字符串
     */
    public static String generateVerificationCode() {
        return generateVerificationCode(6);
    }

    /**
     * 生成数字验证码（指定位数）
     * 
     * @param length 位数
     * @return 验证码字符串
     */
    public static String generateVerificationCode(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("位数必须大于 0");
        }
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        }
        return code.toString();
    }

    /**
     * 生成验证码（包含字母和数字）
     * 
     * @param length 长度
     * @return 验证码字符串
     */
    public static String generateAlphanumericCode(int length) {
        return generateRandomAlphanumeric(length);
    }

    /**
     * 从数组中随机选择一个元素
     * 
     * @param array 数组
     * @param <T>   元素类型
     * @return 随机选择的元素
     */
    public static <T> T randomChoice(T[] array) {
        if (array == null || array.length == 0) {
            throw new IllegalArgumentException("数组不能为空");
        }
        return array[RANDOM.nextInt(array.length)];
    }

    /**
     * 从字符串中随机选择一个字符
     * 
     * @param str 字符串
     * @return 随机选择的字符
     */
    public static char randomChar(String str) {
        if (str == null || str.isEmpty()) {
            throw new IllegalArgumentException("字符串不能为空");
        }
        return str.charAt(RANDOM.nextInt(str.length()));
    }
}
