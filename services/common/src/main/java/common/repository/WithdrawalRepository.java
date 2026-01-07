package common.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import common.bean.Withdrawal;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 提现Repository接口
 */
public interface WithdrawalRepository extends BaseRepository<Withdrawal, Long> {

    /**
     * 按traceId查询（幂等性控制）
     */
    Optional<Withdrawal> findByTraceId(String traceId);

    /**
     * 按主播ID查询所有提现记录
     */
    List<Withdrawal> findByAnchorId(Long anchorId);

    /**
     * 按状态查询提现记录
     */
    List<Withdrawal> findByStatus(Integer status);

    /**
     * 查询待处理的提现记录
     */
    @Query("SELECT w FROM Withdrawal w WHERE w.status IN (0, 1)")
    List<Withdrawal> findPendingWithdrawals();

    /**
     * 查询指定时间范围内的提现记录
     */
    @Query("SELECT w FROM Withdrawal w WHERE w.appliedTime BETWEEN :startTime AND :endTime")
    List<Withdrawal> findByTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 计算主播的总提现金额
     */
    @Query("SELECT COALESCE(SUM(w.withdrawalAmount), 0) FROM Withdrawal w " +
            "WHERE w.anchorId = :anchorId AND w.status IN (2)")
    BigDecimal sumSuccessfulWithdrawalsByAnchor(@Param("anchorId") Long anchorId);

    /**
     * 查询主播申请中的提现总额
     */
    @Query("SELECT COALESCE(SUM(w.withdrawalAmount), 0) FROM Withdrawal w " +
            "WHERE w.anchorId = :anchorId AND w.status IN (0, 1)")
    BigDecimal sumPendingWithdrawalsByAnchor(@Param("anchorId") Long anchorId);

    /**
     * 检查traceId是否存在（防重复）
     */
    boolean existsByTraceId(String traceId);

    /**
     * 批量查询主播的提现记录
     */
    @Query("SELECT w FROM Withdrawal w WHERE w.anchorId IN :anchorIds ORDER BY w.appliedTime DESC")
    List<Withdrawal> findByAnchorIds(@Param("anchorIds") List<Long> anchorIds);

    /**
     * 查询指定状态和时间范围内的提现
     */
    @Query("SELECT w FROM Withdrawal w WHERE w.status = :status AND w.appliedTime BETWEEN :startTime AND :endTime")
    List<Withdrawal> findByStatusAndTimeRange(
            @Param("status") Integer status,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 按主播ID和状态查询提现记录（分页）
     */
    @Query("SELECT w FROM Withdrawal w WHERE w.anchorId = :anchorId " +
            "AND (:status IS NULL OR w.status = :status) " +
            "ORDER BY w.appliedTime DESC")
    Page<Withdrawal> findByAnchorIdAndStatus(
            @Param("anchorId") Long anchorId,
            @Param("status") Integer status,
            Pageable pageable
    );

    /**
     * 按主播ID查询提现记录（分页，按申请时间倒序）
     */
    Page<Withdrawal> findByAnchorIdOrderByAppliedTimeDesc(Long anchorId, Pageable pageable);

    /**
     * 统计主播的提现总额（仅已完成的）
     */
    @Query("SELECT COALESCE(SUM(w.withdrawalAmount), 0) FROM Withdrawal w " +
            "WHERE w.anchorId = :anchorId AND w.status = 2")
    Double sumWithdrawnAmountByAnchorId(@Param("anchorId") Long anchorId);
}