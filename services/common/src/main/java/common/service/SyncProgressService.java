package common.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import common.bean.SyncProgress;
import common.logger.TraceLogger;
import common.repository.SyncProgressRepository;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 数据同步进度Service - 统一的同步进度数据访问接口
 * 支持断点续传和失败重试机制
 */
@Slf4j
@Service
public class SyncProgressService extends BaseService<SyncProgress, Long, SyncProgressRepository> {

    public SyncProgressService(SyncProgressRepository repository) {
        super(repository);
    }

    @Override
    protected String getCachePrefix() {
        return "syncProgress::";
    }

    @Override
    protected String getEntityName() {
        return "SyncProgress";
    }


    /**
     * 按同步类型查询进度
     */
    @Cacheable(value = "syncProgress::syncType", key = "#syncType")
    @Transactional(readOnly = true)
    public List<SyncProgress> findBySyncType(Integer syncType) {
        TraceLogger.info("SyncProgress", "findBySyncType", "查询同步类型: " + syncType);
        return repository.findBySyncType(syncType);
    }

    /**
     * 按源服务和目标服务查询
     */
    @Cacheable(value = "syncProgress::service", key = "#sourceService + '_' + #targetService", 
               unless = "#result == null")
    @Transactional(readOnly = true)
    public Optional<SyncProgress> findByService(String sourceService, String targetService) {
        TraceLogger.info("SyncProgress", "findByService", 
            String.format("查询服务: %s -> %s", sourceService, targetService));
        return repository.findBySourceServiceAndTargetService(sourceService, targetService);
    }

    /**
     * 查询所有待同步的进度
     */
    @Cacheable(value = "syncProgress::pending")
    @Transactional(readOnly = true)
    public List<SyncProgress> findPendingSync() {
        TraceLogger.info("SyncProgress", "findPendingSync", "查询待同步的进度");
        return repository.findPendingSync();
    }

    /**
     * 查询所有失败的同步
     */
    @Cacheable(value = "syncProgress::failed")
    @Transactional(readOnly = true)
    public List<SyncProgress> findFailedSync() {
        TraceLogger.info("SyncProgress", "findFailedSync", "查询失败的同步");
        return repository.findFailedSync();
    }

    /**
     * 按状态查询
     */
    @Cacheable(value = "syncProgress::status", key = "#status")
    @Transactional(readOnly = true)
    public List<SyncProgress> findByStatus(Integer status) {
        TraceLogger.info("SyncProgress", "findByStatus", "查询同步状态: " + status);
        return repository.findBySyncStatus(status);
    }

    /**
     * 查询需要重试的同步
     */
    @Transactional(readOnly = true)
    public List<SyncProgress> findSyncNeedRetry() {
        TraceLogger.info("SyncProgress", "findSyncNeedRetry", "查询需要重试的同步");
        return repository.findSyncNeedRetry(LocalDateTime.now());
    }

    /**
     * 创建同步进度记录
     */
    @CacheEvict(value = {"syncProgress::syncType", "syncProgress::service", 
                         "syncProgress::pending", "syncProgress::failed", 
                         "syncProgress::status"}, allEntries = true)
    @Transactional
    public SyncProgress createSyncProgress(SyncProgress progress) {
        if (progress == null) {
            throw new IllegalArgumentException("同步进度信息不完整");
        }
        TraceLogger.info("SyncProgress", "createSyncProgress", 
            String.format("创建同步进度: %s -> %s", progress.getSourceService(), progress.getTargetService()));
        return repository.save(progress);
    }

    /**
     * 更新同步进度
     */
    @CacheEvict(value = {"syncProgress::syncType", "syncProgress::service", 
                         "syncProgress::pending", "syncProgress::failed", 
                         "syncProgress::status"}, allEntries = true)
    @Transactional
    public SyncProgress updateSyncProgress(SyncProgress progress) {
        if (progress == null || progress.getProgressId() == null) {
            throw new IllegalArgumentException("同步进度ID不能为空");
        }
        TraceLogger.info("SyncProgress", "updateSyncProgress", "更新同步进度ID: " + progress.getProgressId());
        return repository.save(progress);
    }

    /**
     * 更新同步状态
     */
    @CacheEvict(value = {"syncProgress::syncType", "syncProgress::service", 
                         "syncProgress::pending", "syncProgress::failed", 
                         "syncProgress::status"}, allEntries = true)
    @Transactional
    public void updateSyncStatus(Long progressId, Integer status) {
        repository.findById(progressId).ifPresent(progress -> {
            progress.setSyncStatus(status);
            progress.setUpdateTime(LocalDateTime.now());
            repository.save(progress);
            TraceLogger.info("SyncProgress", "updateSyncStatus", 
                String.format("更新同步ID: %d 状态为: %d", progressId, status));
        });
    }

    /**
     * 标记同步失败
     */
    @CacheEvict(value = {"syncProgress::pending", "syncProgress::failed", "syncProgress::status"}, 
                allEntries = true)
    @Transactional
    public void markAsFailed(Long progressId, String errorMessage) {
        repository.findById(progressId).ifPresent(progress -> {
            progress.setSyncStatus(3);  // 失败状态
            progress.setErrorMessage(errorMessage);
            progress.setUpdateTime(LocalDateTime.now());
            repository.save(progress);
            TraceLogger.warn("SyncProgress", "markAsFailed", 
                String.format("标记同步失败: %d, 错误: %s", progressId, errorMessage));
        });
    }

    /**
     * 更新下次同步时间
     */
    @CacheEvict(value = {"syncProgress::pending", "syncProgress::status"}, allEntries = true)
    @Transactional
    public void updateNextSyncTime(Long progressId, LocalDateTime nextSyncTime) {
        repository.findById(progressId).ifPresent(progress -> {
            progress.setNextSyncTime(nextSyncTime);
            progress.setUpdateTime(LocalDateTime.now());
            repository.save(progress);
            TraceLogger.info("SyncProgress", "updateNextSyncTime", 
                String.format("更新同步ID: %d 的下次同步时间: %s", progressId, nextSyncTime));
        });
    }

    /**
     * 批量保存或更新同步进度
     */
    @CacheEvict(value = {"syncProgress::syncType", "syncProgress::service", 
                         "syncProgress::pending", "syncProgress::failed", 
                         "syncProgress::status"}, allEntries = true)
    @Transactional
    public List<SyncProgress> batchSave(List<SyncProgress> progresses) {
        if (progresses == null || progresses.isEmpty()) {
            TraceLogger.warn("SyncProgress", "batchSave", "保存列表为空");
            return List.of();
        }
        TraceLogger.info("SyncProgress", "batchSave", "批量保存同步进度，条数: " + progresses.size());
        return repository.saveAll(progresses);
    }

    /**
     * 删除同步进度记录
     */
    @CacheEvict(value = {"syncProgress::syncType", "syncProgress::service", 
                         "syncProgress::pending", "syncProgress::failed", 
                         "syncProgress::status"}, allEntries = true)
    @Transactional
    public void deleteSyncProgress(Long progressId) {
        if (progressId == null) {
            throw new IllegalArgumentException("同步进度ID不能为空");
        }
        TraceLogger.info("SyncProgress", "deleteSyncProgress", "删除同步进度ID: " + progressId);
        repository.deleteById(progressId);
    }
}
