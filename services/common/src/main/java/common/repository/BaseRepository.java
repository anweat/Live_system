package common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * 基础Repository接口
 * 所有Repository应继承此接口
 * 
 * @param <T> 实体类型
 * @param <ID> 主键类型
 */
@NoRepositoryBean
public interface BaseRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {

    /**
     * 批量查询
     */
    List<T> findAllByIdIn(List<ID> ids);

    /**
     * 批量删除
     */
    void deleteAllByIdIn(List<ID> ids);

    /**
     * 检查是否存在
     */
    boolean existsById(ID id);

    /**
     * 获取所有记录数
     */
    long countAll();
}
