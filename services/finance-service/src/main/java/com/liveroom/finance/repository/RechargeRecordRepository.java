package com.liveroom.finance.repository;

import common.bean.RechargeRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 打赏记录Repository（财务服务）
 */
@Repository
public interface RechargeRecordRepository extends JpaRepository<RechargeRecord, Long> {

    /**
     * 根据traceId查询（幂等性检查）
     */
    Optional<RechargeRecord> findByTraceId(String traceId);

    /**
     * 检查traceId是否存在
     */
    boolean existsByTraceId(String traceId);

    /**
     * 根据批次ID查询
     */
    List<RechargeRecord> findBySyncBatchId(String syncBatchId);

    /**
     * 查询待结算的记录
     */
    @Query("SELECT rr FROM RechargeRecord rr WHERE rr.settlementStatus = 0 " +
            "ORDER BY rr.rechargeTime ASC")
    List<RechargeRecord> findUnsettledRecords(Pageable pageable);

    /**
     * 查询指定主播待结算的记录
     */
    @Query("SELECT rr FROM RechargeRecord rr WHERE rr.anchorId = :anchorId " +
            "AND rr.settlementStatus = 0 " +
            "ORDER BY rr.rechargeTime ASC")
    List<RechargeRecord> findUnsettledRecordsByAnchor(@Param("anchorId") Long anchorId);

    /**
     * 统计主播指定时间段的打赏总额
     */
    @Query("SELECT COALESCE(SUM(rr.rechargeAmount), 0) FROM RechargeRecord rr " +
            "WHERE rr.anchorId = :anchorId " +
            "AND rr.rechargeTime >= :startTime " +
            "AND rr.rechargeTime < :endTime")
    BigDecimal sumAmountByAnchorAndTime(@Param("anchorId") Long anchorId,
                                         @Param("startTime") LocalDateTime startTime,
                                         @Param("endTime") LocalDateTime endTime);

    /**
     * 统计主播指定时间段的打赏笔数
     */
    @Query("SELECT COUNT(rr) FROM RechargeRecord rr " +
            "WHERE rr.anchorId = :anchorId " +
            "AND rr.rechargeTime >= :startTime " +
            "AND rr.rechargeTime < :endTime")
    Long countByAnchorAndTime(@Param("anchorId") Long anchorId,
                               @Param("startTime") LocalDateTime startTime,
                               @Param("endTime") LocalDateTime endTime);

    /**
     * 统计所有主播指定时间段的数据
     */
    @Query("SELECT rr.anchorId, rr.anchorName, " +
            "COUNT(rr), SUM(rr.rechargeAmount), " +
            "MIN(rr.rechargeAmount), MAX(rr.rechargeAmount), " +
            "AVG(rr.rechargeAmount) " +
            "FROM RechargeRecord rr " +
            "WHERE rr.rechargeTime >= :startTime " +
            "AND rr.rechargeTime < :endTime " +
            "GROUP BY rr.anchorId, rr.anchorName")
    List<Object[]> statisticsByTimeRange(@Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime);

    /**
     * 查询主播的TOP观众（按打赏金额）
     */
    @Query("SELECT rr.audienceId, rr.audienceName, SUM(rr.rechargeAmount) as total " +
            "FROM RechargeRecord rr " +
            "WHERE rr.anchorId = :anchorId " +
            "AND rr.rechargeTime >= :startTime " +
            "GROUP BY rr.audienceId, rr.audienceName " +
            "ORDER BY total DESC")
    List<Object[]> findTopAudiencesByAnchor(@Param("anchorId") Long anchorId,
                                             @Param("startTime") LocalDateTime startTime,
                                             Pageable pageable);

    /**
     * 按小时统计数据
     */
    @Query("SELECT FUNCTION('DATE_FORMAT', rr.rechargeTime, '%Y-%m-%d %H:00:00') as hour, " +
            "COUNT(rr), SUM(rr.rechargeAmount) " +
            "FROM RechargeRecord rr " +
            "WHERE rr.anchorId = :anchorId " +
            "AND rr.rechargeTime >= :startTime " +
            "AND rr.rechargeTime < :endTime " +
            "GROUP BY hour " +
            "ORDER BY hour")
    List<Object[]> hourlyStatistics(@Param("anchorId") Long anchorId,
                                     @Param("startTime") LocalDateTime startTime,
                                     @Param("endTime") LocalDateTime endTime);

    /**
     * 批量更新结算状态
     */
    @Query("UPDATE RechargeRecord rr SET rr.settlementStatus = :status, " +
            "rr.settlementTime = :settlementTime, " +
            "rr.appliedCommissionRate = :commissionRate, " +
            "rr.settlementAmount = :settlementAmount, " +
            "rr.updateTime = :updateTime " +
            "WHERE rr.recordId IN :recordIds")
    int batchUpdateSettlementStatus(@Param("recordIds") List<Long> recordIds,
                                     @Param("status") Integer status,
                                     @Param("settlementTime") LocalDateTime settlementTime,
                                     @Param("commissionRate") Double commissionRate,
                                     @Param("settlementAmount") BigDecimal settlementAmount,
                                     @Param("updateTime") LocalDateTime updateTime);
    /**
     * 查询指定时间段内主播收入TOP榜
     */
    @Query("SELECT r.anchorId, r.anchorName, SUM(r.rechargeAmount), COUNT(r.id) " +
           "FROM RechargeRecord r " +
           "WHERE r.rechargeTime BETWEEN :startTime AND :endTime " +
           "GROUP BY r.anchorId, r.anchorName " +
           "ORDER BY SUM(r.rechargeAmount) DESC")
    List<Object[]> getTopAnchorsByRevenue(@Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime,
                                           Pageable pageable);

    default List<Object[]> getTopAnchorsByRevenue(LocalDateTime startTime, 
                                                    LocalDateTime endTime, 
                                                    int topN) {
        return getTopAnchorsByRevenue(startTime, endTime, PageRequest.of(0, topN));
    }
}
