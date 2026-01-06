package com.liveroom.anchor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 主播服务启动类
 *
 * 核心功能：
 * 1. 主播信息管理（创建、查询、更新）
 * 2. 直播间生命周期管理（开播、关播、实时数据更新）
 * 3. 主播统计数据维护（粉丝数、点赞数、累计收益）
 * 
 * @author Team
 * @version 1.0.0
 */
@SpringBootApplication(scanBasePackages = {"com.liveroom", "common"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.liveroom"})
@EnableScheduling
public class AnchorServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnchorServiceApplication.class, args);
        System.out.println("======================================");
        System.out.println("   Anchor Service 启动成功！");
        System.out.println("   端口: 8081");
        System.out.println("   上下文路径: /anchor");
        System.out.println("======================================");
    }

}
