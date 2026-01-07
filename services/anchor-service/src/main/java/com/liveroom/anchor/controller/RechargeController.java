package com.liveroom.anchor.controller;

import com.liveroom.anchor.service.RechargeService;
import com.liveroom.anchor.vo.RechargeVO;
import common.annotation.Log;
import common.bean.ApiResponse;
import common.exception.ValidationException;
import common.logger.TraceLogger;
import common.response.BaseResponse;
import common.response.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 打赏记录查询Controller
 * 提供主播端查询打赏记录的接口
 * 
 * @author Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/recharge")
@Slf4j
@Validated
public class RechargeController {

    @Autowired
    private RechargeService rechargeService;

    /**
     * 查询主播的打赏记录（分页）
     * GET /api/v1/recharge/anchor/{anchorId}
     */
    @GetMapping("/anchor/{anchorId}")
    @Log("查询主播打赏记录")
    public BaseResponse<Object> getRechargesByAnchor(
            @PathVariable Long anchorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) Integer size) {

        if (anchorId == null || anchorId <= 0) {
            throw new ValidationException("主播ID不合法");
        }

        TraceLogger.debug("RechargeController", "getRechargesByAnchor",
                String.format("查询主播打赏记录: anchorId=%d, startTime=%s, endTime=%s",
                        anchorId, startTime, endTime));

        BaseResponse<Object> response = rechargeService.getRechargesByAnchor(
                anchorId, startTime, endTime, page, size);

        return response;
    }

    /**
     * 查询直播间的打赏记录（分页）
     * GET /api/v1/recharge/live-room/{liveRoomId}
     */
    @GetMapping("/live-room/{liveRoomId}")
    @Log("查询直播间打赏记录")
    public BaseResponse<Object> getRechargesByLiveRoom(
            @PathVariable Long liveRoomId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) Integer size) {

        if (liveRoomId == null || liveRoomId <= 0) {
            throw new ValidationException("直播间ID不合法");
        }

        TraceLogger.debug("RechargeController", "getRechargesByLiveRoom",
                String.format("查询直播间打赏记录: liveRoomId=%d", liveRoomId));

        BaseResponse<Object> response = rechargeService.getRechargesByLiveRoom(
                liveRoomId, startTime, endTime, page, size);

        return response;
    }

    /**
     * 按traceId查询打赏记录
     * GET /api/v1/recharge/trace/{traceId}
     */
    @GetMapping("/trace/{traceId}")
    @Log("按traceId查询打赏记录")
    public BaseResponse<RechargeVO> getRechargeByTraceId(@PathVariable String traceId) {

        if (traceId == null || traceId.trim().isEmpty()) {
            throw new ValidationException("traceId不能为空");
        }

        TraceLogger.debug("RechargeController", "getRechargeByTraceId",
                "查询打赏记录: traceId=" + traceId);

        RechargeVO result = rechargeService.getRechargeByTraceId(traceId);

        return ResponseUtil.success(result);
    }

    /**
     * 统计主播的打赏总额
     * GET /api/v1/recharge/anchor/{anchorId}/total
     */
    @GetMapping("/anchor/{anchorId}/total")
    @Log("统计主播打赏总额")
    public BaseResponse<Object> getTotalRechargeByAnchor(
            @PathVariable Long anchorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        if (anchorId == null || anchorId <= 0) {
            throw new ValidationException("主播ID不合法");
        }

        TraceLogger.debug("RechargeController", "getTotalRechargeByAnchor",
                String.format("统计主播打赏总额: anchorId=%d", anchorId));

        Object result = rechargeService.getTotalRechargeByAnchor(anchorId, startTime, endTime);

        return ResponseUtil.success(result);
    }

    /**
     * 查询TOP10打赏观众
     * GET /api/v1/recharge/anchor/{anchorId}/top10
     */
    @GetMapping("/anchor/{anchorId}/top10")
    @Log("查询TOP10打赏观众")
    public BaseResponse<List<RechargeVO.Top10AudienceVO>> getTop10Audiences(
            @PathVariable Long anchorId,
            @RequestParam(defaultValue = "all") String period) {

        if (anchorId == null || anchorId <= 0) {
            throw new ValidationException("主播ID不合法");
        }

        // 验证period参数
        if (!period.matches("day|week|month|all")) {
            throw new ValidationException("period参数只能是: day, week, month, all");
        }

        TraceLogger.debug("RechargeController", "getTop10Audiences",
                String.format("查询TOP10打赏观众: anchorId=%d, period=%s", anchorId, period));

        List<RechargeVO.Top10AudienceVO> result = 
                rechargeService.getTop10Audiences(anchorId, period);

        return ResponseUtil.success(result);
    }
}
