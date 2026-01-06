package com.liveroom.anchor.repository;

import common.bean.liveroom.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 弹幕消息Repository
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * 按直播间ID查询弹幕消息（分页）
     */
    List<Message> findByLiveRoomIdOrderByCreateTimeDesc(Long liveRoomId);

    /**
     * 按直播间ID和时间范围查询弹幕消息
     */
    @Query("SELECT m FROM Message m WHERE m.liveRoomId = :liveRoomId " +
           "AND m.createTime BETWEEN :startTime AND :endTime " +
           "ORDER BY m.createTime DESC")
    List<Message> findByLiveRoomIdAndTimeRange(
            @Param("liveRoomId") Long liveRoomId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 按发送者ID查询弹幕消息
     */
    List<Message> findBySenderIdOrderByCreateTimeDesc(Long senderId);

    /**
     * 统计直播间弹幕总数
     */
    long countByLiveRoomId(Long liveRoomId);

    /**
     * 统计某时间段内的弹幕数
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.liveRoomId = :liveRoomId " +
           "AND m.createTime BETWEEN :startTime AND :endTime")
    long countByLiveRoomIdAndTimeRange(
            @Param("liveRoomId") Long liveRoomId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 查询最近N条弹幕（用于弹幕滚动展示）
     */
    @Query(value = "SELECT * FROM message WHERE live_room_id = :liveRoomId " +
                   "ORDER BY create_time DESC LIMIT :limit", nativeQuery = true)
    List<Message> findRecentMessages(@Param("liveRoomId") Long liveRoomId, 
                                     @Param("limit") int limit);

    /**
     * 删除指定时间之前的弹幕（定期清理）
     */
    void deleteByCreateTimeBefore(LocalDateTime time);
}
