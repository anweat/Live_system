package com.liveroom.redisservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import common.logger.AppLogger;

/**
 * Redis 缓存服务启动类
 * 统一管理项目中所有 Redis 缓存操作
 */
@SpringBootApplication
@EnableScheduling
public class RedisServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedisServiceApplication.class, args);
        AppLogger.logServiceInitializeComplete("redis-service");
    }
}
