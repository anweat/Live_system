package com.liveroom.dbservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import common.logger.AppLogger;

/**
 * 数据库服务启动类
 * 
 * 功能：
 * 1. 初始化数据库表结构（通过Flyway自动迁移）
 * 2. 提供数据库管理接口（REST API）
 * 3. 执行数据库健康检查
 * 4. 支持数据导入导出等工具
 */
@SpringBootApplication
@EnableScheduling
public class DbServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DbServiceApplication.class, args);

        // 记录应用启动成功
        AppLogger.logStartup("db-service", "1.0.0", 8081);

        // 记录服务就绪
        AppLogger.logReady("db-service");

        // 记录访问信息
        AppLogger.info("服务地址: http://localhost:8081");
        AppLogger.info("健康检查: http://localhost:8081/actuator/health");
        AppLogger.info("API文档: http://localhost:8081/swagger-ui.html");
    }
}
