package com.liveroom.mock.constant;

/**
 * 任务状态常量
 */
public class TaskStatus {
    
    public static final String PENDING = "PENDING";
    public static final String RUNNING = "RUNNING";
    public static final String COMPLETED = "COMPLETED";
    public static final String FAILED = "FAILED";
    public static final String CANCELLED = "CANCELLED";
    
    private TaskStatus() {
        // 工具类，禁止实例化
    }
}
