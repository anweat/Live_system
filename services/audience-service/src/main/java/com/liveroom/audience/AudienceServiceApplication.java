package com.liveroom.audience;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 观众服务启动类
 * 
 * 主要功能：
 * - 观众信息管理（创建、查询、修改）
 * - 打赏请求处理（幂等性控制）
 * - 数据同步（打赏数据同步到财务服务）
 * - 用户画像分析
 * 
 * 启动环境变量：
 * - DB_HOST: 数据库主机（默认localhost）
 * - DB_PORT: 数据库端口（默认3306）
 * - DB_NAME: 数据库名称（默认live_system）
 * - DB_USER: 数据库用户（默认root）
 * - DB_PASS: 数据库密码（默认123456）
 * - REDIS_ENABLED: Redis是否启用（默认false）
 * 
 * @author Team
 * @version 1.0.0
 */
@SpringBootApplication(scanBasePackages = {"com.liveroom"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.liveroom.audience.feign"})
@EnableScheduling
public class AudienceServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AudienceServiceApplication.class, args);
    }
}
