package com.liveroom.mock.repository;

import com.liveroom.mock.entity.MockLiveRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Mock直播间Repository
 */
@Repository
public interface MockLiveRoomRepository extends JpaRepository<MockLiveRoom, Long> {

    /**
     * 查询直播中的房间
     */
    List<MockLiveRoom> findByStatus(Integer status);

    /**
     * 分页查询直播中的房间
     */
    Page<MockLiveRoom> findByStatus(Integer status, Pageable pageable);

    /**
     * 根据主播ID查询直播间
     */
    List<MockLiveRoom> findByAnchorId(Long anchorId);

    /**
     * 根据标签查询直播间
     */
    @Query("SELECT r FROM MockLiveRoom r WHERE r.tags LIKE CONCAT('%', :tag, '%')")
    List<MockLiveRoom> findByTag(@Param("tag") String tag);

    /**
     * 查询当前观众数小于指定值的直播间
     */
    @Query("SELECT r FROM MockLiveRoom r WHERE r.status = 1 AND r.currentAudienceCount < :maxCount")
    List<MockLiveRoom> findLiveRoomsWithSpace(@Param("maxCount") Integer maxCount);

    /**
     * 统计直播中的房间数
     */
    @Query("SELECT COUNT(r) FROM MockLiveRoom r WHERE r.status = 1")
    long countLiveRooms();

    /**
     * 查询随机直播间
     */
    @Query(value = "SELECT * FROM mock_live_room WHERE status = 1 ORDER BY RAND() LIMIT :limit", 
           nativeQuery = true)
    List<MockLiveRoom> findRandomLiveRooms(@Param("limit") int limit);
}
