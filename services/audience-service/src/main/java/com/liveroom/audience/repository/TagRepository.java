package com.liveroom.audience.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import common.bean.Tag;

/**
 * 标签数据访问层接口
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    /**
     * 按标签名称查询
     */
    Tag findByTagName(String tagName);

    /**
     * 按分类查询所有标签
     */
    List<Tag> findByCategory(String category);

    /**
     * 查询所有预设标签
     */
    @Query("SELECT t FROM Tag t WHERE t.tagType = 0 ORDER BY t.tagName")
    List<Tag> findAllPresetTags();

    /**
     * 查询观众的所有标签
     */
    @Query("SELECT t FROM Tag t INNER JOIN audience_tag at ON t.tag_id = at.tag_id " +
           "WHERE at.audience_id = :audienceId")
    List<Tag> findByAudienceId(@Param("audienceId") Long audienceId);
}
