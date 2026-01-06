package common.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import common.bean.user.User;
import common.logger.TraceLogger;
import common.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

/**
 * 用户Service - 统一的用户数据访问接口
 * 其他模块必须通过此Service来操作用户数据
 */
@Slf4j
@Service
public class UserService extends BaseService<User, Long, UserRepository> {

    public UserService(UserRepository repository) {
        super(repository);
    }

    @Override
    protected String getCachePrefix() {
        return "user::";
    }

    @Override
    protected String getEntityName() {
        return "User";
    }

    /**
     * 按用户名查询用户（带缓存）
     */
    @Cacheable(value = "user::username", key = "#username", unless = "#result == null")
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        if (username == null || username.isEmpty()) {
            TraceLogger.warn("User", "findByUsername", "用户名为空");
            return Optional.empty();
        }
        TraceLogger.info("User", "findByUsername", "查询用户名: " + username);
        return repository.findByUsername(username);
    }

    /**
     * 按邮箱查询用户（带缓存）
     */
    @Cacheable(value = "user::email", key = "#email", unless = "#result == null")
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        if (email == null || email.isEmpty()) {
            TraceLogger.warn("User", "findByEmail", "邮箱为空");
            return Optional.empty();
        }
        TraceLogger.info("User", "findByEmail", "查询邮箱: " + email);
        return repository.findByEmail(email);
    }

    /**
     * 按用户类型批量查询（带缓存）
     */
    @Cacheable(value = "user::type", key = "#userType")
    @Transactional(readOnly = true)
    public List<User> findByUserType(Integer userType) {
        if (userType == null) {
            TraceLogger.warn("User", "findByUserType", "用户类型为空");
            return List.of();
        }
        TraceLogger.info("User", "findByUserType", "查询用户类型: " + userType);
        return repository.findByUserType(userType);
    }

    /**
     * 按账户状态批量查询
     */
    @Cacheable(value = "user::status", key = "#status")
    @Transactional(readOnly = true)
    public List<User> findByAccountStatus(Integer status) {
        TraceLogger.info("User", "findByAccountStatus", "查询账户状态: " + status);
        return repository.findByAccountStatus(status);
    }

    /**
     * 查询有效的用户（未删除）
     */
    @Cacheable(value = "user::active_type", key = "#userType")
    @Transactional(readOnly = true)
    public List<User> findActiveUsersByType(Integer userType) {
        TraceLogger.info("User", "findActiveUsersByType", "查询有效用户，类型: " + userType);
        return repository.findActiveUsersByType(userType);
    }

    /**
     * 检查用户名是否存在
     */
    @Transactional(readOnly = true)
    public boolean checkUsernameExists(String username) {
        return repository.existsByUsername(username);
    }

    /**
     * 检查邮箱是否存在
     */
    @Transactional(readOnly = true)
    public boolean checkEmailExists(String email) {
        return repository.existsByEmail(email);
    }

    /**
     * 创建用户
     */
    @CacheEvict(value = {"user::username", "user::email", "user::type", "user::status", "user::active_type"}, 
                allEntries = true)
    @Transactional
    public User createUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("用户不能为空");
        }
        TraceLogger.info("User", "createUser", "创建用户: " + user.getUsername());
        return repository.save(user);
    }

    /**
     * 更新用户信息
     */
    @CacheEvict(value = {"user::username", "user::email", "user::type", "user::status", "user::active_type"}, 
                allEntries = true)
    @Transactional
    public User updateUser(User user) {
        if (user == null || user.getUserId() == null) {
            throw new IllegalArgumentException("用户信息不完整");
        }
        TraceLogger.info("User", "updateUser", "更新用户ID: " + user.getUserId());
        return repository.save(user);
    }

    /**
     * 删除用户
     */
    @CacheEvict(value = {"user::username", "user::email", "user::type", "user::status", "user::active_type"}, 
                allEntries = true)
    @Transactional
    public void deleteUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        TraceLogger.info("User", "deleteUser", "删除用户ID: " + userId);
        repository.deleteById(userId);
    }

    /**
     * 批量删除用户
     */
    @CacheEvict(value = {"user::username", "user::email", "user::type", "user::status", "user::active_type"}, 
                allEntries = true)
    @Transactional
    public void batchDeleteUsers(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            TraceLogger.warn("User", "batchDeleteUsers", "删除列表为空");
            return;
        }
        TraceLogger.info("User", "batchDeleteUsers", "批量删除用户，条数: " + userIds.size());
        repository.deleteAllByIdIn(userIds);
    }

    /**
     * 批量创建或更新用户
     */
    @CacheEvict(value = {"user::username", "user::email", "user::type", "user::status", "user::active_type"}, 
                allEntries = true)
    @Transactional
    public List<User> batchSaveUsers(List<User> users) {
        if (users == null || users.isEmpty()) {
            TraceLogger.warn("User", "batchSaveUsers", "保存列表为空");
            return List.of();
        }
        TraceLogger.info("User", "batchSaveUsers", "批量保存用户，条数: " + users.size());
        return repository.saveAll(users);
    }
}
