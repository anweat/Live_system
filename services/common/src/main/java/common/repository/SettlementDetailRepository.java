package common.repository;

import common.bean.SettlementDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 结算明细Repository接口
 */
public interface SettlementDetailRepository extends BaseRepository<SettlementDetail, Long> {

    /**
     * 按主播ID查询结算明细，按开始时间倒序
     */
    Page<SettlementDetail> findByAnchorIdOrderBySettlementStartTimeDesc(Long anchorId, Pageable pageable);

    /**
     * 按主播ID和时间范围查询结算明细
     */
    @Query("SELECT sd FROM SettlementDetail sd WHERE sd.anchorId = :anchorId " +
            "AND sd.settlementStartTime >= :startTime " +
            "AND sd.settlementEndTime <= :endTime " +
            "ORDER BY sd.settlementStartTime DESC")
    Page<SettlementDetail> findByAnchorIdAndTimeRange(
            @Param("anchorId") Long anchorId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);

    /**
     * 按结算ID查询所有明细
     */
    List<SettlementDetail> findBySettlementIdOrderBySettlementStartTimeDesc(Long settlementId);

    /**
     * 按主播ID查询所有明细
     */
    List<SettlementDetail> findByAnchorIdOrderBySettlementStartTimeDesc(Long anchorId);
}

