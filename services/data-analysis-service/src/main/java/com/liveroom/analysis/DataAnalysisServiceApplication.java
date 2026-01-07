package com.liveroom.analysis;

import common.logger.TraceLogger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 数据分析服务主应用
 */
@SpringBootApplication
@EnableCaching
@EnableScheduling
@EnableAsync
@EnableFeignClients(basePackages = {"com.liveroom.analysis.feign", "common.feign"})
@ComponentScan(basePackages = {"com.liveroom.analysis", "common"})
@EntityScan(basePackages = {"common.bean"})
@EnableJpaRepositories(basePackages = {"common.repository"})
public class DataAnalysisServiceApplication {

    public static void main(String[] args) {
        try {
            SpringApplication.run(DataAnalysisServiceApplication.class, args);
            TraceLogger.logServiceStartup("data-analysis-service", "1.0.0", 8088);
        } catch (Exception e) {
            TraceLogger.error("DataAnalysisService", "启动失败", e);
            throw e;
        }
    }
}
