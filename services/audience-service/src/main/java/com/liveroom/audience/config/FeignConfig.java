package com.liveroom.audience.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * Feign客户端配置
 * 开启OpenFeign服务调用功能
 */
@Configuration
@EnableFeignClients(basePackages = "com.liveroom.audience.feign")
public class FeignConfig {
    // Feign客户端配置
}
