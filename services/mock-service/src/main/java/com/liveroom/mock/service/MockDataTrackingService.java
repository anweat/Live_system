package com.liveroom.mock.service;

import com.liveroom.mock.entity.MockDataTracking;
import com.liveroom.mock.repository.MockDataTrackingRepository;
import common.exception.BusinessException;
import common.exception.SystemException;
import common.constant.ErrorConstants;
import common.logger.AppLogger;
import common.logger.TraceLogger;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 模拟数据追踪服务
 * 负责记录和管理所有模拟数据的ID
 */
@Service
@AllArgsConstructor
public class MockDataTrackingService {

    private final MockDataTrackingRepository repository;

    /**
     * 追踪单个模拟数据
     *
     * @param entityType 实体类型
     * @param entityId   实体ID
     * @param traceId    traceId
     * @param batchId    批次ID（可选）
     */
    public void trackEntity(String entityType, Long entityId, String traceId, String batchId) {
        try {
            MockDataTracking tracking = MockDataTracking.builder()
                    .entityType(entityType)
                    .entityId(entityId)
                    .traceId(traceId)
                    .batchId(batchId)
                    .build();

            repository.save(tracking);

            Map<String, String> params = new HashMap<>();
            params.put("entityType", entityType);
            params.put("entityId", String.valueOf(entityId));
            if (batchId != null) {
                params.put("batchId", batchId);
            }

            TraceLogger.info("mock_data", "track_entity", traceId, params);

        } catch (Exception e) {
            AppLogger.error("追踪模拟数据失败", e);
            throw new SystemException(ErrorConstants.SYSTEM_ERROR, "追踪模拟数据失败", e);
        }
    }

    /**
     * 批量追踪模拟数据
     *
     * @param trackingList 追踪列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void trackEntitiesBatch(List<MockDataTracking> trackingList) {
        try {
            repository.saveAll(trackingList);
            AppLogger.info("批量追踪模拟数据成功，数量: {}", trackingList.size());
        } catch (Exception e) {
            AppLogger.error("批量追踪模拟数据失败", e);
            throw new SystemException(ErrorConstants.SYSTEM_ERROR, "批量追踪模拟数据失败", e);
        }
    }

    /**
     * 根据批次ID查询数据
     *
     * @param batchId 批次ID
     * @return 追踪记录列表
     */
    public List<MockDataTracking> findByBatchId(String batchId) {
        return repository.findByBatchIdAndIsDeleted(batchId, false);
    }

    /**
     * 根据实体类型查询数据
     *
     * @param entityType 实体类型
     * @return 追踪记录列表
     */
    public List<MockDataTracking> findByEntityType(String entityType) {
        return repository.findByEntityTypeAndIsDeleted(entityType, false);
    }

    /**
     * 查询所有未删除的数据
     *
     * @return 追踪记录列表
     */
    public List<MockDataTracking> findAll() {
        return repository.findByIsDeleted(false);
    }

    /**
     * 根据实体类型和ID查询
     *
     * @param entityType 实体类型
     * @param entityId   实体ID
     * @return 追踪记录
     */
    public MockDataTracking findByEntityTypeAndId(String entityType, Long entityId) {
        return repository.findByEntityTypeAndEntityIdAndIsDeleted(entityType, entityId, false);
    }

    /**
     * 标记为已删除
     *
     * @param trackingIds 追踪ID列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void markAsDeleted(List<Long> trackingIds) {
        try {
            repository.markAsDeleted(trackingIds, LocalDateTime.now());
            AppLogger.info("标记模拟数据为已删除，数量: {}", trackingIds.size());
        } catch (Exception e) {
            AppLogger.error("标记模拟数据失败", e);
            throw new SystemException(ErrorConstants.SYSTEM_ERROR, "标记模拟数据失败", e);
        }
    }

    /**
     * 统计各类型数据数量
     *
     * @return 类型-数量映射
     */
    public Map<String, Long> countByEntityType() {
        try {
            List<Object[]> results = repository.countByEntityType();
            Map<String, Long> countMap = new HashMap<>();

            for (Object[] result : results) {
                String entityType = (String) result[0];
                Long count = (Long) result[1];
                countMap.put(entityType, count);
            }

            return countMap;
        } catch (Exception e) {
            AppLogger.error("统计模拟数据失败", e);
            throw new SystemException(ErrorConstants.SYSTEM_ERROR, "统计模拟数据失败", e);
        }
    }

    /**
     * 根据实体类型提取实体ID列表
     *
     * @param entityType 实体类型
     * @return 实体ID列表
     */
    public List<Long> getEntityIdsByType(String entityType) {
        List<MockDataTracking> trackingList = findByEntityType(entityType);
        return trackingList.stream()
                .map(MockDataTracking::getEntityId)
                .collect(Collectors.toList());
    }

    /**
     * 查询指定时间之前创建的数据
     *
     * @param beforeTime 时间点
     * @return 追踪记录列表
     */
    public List<MockDataTracking> findBeforeTime(LocalDateTime beforeTime) {
        return repository.findByIsDeletedAndCreatedTimeBefore(false, beforeTime);
    }

    /**
     * 删除追踪记录（物理删除）
     *
     * @param trackingId 追踪ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteTracking(Long trackingId) {
        try {
            repository.deleteById(trackingId);
            AppLogger.info("删除追踪记录，ID: {}", trackingId);
        } catch (Exception e) {
            AppLogger.error("删除追踪记录失败，ID: {}", e, trackingId);
            throw new SystemException(ErrorConstants.SYSTEM_ERROR, "删除追踪记录失败", e);
        }
    }
}
