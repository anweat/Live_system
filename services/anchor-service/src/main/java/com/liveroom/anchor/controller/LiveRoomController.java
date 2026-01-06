package com.liveroom.anchor.controller;

import com.liveroom.anchor.dto.LiveRoomDTO;
import com.liveroom.anchor.service.LiveRoomService;
import common.annotation.Log;
import common.response.BaseResponse;
import common.exception.ValidationException;
import common.logger.TraceLogger;
import common.util.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.math.BigDecimal;

/**
 * 直播间管理Controller
 * 提供直播间生命周期管理的REST API接口
 * 
 * @author Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/live-rooms")
@Slf4j
@Validated
public class LiveRoomController {

    @Autowired
    private LiveRoomService liveRoomService;

    /**
     * 查询直播间信息
     * GET /api/v1/live-rooms/{liveRoomId}
     */
    @GetMapping("/{liveRoomId}")
    @Log("查询直播间信息")
    public BaseResponse<LiveRoomDTO> getLiveRoom(@PathVariable Long liveRoomId) {
        if (liveRoomId == null || liveRoomId <= 0) {
            throw new ValidationException("直播间ID不合法");
        }

        LiveRoomDTO liveRoom = liveRoomService.getLiveRoom(liveRoomId);
        return ResponseUtil.success(liveRoom);
    }

    /**
     * 根据主播ID查询直播间
     * GET /api/v1/live-rooms/anchor/{anchorId}
     */
    @GetMapping("/anchor/{anchorId}")
    @Log("根据主播ID查询直播间")
    public BaseResponse<LiveRoomDTO> getLiveRoomByAnchorId(@PathVariable Long anchorId) {
        if (anchorId == null || anchorId <= 0) {
            throw new ValidationException("主播ID不合法");
        }

        LiveRoomDTO liveRoom = liveRoomService.getLiveRoomByAnchorId(anchorId);
        return ResponseUtil.success(liveRoom);
    }

    /**
     * 开启直播
     * POST /api/v1/live-rooms/{liveRoomId}/start
     */
    @PostMapping("/{liveRoomId}/start")
    @Log("开启直播")
    public BaseResponse<LiveRoomDTO> startLive(
            @PathVariable Long liveRoomId,
            @RequestParam String streamUrl,
            @RequestParam(required = false) String coverUrl) {

        if (liveRoomId == null || liveRoomId <= 0) {
            throw new ValidationException("直播间ID不合法");
        }

        if (streamUrl == null || streamUrl.trim().isEmpty()) {
            throw new ValidationException("直播流URL不能为空");
        }

        TraceLogger.info("LiveRoomController", "startLive", 
            "开启直播请求: liveRoomId=" + liveRoomId);

        LiveRoomDTO result = liveRoomService.startLive(liveRoomId, streamUrl, coverUrl);
        return ResponseUtil.success(result);
    }

    /**
     * 结束直播
     * POST /api/v1/live-rooms/{liveRoomId}/end
     */
    @PostMapping("/{liveRoomId}/end")
    @Log("结束直播")
    public BaseResponse<LiveRoomDTO> endLive(@PathVariable Long liveRoomId) {
        if (liveRoomId == null || liveRoomId <= 0) {
            throw new ValidationException("直播间ID不合法");
        }

        TraceLogger.info("LiveRoomController", "endLive", 
            "结束直播请求: liveRoomId=" + liveRoomId);

        LiveRoomDTO result = liveRoomService.endLive(liveRoomId);
        return ResponseUtil.success(result);
    }

    /**
     * 更新直播间实时数据
     * PATCH /api/v1/live-rooms/{liveRoomId}/realtime
     */
    @PatchMapping("/{liveRoomId}/realtime")
    @Log("更新直播间实时数据")
    public BaseResponse<Void> updateRealtimeData(
            @PathVariable Long liveRoomId,
            @RequestParam(required = false) Long viewersDelta,
            @RequestParam(required = false) BigDecimal earningsDelta) {

        if (liveRoomId == null || liveRoomId <= 0) {
            throw new ValidationException("直播间ID不合法");
        }

        liveRoomService.updateRealtimeData(liveRoomId, viewersDelta, earningsDelta);

        return ResponseUtil.success("实时数据更新成功");
    }

    /**
     * 查询所有正在直播的直播间
     * GET /api/v1/live-rooms/live?page=1&size=20
     */
    @GetMapping("/live")
    @Log("查询正在直播的直播间")
    public BaseResponse<java.util.List<LiveRoomDTO>> listLiveRooms(
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) Integer size) {

        java.util.List<LiveRoomDTO> liveRooms = liveRoomService.listLiveRooms(page, size);

        return ResponseUtil.success(liveRooms);
    }

    /**
     * 按分类查询直播间列表
     * GET /api/v1/live-rooms/category/{category}?page=1&size=20
     */
    @GetMapping("/category/{category}")
    @Log("按分类查询直播间列表")
    public BaseResponse<java.util.List<LiveRoomDTO>> listLiveRoomsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) Integer size) {

        if (category == null || category.trim().isEmpty()) {
            throw new ValidationException("分类参数不能为空");
        }

        java.util.List<LiveRoomDTO> liveRooms = liveRoomService.listLiveRoomsByCategory(category, page, size);

        return ResponseUtil.success(liveRooms);
    }

    /**
     * 更新直播间信息
     * PUT /api/v1/live-rooms/{liveRoomId}
     */
    @PutMapping("/{liveRoomId}")
    @Log("更新直播间信息")
    public BaseResponse<LiveRoomDTO> updateLiveRoom(
            @PathVariable Long liveRoomId,
            @Valid @RequestBody LiveRoomDTO liveRoomDTO) {

        if (liveRoomId == null || liveRoomId <= 0) {
            throw new ValidationException("直播间ID不合法");
        }

        TraceLogger.info("LiveRoomController", "updateLiveRoom", 
            "更新直播间信息: liveRoomId=" + liveRoomId);

        LiveRoomDTO result = liveRoomService.updateLiveRoom(liveRoomId, liveRoomDTO);
        return ResponseUtil.success(result);
    }
}
