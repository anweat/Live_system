package com.liveroom.finance.controller;

import com.liveroom.finance.service.SettlementService;
import com.liveroom.finance.vo.BalanceVO;
import com.liveroom.finance.vo.SettlementDetailVO;
import common.annotation.Log;
import common.exception.ValidationException;
import common.response.BaseResponse;
import common.response.PageResponse;
import common.response.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 结算查询Controller
 */
@RestController
@RequestMapping("/api/v1/settlement")
@Slf4j
public class SettlementController {

    @Autowired
    private SettlementService settlementService;

    /**
     * 查询主播可提取金额
     * GET /api/v1/settlement/{anchorId}/balance
     */
    @GetMapping("/{anchorId}/balance")
    @Log("查询主播余额")
    public BaseResponse<BalanceVO> getAnchorBalance(@PathVariable Long anchorId) {
        if (anchorId == null || anchorId <= 0) {
            throw new ValidationException("主播ID不合法");
        }
        BalanceVO balance = settlementService.getAnchorBalance(anchorId);
        return ResponseUtil.success(balance);
    }

    /**
     * 查询主播结算明细
     * GET /api/v1/settlement/{anchorId}/details
     */
    @GetMapping("/{anchorId}/details")
    @Log("查询结算明细")
    public BaseResponse<PageResponse<SettlementDetailVO>> getSettlementDetails(
            @PathVariable Long anchorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
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

        Page<SettlementDetailVO> pageResult = settlementService
                .getSettlementDetails(anchorId, startDate, endDate, page, size);
        return ResponseUtil.success(PageResponse.of(pageResult.getContent(),
                pageResult.getTotalElements(), page, size));
    }

    /**
     * 手动触发结算（管理员接口）
     * POST /api/v1/settlement/trigger
     */
    @PostMapping("/trigger")
    @Log("手动触发结算")
    public BaseResponse<Void> triggerSettlement(@RequestParam Long anchorId) {
        if (anchorId == null || anchorId <= 0) {
            throw new ValidationException("主播ID不合法");
        }
        // TODO: 实现手动触发结算逻辑
        return ResponseUtil.success("结算任务已触发", null);
    }
}
