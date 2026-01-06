package com.liveroom.anchor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 直播间数据传输对象
 * 用于前后端数据传输
 * 
 * @author Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveRoomDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 直播间ID */
    private Long liveRoomId;

    /** 主播ID */
    @NotNull(message = "主播ID不能为空")
    private Long anchorId;

    /** 主播名称 */
    private String anchorName;

    /** 直播间名称 */
    @NotBlank(message = "直播间名称不能为空")
    @Size(min = 2, max = 256, message = "直播间名称长度为2-256个字符")
    private String roomName;

    /** 直播间描述 */
    @Size(max = 1000, message = "直播间描述最长1000个字符")
    private String description;

    /** 直播间状态：0-未开播、1-直播中、2-直播结束、3-被封禁 */
    private Integer roomStatus;

    /** 分类标签 */
    @Size(max = 128, message = "分类标签最长128个字符")
    private String category;

    /** 封面图URL */
    @Size(max = 500, message = "封面图URL最长500个字符")
    private String coverUrl;

    /** 直播流URL */
    @Size(max = 500, message = "直播流URL最长500个字符")
    private String streamUrl;

    /** 最大容纳观众数 */
    private Integer maxViewers;

    /** 上次开播时间 */
    private LocalDateTime startTime;

    /** 上次关播时间 */
    private LocalDateTime endTime;

    /** 累计观看人次 */
    private Long totalViewers;

    /** 本次直播总营收 */
    private BigDecimal totalEarnings;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
