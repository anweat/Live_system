package common.repository;

import common.bean.liveroom.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 弹幕消息数据访问层
 * 提供消息的查询和保存功能
 * 
 * @author Team
 * @version 1.0.0
 */
@Repository
public interface MessageRepository extends BaseRepository<Message, Long> {

    /**
     * 查询指定直播间的所有弹幕
     */
    @Query("SELECT m FROM Message m WHERE m.liveRoom.liveRoomId = :liveRoomId ORDER BY m.createTime DESC")
    Page<Message> findByLiveRoomId(@Param("liveRoomId") Long liveRoomId, Pageable pageable);

    /**
     * 查询指定观众发送的所有弹幕
     */
    @Query("SELECT m FROM Message m WHERE m.sender.userId = :audienceId ORDER BY m.createTime DESC")
    Page<Message> findByAudienceId(@Param("audienceId") Long audienceId, Pageable pageable);

    /**
     * 查询指定时间范围内的弹幕
     */
    @Query("SELECT m FROM Message m WHERE m.liveRoom.liveRoomId = :liveRoomId " +
           "AND m.createTime BETWEEN :startTime AND :endTime ORDER BY m.createTime DESC")
    List<Message> findByLiveRoomIdAndTimeRange(
        @Param("liveRoomId") Long liveRoomId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * 统计指定直播间的弹幕总数
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.liveRoom.liveRoomId = :liveRoomId")
    Long countByLiveRoomId(@Param("liveRoomId") Long liveRoomId);

    /**
     * 统计指定观众发送的弹幕总数
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.sender.userId = :audienceId")
    Long countByAudienceId(@Param("audienceId") Long audienceId);
}
