package common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import common.bean.CommissionRate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 分成比例Repository接口
 */
public interface CommissionRateRepository extends BaseRepository<CommissionRate, Long> {

    /**
     * 按主播ID查询最新的分成比例
     */
    @Query("SELECT c FROM CommissionRate c WHERE c.anchorId = :anchorId " +
           "AND c.status = 1 AND c.effectiveTime <= :now AND " +
           "(c.expireTime IS NULL OR c.expireTime > :now) " +
           "ORDER BY c.effectiveTime DESC LIMIT 1")
    Optional<CommissionRate> findCurrentRateByAnchor(
        @Param("anchorId") Long anchorId,
        @Param("now") LocalDateTime now
    );

    /**
     * 按主播ID查询所有分成比例历史
     */
    List<CommissionRate> findByAnchorIdOrderByEffectiveTimeDesc(Long anchorId);

    /**
     * 按状态查询分成比例
     */
    List<CommissionRate> findByStatus(Integer status);

    /**
     * 查询待启用的分成比例
     */
    @Query("SELECT c FROM CommissionRate c WHERE c.status = 0 ORDER BY c.effectiveTime")
    List<CommissionRate> findPendingRates();

    /**
     * 查询已过期的分成比例
     */
    @Query("SELECT c FROM CommissionRate c WHERE c.status = 2")
    List<CommissionRate> findExpiredRates();

    /**
     * 检查主播是否有启用的分成比例
     */
    @Query("SELECT COUNT(c) > 0 FROM CommissionRate c WHERE c.anchorId = :anchorId AND c.status = 1")
    boolean hasActiveRate(@Param("anchorId") Long anchorId);

    /**
     * 批量查询主播的最新分成比例
     */
    @Query("SELECT c FROM CommissionRate c WHERE c.anchorId IN :anchorIds AND c.status = 1 " +
           "ORDER BY c.anchorId, c.effectiveTime DESC")
    List<CommissionRate> findLatestRatesByAnchors(@Param("anchorIds") List<Long> anchorIds);
}
