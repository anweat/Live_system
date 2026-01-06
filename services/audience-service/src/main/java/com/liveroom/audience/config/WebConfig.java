package com.liveroom.audience.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Web配置类
 * 提供Web相关的Bean配置
 */
@Configuration
public class WebConfig {

    /**
     * RestTemplate Bean
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
