package com.liveroom.finance.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 分成比例DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommissionRateDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 分成比例ID
     */
    private Long commissionRateId;

    /**
     * 主播ID
     */
    @NotNull(message = "主播ID不能为空")
    private Long anchorId;

    /**
     * 主播名称
     */
    @NotBlank(message = "主播名称不能为空")
    private String anchorName;

    /**
     * 分成比例（百分比，例如70表示70%）
     */
    @NotNull(message = "分成比例不能为空")
    @DecimalMin(value = "0.0", message = "分成比例不能小于0")
    @DecimalMax(value = "100.0", message = "分成比例不能大于100")
    private Double commissionRate;

    /**
     * 生效时间
     */
    private LocalDateTime effectiveTime;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 状态：0-未启用、1-启用、2-已过期
     */
    private Integer status;

    /**
     * 操作备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
