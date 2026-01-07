package com.liveroom.mock.client;

import common.response.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Redis 服务 Feign Client
 */
@FeignClient(
    name = "redis-service",
    url = "${service.redis.url}",
    fallbackFactory = RedisServiceFallbackFactory.class
)
public interface RedisServiceClient {

    /**
     * 幂等性检查（防重复提交）
     */
    @PostMapping("/redis/api/v1/lock/check-idempotency")
    BaseResponse<Boolean> checkIdempotency(
        @RequestParam("idempotentKey") String idempotentKey,
        @RequestParam("ttl") Long ttl
    );

    /**
     * 尝试获取分布式锁
     */
    @PostMapping("/redis/api/v1/lock/try-lock")
    BaseResponse<Boolean> tryLock(
        @RequestParam("lockKey") String lockKey,
        @RequestParam("lockValue") String lockValue,
        @RequestParam("lockTimeout") Long lockTimeout
    );

    /**
     * 释放分布式锁
     */
    @PostMapping("/redis/api/v1/lock/release-lock")
    BaseResponse<Boolean> releaseLock(
        @RequestParam("lockKey") String lockKey,
        @RequestParam("lockValue") String lockValue
    );
}
