package com.liveroom.audience.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import common.logger.TraceLogger;
import com.liveroom.audience.service.SyncService;

/**
 * 打赏数据同步定时任务
 * 定时将打赏数据同步到财务服务
 */
@Component
@Slf4j
public class RechargeDataSyncTask {

    @Autowired
    private SyncService syncService;

    /**
     * 每5分钟执行一次，同步打赏数据到财务服务
     * 支持断点续传，避免重复同步
     */
    @Scheduled(fixedDelay = 300000)  // 5分钟执行一次
    public void syncRechargeData() {
        TraceLogger.info("RechargeDataSyncTask", "syncRechargeData", "开始同步打赏数据到财务服务");

        try {
            // 每次同步最多100条记录
            syncService.syncRechargeDataToFinance("finance-service", 100);

            TraceLogger.info("RechargeDataSyncTask", "syncRechargeData", "打赏数据同步完成");
        } catch (Exception e) {
            TraceLogger.error("RechargeDataSyncTask", "syncRechargeData", 
                "打赏数据同步失败", e);
        }
    }

    /**
     * 每小时执行一次，清理已完成的同步记录
     */
    @Scheduled(cron = "0 0 * * * ?")  // 每小时执行一次
    public void cleanupSyncProgress() {
        TraceLogger.info("RechargeDataSyncTask", "cleanupSyncProgress", "开始清理同步进度记录");

        try {
            // TODO: 实现清理逻辑
            TraceLogger.info("RechargeDataSyncTask", "cleanupSyncProgress", "同步进度记录清理完成");
        } catch (Exception e) {
            TraceLogger.error("RechargeDataSyncTask", "cleanupSyncProgress", 
                "同步进度记录清理失败", e);
        }
    }
}
