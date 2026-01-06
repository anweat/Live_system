package common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import common.bean.Settlement;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 结算Repository接口
 */
public interface SettlementRepository extends BaseRepository<Settlement, Long> {

    /**
     * 按主播ID查询（一一对应）
     */
    Optional<Settlement> findByAnchorId(Long anchorId);

    /**
     * 按结算状态查询
     */
    List<Settlement> findByStatus(Integer status);

    /**
     * 查询可提取金额大于0的结算记录
     */
    @Query("SELECT s FROM Settlement s WHERE s.availableAmount > 0")
    List<Settlement> findSettlementsWithAvailableAmount();

    /**
     * 查询冻结状态的结算记录
     */
    @Query("SELECT s FROM Settlement s WHERE s.status = 1")
    List<Settlement> findFrozenSettlements();

    /**
     * 按下次结算时间查询待结算的记录
     */
    @Query("SELECT s FROM Settlement s WHERE s.nextSettlementTime <= :now AND s.status = 0")
    List<Settlement> findPendingSettlements(@Param("now") LocalDateTime now);

    /**
     * 计算某个主播的总可提取金额
     */
    @Query("SELECT COALESCE(SUM(s.availableAmount), 0) FROM Settlement s WHERE s.anchorId = :anchorId")
    BigDecimal sumAvailableAmountByAnchor(@Param("anchorId") Long anchorId);

    /**
     * 查询可提取金额大于指定值的结算记录
     */
    List<Settlement> findByAvailableAmountGreaterThan(BigDecimal amount);

    /**
     * 检查主播是否存在结算记录
     */
    boolean existsByAnchorId(Long anchorId);

    /**
     * 批量查询主播的结算信息
     */
    @Query("SELECT s FROM Settlement s WHERE s.anchorId IN :anchorIds")
    List<Settlement> findByAnchorIds(@Param("anchorIds") List<Long> anchorIds);
}
