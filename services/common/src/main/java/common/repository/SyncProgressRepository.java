package common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import common.bean.SyncProgress;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 数据同步进度Repository接口
 */
public interface SyncProgressRepository extends BaseRepository<SyncProgress, Long> {

    /**
     * 按同步类型查询进度
     */
    List<SyncProgress> findBySyncType(Integer syncType);

    /**
     * 按源服务和目标服务查询
     */
    Optional<SyncProgress> findBySourceServiceAndTargetService(String sourceService, String targetService);

    /**
     * 查询所有待同步的进度
     */
    @Query("SELECT s FROM SyncProgress s WHERE s.syncStatus = 0")
    List<SyncProgress> findPendingSync();

    /**
     * 查询所有失败的同步
     */
    @Query("SELECT s FROM SyncProgress s WHERE s.syncStatus = 3")
    List<SyncProgress> findFailedSync();

    /**
     * 按状态查询
     */
    List<SyncProgress> findBySyncStatus(Integer status);

    /**
     * 查询需要重试的同步
     */
    @Query("SELECT s FROM SyncProgress s WHERE s.nextSyncTime <= :now AND s.syncStatus IN (0, 3)")
    List<SyncProgress> findSyncNeedRetry(@Param("now") LocalDateTime now);

    /**
     * 检查批次ID是否存在（幂等性检查）
     * 注：需要在errorMessage字段中存储batchId
     */
    @Query("SELECT CASE WHEN COUNT(sp) > 0 THEN true ELSE false END FROM SyncProgress sp " +
            "WHERE sp.errorMessage LIKE CONCAT('%batchId:', :batchId, '%')")
    boolean existsByBatchId(@Param("batchId") String batchId);
}
