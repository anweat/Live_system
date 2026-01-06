package common.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 直播间信息DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LiveRoomDTO extends BaseDTO {

    private static final long serialVersionUID = 1L;

    /** 直播间ID */
    private Long roomId;

    /** 主播ID */
    @NotNull(message = "主播ID不能为空")
    private Long anchorId;

    /** 直播间标题 */
    @NotBlank(message = "直播间标题不能为空")
    private String title;

    /** 直播间描述 */
    private String description;

    /** 直播间分类 */
    private String category;

    /** 直播间封面URL */
    private String coverUrl;

    /** 直播间状态: OFFLINE(0), LIVE(1), FINISHED(2), BANNED(3) */
    @Min(value = 0)
    @Max(value = 3)
    private Integer status;

    /** 当前在线人数 */
    @Min(value = 0)
    private Integer currentAudienceCount;

    /** 累计观众数 */
    @Min(value = 0)
    private Integer totalAudienceCount;

    /** 是否需要PK */
    private Boolean needsPK;

    /** 是否允许礼物 */
    private Boolean allowsGifts;

    /** 是否开启全景模式 */
    private Boolean isPanoramaEnabled;

    /** 操作类型：START为开启直播，END为结束直播，UPDATE为更新信息 */
    private String operationType;

    /**
     * 验证直播间DTO的有效性
     */
    public boolean validate() {
        if (anchorId == null || anchorId <= 0) {
            return false;
        }
        if (title == null || title.trim().isEmpty() || title.length() > 256) {
            return false;
        }
        if (status != null && (status < 0 || status > 3)) {
            return false;
        }
        return true;
    }

    /**
     * 判断直播间是否在直播
     */
    public boolean isLiving() {
        return status != null && status == 1;
    }

    /**
     * 判断直播间是否已下线
     */
    public boolean isOffline() {
        return status != null && status == 0;
    }
}
