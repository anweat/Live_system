package com.liveroom.anchor.repository;

import common.bean.user.Anchor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 主播数据访问层
 * 提供主播信息的CRUD操作和统计查询
 * 
 * @author Team
 * @version 1.0.0
 */
@Repository
public interface AnchorRepository extends JpaRepository<Anchor, Long> {

    /**
     * 根据昵称查询主播（用于检查昵称唯一性）
     */
    Optional<Anchor> findByNickname(String nickname);

    /**
     * 根据直播间ID查询主播
     */
    Optional<Anchor> findByLiveRoomId(Long liveRoomId);

    /**
     * 根据认证状态查询主播列表
     * @param verificationStatus 认证状态：0-未认证、1-已认证、2-认证中
     */
    Page<Anchor> findByVerificationStatus(Integer verificationStatus, Pageable pageable);

    /**
     * 根据主播等级查询主播列表
     * @param anchorLevel 主播等级：0-普通、1-优质、2-顶级
     */
    Page<Anchor> findByAnchorLevel(Integer anchorLevel, Pageable pageable);

    /**
     * 模糊搜索主播（按昵称）
     */
    @Query("SELECT a FROM Anchor a WHERE a.nickname LIKE %:keyword%")
    Page<Anchor> searchByNickname(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 查询所有启用状态的主播（用于定时任务统计）
     */
    @Query("SELECT a FROM Anchor a WHERE a.status = 1")
    List<Anchor> findAllEnabled();

    /**
     * 按粉丝数降序查询主播TOP榜
     */
    @Query("SELECT a FROM Anchor a WHERE a.status = 1 ORDER BY a.fanCount DESC")
    List<Anchor> findTopAnchorsByFanCount(Pageable pageable);

    /**
     * 按累计收益降序查询主播TOP榜
     */
    @Query("SELECT a FROM Anchor a WHERE a.status = 1 ORDER BY a.totalEarnings DESC")
    List<Anchor> findTopAnchorsByEarnings(Pageable pageable);

    /**
     * 更新主播粉丝数（增量更新）
     */
    @Modifying
    @Query("UPDATE Anchor a SET a.fanCount = a.fanCount + :delta, a.updateTime = CURRENT_TIMESTAMP WHERE a.userId = :anchorId")
    int updateFanCount(@Param("anchorId") Long anchorId, @Param("delta") Long delta);

    /**
     * 更新主播点赞数（增量更新）
     */
    @Modifying
    @Query("UPDATE Anchor a SET a.likeCount = a.likeCount + :delta, a.updateTime = CURRENT_TIMESTAMP WHERE a.userId = :anchorId")
    int updateLikeCount(@Param("anchorId") Long anchorId, @Param("delta") Long delta);

    /**
     * 更新主播累计收益（增量更新）
     */
    @Modifying
    @Query("UPDATE Anchor a SET a.totalEarnings = a.totalEarnings + :amount, a.updateTime = CURRENT_TIMESTAMP WHERE a.userId = :anchorId")
    int updateTotalEarnings(@Param("anchorId") Long anchorId, @Param("amount") BigDecimal amount);

    /**
     * 更新主播可提取余额（增量更新）
     */
    @Modifying
    @Query("UPDATE Anchor a SET a.availableAmount = a.availableAmount + :amount, a.updateTime = CURRENT_TIMESTAMP WHERE a.userId = :anchorId")
    int updateAvailableAmount(@Param("anchorId") Long anchorId, @Param("amount") BigDecimal amount);

    /**
     * 查询主播总数（统计）
     */
    @Query("SELECT COUNT(a) FROM Anchor a WHERE a.status = 1")
    long countEnabled();

    /**
     * 按认证状态统计主播数量
     */
    @Query("SELECT COUNT(a) FROM Anchor a WHERE a.verificationStatus = :status AND a.status = 1")
    long countByVerificationStatus(@Param("status") Integer status);
}
