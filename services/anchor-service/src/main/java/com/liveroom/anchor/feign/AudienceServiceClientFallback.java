package com.liveroom.anchor.feign;

import com.liveroom.anchor.vo.RechargeVO;
import common.logger.TraceLogger;
import common.response.BaseResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 观众服务Feign客户端降级处理
 * 
 * @author Team
 * @version 1.0.0
 */
@Component
public class AudienceServiceClientFallback implements AudienceServiceClient {

    @Override
    public BaseResponse<Object> getRechargesByAnchor(
            Long anchorId, LocalDateTime startTime, LocalDateTime endTime,
            Integer page, Integer size) {

        TraceLogger.warn("AudienceServiceClientFallback", "getRechargesByAnchor",
                "观众服务调用失败，返回降级响应: anchorId=" + anchorId);

        BaseResponse<Object> response = new BaseResponse<>();
        response.setCode(500);
        response.setMessage("观众服务暂时不可用，请稍后重试");
        return response;
    }

    @Override
    public BaseResponse<Object> getRechargesByLiveRoom(
            Long liveRoomId, LocalDateTime startTime, LocalDateTime endTime,
            Integer page, Integer size) {

        TraceLogger.warn("AudienceServiceClientFallback", "getRechargesByLiveRoom",
                "观众服务调用失败，返回降级响应: liveRoomId=" + liveRoomId);

        BaseResponse<Object> response = new BaseResponse<>();
        response.setCode(500);
        response.setMessage("观众服务暂时不可用，请稍后重试");
        return response;
    }

    @Override
    public RechargeVO getRechargeByTraceId(String traceId) {
        TraceLogger.warn("AudienceServiceClientFallback", "getRechargeByTraceId",
                "观众服务调用失败，返回null: traceId=" + traceId);
        return null;
    }

    @Override
    public Object getTotalRechargeByAnchor(
            Long anchorId, LocalDateTime startTime, LocalDateTime endTime) {

        TraceLogger.warn("AudienceServiceClientFallback", "getTotalRechargeByAnchor",
                "观众服务调用失败: anchorId=" + anchorId);
        return null;
    }

    @Override
    public List<RechargeVO.Top10AudienceVO> getTop10Audiences(
            Long anchorId, LocalDateTime startTime, LocalDateTime endTime) {

        TraceLogger.warn("AudienceServiceClientFallback", "getTop10Audiences",
                "观众服务调用失败，返回空列表: anchorId=" + anchorId);
        return new ArrayList<>();
    }
}
