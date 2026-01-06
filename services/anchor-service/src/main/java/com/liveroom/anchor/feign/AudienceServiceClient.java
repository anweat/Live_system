package com.liveroom.anchor.feign;

import com.liveroom.anchor.vo.RechargeVO;
import common.response.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 观众服务Feign客户端
 * 用于调用audience-service查询打赏记录
 * 
 * @author Team
 * @version 1.0.0
 */
@FeignClient(
    name = "audience-service",
    path = "/audience/api/v1",
    fallback = AudienceServiceClientFallback.class
)
public interface AudienceServiceClient {

    /**
     * 查询主播的打赏记录（分页）
     */
    @GetMapping("/recharge/anchor/{anchorId}")
    BaseResponse<Object> getRechargesByAnchor(
            @PathVariable("anchorId") Long anchorId,
            @RequestParam(value = "startTime", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(value = "endTime", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size);

    /**
     * 查询直播间的打赏记录（分页）
     */
    @GetMapping("/recharge/live-room/{liveRoomId}")
    BaseResponse<Object> getRechargesByLiveRoom(
            @PathVariable("liveRoomId") Long liveRoomId,
            @RequestParam(value = "startTime", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(value = "endTime", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size);

    /**
     * 按traceId查询打赏记录
     */
    @GetMapping("/recharge/trace/{traceId}")
    RechargeVO getRechargeByTraceId(@PathVariable("traceId") String traceId);

    /**
     * 统计主播的打赏总额
     */
    @GetMapping("/recharge/anchor/{anchorId}/total")
    Object getTotalRechargeByAnchor(
            @PathVariable("anchorId") Long anchorId,
            @RequestParam(value = "startTime", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(value = "endTime", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime);

    /**
     * 查询TOP10打赏观众
     */
    @GetMapping("/recharge/anchor/{anchorId}/top10")
    List<RechargeVO.Top10AudienceVO> getTop10Audiences(
            @PathVariable("anchorId") Long anchorId,
            @RequestParam(value = "startTime", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(value = "endTime", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime);
}
