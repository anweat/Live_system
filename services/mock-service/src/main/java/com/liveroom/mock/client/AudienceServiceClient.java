package com.liveroom.mock.client;

import com.liveroom.mock.dto.external.AudienceVO;
import com.liveroom.mock.dto.external.RechargeVO;
import com.liveroom.mock.dto.request.CreateAudienceRequest;
import com.liveroom.mock.dto.request.RechargeRequest;
import common.response.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 观众服务 Feign Client
 */
@FeignClient(
    name = "audience-service",
    url = "${service.audience.url}",
    fallbackFactory = AudienceServiceFallbackFactory.class
)
public interface AudienceServiceClient {

    /**
     * 创建观众
     */
    @PostMapping("/audience/api/v1/audiences")
    BaseResponse<AudienceVO> createAudience(@RequestBody CreateAudienceRequest request);

    /**
     * 查询观众信息
     */
    @GetMapping("/audience/api/v1/audiences/{id}")
    BaseResponse<AudienceVO> getAudience(@PathVariable("id") Long audienceId);

    /**
     * 观众打赏
     */
    @PostMapping("/audience/api/v1/recharge")
    BaseResponse<RechargeVO> recharge(@RequestBody RechargeRequest request);

    /**
     * 根据traceId查询打赏记录
     */
    @GetMapping("/audience/api/v1/recharge/by-trace-id/{traceId}")
    BaseResponse<RechargeVO> getRechargeByTraceId(@PathVariable("traceId") String traceId);
}
