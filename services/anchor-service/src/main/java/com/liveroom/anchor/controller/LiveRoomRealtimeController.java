package com.liveroom.anchor.controller;

import com.liveroom.anchor.service.LiveRoomRealtimeService;
import com.liveroom.anchor.vo.LiveRoomRealtimeVO;
import common.annotation.Log;
import common.exception.ValidationException;
import common.logger.TraceLogger;
import common.response.BaseResponse;
import common.response.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 直播间实时数据Controller
 * 处理观众进入/离开、弹幕、打赏等实时消息
 * 
 * @author Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/live-rooms/realtime")
@Slf4j
@Validated
public class LiveRoomRealtimeController {

    @Autowired
    private LiveRoomRealtimeService liveRoomRealtimeService;

    /**
     * 观众进入直播间
     * POST /api/v1/live-rooms/realtime/viewer-enter
     */
    @PostMapping("/viewer-enter")
    @Log("观众进入直播间")
    public BaseResponse<String> viewerEnter(
            @RequestParam @NotNull(message = "直播间ID不能为空") Long liveRoomId,
            @RequestParam @NotNull(message = "观众ID不能为空") Long audienceId) {

        TraceLogger.info("LiveRoomRealtimeController", "viewerEnter",
                String.format("观众进入直播间: liveRoomId=%d, audienceId=%d", liveRoomId, audienceId));

        liveRoomRealtimeService.viewerEnter(liveRoomId, audienceId);

        return ResponseUtil.success("观众进入成功");
    }

    /**
     * 观众离开直播间
     * POST /api/v1/live-rooms/realtime/viewer-leave
     */
    @PostMapping("/viewer-leave")
    @Log("观众离开直播间")
    public BaseResponse<String> viewerLeave(
            @RequestParam @NotNull(message = "直播间ID不能为空") Long liveRoomId,
            @RequestParam @NotNull(message = "观众ID不能为空") Long audienceId) {

        TraceLogger.info("LiveRoomRealtimeController", "viewerLeave",
                String.format("观众离开直播间: liveRoomId=%d, audienceId=%d", liveRoomId, audienceId));

        liveRoomRealtimeService.viewerLeave(liveRoomId, audienceId);

        return ResponseUtil.success("观众离开成功");
    }

    /**
     * 观众发送弹幕
     * POST /api/v1/live-rooms/realtime/danmaku
     */
    @PostMapping("/danmaku")
    @Log("观众发送弹幕")
    public BaseResponse<String> viewerDanmaku(
            @RequestParam @NotNull(message = "直播间ID不能为空") Long liveRoomId,
            @RequestParam @NotNull(message = "观众ID不能为空") Long audienceId,
            @RequestParam @NotBlank(message = "弹幕内容不能为空") String content) {

        // 验证弹幕长度
        if (content.length() > 500) {
            throw new ValidationException("弹幕内容不能超过500个字符");
        }

        TraceLogger.debug("LiveRoomRealtimeController", "viewerDanmaku",
                String.format("观众发送弹幕: liveRoomId=%d, audienceId=%d", liveRoomId, audienceId));

        liveRoomRealtimeService.viewerDanmaku(liveRoomId, audienceId, content);

        return ResponseUtil.success("弹幕发送成功");
    }

    /**
     * 观众打赏（只更新直播间数据，主播数据查询财务服务）
     * POST /api/v1/live-rooms/realtime/reward
     */
    @PostMapping("/reward")
    @Log("观众打赏")
    public BaseResponse<String> viewerReward(
            @RequestParam @NotNull(message = "直播间ID不能为空") Long liveRoomId,
            @RequestParam @NotNull(message = "观众ID不能为空") Long audienceId,
            @RequestParam @NotNull(message = "打赏金额不能为空")
            @DecimalMin(value = "0.01", message = "打赏金额最少为0.01元")
            BigDecimal amount) {

        // 验证打赏金额上限
        if (amount.compareTo(new BigDecimal("99999.99")) > 0) {
            throw new ValidationException("单次打赏金额不能超过99999.99元");
        }

        TraceLogger.info("LiveRoomRealtimeController", "viewerReward",
                String.format("观众打赏: liveRoomId=%d, audienceId=%d, amount=%s",
                        liveRoomId, audienceId, amount));

        liveRoomRealtimeService.viewerReward(liveRoomId, audienceId, amount);

        return ResponseUtil.success("打赏成功");
    }

    /**
     * 获取直播间实时数据
     * GET /api/v1/live-rooms/realtime/{liveRoomId}
     */
    @GetMapping("/{liveRoomId}")
    @Log("查询直播间实时数据")
    public BaseResponse<LiveRoomRealtimeVO> getLiveRoomRealtimeData(
            @PathVariable Long liveRoomId) {

        if (liveRoomId == null || liveRoomId <= 0) {
            throw new ValidationException("直播间ID不合法");
        }

        TraceLogger.debug("LiveRoomRealtimeController", "getLiveRoomRealtimeData",
                "查询直播间实时数据: liveRoomId=" + liveRoomId);

        LiveRoomRealtimeVO vo = liveRoomRealtimeService.getLiveRoomRealtimeData(liveRoomId);

        return ResponseUtil.success(vo);
    }
}
