package com.liveroom.redisservice.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import common.logger.AppLogger;

import java.util.concurrent.TimeUnit;

/**
 * Redis 缓存服务
 * 提供统一的 Redis 缓存操作接口
 * 用于：
 * 1. 幂等性检查（分布式）
 * 2. 分布式锁
 * 3. 跨服务数据共享
 */
@Service
@Slf4j
@AllArgsConstructor
public class RedisCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 设置缓存
     *
     * @param key   缓存键
     * @param value 缓存值
     * @param ttl   过期时间（秒）
     * @return 是否成功
     */
    public boolean set(String key, Object value, long ttl) {
        try {
            if (ttl > 0) {
                redisTemplate.opsForValue().set(key, value, ttl, TimeUnit.SECONDS);
            } else {
                redisTemplate.opsForValue().set(key, value);
            }
            AppLogger.logCacheOperation("SET", key, "TTL: " + ttl + "s");
            return true;
        } catch (Exception e) {
            log.error("Failed to set cache for key: {}", key, e);
            return false;
        }
    }

    /**
     * 获取缓存
     *
     * @param key 缓存键
     * @return 缓存值，不存在返回 null
     */
    public Object get(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            AppLogger.logCacheOperation("GET", key, value != null ? "HIT" : "MISS");
            return value;
        } catch (Exception e) {
            log.error("Failed to get cache for key: {}", key, e);
            return null;
        }
    }

    /**
     * 检查键是否存在
     *
     * @param key 缓存键
     * @return 是否存在
     */
    public boolean exists(String key) {
        try {
            Boolean exists = redisTemplate.hasKey(key);
            return exists != null && exists;
        } catch (Exception e) {
            log.error("Failed to check existence for key: {}", key, e);
            return false;
        }
    }

    /**
     * 删除缓存
     *
     * @param key 缓存键
     * @return 是否成功
     */
    public boolean delete(String key) {
        try {
            Boolean deleted = redisTemplate.delete(key);
            AppLogger.logCacheOperation("DELETE", key, deleted ? "success" : "not found");
            return deleted;
        } catch (Exception e) {
            log.error("Failed to delete cache for key: {}", key, e);
            return false;
        }
    }

    /**
     * 批量删除缓存
     *
     * @param keys 缓存键集合
     * @return 删除的数量
     */
    public long deleteMultiple(java.util.Collection<String> keys) {
        try {
            Long deleted = redisTemplate.delete(keys);
            AppLogger.logCacheOperation("DELETE_MULTIPLE", "batch", "Count: " + deleted);
            return deleted != null ? deleted : 0;
        } catch (Exception e) {
            log.error("Failed to delete multiple caches", e);
            return 0;
        }
    }

    /**
     * 设置缓存过期时间
     *
     * @param key  缓存键
     * @param ttl  过期时间（秒）
     * @return 是否成功
     */
    public boolean expire(String key, long ttl) {
        try {
            Boolean expired = redisTemplate.expire(key, ttl, TimeUnit.SECONDS);
            return expired != null && expired;
        } catch (Exception e) {
            log.error("Failed to set expiration for key: {}", key, e);
            return false;
        }
    }

    /**
     * 获取缓存的剩余过期时间
     *
     * @param key 缓存键
     * @return 剩余秒数，-1 表示永不过期，-2 表示键不存在
     */
    public long getExpire(String key) {
        try {
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            return ttl != null ? ttl : -2;
        } catch (Exception e) {
            log.error("Failed to get expiration for key: {}", key, e);
            return -2;
        }
    }

    /**
     * 自增操作
     *
     * @param key   缓存键
     * @param delta 增加量
     * @return 增加后的值
     */
    public long increment(String key, long delta) {
        try {
            Long result = redisTemplate.opsForValue().increment(key, delta);
            return result != null ? result : 0;
        } catch (Exception e) {
            log.error("Failed to increment key: {}", key, e);
            return 0;
        }
    }

    /**
     * 自减操作
     *
     * @param key   缓存键
     * @param delta 减少量
     * @return 减少后的值
     */
    public long decrement(String key, long delta) {
        try {
            Long result = redisTemplate.opsForValue().decrement(key, delta);
            return result != null ? result : 0;
        } catch (Exception e) {
            log.error("Failed to decrement key: {}", key, e);
            return 0;
        }
    }

    /**
     * 清空整个 Redis 数据库
     * 慎用！仅用于测试环境
     */
    public boolean flushDb() {
        try {
            redisTemplate.getConnectionFactory().getConnection().flushDb();
            AppLogger.logCacheOperation("FLUSH_DB", "all", "database cleared");
            return true;
        } catch (Exception e) {
            log.error("Failed to flush database", e);
            return false;
        }
    }

    /**
     * 获取 Redis 连接状态
     *
     * @return 是否连接正常
     */
    public boolean isHealthy() {
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
            return true;
        } catch (Exception e) {
            log.error("Redis health check failed", e);
            return false;
        }
    }
}
