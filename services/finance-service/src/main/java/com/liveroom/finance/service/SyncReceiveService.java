package com.liveroom.finance.service;

import com.liveroom.finance.config.RedisLockUtil;
import com.liveroom.finance.dto.BatchRechargeDTO;
import com.liveroom.finance.repository.SyncProgressRepository;
import com.liveroom.finance.repository.RechargeRecordRepository;
import common.bean.RechargeRecord;
import common.bean.SyncProgress;
import common.constant.ErrorConstants;
import common.exception.BusinessException;
import common.logger.TraceLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 数据同步接收服务
 * 处理从观众服务推送过来的打赏数据
 * 实现批量接收、持久化、统计功能
 */
@Service
@Slf4j
public class SyncReceiveService {

    @Autowired
    private SyncProgressRepository syncProgressRepository;

    @Autowired
    private RechargeRecordRepository rechargeRecordRepository;

    @Autowired
    private SettlementService settlementService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedisLockUtil redisLockUtil;

    private static final String BATCH_CACHE_KEY = "finance:batch:";
    private static final String RECHARGE_CACHE_KEY = "finance:recharge:";

    /**
     * 接收批量打赏数据（幂等性保证 + 持久化）
     */
    @Transactional(rollbackFor = Exception.class)
    public void receiveBatchRecharges(BatchRechargeDTO batchDTO) {
        String batchId = batchDTO.getBatchId();
        TraceLogger.info("SyncReceiveService", "receiveBatchRecharges", 
                "开始接收批量打赏数据，batchId: " + batchId + ", count: " + batchDTO.getTotalCount());

        long startTime = System.currentTimeMillis();

        // 1. 幂等性检查 - Redis缓存
        String cacheBatchKey = BATCH_CACHE_KEY + batchId;
        Boolean cached = redisTemplate.hasKey(cacheBatchKey);
        if (Boolean.TRUE.equals(cached)) {
            TraceLogger.warn("SyncReceiveService", "receiveBatchRecharges", 
                    "批次已处理（Redis），batchId: " + batchId);
            return;
        }

        // 2. 数据库幂等性检查
        if (syncProgressRepository.existsByBatchId(batchId)) {
            TraceLogger.warn("SyncReceiveService", "receiveBatchRecharges", 
                    "批次已存在于数据库，batchId: " + batchId);
            // 更新Redis缓存
            redisTemplate.opsForValue().set(cacheBatchKey, "processed", 24, TimeUnit.HOURS);
            return;
        }

        // 3. 获取分布式锁
        String lockKey = "sync:batch:" + batchId;
        if (!redisLockUtil.tryLock(lockKey, 300)) {
            throw new BusinessException(ErrorConstants.SYSTEM_ERROR, "系统繁忙，请稍后重试");
        }

        try {
            // 4. 批量保存打赏记录到DB2（持久化）
            List<RechargeRecord> recordsToSave = new ArrayList<>();
            List<BatchRechargeDTO.RechargeItemDTO> newItems = new ArrayList<>();
            int duplicateCount = 0;
            BigDecimal totalAmount = BigDecimal.ZERO;

            for (BatchRechargeDTO.RechargeItemDTO item : batchDTO.getRecharges()) {
                try {
                    // 检查traceId是否已存在（数据库幂等性）
                    if (rechargeRecordRepository.existsByTraceId(item.getTraceId())) {
                        duplicateCount++;
                        TraceLogger.debug("SyncReceiveService", "receiveBatchRecharges",
                                "打赏记录已存在，跳过: " + item.getTraceId());
                        continue;
                    }

                    // 转换为RechargeRecord实体
                    RechargeRecord record = convertToRecord(item, batchDTO);
                    recordsToSave.add(record);
                    newItems.add(item);
                    totalAmount = totalAmount.add(item.getRechargeAmount());

                } catch (Exception e) {
                    TraceLogger.error("SyncReceiveService", "receiveBatchRecharges",
                            "处理打赏记录失败: " + item.getRechargeId(), e);
                }
            }

            // 5. 批量插入数据库（高效）
            if (!recordsToSave.isEmpty()) {
                List<RechargeRecord> savedRecords = rechargeRecordRepository.saveAll(recordsToSave);
                TraceLogger.info("SyncReceiveService", "receiveBatchRecharges",
                        "批量插入数据库成功，记录数: " + savedRecords.size());

                // 6. 批量更新Redis缓存（用于快速查询）
                for (RechargeRecord record : savedRecords) {
                    String rechargeKey = RECHARGE_CACHE_KEY + record.getTraceId();
                    redisTemplate.opsForValue().set(rechargeKey, record, 7, TimeUnit.DAYS);
                }
            }

            // 7. 更新同步进度
            SyncProgress progress = syncProgressRepository
                    .findBySourceServiceAndTargetService(batchDTO.getSourceService(), "finance-service")
                    .orElse(SyncProgress.builder()
                            .syncType(0)
                            .sourceService(batchDTO.getSourceService())
                            .targetService("finance-service")
                            .lastSyncRechargeId(0L)
                            .totalSyncedCount(0L)
                            .totalSyncedAmount(BigDecimal.ZERO)
                            .syncStatus(0)
                            .syncIntervalSeconds(300)
                            .createTime(LocalDateTime.now())
                            .build());

            // 在errorMessage中记录batchId（用于幂等性检查）
            String errorMessage = progress.getErrorMessage() != null ? progress.getErrorMessage() : "";
            errorMessage += "batchId:" + batchId + ";";
            
            progress.setLastSyncRechargeId(batchDTO.getBatchTime());
            progress.setTotalSyncedCount(progress.getTotalSyncedCount() + recordsToSave.size());
            progress.setTotalSyncedAmount(progress.getTotalSyncedAmount().add(totalAmount));
            progress.setLastSyncTime(LocalDateTime.now());
            progress.setSyncStatus(2); // 已同步
            progress.setErrorMessage(errorMessage);
            progress.setUpdateTime(LocalDateTime.now());
            syncProgressRepository.save(progress);

            // 8. 标记批次已处理（Redis缓存）
            redisTemplate.opsForValue().set(cacheBatchKey, "processed", 24, TimeUnit.HOURS);

            // 9. 异步触发结算任务（只处理新记录）
            if (!newItems.isEmpty()) {
                settlementService.scheduleSettlement(newItems);
            }

            long endTime = System.currentTimeMillis();
            TraceLogger.info("SyncReceiveService", "receiveBatchRecharges",
                    String.format("批量打赏数据接收完成，batchId: %s, 新增: %d, 重复: %d, 总金额: %s, 耗时: %dms",
                            batchId, recordsToSave.size(), duplicateCount, totalAmount, (endTime - startTime)));

        } catch (Exception e) {
            TraceLogger.error("SyncReceiveService", "receiveBatchRecharges",
                    "接收批量打赏数据失败，batchId: " + batchId, e);
            throw new BusinessException(ErrorConstants.SYSTEM_ERROR, "接收打赏数据失败: " + e.getMessage());
        } finally {
            redisLockUtil.unlock(lockKey);
        }
    }

    /**
     * 转换DTO为实体
     */
    private RechargeRecord convertToRecord(BatchRechargeDTO.RechargeItemDTO item, BatchRechargeDTO batchDTO) {
        LocalDateTime rechargeTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(item.getRechargeTime()), 
                ZoneId.systemDefault()
        );

        return RechargeRecord.builder()
                .originalRechargeId(item.getRechargeId())
                .traceId(item.getTraceId())
                .anchorId(item.getAnchorId())
                .anchorName(item.getAnchorName())
                .audienceId(item.getAudienceId())
                .audienceName(item.getAudienceName())
                .rechargeAmount(item.getRechargeAmount())
                .rechargeTime(rechargeTime)
                .rechargeType(item.getRechargeType() != null ? item.getRechargeType() : 0)
                .liveRoomId(item.getLiveRoomId())
                .syncBatchId(batchDTO.getBatchId())
                .settlementStatus(0) // 待结算
                .sourceService(batchDTO.getSourceService())
                .receivedTime(LocalDateTime.now())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }

    /**
     * 查询同步进度
     */
    public SyncProgress getSyncProgress(String sourceService) {
        return syncProgressRepository
                .findBySourceServiceAndTargetService(sourceService, "finance-service")
                .orElseThrow(() -> new BusinessException(ErrorConstants.SYSTEM_ERROR, "同步进度不存在"));
    }

    /**
     * 根据traceId查询打赏记录（从Redis缓存）
     */
    public BatchRechargeDTO.RechargeItemDTO getRechargeByTraceId(String traceId) {
        String rechargeKey = RECHARGE_CACHE_KEY + traceId;
        Object cached = redisTemplate.opsForValue().get(rechargeKey);
        if (cached instanceof BatchRechargeDTO.RechargeItemDTO) {
            return (BatchRechargeDTO.RechargeItemDTO) cached;
        }
        return null;
    }
}
