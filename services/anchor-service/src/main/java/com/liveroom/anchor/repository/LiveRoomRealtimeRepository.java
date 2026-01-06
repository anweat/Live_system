package com.liveroom.anchor.repository;

import common.bean.liveroom.LiveRoomRealtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 直播间实时数据Repository
 */
@Repository
public interface LiveRoomRealtimeRepository extends JpaRepository<LiveRoomRealtime, Long> {

    /**
     * 按直播间ID查询实时数据
     */
    Optional<LiveRoomRealtime> findByLiveRoomId(Long liveRoomId);

    /**
     * 递增当前观众数
     */
    @Modifying
    @Query("UPDATE LiveRoomRealtime r SET r.currentViewerCount = r.currentViewerCount + :delta, " +
           "r.version = r.version + 1 WHERE r.liveRoomId = :liveRoomId")
    int incrementCurrentViewerCount(@Param("liveRoomId") Long liveRoomId, 
                                    @Param("delta") int delta);

    /**
     * 递减当前观众数（不能小于0）
     */
    @Modifying
    @Query("UPDATE LiveRoomRealtime r SET r.currentViewerCount = " +
           "CASE WHEN r.currentViewerCount >= :delta THEN r.currentViewerCount - :delta ELSE 0 END, " +
           "r.version = r.version + 1 WHERE r.liveRoomId = :liveRoomId")
    int decrementCurrentViewerCount(@Param("liveRoomId") Long liveRoomId, 
                                    @Param("delta") int delta);

    /**
     * 递增弹幕数
     */
    @Modifying
    @Query("UPDATE LiveRoomRealtime r SET r.messageCount = r.messageCount + :delta, " +
           "r.version = r.version + 1 WHERE r.liveRoomId = :liveRoomId")
    int incrementMessageCount(@Param("liveRoomId") Long liveRoomId, 
                              @Param("delta") long delta);

    /**
     * 递增打赏数
     */
    @Modifying
    @Query("UPDATE LiveRoomRealtime r SET r.rechargeCount = r.rechargeCount + :delta, " +
           "r.version = r.version + 1 WHERE r.liveRoomId = :liveRoomId")
    int incrementRechargeCount(@Param("liveRoomId") Long liveRoomId, 
                               @Param("delta") long delta);

    /**
     * 更新当前场次收益
     */
    @Modifying
    @Query("UPDATE LiveRoomRealtime r SET r.currentRevenueAmount = r.currentRevenueAmount + :amount, " +
           "r.version = r.version + 1 WHERE r.liveRoomId = :liveRoomId")
    int incrementCurrentRevenue(@Param("liveRoomId") Long liveRoomId, 
                                @Param("amount") java.math.BigDecimal amount);

    /**
     * 重置当前场次数据（开播时调用）
     */
    @Modifying
    @Query("UPDATE LiveRoomRealtime r SET " +
           "r.currentViewerCount = 0, " +
           "r.currentRevenueAmount = 0, " +
           "r.messageCount = 0, " +
           "r.rechargeCount = 0, " +
           "r.onlineDuration = 0, " +
           "r.version = r.version + 1 " +
           "WHERE r.liveRoomId = :liveRoomId")
    int resetRealtimeData(@Param("liveRoomId") Long liveRoomId);

    /**
     * 更新在线时长
     */
    @Modifying
    @Query("UPDATE LiveRoomRealtime r SET r.onlineDuration = :duration, " +
           "r.version = r.version + 1 WHERE r.liveRoomId = :liveRoomId")
    int updateOnlineDuration(@Param("liveRoomId") Long liveRoomId, 
                            @Param("duration") long duration);
}
