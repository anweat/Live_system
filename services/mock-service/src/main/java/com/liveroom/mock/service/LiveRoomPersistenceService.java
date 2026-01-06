package com.liveroom.mock.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liveroom.mock.entity.MockLiveRoom;
import com.liveroom.mock.repository.MockLiveRoomRepository;
import com.liveroom.mock.service.LiveRoomMockService.MockLiveRoomDTO;
import common.logger.TraceLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 直播间持久化服务
 */
@Service
@RequiredArgsConstructor
@Transactional
public class LiveRoomPersistenceService {

    private final MockLiveRoomRepository mockLiveRoomRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 保存直播间
     */
    public Long saveLiveRoom(MockLiveRoomDTO dto) {
        MockLiveRoom entity = convertToEntity(dto);
        MockLiveRoom saved = mockLiveRoomRepository.save(entity);
        return saved.getId();
    }

    /**
     * 批量保存直播间
     */
    public List<Long> saveLiveRooms(List<MockLiveRoomDTO> liveRooms) {
        TraceLogger.info("LiveRoomPersistenceService", "saveLiveRooms", 
                "批量保存 " + liveRooms.size() + " 个直播间");

        List<MockLiveRoom> entities = liveRooms.stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
        
        List<MockLiveRoom> saved = mockLiveRoomRepository.saveAll(entities);
        return saved.stream().map(MockLiveRoom::getId).collect(Collectors.toList());
    }

    /**
     * 查询所有直播中的房间
     */
    public List<MockLiveRoomDTO> findAllLiveRooms() {
        List<MockLiveRoom> rooms = mockLiveRoomRepository.findByStatus(1);
        return rooms.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * 根据标签查询直播间
     */
    public List<MockLiveRoomDTO> findLiveRoomsByTag(String tag) {
        List<MockLiveRoom> rooms = mockLiveRoomRepository.findByTag(tag);
        return rooms.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * 查询有空位的直播间
     */
    public List<MockLiveRoomDTO> findLiveRoomsWithSpace(int maxCount) {
        List<MockLiveRoom> rooms = mockLiveRoomRepository.findLiveRoomsWithSpace(maxCount);
        return rooms.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * 统计直播中的房间数
     */
    public long countLiveRooms() {
        return mockLiveRoomRepository.countLiveRooms();
    }

    /**
     * 更新直播间观众数
     */
    public void updateAudienceCount(Long roomId, int currentCount, int totalCount) {
        mockLiveRoomRepository.findById(roomId).ifPresent(room -> {
            room.setCurrentAudienceCount(currentCount);
            room.setTotalAudienceCount(totalCount);
            mockLiveRoomRepository.save(room);
        });
    }

    /**
     * 转换DTO为实体
     */
    private MockLiveRoom convertToEntity(MockLiveRoomDTO dto) {
        try {
            return MockLiveRoom.builder()
                    .id(dto.getLiveRoomId())
                    .anchorId(dto.getAnchorId())
                    .title(dto.getTitle())
                    .description(dto.getDescription())
                    .category(dto.getCategory())
                    .coverUrl(dto.getCoverUrl())
                    .status(dto.getStatus())
                    .currentAudienceCount(dto.getCurrentAudienceCount())
                    .totalAudienceCount(dto.getTotalAudienceCount())
                    .startTime(dto.getStartTime())
                    .endTime(dto.getEndTime())
                    .build();
        } catch (Exception e) {
            TraceLogger.error("LiveRoomPersistenceService", "convertToEntity", "转换失败", e);
            throw new RuntimeException("转换失败", e);
        }
    }

    /**
     * 转换实体为DTO
     */
    private MockLiveRoomDTO convertToDTO(MockLiveRoom entity) {
        return MockLiveRoomDTO.builder()
                .liveRoomId(entity.getId())
                .anchorId(entity.getAnchorId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .category(entity.getCategory())
                .coverUrl(entity.getCoverUrl())
                .status(entity.getStatus())
                .currentAudienceCount(entity.getCurrentAudienceCount())
                .totalAudienceCount(entity.getTotalAudienceCount())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .build();
    }
}
