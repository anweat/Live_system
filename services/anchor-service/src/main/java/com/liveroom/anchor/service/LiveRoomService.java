package com.liveroom.anchor.service;

import com.liveroom.anchor.dto.LiveRoomDTO;
import com.liveroom.anchor.vo.LiveRoomVO;
import common.bean.liveroom.LiveRoom;
import common.bean.user.Anchor;
import common.constant.ErrorConstants;
import common.exception.BusinessException;
import common.logger.TraceLogger;
import common.service.DataAccessFacade;
import common.util.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 直播间业务服务
 * 提供直播间生命周期管理
 * 重构后使用 DataAccessFacade 统一访问数据
 * 
 * @author Team
 * @version 2.0.0
 */
@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class LiveRoomService {

    @Autowired
    private DataAccessFacade facade;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 查询直播间信息（带缓存）
     */
    public LiveRoomDTO getLiveRoom(Long liveRoomId) {
        TraceLogger.debug("LiveRoomService", "getLiveRoom", 
            "查询直播间信息: liveRoomId=" + liveRoomId);

        LiveRoom liveRoom = facade.liveRoom().getLiveRoomInfo(liveRoomId);
        if (liveRoom == null) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_FOUND, "直播间不存在");
        }

        return BeanUtil.convert(liveRoom, LiveRoomDTO.class);
    }

    /**
     * 根据主播ID查询直播间
     */
    public LiveRoomDTO getLiveRoomByAnchorId(Long anchorId) {
        TraceLogger.debug("LiveRoomService", "getLiveRoomByAnchorId", 
            "查询主播的直播间: anchorId=" + anchorId);

        LiveRoom liveRoom = facade.liveRoom().getLiveRoomByAnchor(anchorId);
        if (liveRoom == null) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_FOUND, "该主播尚未创建直播间");
        }

        return BeanUtil.convert(liveRoom, LiveRoomDTO.class);
    }

    /**
     * 开启直播
     */
    public LiveRoomDTO startLive(Long liveRoomId, String streamUrl, String coverUrl) {
        long startTime = System.currentTimeMillis();
        TraceLogger.info("LiveRoomService", "startLive", 
            "开启直播: liveRoomId=" + liveRoomId);

        // 1. 查询直播间
        LiveRoom liveRoom = facade.liveRoom().getLiveRoomInfo(liveRoomId);
        if (liveRoom == null) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_FOUND, "直播间不存在");
        }

        // 2. 检查直播间状态
        if (liveRoom.getRoomStatus() == 1) {
            throw new BusinessException(ErrorConstants.VALIDATION_FAILED, "直播间已在直播中，不能重复开播");
        }
        if (liveRoom.getRoomStatus() == 3) {
            throw new BusinessException(ErrorConstants.VALIDATION_FAILED, "直播间已被封禁，无法开播");
        }

        // 3. 更新直播间信息（streamUrl和coverUrl）
        liveRoom.setStreamUrl(streamUrl);
        liveRoom.setCoverUrl(coverUrl);
        liveRoom.setUpdateTime(LocalDateTime.now());
        facade.liveRoom().updateLiveRoom(liveRoom);

        // 4. 使用Facade开启直播
        facade.liveRoom().startBroadcast(liveRoomId);
        LiveRoom updated = facade.liveRoom().getLiveRoomInfo(liveRoomId);

        long endTime = System.currentTimeMillis();
        TraceLogger.info("LiveRoomService", "startLive", 
            String.format("开播成功: liveRoomId=%d, anchorId=%d, 耗时=%dms",
                liveRoomId, liveRoom.getAnchorId(), (endTime - startTime)));

        return BeanUtil.convert(updated, LiveRoomDTO.class);
    }

    /**
     * 结束直播
     */
    public LiveRoomDTO endLive(Long liveRoomId) {
        long startTime = System.currentTimeMillis();
        TraceLogger.info("LiveRoomService", "endLive", 
            "结束直播: liveRoomId=" + liveRoomId);

        // 1. 查询直播间
        LiveRoom liveRoom = facade.liveRoom().getLiveRoomInfo(liveRoomId);
        if (liveRoom == null) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_FOUND, "直播间不存在");
        }

        // 2. 检查直播间状态
        if (liveRoom.getRoomStatus() != 1) {
            throw new BusinessException(ErrorConstants.VALIDATION_FAILED, "直播间未在直播中，无法关播");
        }

        // 3. 使用Facade结束直播
        facade.liveRoom().endBroadcast(liveRoomId);
        LiveRoom updated = facade.liveRoom().getLiveRoomInfo(liveRoomId);

        // 4. 更新主播的累计收益
        if (liveRoom.getTotalEarnings() != null && liveRoom.getTotalEarnings().compareTo(BigDecimal.ZERO) > 0) {
            facade.anchor().incrementEarnings(liveRoom.getAnchorId(), liveRoom.getTotalEarnings());
        }

        long endTime = System.currentTimeMillis();
        TraceLogger.info("LiveRoomService", "endLive", 
            String.format("关播成功: liveRoomId=%d, 本次营收=%s, 耗时=%dms",
                liveRoomId, liveRoom.getTotalEarnings(), (endTime - startTime)));

        return BeanUtil.convert(updated, LiveRoomDTO.class);
    }

    /**
     * 更新直播间实时数据（观众数、营收）- 本地实现，配合Redis缓存
     */
    public void updateRealtimeData(Long liveRoomId, Long viewersDelta, BigDecimal earningsDelta) {
        TraceLogger.debug("LiveRoomService", "updateRealtimeData", 
            String.format("更新直播间实时数据: liveRoomId=%d, viewersDelta=%d, earningsDelta=%s",
                liveRoomId, viewersDelta, earningsDelta));

        // 1. 查询直播间
        LiveRoom liveRoom = facade.liveRoom().getLiveRoomInfo(liveRoomId);
        if (liveRoom == null) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_FOUND, "直播间不存在");
        }

        // 2. 更新累计观看人次
        if (viewersDelta != null && viewersDelta != 0) {
            liveRoom.setTotalViewers(liveRoom.getTotalViewers() + viewersDelta);
        }

        // 3. 更新本次总营收
        if (earningsDelta != null && earningsDelta.compareTo(BigDecimal.ZERO) != 0) {
            liveRoom.setTotalEarnings(liveRoom.getTotalEarnings().add(earningsDelta));
        }

        liveRoom.setUpdateTime(LocalDateTime.now());

        // 4. 保存更新
        facade.liveRoom().updateLiveRoom(liveRoom);

        // 5. 更新Redis缓存
        String cacheKey = "liveroom:" + liveRoomId;
        redisTemplate.opsForValue().set(cacheKey, liveRoom, 30, TimeUnit.MINUTES);
    }

    /**
     * 查询所有正在直播的直播间
     */
    public java.util.List<LiveRoomDTO> listLiveRooms(Integer page, Integer size) {
        TraceLogger.debug("LiveRoomService", "listLiveRooms", 
            String.format("查询正在直播的直播间: page=%d, size=%d", page, size));

        java.util.List<LiveRoom> liveRooms = facade.liveRoom().getLiveRooms();

        return liveRooms.stream()
                .map(liveRoom -> BeanUtil.convert(liveRoom, LiveRoomDTO.class))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 按分类查询直播间列表
     */
    public java.util.List<LiveRoomDTO> listLiveRoomsByCategory(String category, Integer page, Integer size) {
        TraceLogger.debug("LiveRoomService", "listLiveRoomsByCategory", 
            "查询直播间列表: category=" + category);

        java.util.List<LiveRoom> liveRooms = facade.liveRoom().getLiveRoomsByCategory(category);

        return liveRooms.stream()
                .map(liveRoom -> BeanUtil.convert(liveRoom, LiveRoomDTO.class))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 更新直播间信息
     */
    public LiveRoomDTO updateLiveRoom(Long liveRoomId, LiveRoomDTO liveRoomDTO) {
        TraceLogger.info("LiveRoomService", "updateLiveRoom", 
            "更新直播间信息: liveRoomId=" + liveRoomId);

        // 1. 查询直播间
        LiveRoom liveRoom = facade.liveRoom().getLiveRoomInfo(liveRoomId);
        if (liveRoom == null) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_FOUND, "直播间不存在");
        }

        // 2. 更新字段（只更新非空字段）
        if (liveRoomDTO.getRoomName() != null) {
            liveRoom.setRoomName(liveRoomDTO.getRoomName());
        }
        if (liveRoomDTO.getDescription() != null) {
            liveRoom.setDescription(liveRoomDTO.getDescription());
        }
        if (liveRoomDTO.getCategory() != null) {
            liveRoom.setCategory(liveRoomDTO.getCategory());
        }
        if (liveRoomDTO.getCoverUrl() != null) {
            liveRoom.setCoverUrl(liveRoomDTO.getCoverUrl());
        }
        if (liveRoomDTO.getMaxViewers() != null) {
            liveRoom.setMaxViewers(liveRoomDTO.getMaxViewers());
        }

        liveRoom.setUpdateTime(LocalDateTime.now());

        // 3. 保存更新
        facade.liveRoom().updateLiveRoom(liveRoom);
        LiveRoom updated = facade.liveRoom().getLiveRoomInfo(liveRoom.getLiveRoomId());

        TraceLogger.info("LiveRoomService", "updateLiveRoom", "直播间信息更新成功");

        return BeanUtil.convert(updated, LiveRoomDTO.class);
    }

    /**
     * 转换为VO对象（供前端展示）
     */
    public LiveRoomVO convertToVO(LiveRoom liveRoom) {
        if (liveRoom == null) return null;

        return LiveRoomVO.builder()
                .liveRoomId(liveRoom.getLiveRoomId())
                .anchorId(liveRoom.getAnchorId())
                .anchorName(liveRoom.getAnchorName())
                .roomName(liveRoom.getRoomName())
                .description(liveRoom.getDescription())
                .roomStatus(liveRoom.getRoomStatus())
                .category(liveRoom.getCategory())
                .coverUrl(liveRoom.getCoverUrl())
                .streamUrl(liveRoom.getStreamUrl())
                .maxViewers(liveRoom.getMaxViewers())
                .startTime(liveRoom.getStartTime())
                .endTime(liveRoom.getEndTime())
                .totalViewers(liveRoom.getTotalViewers())
                .totalEarnings(liveRoom.getTotalEarnings())
                .createTime(liveRoom.getCreateTime())
                .updateTime(liveRoom.getUpdateTime())
                .build();
    }

    /**
     * 查询直播间信息（返回VO，包含实时在线人数）
     */
    public LiveRoomVO getLiveRoomVO(Long liveRoomId) {
        TraceLogger.debug("LiveRoomService", "getLiveRoomVO", 
            "查询直播间信息VO: liveRoomId=" + liveRoomId);

        LiveRoom liveRoom = facade.liveRoom().getLiveRoomInfo(liveRoomId);
        if (liveRoom == null) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_FOUND, "直播间不存在");
        }

        LiveRoomVO vo = convertToVO(liveRoom);

        // 从Redis获取当前在线人数
        if (vo.getRoomStatus() == 1) {
            String viewersKey = "live:room:viewers:" + liveRoomId;
            Object viewers = redisTemplate.opsForValue().get(viewersKey);
            if (viewers != null) {
                vo.setCurrentViewers(((Number) viewers).longValue());
            }
        }

        return vo;
    }
}
