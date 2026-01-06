package com.liveroom.finance.repository;

import common.bean.SyncProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 同步进度Repository
 */
@Repository
public interface SyncProgressRepository extends JpaRepository<SyncProgress, Long> {

    /**
     * 根据同步类型和源服务查询进度
     */
    @Query("SELECT sp FROM SyncProgress sp WHERE sp.syncType = :syncType " +
            "AND sp.sourceService = :sourceService " +
            "AND sp.targetService = :targetService")
    Optional<SyncProgress> findBySyncTypeAndServices(@Param("syncType") Integer syncType,
                                                      @Param("sourceService") String sourceService,
                                                      @Param("targetService") String targetService);

    /**
     * 根据源服务和目标服务查询进度
     */
    Optional<SyncProgress> findBySourceServiceAndTargetService(String sourceService, String targetService);

    /**
     * 检查批次ID是否存在（幂等性检查）
     * 注：需要在errorMessage字段中存储batchId
     */
    @Query("SELECT CASE WHEN COUNT(sp) > 0 THEN true ELSE false END FROM SyncProgress sp " +
            "WHERE sp.errorMessage LIKE CONCAT('%batchId:', :batchId, '%')")
    boolean existsByBatchId(@Param("batchId") String batchId);
}
