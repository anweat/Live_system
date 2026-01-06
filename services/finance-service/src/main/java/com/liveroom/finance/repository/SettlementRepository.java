package com.liveroom.finance.repository;

import common.bean.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

/**
 * 结算Repository
 */
@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    /**
     * 根据主播ID查询结算记录
     */
    Optional<Settlement> findByAnchorId(Long anchorId);

    /**
     * 根据主播ID查询结算记录（悲观锁）
     * 用于提现操作，防止并发问题
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Settlement s WHERE s.anchorId = :anchorId")
    Optional<Settlement> findByAnchorIdWithLock(@Param("anchorId") Long anchorId);

    /**
     * 查询所有正常状态的结算记录
     */
    List<Settlement> findByStatus(Integer status);

    /**
     * 检查主播是否存在结算记录
     */
    boolean existsByAnchorId(Long anchorId);
}
