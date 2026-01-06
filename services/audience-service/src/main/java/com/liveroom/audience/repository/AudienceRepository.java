package com.liveroom.audience.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import common.bean.user.Audience;

/**
 * 观众数据访问层接口
 * 提供观众信息的CRUD操作和复杂查询
 */
@Repository
public interface AudienceRepository extends JpaRepository<Audience, Long> {

    /**
     * 按昵称查询观众
     */
    Optional<Audience> findByNickname(String nickname);

    /**
     * 按消费等级分页查询
     */
    Page<Audience> findByConsumptionLevel(Integer consumptionLevel, Pageable pageable);

    /**
     * 按粉丝等级分页查询
     */
    Page<Audience> findByVipLevel(Integer vipLevel, Pageable pageable);

    /**
     * 按状态查询
     */
    List<Audience> findByStatus(Integer status);

    /**
     * 查询所有启用的观众
     */
    @Query("SELECT a FROM Audience a WHERE a.status = 0")
    List<Audience> findAllEnabled();

    /**
     * 按昵称模糊查询
     */
    @Query("SELECT a FROM Audience a WHERE a.nickname LIKE %:keyword% ORDER BY a.totalRechargeAmount DESC")
    Page<Audience> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 查询消费金额最高的观众（用于分层分析）
     */
    @Query("SELECT a FROM Audience a WHERE a.totalRechargeAmount > :amount ORDER BY a.totalRechargeAmount DESC")
    List<Audience> findTopConsumers(@Param("amount") java.math.BigDecimal amount, Pageable pageable);

    /**
     * 查询最后打赏时间在指定范围内的观众
     */
    @Query("SELECT a FROM Audience a WHERE a.lastRechargeTime BETWEEN :startTime AND :endTime ORDER BY a.lastRechargeTime DESC")
    List<Audience> findByRechargeTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
