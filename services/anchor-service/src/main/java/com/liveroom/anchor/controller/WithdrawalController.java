package com.liveroom.anchor.controller;

import com.liveroom.anchor.feign.FinanceServiceClient;
import com.liveroom.anchor.service.WithdrawalService;
import common.annotation.Log;
import common.exception.ValidationException;
import common.logger.TraceLogger;
import common.response.BaseResponse;
import common.response.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 提现管理Controller
 * 主播端提现申请和查询接口
 * 
 * @author Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/withdrawal")
@Slf4j
@Validated
public class WithdrawalController {

    @Autowired
    private WithdrawalService withdrawalService;

    /**
     * 主播申请提现
     * POST /api/v1/withdrawal/apply
     */
    @PostMapping("/apply")
    @Log("主播申请提现")
    public BaseResponse<FinanceServiceClient.WithdrawalVO> applyWithdrawal(
            @RequestParam @NotNull(message = "主播ID不能为空") Long anchorId,
            @RequestParam @NotNull(message = "提现金额不能为空")
            @DecimalMin(value = "1.00", message = "提现金额最少为1.00元")
            @DecimalMax(value = "99999.99", message = "提现金额最多为99999.99元")
            BigDecimal amount,
            @RequestParam @NotNull(message = "提现方式不能为空")
            @Min(value = 0, message = "提现方式取值范围0-2")
            Integer withdrawalType,
            @RequestParam(required = false) String bankName,
            @RequestParam @NotNull(message = "账号不能为空") String accountNumber,
            @RequestParam @NotNull(message = "账户持有人不能为空") String accountHolder) {

        TraceLogger.info("WithdrawalController", "applyWithdrawal",
                String.format("主播申请提现: anchorId=%d, amount=%s", anchorId, amount));

        // 验证银行卡提现必须填写开户行
        if (withdrawalType == 0 && (bankName == null || bankName.trim().isEmpty())) {
            throw new ValidationException("银行卡提现必须填写开户行");
        }

        FinanceServiceClient.WithdrawalVO result = withdrawalService.applyWithdrawal(
                anchorId, amount, withdrawalType, bankName, accountNumber, accountHolder);

        return ResponseUtil.<FinanceServiceClient.WithdrawalVO>success(result);
    }

    /**
     * 查询主播的提现记录
     * GET /api/v1/withdrawal/list/{anchorId}
     */
    @GetMapping("/list/{anchorId}")
    @Log("查询提现记录")
    public BaseResponse<Object> listWithdrawals(
            @PathVariable Long anchorId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) Integer size) {

        if (anchorId == null || anchorId <= 0) {
            throw new ValidationException("主播ID不合法");
        }

        TraceLogger.debug("WithdrawalController", "listWithdrawals",
                String.format("查询提现记录: anchorId=%d, status=%s", anchorId, status));

        BaseResponse<Object> response = withdrawalService.listWithdrawals(
                anchorId, status, page, size);

        return response;
    }

    /**
     * 根据traceId查询提现记录
     * GET /api/v1/withdrawal/trace/{traceId}
     */
    @GetMapping("/trace/{traceId}")
    @Log("按traceId查询提现记录")
    public BaseResponse<FinanceServiceClient.WithdrawalVO> getWithdrawalByTraceId(
            @PathVariable String traceId) {

        if (traceId == null || traceId.trim().isEmpty()) {
            throw new ValidationException("traceId不能为空");
        }

        TraceLogger.debug("WithdrawalController", "getWithdrawalByTraceId",
                "查询提现记录: traceId=" + traceId);

        FinanceServiceClient.WithdrawalVO result = 
                withdrawalService.getWithdrawalByTraceId(traceId);

        return ResponseUtil.success(result);
    }
}
