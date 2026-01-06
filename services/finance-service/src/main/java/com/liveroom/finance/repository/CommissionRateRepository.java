package com.liveroom.finance.repository;

import common.bean.CommissionRate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 分成比例Repository
 */
@Repository
public interface CommissionRateRepository extends JpaRepository<CommissionRate, Long> {

    /**
     * 查询主播当前生效的分成比例
     */
    @Query("SELECT cr FROM CommissionRate cr WHERE cr.anchorId = :anchorId " +
            "AND cr.status = 1 " +
            "AND cr.effectiveTime <= :now " +
            "AND (cr.expireTime IS NULL OR cr.expireTime > :now) " +
            "ORDER BY cr.effectiveTime DESC")
    Optional<CommissionRate> findCurrentRateByAnchorId(@Param("anchorId") Long anchorId, 
                                                        @Param("now") LocalDateTime now);

    /**
     * 查询主播的分成比例历史
     */
    Page<CommissionRate> findByAnchorIdOrderByEffectiveTimeDesc(Long anchorId, Pageable pageable);

    /**
     * 查询指定时间生效的分成比例
     */
    @Query("SELECT cr FROM CommissionRate cr WHERE cr.anchorId = :anchorId " +
            "AND cr.effectiveTime <= :time " +
            "AND (cr.expireTime IS NULL OR cr.expireTime > :time) " +
            "ORDER BY cr.effectiveTime DESC")
    Optional<CommissionRate> findRateAtTime(@Param("anchorId") Long anchorId, 
                                            @Param("time") LocalDateTime time);

    /**
     * 查询主播所有启用状态的分成比例
     */
    List<CommissionRate> findByAnchorIdAndStatus(Long anchorId, Integer status);
}
