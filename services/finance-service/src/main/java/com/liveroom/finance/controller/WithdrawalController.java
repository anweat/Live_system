package com.liveroom.finance.controller;

import com.liveroom.finance.dto.WithdrawalRequestDTO;
import com.liveroom.finance.service.WithdrawalService;
import common.annotation.Idempotent;
import common.annotation.Log;
import common.annotation.ValidateParam;
import common.dto.WithdrawalDTO;
import common.exception.ValidationException;
import common.response.BaseResponse;
import common.response.PageResponse;
import common.response.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 提现管理Controller
 */
@RestController
@RequestMapping("/api/v1/withdrawal")
@Slf4j
public class WithdrawalController {

    @Autowired
    private WithdrawalService withdrawalService;

    /**
     * 主播申请提现
     * POST /api/v1/withdrawal
     */
    @PostMapping
    @Log("申请提现")
    @ValidateParam
    @Idempotent(key = "#withdrawalDTO.traceId", timeout = 60)
    public BaseResponse<WithdrawalDTO> applyWithdrawal(
            @Valid @RequestBody WithdrawalRequestDTO withdrawalDTO) {
        WithdrawalDTO result = withdrawalService.applyWithdrawal(withdrawalDTO);
        return ResponseUtil.success(result, "提现申请成功", 0);
    }

    /**
     * 查询提现记录
     * GET /api/v1/withdrawal/{anchorId}
     */
    @GetMapping("/{anchorId}")
    @Log("查询提现记录")
    public BaseResponse<PageResponse<WithdrawalDTO>> listWithdrawals(
            @PathVariable Long anchorId,
            @RequestParam(required = false) Integer status,
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

        Page<WithdrawalDTO> pageResult = withdrawalService
                .listWithdrawals(anchorId, status, page, size);
        return ResponseUtil.pageSuccess(pageResult.getContent(), 
                pageResult.getTotalElements(), page, size);
    }

    /**
     * 根据traceId查询提现记录
     * GET /api/v1/withdrawal/by-trace-id/{traceId}
     */
    @GetMapping("/by-trace-id/{traceId}")
    @Log("按traceId查询提现记录")
    public BaseResponse<WithdrawalDTO> getWithdrawalByTraceId(@PathVariable String traceId) {
        if (traceId == null || traceId.trim().isEmpty()) {
            throw new ValidationException("traceId不能为空");
        }
        WithdrawalDTO result = withdrawalService.getWithdrawalByTraceId(traceId);
        return ResponseUtil.success(result);
    }

    /**
     * 审核提现申请（管理员接口）
     * PUT /api/v1/withdrawal/{withdrawalId}/approve
     */
    @PutMapping("/{withdrawalId}/approve")
    @Log("审核通过提现")
    public BaseResponse<Void> approveWithdrawal(@PathVariable Long withdrawalId) {
        if (withdrawalId == null || withdrawalId <= 0) {
            throw new ValidationException("提现ID不合法");
        }
        withdrawalService.approveWithdrawal(withdrawalId);
        return ResponseUtil.success(null, "审核通过", 0);
    }

    /**
     * 拒绝提现申请（管理员接口）
     * PUT /api/v1/withdrawal/{withdrawalId}/reject
     */
    @PutMapping("/{withdrawalId}/reject")
    @Log("拒绝提现")
    public BaseResponse<Void> rejectWithdrawal(
            @PathVariable Long withdrawalId,
            @RequestParam String reason) {
        if (withdrawalId == null || withdrawalId <= 0) {
            throw new ValidationException("提现ID不合法");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new ValidationException("拒绝原因不能为空");
        }
        withdrawalService.rejectWithdrawal(withdrawalId, reason);
        return ResponseUtil.success(null, "已拒绝", 0);
    }
}
