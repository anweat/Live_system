package com.liveroom.anchor.service;

import com.liveroom.anchor.dto.AnchorDTO;
import com.liveroom.anchor.vo.AnchorVO;
import common.bean.liveroom.LiveRoom;
import common.bean.user.Anchor;
import common.constant.ErrorConstants;
import common.constant.StatusConstants;
import common.exception.BusinessException;
import common.logger.TraceLogger;
import common.service.DataAccessFacade;
import common.util.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 主播业务服务
 * 提供主播相关的核心业务逻辑
 * 重构后使用 DataAccessFacade 统一访问数据
 * 
 * @author Team
 * @version 2.0.0
 */
@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class AnchorService {

    @Autowired
    private DataAccessFacade facade;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 创建主播（注册）
     * 同时自动创建关联的直播间
     */
    public AnchorDTO createAnchor(AnchorDTO anchorDTO) {
        long startTime = System.currentTimeMillis();
        TraceLogger.info("AnchorService", "createAnchor", 
            "创建主播请求: nickname=" + anchorDTO.getNickname());

        // 1. 构建Anchor实体
        Anchor anchor = Anchor.builder()
                .nickname(anchorDTO.getNickname())
                .gender(anchorDTO.getGender() != null ? anchorDTO.getGender() : 0)
                .anchorLevel(0)  // 初始等级为普通
                .availableAmount(BigDecimal.ZERO)
                .likeCount(0L)
                .fanCount(0L)
                .totalEarnings(BigDecimal.ZERO)
                .currentCommissionRate(new BigDecimal("50.00"))  // 默认分成比例50%
                .accountStatus(0)  // 初始状态正常
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        // 2. 通过Facade保存主播信息
        Anchor savedAnchor = facade.anchor().createAnchor(anchor);

        // 3. 创建关联的直播间（一对一）
        LiveRoom liveRoom = LiveRoom.builder()
                .anchorId(savedAnchor.getUserId())
                .anchorName(savedAnchor.getNickname())
                .roomName(savedAnchor.getNickname() + "的直播间")
                .description("欢迎来到" + savedAnchor.getNickname() + "的直播间")
                .roomStatus(0)  // 初始状态：未开播
                .maxViewers(10000)
                .totalViewers(0L)
                .totalEarnings(BigDecimal.ZERO)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        
        facade.liveRoom().createLiveRoom(liveRoom);
        LiveRoom savedRoom = facade.liveRoom().getLiveRoomByAnchor(savedAnchor.getUserId());

        // 4. 更新主播的直播间ID
        savedAnchor.setLiveRoomId(savedRoom.getLiveRoomId());
        facade.anchor().updateAnchor(savedAnchor);

        long endTime = System.currentTimeMillis();
        TraceLogger.info("AnchorService", "createAnchor", 
            String.format("主播创建成功: userId=%d, liveRoomId=%d, 耗时=%dms",
                savedAnchor.getUserId(), savedRoom.getLiveRoomId(), (endTime - startTime)));

        return BeanUtil.convert(savedAnchor, AnchorDTO.class);
    }

    /**
     * 查询主播信息（带缓存）
     */
    public AnchorDTO getAnchor(Long anchorId) {
        TraceLogger.debug("AnchorService", "getAnchor", "查询主播信息: anchorId=" + anchorId);

        // 通过Facade查询，自动使用缓存
        Optional<Anchor> anchorOpt = facade.anchor().findByUserId(anchorId);
        if (!anchorOpt.isPresent()) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_FOUND, "主播不存在");
        }

        return BeanUtil.convert(anchorOpt.get(), AnchorDTO.class);
    }

    /**
     * 更新主播信息
     */
    public AnchorDTO updateAnchor(Long anchorId, AnchorDTO anchorDTO) {
        TraceLogger.info("AnchorService", "updateAnchor", 
            "更新主播信息: anchorId=" + anchorId);

        // 1. 查询主播
        Optional<Anchor> anchorOpt = facade.anchor().findByUserId(anchorId);
        if (!anchorOpt.isPresent()) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_FOUND, "主播不存在");
        }
        
        Anchor anchor = anchorOpt.get();

        // 2. 更新字段（只更新非空字段）
        if (anchorDTO.getNickname() != null) {
            anchor.setNickname(anchorDTO.getNickname());
        }
        if (anchorDTO.getGender() != null) {
            anchor.setGender(anchorDTO.getGender());
        }

        anchor.setUpdateTime(LocalDateTime.now());

        // 3. 保存更新
        Anchor updated = facade.anchor().updateAnchor(anchor);

        TraceLogger.info("AnchorService", "updateAnchor", "主播信息更新成功");

        return BeanUtil.convert(updated, AnchorDTO.class);
    }

    /**
     * 查询主播列表（分页）
     * 注意：分页查询暂时保留，后续可考虑迁移到Common模块
     */
    public java.util.List<AnchorDTO> listAnchors(Integer page, Integer size, String orderBy, String direction) {
        TraceLogger.debug("AnchorService", "listAnchors", 
            String.format("查询主播列表: page=%d, size=%d, orderBy=%s", page, size, orderBy));

        // 使用Common模块查询所有主播，前端应用分页
        java.util.List<Anchor> anchors = facade.anchor().findAll();

        // 转换为DTO
        return anchors.stream()
                .map(anchor -> BeanUtil.convert(anchor, AnchorDTO.class))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 查询粉丝数最多的主播
     */
    public java.util.List<AnchorDTO> listTopAnchorsByFans(int limit) {
        TraceLogger.debug("AnchorService", "listTopAnchorsByFans", "查询TOP主播(按粉丝数): limit=" + limit);

        java.util.List<Anchor> anchors = facade.anchor().findTopAnchorsByFans(limit);

        return anchors.stream()
                .map(anchor -> BeanUtil.convert(anchor, AnchorDTO.class))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 查询收益最多的主播
     */
    public java.util.List<AnchorDTO> listTopAnchorsByEarnings(int limit) {
        TraceLogger.debug("AnchorService", "listTopAnchorsByEarnings", "查询TOP主播(按收益): limit=" + limit);

        java.util.List<Anchor> anchors = facade.anchor().findTopAnchorsByEarnings(limit);

        return anchors.stream()
                .map(anchor -> BeanUtil.convert(anchor, AnchorDTO.class))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 更新主播粉丝数（增量更新）
     */
    public void updateFanCount(Long anchorId, Long delta) {
        TraceLogger.info("AnchorService", "updateFanCount", 
            String.format("更新粉丝数: anchorId=%d, delta=%d", anchorId, delta));

        facade.anchor().incrementFanCount(anchorId, delta);
    }

    /**
     * 更新主播点赞数（增量更新，本地实现）
     */
    public void updateLikeCount(Long anchorId, Long delta) {
        TraceLogger.info("AnchorService", "updateLikeCount", 
            String.format("更新点赞数: anchorId=%d, delta=%d", anchorId, delta));

        // 1. 查询主播
        Optional<Anchor> anchorOpt = facade.anchor().findByUserId(anchorId);
        if (!anchorOpt.isPresent()) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_FOUND, "主播不存在");
        }
        
        Anchor anchor = anchorOpt.get();

        // 2. 更新点赞数
        anchor.setLikeCount(anchor.getLikeCount() + delta);
        anchor.setUpdateTime(LocalDateTime.now());

        // 3. 保存更新
        facade.anchor().updateAnchor(anchor);

        // 4. 更新Redis缓存
        String cacheKey = "anchor:" + anchorId;
        redisTemplate.opsForValue().set(cacheKey, anchor, 30, TimeUnit.MINUTES);
    }

    /**
     * 更新主播累计收益（增量更新）
     */
    public void updateTotalEarnings(Long anchorId, BigDecimal amount) {
        TraceLogger.info("AnchorService", "updateTotalEarnings", 
            String.format("更新累计收益: anchorId=%d, amount=%s", anchorId, amount));

        facade.anchor().incrementEarnings(anchorId, amount);
    }

    /**
     * 查询主播可提取余额
     */
    public BigDecimal getAvailableAmount(Long anchorId) {
        TraceLogger.info("AnchorService", "getAvailableAmount",
            "查询可提取余额: anchorId=" + anchorId);

        Optional<Anchor> anchorOpt = facade.anchor().findByUserId(anchorId);
        if (!anchorOpt.isPresent()) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_FOUND, "主播不存在");
        }

        return anchorOpt.get().getAvailableAmount();
    }

    /**
     * 更新主播可提取余额（增量更新，本地实现）
     */
    public void updateAvailableAmount(Long anchorId, BigDecimal amount) {
        TraceLogger.info("AnchorService", "updateAvailableAmount", 
            String.format("更新可提取余额: anchorId=%d, amount=%s", anchorId, amount));

        // 1. 查询主播
        Optional<Anchor> anchorOpt = facade.anchor().findByUserId(anchorId);
        if (!anchorOpt.isPresent()) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_FOUND, "主播不存在");
        }
        
        Anchor anchor = anchorOpt.get();

        // 2. 更新余额
        anchor.setAvailableAmount(anchor.getAvailableAmount().add(amount));
        anchor.setUpdateTime(LocalDateTime.now());

        // 3. 保存更新
        facade.anchor().updateAnchor(anchor);

        // 4. 更新Redis缓存
        String cacheKey = "anchor:" + anchorId;
        redisTemplate.opsForValue().set(cacheKey, anchor, 30, TimeUnit.MINUTES);
    }

    /**
     * 转换为VO对象（供前端展示）
     */
    public AnchorVO convertToVO(Anchor anchor) {
        if (anchor == null) return null;

        return AnchorVO.builder()
                .userId(anchor.getUserId())
                .nickname(anchor.getNickname())
                .gender(anchor.getGender())
                .anchorLevel(anchor.getAnchorLevel())
                .availableAmount(anchor.getAvailableAmount())
                .liveRoomId(anchor.getLiveRoomId())
                .likeCount(anchor.getLikeCount())
                .fanCount(anchor.getFanCount())
                .totalEarnings(anchor.getTotalEarnings())
                .currentCommissionRate(anchor.getCurrentCommissionRate())
                .bannedUntil(anchor.getBannedUntil())
                .createTime(anchor.getCreateTime())
                .updateTime(anchor.getUpdateTime())
                .build();
    }

    /**
     * 查询主播信息（返回VO）
     */
    public AnchorVO getAnchorVO(Long anchorId) {
        TraceLogger.debug("AnchorService", "getAnchorVO", "查询主播信息VO: anchorId=" + anchorId);

        Optional<Anchor> anchorOpt = facade.anchor().findByUserId(anchorId);
        if (!anchorOpt.isPresent()) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_FOUND, "主播不存在");
        }

        return convertToVO(anchorOpt.get());
    }
}
