package com.liveroom.audience.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 定时任务配置
 * 开启Spring定时任务功能
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
    // 定时任务配置已在此启用
}
