package common.repository;

import common.bean.RechargeRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 打赏记录Repository（财务服务DB2副本）
 * 用于财务结算、统计分析、对账审计
 */
public interface RechargeRecordRepository extends BaseRepository<RechargeRecord, Long> {

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
    @Query("SELECT rr.audienceId, rr.audienceName, SUM(rr.rechargeAmount) as total, COUNT(rr.recordId) " +
            "FROM RechargeRecord rr " +
            "WHERE rr.anchorId = :anchorId " +
            "AND rr.rechargeTime >= :startTime " +
            "AND rr.rechargeTime < :endTime " +
            "GROUP BY rr.audienceId, rr.audienceName " +
            "ORDER BY total DESC")
    List<Object[]> findTopAudiencesByAnchorAndTimeRange(@Param("anchorId") Long anchorId,
                                                         @Param("startTime") LocalDateTime startTime,
                                                         @Param("endTime") LocalDateTime endTime,
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
    List<Object[]> getHourlyStatistics(@Param("anchorId") Long anchorId,
                                        @Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime);

    /**
     * 批量更新结算状态
     */
    @Modifying
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
    @Query("SELECT r.anchorId, r.anchorName, SUM(r.rechargeAmount), COUNT(r.recordId) " +
           "FROM RechargeRecord r " +
           "WHERE r.rechargeTime BETWEEN :startTime AND :endTime " +
           "GROUP BY r.anchorId, r.anchorName " +
           "ORDER BY SUM(r.rechargeAmount) DESC")
    List<Object[]> getTopAnchorsByRevenue(@Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime,
                                           Pageable pageable);

    /**
     * 查询观众消费TOP榜
     */
    @Query("SELECT r.audienceId, r.audienceName, SUM(r.rechargeAmount), COUNT(r.recordId), " +
           "COUNT(DISTINCT r.anchorId), MIN(r.rechargeTime), MAX(r.rechargeTime) " +
           "FROM RechargeRecord r " +
           "WHERE r.rechargeTime BETWEEN :startTime AND :endTime " +
           "GROUP BY r.audienceId, r.audienceName " +
           "ORDER BY SUM(r.rechargeAmount) DESC")
    List<Object[]> getTopAudiencesByConsumption(@Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime,
                                                 Pageable pageable);

    /**
     * 查询时间热力图数据（按星期和小时）
     */
    @Query("SELECT FUNCTION('DAYOFWEEK', r.rechargeTime), FUNCTION('HOUR', r.rechargeTime), " +
           "COUNT(r), SUM(r.rechargeAmount), COUNT(DISTINCT r.audienceId) " +
           "FROM RechargeRecord r " +
           "WHERE r.rechargeTime BETWEEN :startTime AND :endTime " +
           "GROUP BY FUNCTION('DAYOFWEEK', r.rechargeTime), FUNCTION('HOUR', r.rechargeTime)")
    List<Object[]> getTimeHeatmapData(@Param("startTime") LocalDateTime startTime,
                                       @Param("endTime") LocalDateTime endTime);

    /**
     * 查询观众历史打赏记录
     */
    @Query("SELECT r FROM RechargeRecord r WHERE r.audienceId = :audienceId ORDER BY r.rechargeTime DESC")
    List<RechargeRecord> findByAudienceId(@Param("audienceId") Long audienceId);

    /**
     * 查询主播每日收入统计
     */
    @Query("SELECT FUNCTION('DATE', r.rechargeTime), COUNT(r), SUM(r.rechargeAmount), " +
           "AVG(r.rechargeAmount), MAX(r.rechargeAmount), MIN(r.rechargeAmount), " +
           "COUNT(DISTINCT r.audienceId) " +
           "FROM RechargeRecord r " +
           "WHERE r.anchorId = :anchorId " +
           "AND r.rechargeTime BETWEEN :startTime AND :endTime " +
           "GROUP BY FUNCTION('DATE', r.rechargeTime) " +
           "ORDER BY FUNCTION('DATE', r.rechargeTime)")
    List<Object[]> getAnchorDailyStats(@Param("anchorId") Long anchorId,
                                        @Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime);

    /**
     * 统计平台GMV
     */
    @Query("SELECT COALESCE(SUM(r.rechargeAmount), 0) FROM RechargeRecord r " +
           "WHERE r.rechargeTime BETWEEN :startTime AND :endTime")
    BigDecimal calculateGMV(@Param("startTime") LocalDateTime startTime,
                             @Param("endTime") LocalDateTime endTime);

    /**
     * 查询所有记录按时间范围
     */
    @Query("SELECT r FROM RechargeRecord r " +
           "WHERE r.rechargeTime BETWEEN :startTime AND :endTime " +
           "ORDER BY r.rechargeTime")
    List<RechargeRecord> findAllByTimeRange(@Param("startTime") LocalDateTime startTime,
                                             @Param("endTime") LocalDateTime endTime);
}

