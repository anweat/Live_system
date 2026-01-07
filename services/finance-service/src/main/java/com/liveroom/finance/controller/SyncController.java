package com.liveroom.finance.controller;

import com.liveroom.finance.dto.BatchRechargeDTO;
import com.liveroom.finance.service.SyncReceiveService;
import common.annotation.Log;
import common.bean.SyncProgress;
import common.response.BaseResponse;
import common.response.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 数据同步Controller（内部接口）
 * 接收观众服务推送的打赏数据
 */
@RestController
@RequestMapping("/internal/sync")
@Slf4j
public class SyncController {

    @Autowired
    private SyncReceiveService syncReceiveService;

    /**
     * 接收批量打赏数据
     * POST /internal/sync/recharges
     */
    @PostMapping("/recharges")
    @Log("接收批量打赏数据")
    public BaseResponse<Void> receiveBatchRecharges(@Valid @RequestBody BatchRechargeDTO batchRechargeDTO) {
        syncReceiveService.receiveBatchRecharges(batchRechargeDTO);
        return ResponseUtil.success( "数据接收成功",null);
    }

    /**
     * 查询同步进度
     * GET /internal/sync/progress?sourceService=audience-service
     */
    @GetMapping("/progress")
    @Log("查询同步进度")
    public BaseResponse<SyncProgress> getSyncProgress(@RequestParam String sourceService) {
        SyncProgress progress = syncReceiveService.getSyncProgress(sourceService);
        return ResponseUtil.success(progress);
    }
}
