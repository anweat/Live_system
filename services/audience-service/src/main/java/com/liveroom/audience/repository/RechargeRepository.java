package com.liveroom.audience.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import common.bean.Recharge;

/**
 * 打赏记录数据访问层接口
 * 提供打赏记录的CRUD操作和复杂查询
 */
@Repository
public interface RechargeRepository extends JpaRepository<Recharge, Long> {

    /**
     * 按traceId查询（用于幂等性检查）
     */
    Optional<Recharge> findByTraceId(String traceId);

    /**
     * 按主播ID分页查询打赏记录
     */
    Page<Recharge> findByAnchorId(Long anchorId, Pageable pageable);

    /**
     * 按观众ID分页查询打赏记录
     */
    Page<Recharge> findByAudienceId(Long audienceId, Pageable pageable);

    /**
     * 按直播间ID分页查询打赏记录
     */
    Page<Recharge> findByLiveRoomId(Long liveRoomId, Pageable pageable);

    /**
     * 按状态查询打赏记录
     */
    List<Recharge> findByStatus(Integer status);

    /**
     * 按打赏时间范围查询
     */
    @Query("SELECT r FROM Recharge r WHERE r.rechargeTime BETWEEN :startTime AND :endTime ORDER BY r.rechargeTime DESC")
    List<Recharge> findByRechargeTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 查询主播的TOP10打赏观众（按金额降序）
     */
    @Query("SELECT r FROM Recharge r WHERE r.anchorId = :anchorId " +
           "AND r.rechargeTime BETWEEN :startTime AND :endTime " +
           "ORDER BY r.rechargeAmount DESC")
    List<Recharge> findTop10ByAnchorAndTimeRange(@Param("anchorId") Long anchorId,
                                                 @Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime,
                                                 Pageable pageable);

    /**
     * 查询待同步的打赏记录
     */
    @Query("SELECT r FROM Recharge r WHERE r.status = 0 ORDER BY r.rechargeId ASC")
    List<Recharge> findUnsyncedRecharges(Pageable pageable);

    /**
     * 统计主播的总打赏金额
     */
    @Query("SELECT COALESCE(SUM(r.rechargeAmount), 0) FROM Recharge r WHERE r.anchorId = :anchorId")
    BigDecimal sumRechargeAmountByAnchor(@Param("anchorId") Long anchorId);

    /**
     * 统计观众的总打赏金额
     */
    @Query("SELECT COALESCE(SUM(r.rechargeAmount), 0) FROM Recharge r WHERE r.audienceId = :audienceId")
    BigDecimal sumRechargeAmountByAudience(@Param("audienceId") Long audienceId);

    /**
     * 统计观众的打赏次数
     */
    @Query("SELECT COUNT(r) FROM Recharge r WHERE r.audienceId = :audienceId")
    Long countRechargeByAudience(@Param("audienceId") Long audienceId);

    /**
     * 查询未同步的最后一条打赏ID
     */
    @Query(value = "SELECT MAX(recharge_id) FROM recharge WHERE status = 0", nativeQuery = true)
    Long findLastUnsyncedRechargeId();
}
