package common.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import common.logger.TraceLogger;
import common.repository.BaseRepository;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 基础Service层
 * 提供通用的增删改查、缓存、批量操作等功能
 * 
 * @param <T> 实体类型
 * @param <ID> 主键类型
 * @param <R> Repository类型
 */
@RequiredArgsConstructor
public abstract class BaseService<T, ID extends Serializable, R extends BaseRepository<T, ID>> {

    protected final R repository;

    /**
     * 获取缓存前缀，子类应该覆写此方法
     * 例如: "user::", "anchor::", "audience::" 等
     */
    protected abstract String getCachePrefix();

    /**
     * 获取实体类名称
     */
    protected abstract String getEntityName();

    /**
     * 通过ID查询单个实体（带缓存）
     */
    @Cacheable(value = "#{T(java.lang.String).format('cache:%s', @baseService.getCachePrefix())}", 
               key = "#id", unless = "#result == null")
    @Transactional(readOnly = true)
    public Optional<T> findById(ID id) {
        TraceLogger.info(getEntityName(), "findById", "查询ID: " + id);
        return repository.findById(id);
    }

    /**
     * 查询所有实体（不缓存）
     */
    @Transactional(readOnly = true)
    public List<T> findAll() {
        TraceLogger.info(getEntityName(), "findAll", "查询所有");
        return repository.findAll();
    }

    /**
     * 分页查询
     */
    @Transactional(readOnly = true)
    public Page<T> findPage(int pageNum, int pageSize) {
        TraceLogger.info(getEntityName(), "findPage", 
            String.format("分页查询 - pageNum: %d, pageSize: %d", pageNum, pageSize));
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        return repository.findAll(pageable);
    }

    /**
     * 批量查询（带缓存，使用Redis的Pipeline特性）
     */
    @Transactional(readOnly = true)
    public List<T> findBatch(List<ID> ids) {
        if (ids == null || ids.isEmpty()) {
            TraceLogger.warn(getEntityName(), "findBatch", "查询ID列表为空");
            return List.of();
        }
        TraceLogger.info(getEntityName(), "findBatch", 
            String.format("批量查询 - 条数: %d", ids.size()));
        return repository.findAllByIdIn(ids);
    }

    /**
     * 保存单个实体
     */
    @CacheEvict(value = "#{T(java.lang.String).format('cache:%s', @baseService.getCachePrefix())}", 
                key = "#result.id")
    @Transactional
    public T save(T entity) {
        TraceLogger.info(getEntityName(), "save", "保存实体");
        return repository.save(entity);
    }

    /**
     * 批量保存实体
     */
    @CacheEvict(value = "#{T(java.lang.String).format('cache:%s', @baseService.getCachePrefix())}", 
                allEntries = true)
    @Transactional
    public List<T> saveBatch(List<T> entities) {
        if (entities == null || entities.isEmpty()) {
            TraceLogger.warn(getEntityName(), "saveBatch", "保存列表为空");
            return List.of();
        }
        TraceLogger.info(getEntityName(), "saveBatch", 
            String.format("批量保存 - 条数: %d", entities.size()));
        return repository.saveAll(entities);
    }

    /**
     * 删除单个实体
     */
    @CacheEvict(value = "#{T(java.lang.String).format('cache:%s', @baseService.getCachePrefix())}", 
                key = "#id")
    @Transactional
    public void deleteById(ID id) {
        TraceLogger.info(getEntityName(), "deleteById", "删除ID: " + id);
        repository.deleteById(id);
    }

    /**
     * 批量删除
     */
    @CacheEvict(value = "#{T(java.lang.String).format('cache:%s', @baseService.getCachePrefix())}", 
                allEntries = true)
    @Transactional
    public void deleteBatch(List<ID> ids) {
        if (ids == null || ids.isEmpty()) {
            TraceLogger.warn(getEntityName(), "deleteBatch", "删除ID列表为空");
            return;
        }
        TraceLogger.info(getEntityName(), "deleteBatch", 
            String.format("批量删除 - 条数: %d", ids.size()));
        repository.deleteAllByIdIn(ids);
    }

    /**
     * 更新实体
     */
    @CacheEvict(value = "#{T(java.lang.String).format('cache:%s', @baseService.getCachePrefix())}", 
                key = "#result.id")
    @Transactional
    public T update(T entity) {
        TraceLogger.info(getEntityName(), "update", "更新实体");
        return repository.save(entity);
    }

    /**
     * 批量更新
     */
    @CacheEvict(value = "#{T(java.lang.String).format('cache:%s', @baseService.getCachePrefix())}", 
                allEntries = true)
    @Transactional
    public List<T> updateBatch(List<T> entities) {
        if (entities == null || entities.isEmpty()) {
            TraceLogger.warn(getEntityName(), "updateBatch", "更新列表为空");
            return List.of();
        }
        TraceLogger.info(getEntityName(), "updateBatch", 
            String.format("批量更新 - 条数: %d", entities.size()));
        return repository.saveAll(entities);
    }

    /**
     * 检查是否存在
     */
    @Transactional(readOnly = true)
    public boolean existsById(ID id) {
        return repository.existsById(id);
    }

    /**
     * 获取总记录数
     */
    @Transactional(readOnly = true)
    public long count() {
        return repository.count();
    }

    /**
     * 按ID删除多个实体（适合需要验证权限的场景）
     */
    @CacheEvict(value = "#{T(java.lang.String).format('cache:%s', @baseService.getCachePrefix())}", 
                allEntries = true)
    @Transactional
    public void deleteByIds(List<ID> ids) {
        deleteBatch(ids);
    }
}
