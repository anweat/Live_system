package common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import common.bean.Recharge;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 打赏Repository接口 - 核心业务表
 */
public interface RechargeRepository extends BaseRepository<Recharge, Long> {

    /**
     * 按traceId查询（幂等性控制）
     */
    Optional<Recharge> findByTraceId(String traceId);

    /**
     * 按主播ID查询所有打赏记录
     */
    List<Recharge> findByAnchorId(Long anchorId);

    /**
     * 按观众ID查询所有打赏记录
     */
    List<Recharge> findByAudienceId(Long audienceId);

    /**
     * 按直播间ID查询所有打赏记录
     */
    List<Recharge> findByLiveRoomId(Long liveRoomId);

    /**
     * 按状态查询打赏记录
     */
    List<Recharge> findByStatus(Integer status);

    /**
     * 按结算ID查询打赏记录
     */
    List<Recharge> findBySettlementId(Long settlementId);

    /**
     * 查询指定时间范围内的打赏记录
     */
    @Query("SELECT r FROM Recharge r WHERE r.rechargeTime BETWEEN :startTime AND :endTime")
    List<Recharge> findByTimeRange(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * 查询指定主播在时间范围内的打赏总额
     */
    @Query("SELECT COALESCE(SUM(r.rechargeAmount), 0) FROM Recharge r " +
           "WHERE r.anchorId = :anchorId AND r.rechargeTime BETWEEN :startTime AND :endTime")
    BigDecimal sumRechargeAmountByAnchorAndTimeRange(
        @Param("anchorId") Long anchorId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * 查询指定观众的打赏总额
     */
    @Query("SELECT COALESCE(SUM(r.rechargeAmount), 0) FROM Recharge r WHERE r.audienceId = :audienceId")
    BigDecimal sumRechargeAmountByAudience(@Param("audienceId") Long audienceId);

    /**
     * 查询指定主播的打赏总额
     */
    @Query("SELECT COALESCE(SUM(r.rechargeAmount), 0) FROM Recharge r WHERE r.anchorId = :anchorId")
    BigDecimal sumRechargeAmountByAnchor(@Param("anchorId") Long anchorId);

    /**
     * 查询指定主播的打赏次数
     */
    @Query("SELECT COUNT(r) FROM Recharge r WHERE r.anchorId = :anchorId")
    Long countRechargeByAnchor(@Param("anchorId") Long anchorId);

    /**
     * 检查traceId是否存在（防重复）
     */
    boolean existsByTraceId(String traceId);

    /**
     * 批量查询未结算的打赏记录
     */
    @Query("SELECT r FROM Recharge r WHERE r.status IN (0, 1) AND r.anchorId IN :anchorIds")
    List<Recharge> findUnsettledRechargesByAnchors(@Param("anchorIds") List<Long> anchorIds);
}
