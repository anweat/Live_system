package com.liveroom.dbservice.controller;

import com.liveroom.dbservice.service.DatabaseInitializeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import common.logger.AppLogger;
import common.logger.TraceLogger;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据库管理REST API
 * 提供数据库初始化、检查、查看等操作接口
 */
@RestController
@RequestMapping("/api/database")
@RequiredArgsConstructor
public class DatabaseController {

    private final DatabaseInitializeService databaseInitializeService;

    /**
     * 获取数据库健康状态
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        try {
            // 记录请求
            AppLogger.info("执行数据库健康检查");

            databaseInitializeService.checkDatabaseConnection();

            // 记录成功
            AppLogger.logHealthCheck("database_connection", true, "连接正常");
            TraceLogger.info("database", "health_check_passed");

            Map<String, Object> response = new HashMap<>();
            response.put("status", "UP");
            response.put("message", "数据库连接正常");
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);
        } catch (SQLException e) {
            // 记录失败
            AppLogger.logHealthCheck("database_connection", false, e.getMessage());
            TraceLogger.error("database", "health_check_failed", null, e);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "DOWN");
            response.put("message", "数据库连接失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.status(503).body(response);
        }
    }

    /**
     * 初始化数据库表
     */
    @PostMapping("/initialize")
    public ResponseEntity<Map<String, Object>> initialize() {
        try {
            AppLogger.info("收到数据库初始化请求");
            TraceLogger.info("database", "initialize_requested");

            // 这里可以调用初始化脚本
            databaseInitializeService.initializeTables();

            AppLogger.info("数据库初始化成功");
            TraceLogger.info("database", "initialize_completed");

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "数据库初始化成功");
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            AppLogger.error("数据库初始化失败", e);
            TraceLogger.error("database", "initialize_failed", null, e);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "数据库初始化失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 检查数据库表
     */
    @GetMapping("/tables")
    public ResponseEntity<Map<String, Object>> checkTables() {
        try {
            AppLogger.info("执行数据库表检查");
            TraceLogger.info("database", "table_check_requested");

            databaseInitializeService.checkDatabaseTables();

            AppLogger.info("数据库表检查完成");
            TraceLogger.info("database", "table_check_completed");

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "数据库表检查完成");
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);
        } catch (SQLException e) {
            AppLogger.error("数据库表检查失败", e);
            TraceLogger.error("database", "table_check_failed", null, e);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "数据库表检查失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取服务信息
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getInfo() {
        AppLogger.debug("请求服务信息");

        Map<String, Object> response = new HashMap<>();
        response.put("service", "db-service");
        response.put("version", "1.0.0");
        response.put("description", "数据库初始化和管理服务");
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }
}
