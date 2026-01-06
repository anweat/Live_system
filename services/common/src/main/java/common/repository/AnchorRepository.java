package common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import common.bean.user.Anchor;

import java.util.List;
import java.util.Optional;

/**
 * 主播Repository接口
 */
public interface AnchorRepository extends BaseRepository<Anchor, Long> {

    /**
     * 按主播ID查询
     */
    Optional<Anchor> findByUserId(Long userId);

    /**
     * 按直播间ID查询
     */
    Optional<Anchor> findByLiveRoomId(Long liveRoomId);

    /**
     * 按粉丝数排序查询top N主播
     */
    @Query(value = "SELECT * FROM anchor ORDER BY fan_count DESC LIMIT :limit", nativeQuery = true)
    List<Anchor> findTopAnchorsByFans(@Param("limit") int limit);

    /**
     * 按收益排序查询top N主播
     */
    @Query(value = "SELECT * FROM anchor ORDER BY total_earnings DESC LIMIT :limit", nativeQuery = true)
    List<Anchor> findTopAnchorsByEarnings(@Param("limit") int limit);

    /**
     * 查询粉丝数大于指定值的主播
     */
    List<Anchor> findByFanCountGreaterThan(Long minFans);

    /**
     * 查询收益大于指定值的主播
     */
    List<Anchor> findByTotalEarningsGreaterThan(java.math.BigDecimal minEarnings);

    /**
     * 检查用户是否是主播
     */
    boolean existsByUserId(Long userId);
}
