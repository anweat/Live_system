package com.liveroom.audience.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import common.bean.SyncProgress;

/**
 * 同步进度数据访问层接口
 * 记录打赏数据同步至财务服务的进度
 */
@Repository
public interface SyncProgressRepository extends JpaRepository<SyncProgress, Long> {

    /**
     * 按源服务和目标服务查询同步进度
     */
    SyncProgress findBySourceServiceAndTargetService(String sourceService, String targetService);

    /**
     * 查询所有待同步的进度
     */
    @Query("SELECT s FROM SyncProgress s WHERE s.syncStatus = 0 ORDER BY s.progressId ASC")
    List<SyncProgress> findUnsyncedProgress();
}
