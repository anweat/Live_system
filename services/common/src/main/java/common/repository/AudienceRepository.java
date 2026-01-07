package common.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import common.bean.user.Audience;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;


/**
 * 观众Repository接口
 */
public interface AudienceRepository extends BaseRepository<Audience, Long> {

    /**
     * 按观众ID查询
     */
    Optional<Audience> findByUserId(Long userId);

    /**
     * 按昵称查询观众
     */
    Optional<Audience> findByNickname(String nickname);

    /**
     * 按消费等级查询观众
     */
    List<Audience> findByConsumptionLevel(Integer level);

    /**
     * 按消费等级分页查询观众
     */
    Page<Audience> findByConsumptionLevel(Integer level, Pageable pageable);

    /**
     * 按关键词搜索观众（分页）
     */
    @Query("SELECT a FROM Audience a WHERE a.nickname LIKE %:keyword% OR a.realName LIKE %:keyword%")
    Page<Audience> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 按粉丝等级查询观众
     */
    List<Audience> findByVipLevel(Integer level);

    /**
     * 查询高消费观众（前20%）
     */
    @Query(value = "SELECT * FROM audience WHERE consumption_level = 2", nativeQuery = true)
    List<Audience> findHighValueAudience();

    /**
     * 查询消费金额大于指定值的观众
     */
    List<Audience> findByTotalRechargeAmountGreaterThan(java.math.BigDecimal amount);

    /**
     * 查询消费次数大于指定值的观众
     */
    List<Audience> findByTotalRechargeCountGreaterThan(Long count);

    /**
     * 按打赏金额排序查询top N观众
     */
    @Query(value = "SELECT * FROM audience ORDER BY total_recharge_amount DESC LIMIT :limit", nativeQuery = true)
    List<Audience> findTopAudienceBySpending(@Param("limit") int limit);

    /**
     * 检查用户是否是观众
     */
    boolean existsByUserId(Long userId);

    /**
     * 按创建日期查询观众
     */
    @Query("SELECT a FROM Audience a WHERE DATE(a.createTime) = :date")
    List<Audience> findByCreatedDate(@Param("date") LocalDate date);

    /**
     * 按创建日期范围查询观众
     */
    @Query("SELECT a FROM Audience a WHERE DATE(a.createTime) BETWEEN :startDate AND :endDate")
    List<Audience> findByCreatedDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
