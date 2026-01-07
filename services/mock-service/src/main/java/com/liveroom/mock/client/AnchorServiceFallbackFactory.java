package com.liveroom.mock.client;

import com.liveroom.mock.dto.external.AnchorVO;
import com.liveroom.mock.dto.external.LiveRoomVO;
import com.liveroom.mock.dto.request.CreateAnchorRequest;
import com.liveroom.mock.dto.request.DanmakuRequest;
import com.liveroom.mock.dto.request.ViewerEnterRequest;
import common.constant.ErrorConstants;
import common.logger.AppLogger;
import common.response.BaseResponse;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * 主播服务降级工厂
 */
@Component
public class AnchorServiceFallbackFactory implements FallbackFactory<AnchorServiceClient> {

    @Override
    public AnchorServiceClient create(Throwable cause) {
        return new AnchorServiceClient() {
            @Override
            public BaseResponse<AnchorVO> createAnchor(CreateAnchorRequest request) {
                AppLogger.error("调用主播服务创建主播失败", cause);
                return BaseResponse.error(ErrorConstants.SERVICE_UNAVAILABLE, "主播服务不可用");
            }

            @Override
            public BaseResponse<AnchorVO> getAnchor(Long anchorId) {
                AppLogger.error("调用主播服务查询主播失败，anchorId: {}", cause, anchorId);
                return BaseResponse.error(ErrorConstants.SERVICE_UNAVAILABLE, "主播服务不可用");
            }

            @Override
            public BaseResponse<LiveRoomVO> getLiveRoomByAnchorId(Long anchorId) {
                AppLogger.error("调用主播服务查询直播间失败，anchorId: {}", cause, anchorId);
                return BaseResponse.error(ErrorConstants.SERVICE_UNAVAILABLE, "主播服务不可用");
            }

            @Override
            public BaseResponse<LiveRoomVO> startLiveRoom(Long liveRoomId) {
                AppLogger.error("调用主播服务开启直播失败，liveRoomId: {}", cause, liveRoomId);
                return BaseResponse.error(ErrorConstants.SERVICE_UNAVAILABLE, "主播服务不可用");
            }

            @Override
            public BaseResponse<LiveRoomVO> endLiveRoom(Long liveRoomId) {
                AppLogger.error("调用主播服务结束直播失败，liveRoomId: {}", cause, liveRoomId);
                return BaseResponse.error(ErrorConstants.SERVICE_UNAVAILABLE, "主播服务不可用");
            }

            @Override
            public BaseResponse<Void> viewerEnter(ViewerEnterRequest request) {
                AppLogger.warn("调用主播服务观众进入失败（降级处理）", cause);
                return BaseResponse.error(ErrorConstants.SERVICE_UNAVAILABLE, "主播服务不可用");
            }

            @Override
            public BaseResponse<Void> viewerLeave(ViewerEnterRequest request) {
                AppLogger.warn("调用主播服务观众离开失败（降级处理）", cause);
                return BaseResponse.error(ErrorConstants.SERVICE_UNAVAILABLE, "主播服务不可用");
            }

            @Override
            public BaseResponse<Void> sendDanmaku(DanmakuRequest request) {
                AppLogger.warn("调用主播服务发送弹幕失败（降级处理）", cause);
                return BaseResponse.error(ErrorConstants.SERVICE_UNAVAILABLE, "主播服务不可用");
            }
        };
    }
}
