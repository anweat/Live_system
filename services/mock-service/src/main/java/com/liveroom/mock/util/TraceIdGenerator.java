package com.liveroom.mock.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * TraceId 生成器
 * 格式: PREFIX-YYYYMMDD-HHMMSS-SEQUENCE
 */
public class TraceIdGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmmss");
    private static final AtomicLong SEQUENCE = new AtomicLong(0);

    /**
     * 生成 TraceId
     * 
     * @param prefix 前缀
     * @return traceId
     */
    public static String generate(String prefix) {
        LocalDateTime now = LocalDateTime.now();
        String date = now.format(DATE_FORMATTER);
        String time = now.format(TIME_FORMATTER);
        long seq = SEQUENCE.incrementAndGet() % 10000; // 4位序列号
        
        return String.format("%s-%s-%s-%04d", prefix, date, time, seq);
    }

    /**
     * 生成批次ID
     * 
     * @param batchType 批次类型
     * @return batchId
     */
    public static String generateBatchId(String batchType) {
        return String.format("BATCH_%s_%d", batchType, System.currentTimeMillis());
    }

    /**
     * 生成任务ID
     * 
     * @return taskId
     */
    public static String generateTaskId() {
        return String.format("TASK_%d", System.currentTimeMillis());
    }

    /**
     * 生成随机UUID（短格式，用于Bot名称等）
     * 
     * @return 8位随机字符串
     */
    public static String generateShortUuid() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private TraceIdGenerator() {
        // 工具类，禁止实例化
    }
}
