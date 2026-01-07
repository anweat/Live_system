package com.liveroom.mock.repository;

import com.liveroom.mock.entity.MockDataTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 模拟数据追踪 Repository
 */
@Repository
public interface MockDataTrackingRepository extends JpaRepository<MockDataTracking, Long> {

    /**
     * 根据批次ID和删除状态查询
     */
    List<MockDataTracking> findByBatchIdAndIsDeleted(String batchId, Boolean isDeleted);

    /**
     * 根据实体类型和删除状态查询
     */
    List<MockDataTracking> findByEntityTypeAndIsDeleted(String entityType, Boolean isDeleted);

    /**
     * 根据实体类型、实体ID和删除状态查询
     */
    MockDataTracking findByEntityTypeAndEntityIdAndIsDeleted(String entityType, Long entityId, Boolean isDeleted);

    /**
     * 根据traceId查询
     */
    List<MockDataTracking> findByTraceId(String traceId);

    /**
     * 查询指定时间之前创建的数据
     */
    List<MockDataTracking> findByIsDeletedAndCreatedTimeBefore(Boolean isDeleted, LocalDateTime beforeTime);

    /**
     * 批量标记为已删除
     */
    @Modifying
    @Query("UPDATE MockDataTracking m SET m.isDeleted = true, m.deletedTime = :deletedTime WHERE m.id IN :ids")
    void markAsDeleted(@Param("ids") List<Long> ids, @Param("deletedTime") LocalDateTime deletedTime);

    /**
     * 统计各类型数据数量
     */
    @Query("SELECT m.entityType, COUNT(m) FROM MockDataTracking m WHERE m.isDeleted = false GROUP BY m.entityType")
    List<Object[]> countByEntityType();

    /**
     * 查询所有未删除的数据
     */
    List<MockDataTracking> findByIsDeleted(Boolean isDeleted);
}
