package common.repository;

import common.bean.liveroom.LiveRoomRealtime;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * 直播间实时数据访问层
 * 提供直播间实时数据的增量更新功能
 * 
 * @author Team
 * @version 1.0.0
 */
@Repository
public interface LiveRoomRealtimeRepository extends BaseRepository<LiveRoomRealtime, Long> {

    /**
     * 根据直播间ID查询实时数据
     */
    @Query("SELECT r FROM LiveRoomRealtime r WHERE r.liveRoomId = :liveRoomId")
    Optional<LiveRoomRealtime> findByLiveRoomId(@Param("liveRoomId") Long liveRoomId);

    /**
     * 增加弹幕计数
     */
    @Modifying
    @Query("UPDATE LiveRoomRealtime r SET r.messageCount = r.messageCount + :delta, " +
           "r.lastUpdateTime = CURRENT_TIMESTAMP WHERE r.liveRoomId = :liveRoomId")
    int incrementMessageCount(@Param("liveRoomId") Long liveRoomId, @Param("delta") long delta);

    /**
     * 增加打赏计数
     */
    @Modifying
    @Query("UPDATE LiveRoomRealtime r SET r.rechargeCount = r.rechargeCount + :delta, " +
           "r.lastUpdateTime = CURRENT_TIMESTAMP WHERE r.liveRoomId = :liveRoomId")
    int incrementRechargeCount(@Param("liveRoomId") Long liveRoomId, @Param("delta") long delta);

    /**
     * 增加当前场次收益
     */
    @Modifying
    @Query("UPDATE LiveRoomRealtime r SET r.currentRevenueAmount = r.currentRevenueAmount + :delta, " +
           "r.lastUpdateTime = CURRENT_TIMESTAMP WHERE r.liveRoomId = :liveRoomId")
    int incrementCurrentRevenue(@Param("liveRoomId") Long liveRoomId, @Param("delta") BigDecimal delta);

    /**
     * 更新当前观众数
     */
    @Modifying
    @Query("UPDATE LiveRoomRealtime r SET r.currentViewerCount = :count, " +
           "r.lastUpdateTime = CURRENT_TIMESTAMP WHERE r.liveRoomId = :liveRoomId")
    int updateCurrentViewerCount(@Param("liveRoomId") Long liveRoomId, @Param("count") int count);

    /**
     * 重置当前场次数据（开播时调用）
     */
    @Modifying
    @Query("UPDATE LiveRoomRealtime r SET r.currentViewerCount = 0, " +
           "r.currentRevenueAmount = 0, r.messageCount = 0, r.rechargeCount = 0, " +
           "r.lastUpdateTime = CURRENT_TIMESTAMP WHERE r.liveRoomId = :liveRoomId")
    int resetRealtimeData(@Param("liveRoomId") Long liveRoomId);
}
