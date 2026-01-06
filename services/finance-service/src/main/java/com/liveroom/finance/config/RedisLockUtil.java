package com.liveroom.finance.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis分布式锁工具类
 * 用于保证定时任务单节点执行、提现操作互斥等场景
 */
@Component
public class RedisLockUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 尝试获取锁
     *
     * @param key     锁的键
     * @param timeout 锁超时时间（秒）
     * @return 是否成功获取锁
     */
    public boolean tryLock(String key, long timeout) {
        try {
            String lockKey = "lock:" + key;
            String lockValue = String.valueOf(System.currentTimeMillis() + timeout * 1000);
            
            Boolean result = redisTemplate.opsForValue()
                    .setIfAbsent(lockKey, lockValue, timeout, TimeUnit.SECONDS);
            
            return result != null && result;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 释放锁
     *
     * @param key 锁的键
     */
    public void unlock(String key) {
        try {
            String lockKey = "lock:" + key;
            redisTemplate.delete(lockKey);
        } catch (Exception e) {
            // 忽略异常，锁会自动过期
        }
    }

    /**
     * 检查锁是否存在
     *
     * @param key 锁的键
     * @return 锁是否存在
     */
    public boolean isLocked(String key) {
        try {
            String lockKey = "lock:" + key;
            return Boolean.TRUE.equals(redisTemplate.hasKey(lockKey));
        } catch (Exception e) {
            return false;
        }
    }
}
