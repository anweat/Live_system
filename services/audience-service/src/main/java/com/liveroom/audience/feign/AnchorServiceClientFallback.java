package com.liveroom.audience.feign;

import common.logger.TraceLogger;
import common.response.BaseResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 主播服务Feign客户端降级处理
 * 
 * @author Team
 * @version 1.0.0
 */
@Component
public class AnchorServiceClientFallback implements AnchorServiceClient {

    @Override
    public BaseResponse<Void> notifyReward(Long liveRoomId, Long audienceId, BigDecimal amount) {
        TraceLogger.warn("AnchorServiceClientFallback", "notifyReward",
                String.format("主播服务调用失败，打赏通知未送达: liveRoomId=%d, audienceId=%d, amount=%s",
                        liveRoomId, audienceId, amount));
        
        // 降级处理：记录日志但不抛出异常，避免影响打赏记录的保存
        BaseResponse<Void> response = new BaseResponse<>();
        response.setCode(500);
        response.setMessage("主播服务暂时不可用，打赏已记录但未实时更新直播间数据");
        return response;
    }
}
