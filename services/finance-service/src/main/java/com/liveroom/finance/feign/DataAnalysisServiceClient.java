package com.liveroom.finance.feign;

import common.response.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 数据分析服务Feign客户端
 * 用于调用数据分析服务的接口
 */
@FeignClient(
        name = "data-analysis-service",
        fallback = DataAnalysisServiceClientFallback.class
)
public interface DataAnalysisServiceClient {

    /**
     * 推送结算数据到分析服务
     * （预留接口，用于数据分析）
     */
    @GetMapping("/api/v1/analysis/settlement/sync")
    BaseResponse<Void> syncSettlementData(@RequestParam Long anchorId);
}
