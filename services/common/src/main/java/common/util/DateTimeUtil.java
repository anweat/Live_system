package common.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * 日期时间工具类
 * 
 * 功能：
 * - 格式化日期时间
 * - 解析日期时间字符串
 * - 时间戳转换
 * - 时间计算
 */
public class DateTimeUtil {

    // 常用日期格式化器
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DATETIME_MS_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * 获取当前日期时间（LocalDateTime）
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * 获取当前日期（LocalDate）
     */
    public static LocalDate today() {
        return LocalDate.now();
    }

    /**
     * 获取当前时间毫秒时间戳
     */
    public static long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * 获取当前时间秒级时间戳
     */
    public static long currentTimeSeconds() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * 格式化日期时间为字符串（默认格式：yyyy-MM-dd HH:mm:ss）
     * 
     * @param dateTime 日期时间对象
     * @return 格式化后的字符串
     */
    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DATETIME_FORMATTER);
    }

    /**
     * 格式化日期时间为字符串（指定格式）
     * 
     * @param dateTime 日期时间对象
     * @param pattern  格式化模式
     * @return 格式化后的字符串
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return dateTime.format(formatter);
    }

    /**
     * 格式化日期为字符串（默认格式：yyyy-MM-dd）
     * 
     * @param date 日期对象
     * @return 格式化后的字符串
     */
    public static String format(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.format(DATE_FORMATTER);
    }

    /**
     * 格式化日期为字符串（指定格式）
     * 
     * @param date    日期对象
     * @param pattern 格式化模式
     * @return 格式化后的字符串
     */
    public static String format(LocalDate date, String pattern) {
        if (date == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return date.format(formatter);
    }

    /**
     * 解析字符串为日期时间（默认格式：yyyy-MM-dd HH:mm:ss）
     * 
     * @param dateTimeStr 日期时间字符串
     * @return LocalDateTime 对象
     */
    public static LocalDateTime parse(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
    }

    /**
     * 解析字符串为日期时间（指定格式）
     * 
     * @param dateTimeStr 日期时间字符串
     * @param pattern     格式化模式
     * @return LocalDateTime 对象
     */
    public static LocalDateTime parse(String dateTimeStr, String pattern) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return LocalDateTime.parse(dateTimeStr, formatter);
    }

    /**
     * 解析字符串为日期（默认格式：yyyy-MM-dd）
     * 
     * @param dateStr 日期字符串
     * @return LocalDate 对象
     */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        return LocalDate.parse(dateStr, DATE_FORMATTER);
    }

    /**
     * 解析字符串为日期（指定格式）
     * 
     * @param dateStr 日期字符串
     * @param pattern 格式化模式
     * @return LocalDate 对象
     */
    public static LocalDate parseDate(String dateStr, String pattern) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return LocalDate.parse(dateStr, formatter);
    }

    /**
     * LocalDateTime 转毫秒时间戳
     * 
     * @param dateTime 日期时间对象
     * @return 毫秒时间戳
     */
    public static long toMillis(LocalDateTime dateTime) {
        if (dateTime == null) {
            return 0;
        }
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * LocalDateTime 转秒级时间戳
     * 
     * @param dateTime 日期时间对象
     * @return 秒级时间戳
     */
    public static long toSeconds(LocalDateTime dateTime) {
        if (dateTime == null) {
            return 0;
        }
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();
    }

    /**
     * 毫秒时间戳转 LocalDateTime
     * 
     * @param millis 毫秒时间戳
     * @return LocalDateTime 对象
     */
    public static LocalDateTime fromMillis(long millis) {
        return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(millis),
                ZoneId.systemDefault());
    }

    /**
     * 秒级时间戳转 LocalDateTime
     * 
     * @param seconds 秒级时间戳
     * @return LocalDateTime 对象
     */
    public static LocalDateTime fromSeconds(long seconds) {
        return LocalDateTime.ofInstant(
                Instant.ofEpochSecond(seconds),
                ZoneId.systemDefault());
    }

    /**
     * 计算两个日期时间的差值（毫秒）
     * 
     * @param startDateTime 开始时间
     * @param endDateTime   结束时间
     * @return 差值（毫秒）
     */
    public static long getDiffMillis(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (startDateTime == null || endDateTime == null) {
            return 0;
        }
        return Duration.between(startDateTime, endDateTime).toMillis();
    }

    /**
     * 计算两个日期时间的差值（秒）
     * 
     * @param startDateTime 开始时间
     * @param endDateTime   结束时间
     * @return 差值（秒）
     */
    public static long getDiffSeconds(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (startDateTime == null || endDateTime == null) {
            return 0;
        }
        return Duration.between(startDateTime, endDateTime).getSeconds();
    }

    /**
     * 计算两个日期时间的差值（分钟）
     * 
     * @param startDateTime 开始时间
     * @param endDateTime   结束时间
     * @return 差值（分钟）
     */
    public static long getDiffMinutes(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (startDateTime == null || endDateTime == null) {
            return 0;
        }
        return Duration.between(startDateTime, endDateTime).toMinutes();
    }

    /**
     * 计算两个日期时间的差值（小时）
     * 
     * @param startDateTime 开始时间
     * @param endDateTime   结束时间
     * @return 差值（小时）
     */
    public static long getDiffHours(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (startDateTime == null || endDateTime == null) {
            return 0;
        }
        return Duration.between(startDateTime, endDateTime).toHours();
    }

    /**
     * 计算两个日期的差值（天数）
     * 
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 差值（天数）
     */
    public static long getDiffDays(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * 日期时间加天数
     * 
     * @param dateTime 日期时间
     * @param days     天数
     * @return 计算后的日期时间
     */
    public static LocalDateTime addDays(LocalDateTime dateTime, long days) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.plusDays(days);
    }

    /**
     * 日期加天数
     * 
     * @param date 日期
     * @param days 天数
     * @return 计算后的日期
     */
    public static LocalDate addDays(LocalDate date, long days) {
        if (date == null) {
            return null;
        }
        return date.plusDays(days);
    }

    /**
     * 日期时间加小时
     * 
     * @param dateTime 日期时间
     * @param hours    小时数
     * @return 计算后的日期时间
     */
    public static LocalDateTime addHours(LocalDateTime dateTime, long hours) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.plusHours(hours);
    }

    /**
     * 日期时间加分钟
     * 
     * @param dateTime 日期时间
     * @param minutes  分钟数
     * @return 计算后的日期时间
     */
    public static LocalDateTime addMinutes(LocalDateTime dateTime, long minutes) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.plusMinutes(minutes);
    }

    /**
     * 获取某月的第一天
     * 
     * @param dateTime 该月的任意一个日期
     * @return 该月的第一天
     */
    public static LocalDate getFirstDayOfMonth(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.toLocalDate().withDayOfMonth(1);
    }

    /**
     * 获取某月的最后一天
     * 
     * @param dateTime 该月的任意一个日期
     * @return 该月的最后一天
     */
    public static LocalDate getLastDayOfMonth(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.toLocalDate().withDayOfMonth(dateTime.toLocalDate().lengthOfMonth());
    }

    /**
     * 判断是否为闰年
     * 
     * @param year 年份
     * @return true 为闰年，false 不为闰年
     */
    public static boolean isLeapYear(int year) {
        return Year.of(year).isLeap();
    }

    /**
     * 获取日期是周几（1=周一，7=周日）
     * 
     * @param date 日期
     * @return 周几（1-7）
     */
    public static int getDayOfWeek(LocalDate date) {
        if (date == null) {
            return 0;
        }
        return date.getDayOfWeek().getValue();
    }

    /**
     * Java Date 转 LocalDateTime
     * 
     * @param date Java Date 对象
     * @return LocalDateTime 对象
     */
    public static LocalDateTime dateToLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * LocalDateTime 转 Java Date
     * 
     * @param dateTime LocalDateTime 对象
     * @return Java Date 对象
     */
    public static Date localDateTimeToDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
