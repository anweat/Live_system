package com.liveroom.audience.feign;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import common.bean.ApiResponse;
import common.logger.TraceLogger;
import com.liveroom.audience.service.SyncService.BatchRechargeDTO;

/**
 * 财务服务Feign客户端降级处理
 */
@Component
@Slf4j
public class FinanceServiceClientFallback implements FinanceServiceClient {

    @Override
    public ApiResponse<String> receiveBatchRecharges(BatchRechargeDTO batchDTO) {
        TraceLogger.error("FinanceServiceClientFallback", "receiveBatchRecharges", 
            "财务服务调用失败，批次ID: " + batchDTO.getBatchId());

        // 返回失败响应，触发重试机制
        return ApiResponse.error(500, "财务服务不可用");
    }
}
