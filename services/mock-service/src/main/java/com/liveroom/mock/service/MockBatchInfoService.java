package com.liveroom.mock.service;

import com.liveroom.mock.constant.BatchStatus;
import com.liveroom.mock.entity.MockBatchInfo;
import com.liveroom.mock.repository.MockBatchInfoRepository;
import common.constant.ErrorConstants;
import common.exception.SystemException;
import common.logger.AppLogger;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 批次信息服务
 * 负责管理批量创建任务的批次信息
 */
@Service
@AllArgsConstructor
public class MockBatchInfoService {

    private final MockBatchInfoRepository repository;

    /**
     * 创建批次信息
     *
     * @param batchId    批次ID
     * @param batchType  批次类型
     * @param totalCount 总数量
     * @return 批次信息
     */
    public MockBatchInfo createBatchInfo(String batchId, String batchType, Integer totalCount) {
        try {
            MockBatchInfo batchInfo = MockBatchInfo.builder()
                    .batchId(batchId)
                    .batchType(batchType)
                    .totalCount(totalCount)
                    .build();

            MockBatchInfo saved = repository.save(batchInfo);
            AppLogger.info("创建批次信息成功，batchId: {}, batchType: {}, totalCount: {}", 
                    batchId, batchType, totalCount);

            return saved;
        } catch (Exception e) {
            AppLogger.error("创建批次信息失败", e);
            throw new SystemException(ErrorConstants.SYSTEM_ERROR, "创建批次信息失败", e);
        }
    }

    /**
     * 更新批次信息
     *
     * @param batchId      批次ID
     * @param successCount 成功数量
     * @param failCount    失败数量
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateBatchInfo(String batchId, Integer successCount, Integer failCount) {
        try {
            Optional<MockBatchInfo> optional = repository.findByBatchId(batchId);
            if (optional.isPresent()) {
                MockBatchInfo batchInfo = optional.get();
                batchInfo.setSuccessCount(successCount);
                batchInfo.setFailCount(failCount);
                batchInfo.setEndTime(LocalDateTime.now());

                // 根据结果设置状态
                if (failCount == 0) {
                    batchInfo.setStatus(BatchStatus.SUCCESS);
                } else if (successCount > 0) {
                    batchInfo.setStatus(BatchStatus.PARTIAL);
                } else {
                    batchInfo.setStatus(BatchStatus.FAILED);
                }

                repository.save(batchInfo);
                AppLogger.info("更新批次信息成功，batchId: {}, success: {}, fail: {}", 
                        batchId, successCount, failCount);
            }
        } catch (Exception e) {
            AppLogger.error("更新批次信息失败，batchId: {}", e, batchId);
            throw new SystemException(ErrorConstants.SYSTEM_ERROR, "更新批次信息失败", e);
        }
    }

    /**
     * 根据批次ID查询
     *
     * @param batchId 批次ID
     * @return 批次信息
     */
    public MockBatchInfo findByBatchId(String batchId) {
        return repository.findByBatchId(batchId).orElse(null);
    }

    /**
     * 根据批次类型查询
     *
     * @param batchType 批次类型
     * @return 批次信息列表
     */
    public List<MockBatchInfo> findByBatchType(String batchType) {
        return repository.findByBatchType(batchType);
    }

    /**
     * 查询所有批次
     *
     * @return 批次信息列表
     */
    public List<MockBatchInfo> findAll() {
        return repository.findAll();
    }

    /**
     * 标记批次失败
     *
     * @param batchId      批次ID
     * @param errorMessage 错误信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void markBatchFailed(String batchId, String errorMessage) {
        try {
            Optional<MockBatchInfo> optional = repository.findByBatchId(batchId);
            if (optional.isPresent()) {
                MockBatchInfo batchInfo = optional.get();
                batchInfo.setStatus(BatchStatus.FAILED);
                batchInfo.setErrorMessage(errorMessage);
                batchInfo.setEndTime(LocalDateTime.now());
                repository.save(batchInfo);

                AppLogger.warn("标记批次失败，batchId: {}, error: {}", batchId, errorMessage);
            }
        } catch (Exception e) {
            AppLogger.error("标记批次失败异常，batchId: {}", e, batchId);
        }
    }
}
