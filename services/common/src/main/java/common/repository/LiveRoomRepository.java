package common.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import common.bean.liveroom.LiveRoom;

import java.util.List;
import java.util.Optional;

/**
 * 直播间Repository接口
 */
public interface LiveRoomRepository extends BaseRepository<LiveRoom, Long> {

    /**
     * 按主播ID查询
     */
    @Query("SELECT lr FROM LiveRoom lr WHERE lr.anchorId = :anchorId")
    Optional<LiveRoom> findByAnchorId(@Param("anchorId") Long anchorId);

    /**
     * 查询正在直播的直播间
     */
    @Query("SELECT lr FROM LiveRoom lr WHERE lr.roomStatus = 1 ORDER BY lr.totalViewers DESC")
    List<LiveRoom> findLiveRooms();

    /**
     * 查询按观众数排行的直播间（TOP N）
     */
    @Query(value = "SELECT * FROM live_room WHERE room_status = 1 ORDER BY total_viewers DESC LIMIT :limit", nativeQuery = true)
    List<LiveRoom> findTopByViewers(@Param("limit") int limit);

    /**
     * 按分类查询直播间
     */
    @Query("SELECT lr FROM LiveRoom lr WHERE lr.category = :category")
    List<LiveRoom> findByCategory(@Param("category") String category);

    /**
     * 查询按收益排行的直播间（返回对象数组）
     */
    @Query(value = "SELECT lr.live_room_id, lr.anchor_id, lr.room_name, lr.total_earnings, lr.total_viewers " +
                   "FROM live_room lr WHERE lr.room_status = 1 " +
                   "ORDER BY lr.total_earnings DESC LIMIT :limit", 
           nativeQuery = true)
    List<Object[]> findTopByEarnings(@Param("limit") int limit);

    /**
     * 查询按观众数排行的直播间（返回对象数组）
     */
    @Query(value = "SELECT lr.live_room_id, lr.anchor_id, lr.room_name, lr.total_viewers, lr.max_viewers " +
                   "FROM live_room lr WHERE lr.room_status = 1 " +
                   "ORDER BY lr.total_viewers DESC LIMIT :limit", 
           nativeQuery = true)
    List<Object[]> findTopByViewersCount(@Param("limit") int limit);

    /**
     * 获取所有分类
     */
    @Query(value = "SELECT DISTINCT category FROM live_room WHERE category IS NOT NULL", nativeQuery = true)
    List<String> findAllCategories();

    /**
     * 按分类统计
     */
    @Query(value = "SELECT category, COUNT(*) as count FROM live_room GROUP BY category", nativeQuery = true)
    List<Object[]> countByCategory();

    /**
     * 按状态统计
     */
    @Query(value = "SELECT room_status, COUNT(*) as count FROM live_room GROUP BY room_status", nativeQuery = true)
    List<Object[]> countByStatus();

    /**
     * 按状态查询直播间
     */
    @Query("SELECT lr FROM LiveRoom lr WHERE lr.roomStatus = :status")
    List<LiveRoom> findByStatus(@Param("status") Integer status);

    /**
     * 按名称查询
     */
    @Query("SELECT lr FROM LiveRoom lr WHERE lr.roomName LIKE %:roomName%")
    List<LiveRoom> findByRoomNameContaining(@Param("roomName") String roomName);

    /**
     * 检查主播是否有直播间
     */
    @Query("SELECT COUNT(lr) > 0 FROM LiveRoom lr WHERE lr.anchorId = :anchorId")
    boolean existsByAnchorId(@Param("anchorId") Long anchorId);
}
