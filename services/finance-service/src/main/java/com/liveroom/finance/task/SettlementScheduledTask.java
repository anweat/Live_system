package com.liveroom.finance.task;

import com.liveroom.finance.config.RedisLockUtil;
import com.liveroom.finance.service.SettlementService;
import common.logger.TraceLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时结算任务
 * 每10分钟执行一次自动结算
 */
@Component
@Slf4j
public class SettlementScheduledTask {

    @Autowired
    private SettlementService settlementService;

    @Autowired
    private RedisLockUtil redisLockUtil;

    /**
     * 自动结算任务
     * 每10分钟执行一次
     */
    @Scheduled(cron = "${finance.settlement.cron:0 */10 * * * ?}")
    public void autoSettlement() {
        String lockKey = "task:settlement:auto";

        // 使用分布式锁确保单节点执行
        if (!redisLockUtil.tryLock(lockKey, 600)) {
            TraceLogger.debug("SettlementScheduledTask", "autoSettlement", 
                    "获取锁失败，跳过本次结算任务");
            return;
        }

        try {
            TraceLogger.info("SettlementScheduledTask", "autoSettlement", 
                    "开始执行自动结算任务");

            long startTime = System.currentTimeMillis();

            // TODO: 查询所有需要结算的主播
            // 实际应该从同步进度表或其他数据源获取需要结算的主播列表
            // List<Long> anchorIds = settlementService.getAnchorsNeedSettlement();
            
            // for (Long anchorId : anchorIds) {
            //     try {
            //         settlementService.settleForAnchor(anchorId, null);
            //     } catch (Exception e) {
            //         TraceLogger.error("SettlementScheduledTask", "autoSettlement",
            //                 "主播结算失败，主播ID: " + anchorId, e);
            //     }
            // }

            long endTime = System.currentTimeMillis();
            TraceLogger.info("SettlementScheduledTask", "autoSettlement",
                    "自动结算任务完成，耗时: " + (endTime - startTime) + "ms");

        } catch (Exception e) {
            TraceLogger.error("SettlementScheduledTask", "autoSettlement",
                    "自动结算任务异常", e);
        } finally {
            redisLockUtil.unlock(lockKey);
        }
    }

    /**
     * 清理过期缓存任务
     * 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanExpiredCache() {
        String lockKey = "task:clean:cache";

        if (!redisLockUtil.tryLock(lockKey, 3600)) {
            return;
        }

        try {
            TraceLogger.info("SettlementScheduledTask", "cleanExpiredCache",
                    "开始清理过期缓存");

            // TODO: 实现缓存清理逻辑

            TraceLogger.info("SettlementScheduledTask", "cleanExpiredCache",
                    "过期缓存清理完成");

        } catch (Exception e) {
            TraceLogger.error("SettlementScheduledTask", "cleanExpiredCache",
                    "清理过期缓存异常", e);
        } finally {
            redisLockUtil.unlock(lockKey);
        }
    }
}
