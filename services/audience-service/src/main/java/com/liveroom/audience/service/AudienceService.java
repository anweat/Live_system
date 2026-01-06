package com.liveroom.audience.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import common.bean.user.Audience;
import common.bean.user.User;
import common.constant.ErrorConstants;
import common.dto.BaseDTO;
import common.exception.BusinessException;
import common.exception.ValidationException;
import common.logger.TraceLogger;
import common.util.BeanUtil;
import common.util.DateTimeUtil;
import common.util.IdGeneratorUtil;
import com.liveroom.audience.dto.AudienceDTO;
import com.liveroom.audience.dto.ConsumptionStatsDTO;
import com.liveroom.audience.repository.AudienceRepository;

/**
 * 观众业务逻辑服务
 * 处理观众相关的所有业务逻辑，包括创建、查询、修改等操作
 */
@Service
@Slf4j
@Transactional
public class AudienceService {

    @Autowired
    private AudienceRepository audienceRepository;

    /**
     * 创建观众（注册用户）
     */
    public AudienceDTO createAudience(AudienceDTO audienceDTO) {
        TraceLogger.info("AudienceService", "createAudience", "开始创建观众");

        // 参数验证
        if (audienceDTO == null || audienceDTO.getNickname() == null) {
            TraceLogger.warn("AudienceService", "createAudience", "参数验证失败");
            throw new ValidationException("观众昵称不能为空");
        }

        // 检查昵称唯一性
        if (audienceRepository.findByNickname(audienceDTO.getNickname()).isPresent()) {
            TraceLogger.warn("AudienceService", "createAudience", "昵称已存在: " + audienceDTO.getNickname());
            throw new BusinessException(ErrorConstants.USER_ALREADY_EXISTS, "观众昵称已存在");
        }

        // 创建Audience实体
        Audience audience = new Audience();
        audience.setUserId(IdGeneratorUtil.nextId());
        audience.setNickname(audienceDTO.getNickname());
        audience.setRealName(audienceDTO.getRealName());
        audience.setUserType(audienceDTO.getUserType() != null ? audienceDTO.getUserType() : 1);
        audience.setGender(audienceDTO.getGender());
        audience.setBirthDate(audienceDTO.getBirthDate());
        audience.setSignature(audienceDTO.getSignature());
        audience.setIpLocation(audienceDTO.getIpLocation());

        // 初始化观众特有字段
        audience.setConsumptionLevel(0);  // 初始为低消费
        audience.setTotalRechargeAmount(BigDecimal.ZERO);
        audience.setTotalRechargeCount(0L);
        audience.setVipLevel(0);  // 初始为普通粉丝
        audience.setStatus(0);  // 正常状态
        audience.setCreateTime(DateTimeUtil.now());
        audience.setUpdateTime(DateTimeUtil.now());

        // 保存到数据库
        Audience savedAudience = audienceRepository.save(audience);

        TraceLogger.info("AudienceService", "createAudience", "观众创建成功: " + savedAudience.getUserId());

        return BeanUtil.convert(savedAudience, AudienceDTO.class);
    }

    /**
     * 创建游客观众
     */
    public AudienceDTO createGuestAudience(AudienceDTO audienceDTO) {
        TraceLogger.info("AudienceService", "createGuestAudience", "开始创建游客观众");

        Audience guest = new Audience();
        guest.setUserId(IdGeneratorUtil.nextId());
        guest.setNickname(audienceDTO.getNickname() != null ? audienceDTO.getNickname() : "游客_" + guest.getUserId());
        guest.setUserType(0);  // 游客
        guest.setStatus(0);

        // 初始化统计字段
        guest.setConsumptionLevel(0);
        guest.setTotalRechargeAmount(BigDecimal.ZERO);
        guest.setTotalRechargeCount(0L);
        guest.setVipLevel(0);
        guest.setCreateTime(DateTimeUtil.now());
        guest.setUpdateTime(DateTimeUtil.now());

        Audience savedGuest = audienceRepository.save(guest);

        TraceLogger.info("AudienceService", "createGuestAudience", "游客观众创建成功: " + savedGuest.getUserId());

        return BeanUtil.convert(savedGuest, AudienceDTO.class);
    }

    /**
     * 获取观众信息
     */
    public AudienceDTO getAudience(Long audienceId) {
        if (audienceId == null || audienceId <= 0) {
            throw new ValidationException("观众ID不合法");
        }

        Optional<Audience> optional = audienceRepository.findById(audienceId);
        if (!optional.isPresent()) {
            throw new BusinessException(ErrorConstants.AUDIENCE_NOT_FOUND, "观众不存在");
        }

        return BeanUtil.convert(optional.get(), AudienceDTO.class);
    }

    /**
     * 修改观众信息
     */
    public AudienceDTO updateAudience(Long audienceId, AudienceDTO audienceDTO) {
        TraceLogger.info("AudienceService", "updateAudience", "开始修改观众信息: " + audienceId);

        Audience audience = audienceRepository.findById(audienceId)
            .orElseThrow(() -> new BusinessException(ErrorConstants.AUDIENCE_NOT_FOUND, "观众不存在"));

        // 更新可修改的字段
        if (audienceDTO.getNickname() != null) {
            audience.setNickname(audienceDTO.getNickname());
        }
        if (audienceDTO.getSignature() != null) {
            audience.setSignature(audienceDTO.getSignature());
        }
        if (audienceDTO.getGender() != null) {
            audience.setGender(audienceDTO.getGender());
        }

        audience.setUpdateTime(DateTimeUtil.now());
        Audience updated = audienceRepository.save(audience);

        TraceLogger.info("AudienceService", "updateAudience", "观众信息修改成功: " + audienceId);

        return BeanUtil.convert(updated, AudienceDTO.class);
    }

    /**
     * 分页查询观众列表
     */
    public Page<AudienceDTO> listAudiences(Integer page, Integer size, Integer consumptionLevel) {
        Pageable pageable = PageRequest.of(page - 1, size);

        Page<Audience> audiences;
        if (consumptionLevel != null) {
            audiences = audienceRepository.findByConsumptionLevel(consumptionLevel, pageable);
        } else {
            audiences = audienceRepository.findAll(pageable);
        }

        return audiences.map(a -> BeanUtil.convert(a, AudienceDTO.class));
    }

    /**
     * 搜索观众（按昵称关键词）
     */
    public Page<AudienceDTO> searchAudiences(String keyword, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Audience> audiences = audienceRepository.searchByKeyword(keyword, pageable);
        return audiences.map(a -> BeanUtil.convert(a, AudienceDTO.class));
    }

    /**
     * 获取观众的消费统计信息
     */
    public ConsumptionStatsDTO getConsumptionStats(Long audienceId) {
        Audience audience = audienceRepository.findById(audienceId)
            .orElseThrow(() -> new BusinessException(ErrorConstants.AUDIENCE_NOT_FOUND, "观众不存在"));

        return ConsumptionStatsDTO.builder()
            .audienceId(audienceId)
            .audienceNickname(audience.getNickname())
            .totalRechargeAmount(audience.getTotalRechargeAmount())
            .totalRechargeCount(audience.getTotalRechargeCount())
            .consumptionLevel(audience.getConsumptionLevel())
            .consumptionLevelDesc(getConsumptionLevelDesc(audience.getConsumptionLevel()))
            .vipLevel(audience.getVipLevel())
            .vipLevelDesc(getVipLevelDesc(audience.getVipLevel()))
            .build();
    }

    /**
     * 更新观众消费统计（打赏后调用）
     */
    public void updateConsumptionStats(Long audienceId, BigDecimal rechargeAmount) {
        Audience audience = audienceRepository.findById(audienceId)
            .orElseThrow(() -> new BusinessException(ErrorConstants.AUDIENCE_NOT_FOUND, "观众不存在"));

        // 更新消费金额和次数
        audience.setTotalRechargeAmount(audience.getTotalRechargeAmount().add(rechargeAmount));
        audience.setTotalRechargeCount(audience.getTotalRechargeCount() + 1);
        audience.setLastRechargeTime(DateTimeUtil.now());

        // 更新消费等级（简单规则：>5000为高消费，>1000为中消费）
        if (audience.getTotalRechargeAmount().compareTo(new BigDecimal("5000")) >= 0) {
            audience.setConsumptionLevel(2);  // 高消费
        } else if (audience.getTotalRechargeAmount().compareTo(new BigDecimal("1000")) >= 0) {
            audience.setConsumptionLevel(1);  // 中消费
        } else {
            audience.setConsumptionLevel(0);  // 低消费
        }

        // 更新粉丝等级（基于消费金额和次数）
        audience.setVipLevel(calculateVipLevel(audience.getTotalRechargeAmount(), audience.getTotalRechargeCount()));

        audience.setUpdateTime(DateTimeUtil.now());
        audienceRepository.save(audience);

        TraceLogger.info("AudienceService", "updateConsumptionStats", 
            "观众消费统计已更新: " + audienceId + ", 总金额: " + audience.getTotalRechargeAmount());
    }

    /**
     * 禁用观众账户
     */
    public void disableAudience(Long audienceId, String reason) {
        TraceLogger.info("AudienceService", "disableAudience", "禁用观众: " + audienceId);

        Audience audience = audienceRepository.findById(audienceId)
            .orElseThrow(() -> new BusinessException(ErrorConstants.AUDIENCE_NOT_FOUND, "观众不存在"));

        audience.setStatus(1);  // 1 = 禁用
        audience.setUpdateTime(DateTimeUtil.now());
        audienceRepository.save(audience);
    }

    /**
     * 启用观众账户
     */
    public void enableAudience(Long audienceId) {
        Audience audience = audienceRepository.findById(audienceId)
            .orElseThrow(() -> new BusinessException(ErrorConstants.AUDIENCE_NOT_FOUND, "观众不存在"));

        audience.setStatus(0);  // 0 = 正常
        audience.setUpdateTime(DateTimeUtil.now());
        audienceRepository.save(audience);
    }

    /**
     * 计算粉丝等级
     */
    private Integer calculateVipLevel(BigDecimal totalAmount, Long totalCount) {
        if (totalAmount.compareTo(new BigDecimal("10000")) >= 0 && totalCount >= 100) {
            return 4;  // 超级粉丝
        } else if (totalAmount.compareTo(new BigDecimal("5000")) >= 0 && totalCount >= 50) {
            return 3;  // 金粉
        } else if (totalAmount.compareTo(new BigDecimal("1000")) >= 0 && totalCount >= 10) {
            return 2;  // 银粉
        } else if (totalAmount.compareTo(new BigDecimal("100")) >= 0 && totalCount >= 3) {
            return 1;  // 铁粉
        }
        return 0;  // 普通
    }

    /**
     * 获取消费等级描述
     */
    private String getConsumptionLevelDesc(Integer level) {
        switch (level) {
            case 0: return "低消费";
            case 1: return "中消费";
            case 2: return "高消费";
            default: return "未知";
        }
    }

    /**
     * 获取粉丝等级描述
     */
    private String getVipLevelDesc(Integer level) {
        switch (level) {
            case 0: return "普通";
            case 1: return "铁粉";
            case 2: return "银粉";
            case 3: return "金粉";
            case 4: return "超级粉丝";
            default: return "未知";
        }
    }
}
