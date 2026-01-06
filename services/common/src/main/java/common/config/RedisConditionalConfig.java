package common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import common.logger.AppLogger;

/**
 * Redis 条件配置
 * 根据 spring.redis.enabled 开关决定是否启用 Redis
 * 
 * 使用方式：
 * 1. application.yml 中设置 spring.redis.enabled: true/false
 * 2. 如果 enabled=false，Redis 相关 Bean 不会被创建
 */
@Configuration
@ConditionalOnProperty(
        prefix = "spring.redis",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
@EnableRedisRepositories
public class RedisConditionalConfig {
    
    public RedisConditionalConfig() {
        AppLogger.logServiceInitialize("Redis Configuration");
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
