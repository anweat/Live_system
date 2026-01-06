package com.liveroom.audience.controller;

import java.util.List;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import common.annotation.Idempotent;
import common.annotation.Log;
import common.annotation.ValidateParam;
import common.exception.ValidationException;
import common.response.BaseResponse;
import common.response.PageResponse;
import common.response.ResponseUtil;
import common.logger.TraceLogger;
import com.liveroom.audience.dto.RechargeDTO;
import com.liveroom.audience.service.RechargeService;
import com.liveroom.audience.vo.Top10AudienceVO;

/**
 * 打赏相关API接口
 * 提供打赏请求处理、查询等相关操作
 */
@RestController
@RequestMapping("/api/v1/recharge")
@Slf4j
public class RechargeController {

    @Autowired
    private RechargeService rechargeService;

    /**
     * 创建打赏记录（支持幂等性）
     */
    @PostMapping
    @Log("创建打赏记录")
    @ValidateParam
    @Idempotent(key = "#rechargeDTO.traceId", timeout = 60)
    public BaseResponse<RechargeDTO> createRecharge(@Valid @RequestBody RechargeDTO rechargeDTO) {
        RechargeDTO result = rechargeService.createRecharge(rechargeDTO);
        return ResponseUtil.success(result, "打赏成功", 0);
    }

    /**
     * 获取打赏记录详情
     */
    @GetMapping("/{rechargeId}")
    @Log("查询打赏记录")
    public BaseResponse<RechargeDTO> getRecharge(@PathVariable Long rechargeId) {
        if (rechargeId == null || rechargeId <= 0) {
            throw new ValidationException("打赏ID不合法");
        }
        RechargeDTO result = rechargeService.getRecharge(rechargeId);
        if (result == null) {
            TraceLogger.info("打赏记录不存在，rechargeId=" + rechargeId);
        }
        return ResponseUtil.success(result);
    }

    /**
     * 按traceId查询打赏记录（用于检查重复）
     */
    @GetMapping("/by-trace-id/{traceId}")
    @Log("按traceId查询打赏记录")
    public BaseResponse<RechargeDTO> getRechargeByTraceId(@PathVariable String traceId) {
        if (traceId == null || traceId.trim().isEmpty()) {
            throw new ValidationException("traceId不能为空");
        }
        if (traceId.length() > 64) {
            throw new ValidationException("traceId长度不能超过64");
        }
        RechargeDTO result = rechargeService.getRechargeByTraceId(traceId);
        if (result == null) {
            TraceLogger.info("未找到对应的打赏记录，traceId=" + traceId);
        }
        return ResponseUtil.success(result);
    }

    /**
     * 查询主播的打赏列表
     */
    @GetMapping("/anchor/{anchorId}")
    @Log("查询主播的打赏列表")
    public BaseResponse<PageResponse<RechargeDTO>> listAnchorRecharges(
            @PathVariable Long anchorId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        if (anchorId == null || anchorId <= 0) {
            throw new ValidationException("主播ID不合法");
        }
        if (page == null || page < 1) {
            throw new ValidationException("页码必须从1开始");
        }
        if (size == null || size < 1 || size > 100) {
            throw new ValidationException("每页大小必须在1-100之间");
        }
        Page<RechargeDTO> pageResult = rechargeService.listAnchorRecharges(anchorId, page, size);
        return ResponseUtil.pageSuccess(pageResult.getContent(), pageResult.getTotalElements(), page, size);
    }

    /**
     * 查询观众的打赏历史
     */
    @GetMapping("/audience/{audienceId}")
    @Log("查询观众的打赏历史")
    public BaseResponse<PageResponse<RechargeDTO>> listAudienceRecharges(
            @PathVariable Long audienceId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {        if (audienceId == null || audienceId <= 0) {
            throw new ValidationException("观众ID不合法");
        }
        if (page == null || page < 1) {
            throw new ValidationException("页码必须从1开始");
        }
        if (size == null || size < 1 || size > 100) {
            throw new ValidationException("每页大小必须在1-100之间");
        }        Page<RechargeDTO> pageResult = rechargeService.listAudienceRecharges(audienceId, page, size);
        return ResponseUtil.pageSuccess(pageResult.getContent(), pageResult.getTotalElements(), page, size);
    }

    /**
     * 查询直播间的打赏列表
     */
    @GetMapping("/live-room/{liveRoomId}")
    @Log("查询直播间的打赏列表")
    public BaseResponse<PageResponse<RechargeDTO>> listLiveRoomRecharges(
            @PathVariable Long liveRoomId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        if (liveRoomId == null || liveRoomId <= 0) {
            throw new ValidationException("直播间ID不合法");
        }
        if (page == null || page < 1) {
            throw new ValidationException("页码必须从1开始");
        }
        if (size == null || size < 1 || size > 100) {
            throw new ValidationException("每页大小必须在1-100之间");
        }
        Page<RechargeDTO> pageResult = rechargeService.listLiveRoomRecharges(liveRoomId, page, size);
        return ResponseUtil.pageSuccess(pageResult.getContent(), pageResult.getTotalElements(), page, size);
    }

    /**
     * 查询主播的TOP10打赏观众
     */
    @GetMapping("/top10")
    @Log("查询主播的TOP10打赏观众")
    public BaseResponse<List<Top10AudienceVO>> getTop10Audiences(
            @RequestParam Long anchorId,
            @RequestParam(defaultValue = "all") String period) {
        if (anchorId == null || anchorId <= 0) {
            throw new ValidationException("主播ID不合法");
        }
        if (period == null || (!period.matches("^(day|week|month|all)$"))) {
            throw new ValidationException("时间段必须为：day、week、month、all");
        }
        List<Top10AudienceVO> result = rechargeService.getTop10Audiences(anchorId, period);
        if (result == null || result.isEmpty()) {
            TraceLogger.info("主播无打赏记录，anchorId=" + anchorId + ",period=" + period);
        }
        return ResponseUtil.success(result);
    }

    /**
     * 查询未同步的打赏记录
     */
    @GetMapping("/unsync")
    @Log("查询未同步的打赏记录")
    public BaseResponse<List<RechargeDTO>> listUnsyncedRecharges(
            @RequestParam(required = false) Integer limit) {
        if (limit != null && (limit < 1 || limit > 1000)) {
            throw new ValidationException("查询数量必须在1-1000之间");
        }
        List<RechargeDTO> result = rechargeService.listUnsyncedRecharges(limit);
        if (result == null || result.isEmpty()) {
            TraceLogger.info("暂无待同步的打赏记录，limit=" + limit);
        }
        return ResponseUtil.success(result);
    }

    /**
     * 标记打赏为已同步
     */
    @PatchMapping("/{rechargeId}/sync")
    @Log("标记打赏为已同步")
    public BaseResponse<Void> markRechargeAsSynced(
            @PathVariable Long rechargeId,
            @RequestParam Long settlementId) {
        if (rechargeId == null || rechargeId <= 0) {
            throw new ValidationException("打赏ID不合法");
        }
        if (settlementId == null || settlementId <= 0) {
            throw new ValidationException("结算ID不合法");
        }
        rechargeService.markRechargeAsSynced(rechargeId, settlementId);
        return ResponseUtil.success(null, "打赏记录已标记为同步");
    }
}
