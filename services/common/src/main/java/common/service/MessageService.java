package common.service;

import common.bean.liveroom.LiveRoom;
import common.bean.liveroom.Message;
import common.bean.user.Audience;
import common.logger.TraceLogger;
import common.repository.MessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 弹幕消息业务服务层
 * 封装弹幕相关的业务逻辑，包括发送和查询
 * 使用 Repository + Service 架构，支持自动缓存
 * 
 * @author Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class MessageService extends BaseService<Message, Long, MessageRepository> {

    private final LiveRoomService liveRoomService;
    private final AudienceService audienceService;

    public MessageService(MessageRepository repository, 
                         LiveRoomService liveRoomService,
                         AudienceService audienceService) {
        super(repository);
        this.liveRoomService = liveRoomService;
        this.audienceService = audienceService;
    }

    @Override
    protected String getCachePrefix() {
        return "message::";
    }

    @Override
    protected String getEntityName() {
        return "Message";
    }

    /**
     * 保存弹幕消息
     */
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "message::liveRoom", key = "#liveRoomId"),
        @CacheEvict(value = "message::audience", key = "#audienceId")
    })
    public Message saveMessage(Long liveRoomId, Long audienceId, String content) {
        TraceLogger.info("Message", "saveMessage", 
            String.format("保存弹幕: liveRoomId=%d, audienceId=%d", liveRoomId, audienceId));

        // 1. 查询直播间和观众信息
        LiveRoom liveRoom = liveRoomService.getLiveRoomInfo(liveRoomId);
        if (liveRoom == null) {
            TraceLogger.warn("Message", "saveMessage", "直播间不存在: " + liveRoomId);
            throw new IllegalArgumentException("直播间不存在");
        }

        Audience audience = audienceService.findById(audienceId).orElse(null);
        if (audience == null) {
            TraceLogger.warn("Message", "saveMessage", "观众不存在: " + audienceId);
            throw new IllegalArgumentException("观众不存在");
        }

        // 2. 创建消息实体
        Message message = Message.builder()
            .liveRoom(liveRoom)
            .sender(audience)
            .content(content)
            .createTime(LocalDateTime.now())
            .build();

        // 3. 保存到数据库
        Message saved = repository.save(message);

        TraceLogger.debug("Message", "saveMessage", 
            String.format("弹幕保存成功: messageId=%d", saved.getMessageId()));

        return saved;
    }

    /**
     * 查询指定直播间的弹幕（带缓存）
     */
    @Cacheable(value = "message::liveRoom", key = "#liveRoomId + '-' + #pageable.pageNumber", unless = "#result == null")
    @Transactional(readOnly = true)
    public Page<Message> findByLiveRoomId(Long liveRoomId, Pageable pageable) {
        TraceLogger.debug("Message", "findByLiveRoomId", 
            String.format("查询直播间弹幕: liveRoomId=%d, page=%d", liveRoomId, pageable.getPageNumber()));

        return repository.findByLiveRoomId(liveRoomId, pageable);
    }

    /**
     * 查询指定观众的弹幕（带缓存）
     */
    @Cacheable(value = "message::audience", key = "#audienceId + '-' + #pageable.pageNumber", unless = "#result == null")
    @Transactional(readOnly = true)
    public Page<Message> findByAudienceId(Long audienceId, Pageable pageable) {
        TraceLogger.debug("Message", "findByAudienceId", 
            String.format("查询观众弹幕: audienceId=%d, page=%d", audienceId, pageable.getPageNumber()));

        return repository.findByAudienceId(audienceId, pageable);
    }

    /**
     * 查询指定时间范围内的弹幕
     */
    @Transactional(readOnly = true)
    public List<Message> findByLiveRoomIdAndTimeRange(Long liveRoomId, 
                                                       LocalDateTime startTime, 
                                                       LocalDateTime endTime) {
        TraceLogger.debug("Message", "findByLiveRoomIdAndTimeRange", 
            String.format("查询时间范围内弹幕: liveRoomId=%d, start=%s, end=%s", 
                liveRoomId, startTime, endTime));

        return repository.findByLiveRoomIdAndTimeRange(liveRoomId, startTime, endTime);
    }

    /**
     * 统计指定直播间的弹幕总数
     */
    @Transactional(readOnly = true)
    public Long countByLiveRoomId(Long liveRoomId) {
        TraceLogger.debug("Message", "countByLiveRoomId", 
            "统计直播间弹幕数: liveRoomId=" + liveRoomId);

        return repository.countByLiveRoomId(liveRoomId);
    }

    /**
     * 统计指定观众的弹幕总数
     */
    @Transactional(readOnly = true)
    public Long countByAudienceId(Long audienceId) {
        TraceLogger.debug("Message", "countByAudienceId", 
            "统计观众弹幕数: audienceId=" + audienceId);

        return repository.countByAudienceId(audienceId);
    }
}
