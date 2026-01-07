package com.liveroom.mock.client;

import com.liveroom.mock.dto.external.AnchorVO;
import com.liveroom.mock.dto.external.LiveRoomVO;
import com.liveroom.mock.dto.request.CreateAnchorRequest;
import com.liveroom.mock.dto.request.DanmakuRequest;
import com.liveroom.mock.dto.request.ViewerEnterRequest;
import common.response.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 主播服务 Feign Client
 */
@FeignClient(
    name = "anchor-service",
    url = "${service.anchor.url}",
    fallbackFactory = AnchorServiceFallbackFactory.class
)
public interface AnchorServiceClient {

    /**
     * 创建主播
     */
    @PostMapping("/anchor/api/v1/anchors")
    BaseResponse<AnchorVO> createAnchor(@RequestBody CreateAnchorRequest request);

    /**
     * 查询主播信息
     */
    @GetMapping("/anchor/api/v1/anchors/{id}")
    BaseResponse<AnchorVO> getAnchor(@PathVariable("id") Long anchorId);

    /**
     * 查询主播的直播间
     */
    @GetMapping("/anchor/api/v1/live-rooms/anchor/{anchorId}")
    BaseResponse<LiveRoomVO> getLiveRoomByAnchorId(@PathVariable("anchorId") Long anchorId);

    /**
     * 开启直播
     */
    @PostMapping("/anchor/api/v1/live-rooms/{id}/start")
    BaseResponse<LiveRoomVO> startLiveRoom(@PathVariable("id") Long liveRoomId);

    /**
     * 结束直播
     */
    @PostMapping("/anchor/api/v1/live-rooms/{id}/end")
    BaseResponse<LiveRoomVO> endLiveRoom(@PathVariable("id") Long liveRoomId);

    /**
     * 观众进入直播间
     */
    @PostMapping("/anchor/api/v1/live-rooms/realtime/viewer-enter")
    BaseResponse<Void> viewerEnter(@RequestBody ViewerEnterRequest request);

    /**
     * 观众离开直播间
     */
    @PostMapping("/anchor/api/v1/live-rooms/realtime/viewer-leave")
    BaseResponse<Void> viewerLeave(@RequestBody ViewerEnterRequest request);

    /**
     * 发送弹幕
     */
    @PostMapping("/anchor/api/v1/live-rooms/realtime/danmaku")
    BaseResponse<Void> sendDanmaku(@RequestBody DanmakuRequest request);
}
