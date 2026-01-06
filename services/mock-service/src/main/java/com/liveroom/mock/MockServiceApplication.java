package com.liveroom.mock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Mock Service 启动类
 * 模拟测试服务，用于生成测试数据和模拟用户行为
 */
@SpringBootApplication
@EnableFeignClients
@EnableScheduling
@EnableAsync
@ComponentScan(basePackages = {"com.liveroom.mock", "common"})
@EntityScan(basePackages = {"common.bean"})
@EnableJpaRepositories(basePackages = {"com.liveroom.mock.repository"})
public class MockServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MockServiceApplication.class, args);
        System.out.println("========================================");
        System.out.println("Mock Service 启动成功!");
        System.out.println("访问地址: http://localhost:8090/mock");
        System.out.println("========================================");
    }
}
