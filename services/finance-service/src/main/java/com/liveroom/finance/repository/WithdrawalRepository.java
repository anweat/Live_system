package com.liveroom.finance.repository;

import common.bean.Withdrawal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 提现Repository
 */
@Repository
public interface WithdrawalRepository extends JpaRepository<Withdrawal, Long> {

    /**
     * 根据traceId查询提现记录（幂等性检查）
     */
    Optional<Withdrawal> findByTraceId(String traceId);

    /**
     * 查询主播的提现记录
     */
    Page<Withdrawal> findByAnchorIdOrderByAppliedTimeDesc(Long anchorId, Pageable pageable);

    /**
     * 查询主播指定状态的提现记录
     */
    @Query("SELECT w FROM Withdrawal w WHERE w.anchorId = :anchorId " +
            "AND (:status IS NULL OR w.status = :status) " +
            "ORDER BY w.appliedTime DESC")
    Page<Withdrawal> findByAnchorIdAndStatus(@Param("anchorId") Long anchorId,
                                              @Param("status") Integer status,
                                              Pageable pageable);

    /**
     * 查询指定状态的所有提现记录
     */
    List<Withdrawal> findByStatusOrderByAppliedTimeAsc(Integer status);

    /**
     * 统计主播的提现总额
     */
    @Query("SELECT COALESCE(SUM(w.withdrawalAmount), 0) FROM Withdrawal w " +
            "WHERE w.anchorId = :anchorId AND w.status = 2")
    Double sumWithdrawnAmountByAnchorId(@Param("anchorId") Long anchorId);

    /**
     * 检查traceId是否存在
     */
    boolean existsByTraceId(String traceId);
}
