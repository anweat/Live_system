package com.liveroom.finance.repository;

import common.bean.SettlementDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 结算明细Repository
 */
@Repository
public interface SettlementDetailRepository extends JpaRepository<SettlementDetail, Long> {

    /**
     * 查询主播的结算明细
     */
    Page<SettlementDetail> findByAnchorIdOrderBySettlementStartTimeDesc(Long anchorId, Pageable pageable);

    /**
     * 查询主播指定时间范围的结算明细
     */
    @Query("SELECT sd FROM SettlementDetail sd WHERE sd.anchorId = :anchorId " +
            "AND sd.settlementStartTime >= :startTime " +
            "AND sd.settlementEndTime <= :endTime " +
            "ORDER BY sd.settlementStartTime DESC")
    Page<SettlementDetail> findByAnchorIdAndTimeRange(@Param("anchorId") Long anchorId,
                                                       @Param("startTime") LocalDateTime startTime,
                                                       @Param("endTime") LocalDateTime endTime,
                                                       Pageable pageable);

    /**
     * 查询结算记录的所有明细
     */
    List<SettlementDetail> findBySettlementIdOrderBySettlementStartTimeDesc(Long settlementId);
}
