package com.liveroom.audience.feign;

import common.response.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

/**
 * 主播服务Feign客户端
 * 用于调用anchor-service的实时数据接口
 * 
 * @author Team
 * @version 1.0.0
 */
@FeignClient(
    name = "anchor-service",
    path = "/anchor/api/v1",
    fallback = AnchorServiceClientFallback.class
)
public interface AnchorServiceClient {

    /**
     * 通知直播间有观众打赏
     */
    @PostMapping("/live-rooms/realtime/reward")
    BaseResponse<Void> notifyReward(
            @RequestParam("liveRoomId") Long liveRoomId,
            @RequestParam("audienceId") Long audienceId,
            @RequestParam("amount") BigDecimal amount);
}
