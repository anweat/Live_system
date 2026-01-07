package com.liveroom.mock.client;

import com.liveroom.mock.dto.external.AudienceVO;
import com.liveroom.mock.dto.external.RechargeVO;
import com.liveroom.mock.dto.request.CreateAudienceRequest;
import com.liveroom.mock.dto.request.RechargeRequest;
import common.constant.ErrorConstants;
import common.logger.AppLogger;
import common.response.BaseResponse;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * 观众服务降级工厂
 */
@Component
public class AudienceServiceFallbackFactory implements FallbackFactory<AudienceServiceClient> {

    @Override
    public AudienceServiceClient create(Throwable cause) {
        return new AudienceServiceClient() {
            @Override
            public BaseResponse<AudienceVO> createAudience(CreateAudienceRequest request) {
                AppLogger.error("调用观众服务创建观众失败", cause);
                return BaseResponse.error(ErrorConstants.SERVICE_UNAVAILABLE, "观众服务不可用");
            }

            @Override
            public BaseResponse<AudienceVO> getAudience(Long audienceId) {
                AppLogger.error("调用观众服务查询观众失败，audienceId: {}", cause, audienceId);
                return BaseResponse.error(ErrorConstants.SERVICE_UNAVAILABLE, "观众服务不可用");
            }

            @Override
            public BaseResponse<RechargeVO> recharge(RechargeRequest request) {
                AppLogger.error("调用观众服务打赏失败", cause);
                return BaseResponse.error(ErrorConstants.SERVICE_UNAVAILABLE, "观众服务不可用");
            }

            @Override
            public BaseResponse<RechargeVO> getRechargeByTraceId(String traceId) {
                AppLogger.error("调用观众服务查询打赏失败，traceId: {}", cause, traceId);
                return BaseResponse.error(ErrorConstants.SERVICE_UNAVAILABLE, "观众服务不可用");
            }
        };
    }
}
