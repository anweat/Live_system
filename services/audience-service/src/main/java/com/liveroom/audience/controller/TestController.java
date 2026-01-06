package com.liveroom.audience.controller;

import com.liveroom.audience.service.RechargeService;
import com.liveroom.audience.service.SyncService;
import common.bean.ApiResponse;
import common.logger.TraceLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器
 * 提供测试用的API接口
 */
@RestController
@RequestMapping("/api/v1/test")
@Slf4j
public class TestController {

    @Autowired
    private RechargeService rechargeService;

    @Autowired
    private SyncService syncService;

    /**
     * 查询同步队列状态（测试用）
     */
    @GetMapping("/sync/queue/status")
    public ApiResponse<Map<String, Object>> getSyncQueueStatus() {
        int queueSize = rechargeService.getSyncQueueSize();

        Map<String, Object> result = new HashMap<>();
        result.put("queueSize", queueSize);
        result.put("description", "待同步到财务服务的打赏记录数");

        return ApiResponse.success(result);
    }

    /**
     * 手动触发同步（测试用）
     */
    @PostMapping("/sync/trigger")
    public ApiResponse<Map<String, Object>> triggerSync(@RequestParam(defaultValue = "100") Integer batchSize) {
        TraceLogger.info("TestController", "triggerSync", "手动触发同步，批次大小: " + batchSize);

        try {
            int beforeSize = rechargeService.getSyncQueueSize();
            syncService.syncRechargeDataToFinance("finance-service", batchSize);
            int afterSize = rechargeService.getSyncQueueSize();

            Map<String, Object> result = new HashMap<>();
            result.put("beforeQueueSize", beforeSize);
            result.put("afterQueueSize", afterSize);
            result.put("syncedCount", beforeSize - afterSize);
            result.put("status", "success");

            return ApiResponse.success(result);
        } catch (Exception e) {
            TraceLogger.error("TestController", "triggerSync", "同步失败", e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", "failed");
            result.put("error", e.getMessage());
            
            return ApiResponse.error(500, "同步失败: " + e.getMessage());
        }
    }
}
