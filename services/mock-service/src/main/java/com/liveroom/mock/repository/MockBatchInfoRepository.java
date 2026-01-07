package com.liveroom.mock.repository;

import com.liveroom.mock.entity.MockBatchInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 批次信息 Repository
 */
@Repository
public interface MockBatchInfoRepository extends JpaRepository<MockBatchInfo, Long> {

    /**
     * 根据批次ID查询
     */
    Optional<MockBatchInfo> findByBatchId(String batchId);

    /**
     * 根据批次类型查询
     */
    List<MockBatchInfo> findByBatchType(String batchType);

    /**
     * 根据状态查询
     */
    List<MockBatchInfo> findByStatus(String status);

    /**
     * 根据批次类型和状态查询
     */
    List<MockBatchInfo> findByBatchTypeAndStatus(String batchType, String status);
}
