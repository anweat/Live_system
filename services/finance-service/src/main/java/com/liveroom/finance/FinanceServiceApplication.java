package com.liveroom.finance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 财务服务启动类
 * 负责打赏结算、分成管理、提现处理
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.liveroom.finance.feign")
@EnableScheduling
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.liveroom.finance.repository")
@ComponentScan(basePackages = {"com.liveroom.finance", "common"})
public class FinanceServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinanceServiceApplication.class, args);
        System.out.println("========================================");
        System.out.println("   Finance Service Started Successfully");
        System.out.println("========================================");
    }
}
