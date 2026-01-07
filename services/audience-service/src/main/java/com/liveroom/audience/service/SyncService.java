package com.liveroom.audience.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import common.bean.Recharge;
import common.bean.SyncProgress;
import common.constant.ErrorConstants;
import common.exception.BusinessException;
import common.logger.TraceLogger;
import common.service.DataAccessFacade;
import common.util.DateTimeUtil;
import com.liveroom.audience.feign.FinanceServiceClient;

/**
 * 数据同步服务
 * 管理打赏数据同步至财务服务的进度
 *
 * 重构说明：已改为通过 DataAccessFacade 统一访问数据库
 */
@Service
@Slf4j
@Transactional
public class SyncService {

    @Autowired
    private DataAccessFacade dataAccessFacade;

    @Autowired
    private RechargeService rechargeService;

    @Autowired
    private FinanceServiceClient financeServiceClient;

    /**
     * 同步打赏数据到财务服务（从内存队列批量同步）
     */
    public void syncRechargeDataToFinance(String financeServiceName, Integer batchSize) {
        TraceLogger.info("SyncService", "syncRechargeDataToFinance", 
            "开始同步打赏数据到财务服务: " + financeServiceName);

        try {
            // 1. 从内存队列中获取待同步的打赏记录
            List<Recharge> recharges = rechargeService.pollSyncQueue(batchSize);

            if (recharges.isEmpty()) {
                TraceLogger.debug("SyncService", "syncRechargeDataToFinance", 
                        "同步队列为空，本次无数据需要同步");
                return;
            }

            TraceLogger.info("SyncService", "syncRechargeDataToFinance", 
                    "从同步队列获取到 " + recharges.size() + " 条待同步记录");

            // 2. 构建批量同步DTO
            BatchRechargeDTO batchDTO = buildBatchDTO(recharges);

            // 3. 调用财务服务API进行批量同步
            try {
                financeServiceClient.receiveBatchRecharges(batchDTO);
                TraceLogger.info("SyncService", "syncRechargeDataToFinance", 
                        String.format("打赏数据同步成功，batchId=%s, 共%d条记录，总金额=%s",
                                batchDTO.getBatchId(), batchDTO.getTotalCount(), batchDTO.getTotalAmount()));
            } catch (Exception e) {
                // 同步失败，将数据重新放回队列（简单重试机制）
                TraceLogger.error("SyncService", "syncRechargeDataToFinance", 
                        "调用财务服务失败，批次ID: " + batchDTO.getBatchId(), e);
                // 可选：重新放回队列或记录失败
                throw new BusinessException(ErrorConstants.SYSTEM_ERROR, "同步到财务服务失败: " + e.getMessage());
            }

        } catch (Exception e) {
            TraceLogger.error("SyncService", "syncRechargeDataToFinance", 
                "打赏数据同步失败", e);
            throw e;
        }
    }

    /**
     * 构建批量同步DTO
     */
    private BatchRechargeDTO buildBatchDTO(List<Recharge> recharges) {
        String batchId = "BATCH-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
        
        List<BatchRechargeDTO.RechargeItemDTO> items = recharges.stream()
                .map(this::convertToItemDTO)
                .collect(Collectors.toList());

        BigDecimal totalAmount = recharges.stream()
                .map(Recharge::getRechargeAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BatchRechargeDTO batchDTO = new BatchRechargeDTO();
        batchDTO.setBatchId(batchId);
        batchDTO.setSourceService("audience-service");
        batchDTO.setBatchTime(System.currentTimeMillis());
        batchDTO.setRecharges(items);
        batchDTO.setTotalCount(items.size());
        batchDTO.setTotalAmount(totalAmount);

        return batchDTO;
    }

    /**
     * 转换Recharge为RechargeItemDTO
     */
    private BatchRechargeDTO.RechargeItemDTO convertToItemDTO(Recharge recharge) {
        BatchRechargeDTO.RechargeItemDTO item = new BatchRechargeDTO.RechargeItemDTO();
        item.setRechargeId(recharge.getRechargeId());
        item.setTraceId(recharge.getTraceId());
        item.setAnchorId(recharge.getAnchorId());
        item.setAnchorName(recharge.getAnchorName());
        item.setAudienceId(recharge.getAudienceId());
        item.setAudienceName(recharge.getAudienceNickname());
        item.setRechargeAmount(recharge.getRechargeAmount());
        item.setRechargeTime(recharge.getRechargeTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli());
        item.setRechargeType(recharge.getRechargeType());
        item.setLiveRoomId(recharge.getLiveRoomId());
        return item;
    }

    /**
     * DTO类定义（内部类）
     */
    public static class BatchRechargeDTO {
        private String batchId;
        private String sourceService;
        private Long batchTime;
        private List<RechargeItemDTO> recharges;
        private Integer totalCount;
        private BigDecimal totalAmount;

        // Getters and Setters
        public String getBatchId() { return batchId; }
        public void setBatchId(String batchId) { this.batchId = batchId; }
        public String getSourceService() { return sourceService; }
        public void setSourceService(String sourceService) { this.sourceService = sourceService; }
        public Long getBatchTime() { return batchTime; }
        public void setBatchTime(Long batchTime) { this.batchTime = batchTime; }
        public List<RechargeItemDTO> getRecharges() { return recharges; }
        public void setRecharges(List<RechargeItemDTO> recharges) { this.recharges = recharges; }
        public Integer getTotalCount() { return totalCount; }
        public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

        public static class RechargeItemDTO {
            private Long rechargeId;
            private String traceId;
            private Long anchorId;
            private String anchorName;
            private Long audienceId;
            private String audienceName;
            private BigDecimal rechargeAmount;
            private Long rechargeTime;
            private Integer rechargeType;
            private Long liveRoomId;

            // Getters and Setters
            public Long getRechargeId() { return rechargeId; }
            public void setRechargeId(Long rechargeId) { this.rechargeId = rechargeId; }
            public String getTraceId() { return traceId; }
            public void setTraceId(String traceId) { this.traceId = traceId; }
            public Long getAnchorId() { return anchorId; }
            public void setAnchorId(Long anchorId) { this.anchorId = anchorId; }
            public String getAnchorName() { return anchorName; }
            public void setAnchorName(String anchorName) { this.anchorName = anchorName; }
            public Long getAudienceId() { return audienceId; }
            public void setAudienceId(Long audienceId) { this.audienceId = audienceId; }
            public String getAudienceName() { return audienceName; }
            public void setAudienceName(String audienceName) { this.audienceName = audienceName; }
            public BigDecimal getRechargeAmount() { return rechargeAmount; }
            public void setRechargeAmount(BigDecimal rechargeAmount) { this.rechargeAmount = rechargeAmount; }
            public Long getRechargeTime() { return rechargeTime; }
            public void setRechargeTime(Long rechargeTime) { this.rechargeTime = rechargeTime; }
            public Integer getRechargeType() { return rechargeType; }
            public void setRechargeType(Integer rechargeType) { this.rechargeType = rechargeType; }
            public Long getLiveRoomId() { return liveRoomId; }
            public void setLiveRoomId(Long liveRoomId) { this.liveRoomId = liveRoomId; }
        }
    }

    /**
     * 获取同步进度
     */
    public SyncProgress getSyncProgress(String sourceService, String targetService) {
        SyncProgress progress = dataAccessFacade.syncProgress().findByService(sourceService, targetService)
                .orElseThrow(() -> new BusinessException(ErrorConstants.SYSTEM_ERROR, "同步进度不存在"));
        return progress;
    }
}
