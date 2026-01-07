package com.liveroom.mock.controller;

import common.logger.AppLogger;
import common.response.BaseResponse;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Mock Service 健康检查控制器
 */
@RestController
@RequestMapping("/api/v1/health")
@AllArgsConstructor
public class MockHealthController {

    /**
     * 健康检查
     */
    @GetMapping
    public BaseResponse<Map<String, Object>> health() {
        AppLogger.info("健康检查请求");
        
        Map<String, Object> data = new HashMap<>();
        data.put("service", "mock-service");
        data.put("version", "2.0");
        data.put("status", "UP");
        data.put("timestamp", System.currentTimeMillis());
        
        return BaseResponse.success("服务正常", data);
    }
}
