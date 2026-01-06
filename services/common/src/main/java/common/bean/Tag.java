package common.bean;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;

/**
 * 标签表实体
 * 用于给主播、观众进行标签化分类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tag", indexes = {
        @Index(name = "idx_tag_name", columnList = "tag_name")
})
public class Tag implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tagId;

    /** 标签名称 */
    @Column(nullable = false, length = 100, unique = true)
    private String tagName;

    /** 标签描述 */
    @Column(length = 500)
    private String description;

    /** 标签权重 */
    @Column(nullable = false)
    @Builder.Default
    private Integer weight = 0;

    /** 创建时间 */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;

    /** 更新时间 */
    @Column(nullable = false)
    private LocalDateTime updateTime;
}
