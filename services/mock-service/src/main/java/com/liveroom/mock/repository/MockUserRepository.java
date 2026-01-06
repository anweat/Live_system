package com.liveroom.mock.repository;

import com.liveroom.mock.entity.MockUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Mock用户Repository
 */
@Repository
public interface MockUserRepository extends JpaRepository<MockUser, Long> {

    /**
     * 查询所有Bot用户
     */
    List<MockUser> findByIsBotTrue();

    /**
     * 分页查询Bot用户
     */
    Page<MockUser> findByIsBotTrue(Pageable pageable);

    /**
     * 根据消费等级查询Bot
     */
    List<MockUser> findByIsBotTrueAndConsumptionLevel(Integer consumptionLevel);

    /**
     * 根据性别查询Bot
     */
    List<MockUser> findByIsBotTrueAndGender(Integer gender);

    /**
     * 根据标签查询Bot（支持JSON LIKE查询）
     */
    @Query("SELECT u FROM MockUser u WHERE u.isBot = true AND u.tags LIKE CONCAT('%', :tag, '%')")
    List<MockUser> findBotsByTag(@Param("tag") String tag);

    /**
     * 根据多个标签查询Bot（任意匹配）
     */
    @Query("SELECT u FROM MockUser u WHERE u.isBot = true AND (" +
           "u.tags LIKE CONCAT('%', :tag1, '%') OR " +
           "u.tags LIKE CONCAT('%', :tag2, '%') OR " +
           "u.tags LIKE CONCAT('%', :tag3, '%'))")
    List<MockUser> findBotsByAnyTags(@Param("tag1") String tag1, 
                                      @Param("tag2") String tag2, 
                                      @Param("tag3") String tag3);

    /**
     * 查询指定数量的随机Bot
     */
    @Query(value = "SELECT * FROM mock_user WHERE is_bot = 1 ORDER BY RAND() LIMIT :limit", 
           nativeQuery = true)
    List<MockUser> findRandomBots(@Param("limit") int limit);

    /**
     * 根据标签和消费等级查询Bot
     */
    @Query("SELECT u FROM MockUser u WHERE u.isBot = true AND u.consumptionLevel = :level " +
           "AND u.tags LIKE CONCAT('%', :tag, '%')")
    List<MockUser> findBotsByTagAndLevel(@Param("tag") String tag, @Param("level") Integer level);

    /**
     * 统计Bot总数
     */
    @Query("SELECT COUNT(u) FROM MockUser u WHERE u.isBot = true")
    long countBots();

    /**
     * 按消费等级统计Bot数量
     */
    @Query("SELECT u.consumptionLevel, COUNT(u) FROM MockUser u WHERE u.isBot = true GROUP BY u.consumptionLevel")
    List<Object[]> countBotsByConsumptionLevel();

    /**
     * 查询启用的Bot
     */
    List<MockUser> findByIsBotTrueAndStatus(Integer status);
}
