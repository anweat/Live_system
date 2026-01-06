package com.liveroom.anchor.repository;

import common.bean.liveroom.LiveRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 直播间数据访问层
 * 提供直播间信息的CRUD操作和统计查询
 * 
 * @author Team
 * @version 1.0.0
 */
@Repository
public interface LiveRoomRepository extends JpaRepository<LiveRoom, Long> {

    /**
     * 根据主播ID查询直播间（一对一关系）
     */
    Optional<LiveRoom> findByAnchorId(Long anchorId);

    /**
     * 根据直播间名称查询（用于检查名称唯一性）
     */
    Optional<LiveRoom> findByRoomName(String roomName);

    /**
     * 根据直播间状态查询
     * @param roomStatus 状态：0-未开播、1-直播中、2-直播结束、3-被封禁
     */
    Page<LiveRoom> findByRoomStatus(Integer roomStatus, Pageable pageable);

    /**
     * 查询所有正在直播的直播间
     */
    @Query("SELECT lr FROM LiveRoom lr WHERE lr.roomStatus = 1")
    List<LiveRoom> findAllLiveRooms();

    /**
     * 按分类查询直播间列表
     */
    Page<LiveRoom> findByCategory(String category, Pageable pageable);

    /**
     * 按分类和状态查询直播间
     */
    @Query("SELECT lr FROM LiveRoom lr WHERE lr.category = :category AND lr.roomStatus = :status")
    Page<LiveRoom> findByCategoryAndStatus(@Param("category") String category, 
                                           @Param("status") Integer status, 
                                           Pageable pageable);

    /**
     * 查询累计观看人次TOP榜
     */
    @Query("SELECT lr FROM LiveRoom lr WHERE lr.roomStatus IN (1, 2) ORDER BY lr.totalViewers DESC")
    List<LiveRoom> findTopRoomsByViewers(Pageable pageable);

    /**
     * 查询本次营收TOP榜
     */
    @Query("SELECT lr FROM LiveRoom lr WHERE lr.roomStatus = 1 ORDER BY lr.totalEarnings DESC")
    List<LiveRoom> findTopRoomsByEarnings(Pageable pageable);

    /**
     * 更新直播间状态
     */
    @Modifying
    @Query("UPDATE LiveRoom lr SET lr.roomStatus = :status, lr.updateTime = CURRENT_TIMESTAMP WHERE lr.liveRoomId = :roomId")
    int updateRoomStatus(@Param("roomId") Long roomId, @Param("status") Integer status);

    /**
     * 开始直播（更新状态和开始时间）
     */
    @Modifying
    @Query("UPDATE LiveRoom lr SET lr.roomStatus = 1, lr.startTime = :startTime, " +
           "lr.totalEarnings = 0, lr.updateTime = CURRENT_TIMESTAMP WHERE lr.liveRoomId = :roomId")
    int startLive(@Param("roomId") Long roomId, @Param("startTime") LocalDateTime startTime);

    /**
     * 结束直播（更新状态和结束时间）
     */
    @Modifying
    @Query("UPDATE LiveRoom lr SET lr.roomStatus = 2, lr.endTime = :endTime, " +
           "lr.updateTime = CURRENT_TIMESTAMP WHERE lr.liveRoomId = :roomId")
    int endLive(@Param("roomId") Long roomId, @Param("endTime") LocalDateTime endTime);

    /**
     * 更新累计观看人次（增量更新）
     */
    @Modifying
    @Query("UPDATE LiveRoom lr SET lr.totalViewers = lr.totalViewers + :delta, " +
           "lr.updateTime = CURRENT_TIMESTAMP WHERE lr.liveRoomId = :roomId")
    int updateTotalViewers(@Param("roomId") Long roomId, @Param("delta") Long delta);

    /**
     * 更新本次总营收（增量更新）
     */
    @Modifying
    @Query("UPDATE LiveRoom lr SET lr.totalEarnings = lr.totalEarnings + :amount, " +
           "lr.updateTime = CURRENT_TIMESTAMP WHERE lr.liveRoomId = :roomId")
    int updateTotalEarnings(@Param("roomId") Long roomId, @Param("amount") BigDecimal amount);

    /**
     * 统计正在直播的直播间数量
     */
    @Query("SELECT COUNT(lr) FROM LiveRoom lr WHERE lr.roomStatus = 1")
    long countLiveRooms();

    /**
     * 按分类统计直播间数量
     */
    @Query("SELECT COUNT(lr) FROM LiveRoom lr WHERE lr.category = :category")
    long countByCategory(@Param("category") String category);

    /**
     * 统计指定时间范围内的直播间数量
     */
    @Query("SELECT COUNT(lr) FROM LiveRoom lr WHERE lr.startTime BETWEEN :startTime AND :endTime")
    long countByTimeRange(@Param("startTime") LocalDateTime startTime, 
                         @Param("endTime") LocalDateTime endTime);
}
