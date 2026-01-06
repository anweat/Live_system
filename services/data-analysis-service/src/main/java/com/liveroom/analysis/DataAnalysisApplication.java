package com.liveroom.analysis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 数据分析服务启动类
 */
@SpringBootApplication(scanBasePackages = {"com.liveroom.analysis", "common"})
@EnableFeignClients
@EnableCaching
@EnableScheduling
@EnableAsync
public class DataAnalysisApplication {
    public static void main(String[] args) {
        SpringApplication.run(DataAnalysisApplication.class, args);
        System.out.println("========================================");
        System.out.println("数据分析服务启动成功！");
        System.out.println("端口: 8084");
        System.out.println("上下文路径: /data-analysis");
        System.out.println("========================================");
    }
}
