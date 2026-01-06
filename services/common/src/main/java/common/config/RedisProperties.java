package common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Redis 配置属性
 * 支持通过配置文件动态启用/禁用 Redis
 * 
 * 使用示例：
 * spring:
 *   redis:
 *     enabled: true
 *     host: localhost
 *     port: 6379
 */
@Component
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedisProperties {

    /**
     * 是否启用 Redis
     * 默认值: true
     * - true: 启用本地 Redis 缓存
     * - false: 禁用 Redis，只使用内存缓存或分布式锁服务
     */
    private Boolean enabled = true;

    /**
     * Redis 服务器地址
     * 默认: localhost
     */
    private String host = "localhost";

    /**
     * Redis 服务器端口
     * 默认: 6379
     */
    private Integer port = 6379;

    /**
     * Redis 数据库编号
     * 默认: 0
     * 不同服务应使用不同的数据库：
     * - anchor-service: 0
     * - audience-service: 1
     * - finance-service: 2
     * - data-analysis-service: 3
     */
    private Integer database = 0;

    /**
     * Redis 密码
     * 默认: 空（无密码）
     */
    private String password = "";

    /**
     * 连接超时（毫秒）
     * 默认: 2000ms
     */
    private Integer timeout = 2000;

    /**
     * 连接池配置
     */
    private Pool pool = new Pool();

    @Data
    public static class Pool {
        /**
         * 连接池最大连接数
         * 默认: 8
         */
        private Integer maxActive = 8;

        /**
         * 连接池最大空闲连接数
         * 默认: 8
         */
        private Integer maxIdle = 8;

        /**
         * 连接池最小空闲连接数
         * 默认: 0
         */
        private Integer minIdle = 0;

        /**
         * 最大等待时间（毫秒）
         * 默认: -1（无限等待）
         */
        private Long maxWait = -1L;
    }

    /**
     * 是否启用（安全方法）
     */
    public boolean isEnabled() {
        return enabled != null && enabled;
    }
}
