package com.liveroom.finance.service;

import com.liveroom.finance.dto.CommissionRateDTO;
import common.bean.CommissionRate;
import common.constant.ErrorConstants;
import common.exception.BusinessException;
import common.logger.TraceLogger;
import common.repository.CommissionRateRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 分成比例服务
 * 管理主播的分成比例及其变化历史
 */
@Service
@Slf4j
public class CommissionRateService {

    @Autowired
    private CommissionRateRepository commissionRateRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String COMMISSION_CACHE_KEY = "finance:commission:";
    private static final int CACHE_EXPIRE_HOURS = 24;

    /**
     * 创建或更新主播分成比例（Redis缓存失效）
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "commissionRate", key = "#dto.anchorId")
    public CommissionRateDTO createOrUpdateCommissionRate(CommissionRateDTO dto) {
        TraceLogger.info("CommissionRateService", "createOrUpdateCommissionRate",
                "创建/更新分成比例，主播ID: " + dto.getAnchorId() + ", 比例: " + dto.getCommissionRate());

        // 1. 参数校验
        if (dto.getCommissionRate() == null ||
            dto.getCommissionRate().compareTo(BigDecimal.ZERO) < 0 ||
            dto.getCommissionRate().compareTo(new BigDecimal("100")) > 0) {
            throw new BusinessException(ErrorConstants.INVALID_AMOUNT, "分成比例必须在0-100之间");
        }

        // 2. 查询当前生效的分成比例
        CommissionRate currentRate = commissionRateRepository
                .findCurrentRateByAnchorId(dto.getAnchorId(), LocalDateTime.now())
                .orElse(null);

        // 3. 如果存在当前比例，则将其设为过期
        if (currentRate != null) {
            currentRate.setExpireTime(LocalDateTime.now());
            currentRate.setStatus(2); // 已过期
            currentRate.setUpdateTime(LocalDateTime.now());
            commissionRateRepository.save(currentRate);
            
            TraceLogger.info("CommissionRateService", "createOrUpdateCommissionRate",
                    "旧分成比例已过期，ID: " + currentRate.getCommissionRateId());
        }

        // 4. 创建新的分成比例记录
        CommissionRate newRate = CommissionRate.builder()
                .anchorId(dto.getAnchorId())
                .anchorName(dto.getAnchorName())
                .commissionRate(dto.getCommissionRate())
                .effectiveTime(dto.getEffectiveTime() != null ? dto.getEffectiveTime() : LocalDateTime.now())
                .expireTime(null)
                .status(1) // 启用
                .remark(dto.getRemark())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        newRate = commissionRateRepository.save(newRate);

        // 5. 清除Redis缓存
        String cacheKey = COMMISSION_CACHE_KEY + dto.getAnchorId();
        redisTemplate.delete(cacheKey);

        TraceLogger.info("CommissionRateService", "createOrUpdateCommissionRate",
                "新分成比例已生效，ID: " + newRate.getCommissionRateId());

        return convertToDTO(newRate);
    }

    /**
     * 查询主播当前生效的分成比例（Redis缓存）
     */
    @Cacheable(value = "commissionRate", key = "#anchorId", unless = "#result == null")
    public CommissionRateDTO getCurrentCommissionRate(Long anchorId) {
        // 1. 先从Redis缓存查询
        String cacheKey = COMMISSION_CACHE_KEY + anchorId;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof CommissionRateDTO) {
            TraceLogger.debug("CommissionRateService", "getCurrentCommissionRate",
                    "从缓存获取分成比例，主播ID: " + anchorId);
            return (CommissionRateDTO) cached;
        }

        // 2. 从数据库查询
        CommissionRate rate = commissionRateRepository
                .findCurrentRateByAnchorId(anchorId, LocalDateTime.now())
                .orElse(null);

        if (rate == null) {
            TraceLogger.warn("CommissionRateService", "getCurrentCommissionRate",
                    "主播分成比例不存在，主播ID: " + anchorId);
            return null;
        }

        CommissionRateDTO dto = convertToDTO(rate);

        // 3. 更新Redis缓存
        redisTemplate.opsForValue().set(cacheKey, dto, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);

        return dto;
    }

    /**
     * 查询主播分成比例历史
     */
    public Page<CommissionRateDTO> getCommissionRateHistory(Long anchorId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<CommissionRate> ratePage = commissionRateRepository
                .findByAnchorIdOrderByEffectiveTimeDesc(anchorId, pageable);

        return ratePage.map(this::convertToDTO);
    }

    /**
     * 查询指定时间的有效分成比例
     * 用于历史结算计算
     */
    public BigDecimal getCommissionRateAtTime(Long anchorId, LocalDateTime time) {
        CommissionRate rate = commissionRateRepository
                .findRateAtTime(anchorId, time)
                .orElse(null);

        if (rate == null) {
            // 如果没有历史记录，返回当前比例
            CommissionRateDTO current = getCurrentCommissionRate(anchorId);
            return current != null ? current.getCommissionRate() : new BigDecimal("70.00"); // 默认70%
        }

        return rate.getCommissionRate();
    }

    /**
     * 转换为DTO
     */
    private CommissionRateDTO convertToDTO(CommissionRate rate) {
        return CommissionRateDTO.builder()
                .commissionRateId(rate.getCommissionRateId())
                .anchorId(rate.getAnchorId())
                .anchorName(rate.getAnchorName())
                .commissionRate(rate.getCommissionRate())
                .effectiveTime(rate.getEffectiveTime())
                .expireTime(rate.getExpireTime())
                .status(rate.getStatus())
                .remark(rate.getRemark())
                .createTime(rate.getCreateTime())
                .updateTime(rate.getUpdateTime())
                .build();
    }
}
