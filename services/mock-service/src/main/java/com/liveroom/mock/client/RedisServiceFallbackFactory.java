package com.liveroom.mock.client;

import common.constant.ErrorConstants;
import common.logger.AppLogger;
import common.response.BaseResponse;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * Redis 服务降级工厂
 */
@Component
public class RedisServiceFallbackFactory implements FallbackFactory<RedisServiceClient> {

    @Override
    public RedisServiceClient create(Throwable cause) {
        return new RedisServiceClient() {
            @Override
            public BaseResponse<Boolean> checkIdempotency(String idempotentKey, Long ttl) {
                AppLogger.warn("调用Redis服务幂等性检查失败（降级处理），返回false", cause);
                // 降级返回 false，表示不是首次请求，防止重复操作
                return BaseResponse.success("Redis服务不可用（降级）", false);
            }

            @Override
            public BaseResponse<Boolean> tryLock(String lockKey, String lockValue, Long lockTimeout) {
                AppLogger.warn("调用Redis服务获取锁失败（降级处理），返回false", cause);
                return BaseResponse.success("Redis服务不可用（降级）", false);
            }

            @Override
            public BaseResponse<Boolean> releaseLock(String lockKey, String lockValue) {
                AppLogger.warn("调用Redis服务释放锁失败（降级处理），返回false", cause);
                return BaseResponse.success("Redis服务不可用（降级）", false);
            }
        };
    }
}
