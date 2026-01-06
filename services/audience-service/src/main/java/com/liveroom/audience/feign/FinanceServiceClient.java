package com.liveroom.audience.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import common.bean.ApiResponse;
import com.liveroom.audience.service.SyncService.BatchRechargeDTO;

/**
 * 财务服务Feign客户端
 * 用于调用财务服务的批量同步接口
 */
@FeignClient(name = "finance-service", fallback = FinanceServiceClientFallback.class)
public interface FinanceServiceClient {

    /**
     * 批量同步打赏数据到财务服务
     */
    @PostMapping("/api/finance/sync/batch")
    ApiResponse<String> receiveBatchRecharges(@RequestBody BatchRechargeDTO batchDTO);
}
