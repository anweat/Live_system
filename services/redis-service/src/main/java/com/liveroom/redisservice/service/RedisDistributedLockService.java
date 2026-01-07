package com.liveroom.redisservice.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import common.logger.AppLogger;

import java.time.Duration;
import java.util.Collections;

/**
 * 分布式锁服务
 * 使用 Redis 实现分布式锁，支持：
 * 1. 幂等性检查（防重复提交）
 * 2. 定时任务的单一执行
 * 3. 分布式事务保护
 */
@Service
@Slf4j
@AllArgsConstructor
public class RedisDistributedLockService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 尝试获取分布式锁
     *
     * @param lockKey      锁键
     * @param lockValue    锁值（用于标识持有者）
     * @param lockTimeout  锁超时时间（毫秒）
     * @return 是否获取成功
     */
    public boolean tryLock(String lockKey, String lockValue, long lockTimeout) {
        try {
            Boolean success = redisTemplate.opsForValue()
                    .setIfAbsent(lockKey, lockValue, Duration.ofMillis(lockTimeout));
            
            if (Boolean.TRUE.equals(success)) {
                AppLogger.logCacheOperation("LOCK_ACQUIRED", lockKey, lockValue);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Failed to acquire lock for key: {}", lockKey, e);
            return false;
        }
    }

    /**
     * 释放分布式锁（使用 Lua 脚本保证原子性）
     *
     * @param lockKey   锁键
     * @param lockValue 锁值（必须匹配才能释放）
     * @return 是否释放成功
     */
    public boolean releaseLock(String lockKey, String lockValue) {
        try {
            // Lua脚本：先比较值，再删除，保证原子性
            String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "return redis.call('del', KEYS[1]) " +
                    "else " +
                    "return 0 " +
                    "end";
            
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptText(luaScript);
            redisScript.setResultType(Long.class);
            
            Long result = redisTemplate.execute(
                    redisScript,
                    Collections.singletonList(lockKey),
                    lockValue
            );
            
            boolean success = result != null && result > 0;
            if (success) {
                AppLogger.logCacheOperation("LOCK_RELEASED", lockKey, lockValue);
            }
            return success;
        } catch (Exception e) {
            // 降级处理：直接删除
            log.warn("Failed to release lock with Lua script for key: {}, fallback to direct delete", lockKey);
            Object currentValue = redisTemplate.opsForValue().get(lockKey);
            if (lockValue.equals(currentValue)) {
                redisTemplate.delete(lockKey);
                return true;
            }
            return false;
        }
    }

    /**
     * 检查幂等性（防重复提交）
     * 用于防止重复的请求被处理多次
     *
     * @param idempotentKey 幂等性键（通常是 traceId）
     * @param ttl          过期时间（秒）
     * @return true 表示这是首次请求，false 表示重复请求
     */
    public boolean checkIdempotency(String idempotentKey, long ttl) {
        try {
            Boolean success = redisTemplate.opsForValue()
                    .setIfAbsent(idempotentKey, System.currentTimeMillis(), Duration.ofSeconds(ttl));
            
            if (Boolean.TRUE.equals(success)) {
                AppLogger.logCacheOperation("IDEMPOTENCY_CHECK_PASS", idempotentKey, "First request");
                return true;
            } else {
                AppLogger.logCacheOperation("IDEMPOTENCY_CHECK_FAIL", idempotentKey, "Duplicate request");
                return false;
            }
        } catch (Exception e) {
            log.error("Failed to check idempotency for key: {}", idempotentKey, e);
            // 发生异常时，为了安全起见，返回 false（不处理）
            return false;
        }
    }

    /**
     * 获取当前持有锁的值
     *
     * @param lockKey 锁键
     * @return 锁值，不存在返回 null
     */
    public String getLockValue(String lockKey) {
        try {
            Object value = redisTemplate.opsForValue().get(lockKey);
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            log.error("Failed to get lock value for key: {}", lockKey, e);
            return null;
        }
    }

    /**
     * 检查锁是否被持有
     *
     * @param lockKey 锁键
     * @return 是否被持有
     */
    public boolean isLocked(String lockKey) {
        try {
            return redisTemplate.hasKey(lockKey);
        } catch (Exception e) {
            log.error("Failed to check lock for key: {}", lockKey, e);
            return false;
        }
    }

    /**
     * 强制释放锁（不验证 lockValue）
     * 慎用！仅在必要时使用
     *
     * @param lockKey 锁键
     * @return 是否释放成功
     */
    public boolean forceReleaseLock(String lockKey) {
        try {
            Boolean deleted = redisTemplate.delete(lockKey);
            if (deleted) {
                AppLogger.logCacheOperation("LOCK_FORCE_RELEASED", lockKey, "forced");
            }
            return deleted;
        } catch (Exception e) {
            log.error("Failed to force release lock for key: {}", lockKey, e);
            return false;
        }
    }
}
