package com.liveroom.redisservice.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.liveroom.redisservice.service.RedisCacheService;
import com.liveroom.redisservice.service.RedisDistributedLockService;
import common.response.BaseResponse;
import common.logger.AppLogger;

/**
 * Redis 缓存 REST API 控制器
 * 提供 HTTP 接口供其他微服务调用 Redis 缓存功能
 */
@RestController
@RequestMapping("/redis/api/v1/cache")
@AllArgsConstructor
public class RedisCacheController {

    private final RedisCacheService cacheService;

    /**
     * 设置缓存
     *
     * @param key   缓存键
     * @param value 缓存值
     * @param ttl   过期时间（秒），0 表示永不过期
     * @return 操作结果
     */
    @PostMapping("/set")
    public BaseResponse<Boolean> set(
            @RequestParam String key,
            @RequestParam String value,
            @RequestParam(defaultValue = "0") long ttl) {
        
        boolean success = cacheService.set(key, value, ttl);
        return BaseResponse.success(success, "Cache set " + (success ? "success" : "failed"));
    }

    /**
     * 获取缓存
     *
     * @param key 缓存键
     * @return 缓存值
     */
    @GetMapping("/get")
    public BaseResponse<Object> get(@RequestParam String key) {
        Object value = cacheService.get(key);
        return BaseResponse.success(value, value != null ? "Cache hit" : "Cache miss");
    }

    /**
     * 检查键是否存在
     *
     * @param key 缓存键
     * @return 是否存在
     */
    @GetMapping("/exists")
    public BaseResponse<Boolean> exists(@RequestParam String key) {
        boolean exists = cacheService.exists(key);
        return BaseResponse.success(exists);
    }

    /**
     * 删除缓存
     *
     * @param key 缓存键
     * @return 操作结果
     */
    @DeleteMapping("/delete")
    public BaseResponse<Boolean> delete(@RequestParam String key) {
        boolean success = cacheService.delete(key);
        return BaseResponse.success(success);
    }

    /**
     * 设置缓存过期时间
     *
     * @param key 缓存键
     * @param ttl 过期时间（秒）
     * @return 操作结果
     */
    @PostMapping("/expire")
    public BaseResponse<Boolean> expire(
            @RequestParam String key,
            @RequestParam long ttl) {
        
        boolean success = cacheService.expire(key, ttl);
        return BaseResponse.success(success);
    }

    /**
     * 获取缓存的剩余过期时间
     *
     * @param key 缓存键
     * @return 剩余秒数
     */
    @GetMapping("/ttl")
    public BaseResponse<Long> getTTL(@RequestParam String key) {
        long ttl = cacheService.getExpire(key);
        return BaseResponse.success(ttl);
    }

    /**
     * 自增操作
     *
     * @param key   缓存键
     * @param delta 增加量（默认为 1）
     * @return 增加后的值
     */
    @PostMapping("/increment")
    public BaseResponse<Long> increment(
            @RequestParam String key,
            @RequestParam(defaultValue = "1") long delta) {
        
        long result = cacheService.increment(key, delta);
        return BaseResponse.success(result);
    }

    /**
     * 自减操作
     *
     * @param key   缓存键
     * @param delta 减少量（默认为 1）
     * @return 减少后的值
     */
    @PostMapping("/decrement")
    public BaseResponse<Long> decrement(
            @RequestParam String key,
            @RequestParam(defaultValue = "1") long delta) {
        
        long result = cacheService.decrement(key, delta);
        return BaseResponse.success(result);
    }

    /**
     * Redis 健康检查
     *
     * @return 连接状态
     */
    @GetMapping("/health")
    public BaseResponse<Boolean> health() {
        boolean healthy = cacheService.isHealthy();
        return BaseResponse.success(healthy, healthy ? "Redis is healthy" : "Redis is unavailable");
    }
}
