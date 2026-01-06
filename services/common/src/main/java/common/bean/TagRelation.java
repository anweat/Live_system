package common.bean;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;

/**
 * 标签关联度表实体
 * 记录标签之间的关联关系和权重
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tag_relation", indexes = {
        @Index(name = "idx_tag_id1", columnList = "tag_id1"),
        @Index(name = "idx_tag_id2", columnList = "tag_id2")
})
public class TagRelation implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long relationId;

    /** 标签1 ID */
    @Column(nullable = false)
    private Long tagId1;

    /** 标签1 名称 */
    @Column(nullable = false, length = 64)
    private String tagName1;

    /** 标签2 ID */
    @Column(nullable = false)
    private Long tagId2;

    /** 标签2 名称 */
    @Column(nullable = false, length = 64)
    private String tagName2;

    /** 关联度分数 (0-100) */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal relationScore;

    /** 关联类型：0-推荐关联、1-互斥关联、2-强相关 */
    @Column(nullable = false)
    private Integer relationType;

    /** 共同出现次数 */
    @Column(nullable = false)
    @Builder.Default
    private Long cooccurrenceCount = 0L;

    /** 关联强度等级：1-弱、2-中、3-强 */
    @Column(nullable = false)
    private Integer strengthLevel;

    /** 创建时间 */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;

    /** 更新时间 */
    @Column(nullable = false)
    private LocalDateTime updateTime;
}
