package com.liveroom.redisservice.config;

import io.lettuce.core.ClientOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import common.logger.AppLogger;

import java.time.Duration;

/**
 * Redis 连接池配置
 * 用于 Redis-Service 服务的中心化 Redis 连接
 */
@Configuration
public class RedisConfig {

    /**
     * 配置 Redis 连接工厂
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        AppLogger.logServiceInitialize("redis-connection-factory");
        
        // 配置 Lettuce 客户端选项
        ClientOptions clientOptions = ClientOptions.builder()
                .autoReconnect(true)
                .build();

        LettuceClientConfiguration lettuceClientConfig = LettuceClientConfiguration.builder()
                .clientOptions(clientOptions)
                .commandTimeout(Duration.ofSeconds(2))
                .build();

        LettuceConnectionFactory factory = new LettuceConnectionFactory(lettuceClientConfig);
        
        AppLogger.logServiceInitializeComplete("redis-connection-factory");
        return factory;
    }

    /**
     * 配置 RedisTemplate 并设置序列化器
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        AppLogger.logServiceInitialize("redisTemplate");
        
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        try {
            // 动态加载序列化器类（避免编译时依赖问题）
            Class<?> stringSerializerClass = Class.forName(
                    "org.springframework.data.redis.serialization.StringRedisSerializer");
            Class<?> jsonSerializerClass = Class.forName(
                    "org.springframework.data.redis.serialization.GenericJackson2JsonRedisSerializer");
            
            // 使用反射创建实例
            Object stringSerializer = stringSerializerClass.getDeclaredConstructor().newInstance();
            Object jsonSerializer = jsonSerializerClass.getDeclaredConstructor().newInstance();
            
            // 使用反射设置序列化器
            template.getClass().getMethod("setKeySerializer", Object.class).invoke(template, stringSerializer);
            template.getClass().getMethod("setValueSerializer", Object.class).invoke(template, jsonSerializer);
            template.getClass().getMethod("setHashKeySerializer", Object.class).invoke(template, stringSerializer);
            template.getClass().getMethod("setHashValueSerializer", Object.class).invoke(template, jsonSerializer);
            
            AppLogger.logConfiguration("redis.serialization", "GenericJackson2Json");
        } catch (Exception e) {
            // 如果序列化器加载失败，使用默认配置
            System.err.println("Failed to configure custom serializers: " + e.getMessage());
            e.printStackTrace();
        }

        template.afterPropertiesSet();
        AppLogger.logServiceInitializeComplete("redisTemplate");
        return template;
    }
}
