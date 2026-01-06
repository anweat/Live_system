package common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 金钱处理工具类
 * 
 * 功能：
 * - 金额格式化
 * - 金额计算（避免浮点数精度问题）
 * - 四舍五入
 * - 金额比较
 */
public class MoneyUtil {

    // 默认两位小数
    private static final int DEFAULT_SCALE = 2;
    private static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_UP;

    /**
     * 将分转换为元（Long 类型）
     * 
     * @param cents 分数
     * @return 元数（BigDecimal）
     */
    public static BigDecimal centsToDollars(long cents) {
        return BigDecimal.valueOf(cents).divide(BigDecimal.valueOf(100), DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);
    }

    /**
     * 将元转换为分（Long 类型）
     * 
     * @param dollars 元数
     * @return 分数（Long）
     */
    public static long dollarsToCents(BigDecimal dollars) {
        if (dollars == null) {
            return 0;
        }
        return dollars.multiply(BigDecimal.valueOf(100)).longValue();
    }

    /**
     * 将元转换为分（Double 类型）
     * 
     * @param dollars 元数
     * @return 分数（Long）
     */
    public static long dollarsToCents(double dollars) {
        return dollarsToCents(BigDecimal.valueOf(dollars));
    }

    /**
     * 格式化金额为字符串（两位小数）
     * 
     * @param amount 金额
     * @return 格式化后的字符串（如：1234.56）
     */
    public static String formatAmount(BigDecimal amount) {
        return formatAmount(amount, DEFAULT_SCALE);
    }

    /**
     * 格式化金额为字符串（指定小数位）
     * 
     * @param amount 金额
     * @param scale  小数位数
     * @return 格式化后的字符串
     */
    public static String formatAmount(BigDecimal amount, int scale) {
        if (amount == null) {
            return "0.00";
        }
        return amount.setScale(scale, DEFAULT_ROUNDING_MODE).toPlainString();
    }

    /**
     * 格式化金额为字符串（两位小数）
     * 
     * @param amount 金额
     * @return 格式化后的字符串
     */
    public static String formatAmount(double amount) {
        return formatAmount(BigDecimal.valueOf(amount), DEFAULT_SCALE);
    }

    /**
     * 金额四舍五入（两位小数）
     * 
     * @param amount 金额
     * @return 四舍五入后的金额
     */
    public static BigDecimal round(BigDecimal amount) {
        return round(amount, DEFAULT_SCALE);
    }

    /**
     * 金额四舍五入（指定小数位）
     * 
     * @param amount 金额
     * @param scale  小数位数
     * @return 四舍五入后的金额
     */
    public static BigDecimal round(BigDecimal amount, int scale) {
        if (amount == null) {
            return BigDecimal.ZERO.setScale(scale, DEFAULT_ROUNDING_MODE);
        }
        return amount.setScale(scale, DEFAULT_ROUNDING_MODE);
    }

    /**
     * 金额四舍五入（两位小数）
     * 
     * @param amount 金额
     * @return 四舍五入后的金额
     */
    public static BigDecimal round(double amount) {
        return round(BigDecimal.valueOf(amount), DEFAULT_SCALE);
    }

    /**
     * 金额相加
     * 
     * @param a 金额 A
     * @param b 金额 B
     * @return 相加结果
     */
    public static BigDecimal add(BigDecimal a, BigDecimal b) {
        if (a == null) {
            a = BigDecimal.ZERO;
        }
        if (b == null) {
            b = BigDecimal.ZERO;
        }
        return a.add(b).setScale(DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);
    }

    /**
     * 金额相减
     * 
     * @param a 金额 A
     * @param b 金额 B（被减数）
     * @return 相减结果
     */
    public static BigDecimal subtract(BigDecimal a, BigDecimal b) {
        if (a == null) {
            a = BigDecimal.ZERO;
        }
        if (b == null) {
            b = BigDecimal.ZERO;
        }
        return a.subtract(b).setScale(DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);
    }

    /**
     * 金额相乘
     * 
     * @param a 金额 A
     * @param b 金额 B
     * @return 相乘结果
     */
    public static BigDecimal multiply(BigDecimal a, BigDecimal b) {
        if (a == null) {
            a = BigDecimal.ZERO;
        }
        if (b == null) {
            b = BigDecimal.ZERO;
        }
        return a.multiply(b).setScale(DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);
    }

    /**
     * 金额相除
     * 
     * @param a 被除数
     * @param b 除数
     * @return 相除结果
     */
    public static BigDecimal divide(BigDecimal a, BigDecimal b) {
        if (a == null) {
            a = BigDecimal.ZERO;
        }
        if (b == null || b.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("除数不能为 0");
        }
        return a.divide(b, DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);
    }

    /**
     * 金额比较（A > B）
     * 
     * @param a 金额 A
     * @param b 金额 B
     * @return true 表示 A > B
     */
    public static boolean greaterThan(BigDecimal a, BigDecimal b) {
        if (a == null || b == null) {
            return false;
        }
        return a.compareTo(b) > 0;
    }

    /**
     * 金额比较（A >= B）
     * 
     * @param a 金额 A
     * @param b 金额 B
     * @return true 表示 A >= B
     */
    public static boolean greaterThanOrEqual(BigDecimal a, BigDecimal b) {
        if (a == null || b == null) {
            return false;
        }
        return a.compareTo(b) >= 0;
    }

    /**
     * 金额比较（A < B）
     * 
     * @param a 金额 A
     * @param b 金额 B
     * @return true 表示 A < B
     */
    public static boolean lessThan(BigDecimal a, BigDecimal b) {
        if (a == null || b == null) {
            return false;
        }
        return a.compareTo(b) < 0;
    }

    /**
     * 金额比较（A <= B）
     * 
     * @param a 金额 A
     * @param b 金额 B
     * @return true 表示 A <= B
     */
    public static boolean lessThanOrEqual(BigDecimal a, BigDecimal b) {
        if (a == null || b == null) {
            return false;
        }
        return a.compareTo(b) <= 0;
    }

    /**
     * 金额比较（A == B）
     * 
     * @param a 金额 A
     * @param b 金额 B
     * @return true 表示 A == B
     */
    public static boolean equals(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.compareTo(b) == 0;
    }

    /**
     * 判断金额是否为正数（> 0）
     * 
     * @param amount 金额
     * @return true 表示正数
     */
    public static boolean isPositive(BigDecimal amount) {
        if (amount == null) {
            return false;
        }
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 判断金额是否为负数（< 0）
     * 
     * @param amount 金额
     * @return true 表示负数
     */
    public static boolean isNegative(BigDecimal amount) {
        if (amount == null) {
            return false;
        }
        return amount.compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * 判断金额是否为零
     * 
     * @param amount 金额
     * @return true 表示为零
     */
    public static boolean isZero(BigDecimal amount) {
        if (amount == null) {
            return true;
        }
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * 获取金额的绝对值
     * 
     * @param amount 金额
     * @return 绝对值
     */
    public static BigDecimal abs(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        return amount.abs();
    }

    /**
     * 计算百分比（amount * percent / 100）
     * 
     * @param amount  金额
     * @param percent 百分比（如：10 表示 10%）
     * @return 计算结果
     */
    public static BigDecimal calculatePercent(BigDecimal amount, BigDecimal percent) {
        if (amount == null || percent == null) {
            return BigDecimal.ZERO;
        }
        return amount.multiply(percent).divide(BigDecimal.valueOf(100), DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);
    }

    /**
     * 计算百分比（amount * percent / 100）
     * 
     * @param amount  金额
     * @param percent 百分比（如：0.1 表示 10%）
     * @return 计算结果
     */
    public static BigDecimal calculatePercentDecimal(BigDecimal amount, BigDecimal percent) {
        if (amount == null || percent == null) {
            return BigDecimal.ZERO;
        }
        return amount.multiply(percent).setScale(DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);
    }
}
