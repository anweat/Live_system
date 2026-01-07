package com.liveroom.redisservice.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.liveroom.redisservice.service.RedisDistributedLockService;
import common.response.BaseResponse;

/**
 * Redis 分布式锁 REST API 控制器
 * 提供 HTTP 接口供其他微服务调用分布式锁功能
 */
@RestController
@RequestMapping("/redis/api/v1/lock")
@AllArgsConstructor
public class RedisDistributedLockController {

    private final RedisDistributedLockService lockService;

    /**
     * 尝试获取分布式锁
     *
     * @param lockKey     锁键
     * @param lockValue   锁值（用于标识持有者）
     * @param lockTimeout 锁超时时间（毫秒）
     * @return 是否获取成功
     */
    @PostMapping("/try-lock")
    public BaseResponse<Boolean> tryLock(
            @RequestParam String lockKey,
            @RequestParam String lockValue,
            @RequestParam(defaultValue = "30000") long lockTimeout) {
        
        boolean success = lockService.tryLock(lockKey, lockValue, lockTimeout);
        return BaseResponse.success(success ? "Lock acquired" : "Lock already held", success);
    }

    /**
     * 释放分布式锁
     *
     * @param lockKey   锁键
     * @param lockValue 锁值（必须匹配）
     * @return 是否释放成功
     */
    @PostMapping("/release-lock")
    public BaseResponse<Boolean> releaseLock(
            @RequestParam String lockKey,
            @RequestParam String lockValue) {
        
        boolean success = lockService.releaseLock(lockKey, lockValue);
        return BaseResponse.success(success ? "Lock released" : "Lock release failed", success);
    }

    /**
     * 强制释放锁（不验证 lockValue）
     * 慎用！仅在必要时使用
     *
     * @param lockKey 锁键
     * @return 是否释放成功
     */
    @PostMapping("/force-release-lock")
    public BaseResponse<Boolean> forceReleaseLock(@RequestParam String lockKey) {
        boolean success = lockService.forceReleaseLock(lockKey);
        return BaseResponse.success(success);
    }

    /**
     * 检查锁是否被持有
     *
     * @param lockKey 锁键
     * @return 是否被持有
     */
    @GetMapping("/is-locked")
    public BaseResponse<Boolean> isLocked(@RequestParam String lockKey) {
        boolean locked = lockService.isLocked(lockKey);
        return BaseResponse.success(locked);
    }

    /**
     * 获取锁值
     *
     * @param lockKey 锁键
     * @return 锁值
     */
    @GetMapping("/get-lock-value")
    public BaseResponse<String> getLockValue(@RequestParam String lockKey) {
        String lockValue = lockService.getLockValue(lockKey);
        return BaseResponse.success(lockValue);
    }

    /**
     * 检查幂等性（防重复提交）
     *
     * @param idempotentKey 幂等性键（通常是 traceId）
     * @param ttl          过期时间（秒）
     * @return true 表示首次请求，false 表示重复请求
     */
    @PostMapping("/check-idempotency")
    public BaseResponse<Boolean> checkIdempotency(
            @RequestParam String idempotentKey,
            @RequestParam(defaultValue = "3600") long ttl) {
        
        boolean isFirstRequest = lockService.checkIdempotency(idempotentKey, ttl);
        return BaseResponse.success(
                isFirstRequest ? "First request, proceed" : "Duplicate request, reject", isFirstRequest);
    }
}
