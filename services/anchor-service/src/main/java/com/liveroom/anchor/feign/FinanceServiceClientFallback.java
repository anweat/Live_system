package com.liveroom.anchor.feign;

import common.bean.ApiResponse;
import common.logger.TraceLogger;
import common.response.BaseResponse;
import common.response.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 财务服务Feign客户端降级处理
 * 
 * @author Team
 * @version 1.0.0
 */
@Component
@Slf4j
public class FinanceServiceClientFallback implements FinanceServiceClient {

    @Override
    public BaseResponse<CommissionRateVO> getCurrentCommissionRate(Long anchorId) {
        TraceLogger.error("FinanceServiceClientFallback", "getCurrentCommissionRate",
                "财务服务调用失败，主播ID: " + anchorId);
        return ResponseUtil.<CommissionRateVO>error(500, "财务服务不可用，无法获取分成比例");
    }

    @Override
    public BaseResponse<WithdrawalVO> applyWithdrawal(WithdrawalRequestDTO requestDTO) {
        TraceLogger.error("FinanceServiceClientFallback", "applyWithdrawal",
                "财务服务调用失败，traceId: " + requestDTO.getTraceId());
        return ResponseUtil.<WithdrawalVO>error(500, "财务服务不可用，提现申请失败");
    }

    @Override
    public BaseResponse<Object> listWithdrawals(Long anchorId, Integer status, Integer page, Integer size) {
        TraceLogger.error("FinanceServiceClientFallback", "listWithdrawals",
                "财务服务调用失败，主播ID: " + anchorId);
        return ResponseUtil.<Object>error(500, "财务服务不可用，无法查询提现记录");
    }

    @Override
    public BaseResponse<WithdrawalVO> getWithdrawalByTraceId(String traceId) {
        TraceLogger.error("FinanceServiceClientFallback", "getWithdrawalByTraceId",
                "财务服务调用失败，traceId: " + traceId);
        return ResponseUtil.<WithdrawalVO>error(500, "财务服务不可用，无法查询提现记录");
    }
}
