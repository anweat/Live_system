package com.liveroom.mock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import common.logger.AppLogger;

/**
 * Mock Service 启动类
 * 用于模拟主播、观众、直播间和各种行为的测试服务
 */
@SpringBootApplication(scanBasePackages = {"com.liveroom.mock", "common"})
@EnableFeignClients
@EnableAsync
@EnableScheduling
public class MockServiceApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(MockServiceApplication.class, args);
        
        // 获取配置信息
        String appName = context.getEnvironment().getProperty("spring.application.name");
        String port = context.getEnvironment().getProperty("server.port");
        String contextPath = context.getEnvironment().getProperty("server.servlet.context-path");
        
        AppLogger.logStartup(appName, "2.0", Integer.parseInt(port));
        AppLogger.info("服务访问地址: http://localhost:{}{}", port, contextPath);
        AppLogger.info("API文档地址: http://localhost:{}{}/swagger-ui.html", port, contextPath);
        AppLogger.logReady(appName);
    }
}
