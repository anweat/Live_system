package common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import common.bean.user.User;

import java.util.List;
import java.util.Optional;

/**
 * 用户Repository接口
 * 继承BaseRepository，可直接使用基础的CRUD操作
 */
public interface UserRepository extends BaseRepository<User, Long> {

    /**
     * 按用户名查询
     */
    Optional<User> findByUsername(String username);

    /**
     * 按邮箱查询
     */
    Optional<User> findByEmail(String email);

    /**
     * 按用户类型查询
     */
    List<User> findByUserType(Integer userType);

    /**
     * 按账户状态查询
     */
    List<User> findByAccountStatus(Integer status);

    /**
     * 查询账户未删除的用户
     */
    @Query("SELECT u FROM User u WHERE u.isDeleted = 0 AND u.userType = :userType")
    List<User> findActiveUsersByType(@Param("userType") Integer userType);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);
}
