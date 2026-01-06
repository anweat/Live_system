package com.liveroom.finance.config;

import feign.Logger;
import feign.Request;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign客户端配置
 * 配置超时、重试、日志级别
 */
@Configuration
public class FeignConfig {

    /**
     * Feign日志级别配置
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    /**
     * Feign超时配置
     */
    @Bean
    public Request.Options options() {
        // 连接超时2秒，读取超时2秒
        return new Request.Options(2000, 2000);
    }

    /**
     * Feign重试配置
     * 幂等接口支持重试，非幂等接口禁用重试
     */
    @Bean
    public Retryer retryer() {
        // 初始间隔100ms，最大间隔1000ms，最多重试3次
        return new Retryer.Default(100, 1000, 3);
    }
}
