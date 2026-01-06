package com.liveroom.finance.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 批量打赏数据DTO
 * 用于观众服务批量推送打赏数据到财务服务
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BatchRechargeDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 批次ID（用于幂等性检查）
     */
    @NotBlank(message = "批次ID不能为空")
    private String batchId;

    /**
     * 源服务名称
     */
    @NotBlank(message = "源服务名称不能为空")
    private String sourceService;

    /**
     * 批次创建时间
     */
    @NotNull(message = "批次创建时间不能为空")
    private Long batchTime;

    /**
     * 打赏记录列表
     */
    @NotEmpty(message = "打赏记录列表不能为空")
    private List<RechargeItemDTO> recharges;

    /**
     * 批次总金额
     */
    @NotNull(message = "批次总金额不能为空")
    private BigDecimal totalAmount;

    /**
     * 批次记录数
     */
    @NotNull(message = "批次记录数不能为空")
    private Integer totalCount;

    /**
     * 打赏记录项
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RechargeItemDTO implements Serializable {
        
        private static final long serialVersionUID = 1L;

        /** 打赏记录ID */
        @NotNull(message = "打赏记录ID不能为空")
        private Long rechargeId;

        /** traceId */
        @NotBlank(message = "traceId不能为空")
        private String traceId;

        /** 主播ID */
        @NotNull(message = "主播ID不能为空")
        private Long anchorId;

        /** 主播名称 */
        private String anchorName;

        /** 观众ID */
        @NotNull(message = "观众ID不能为空")
        private Long audienceId;

        /** 观众名称 */
        private String audienceName;

        /** 打赏金额 */
        @NotNull(message = "打赏金额不能为空")
        private BigDecimal rechargeAmount;

        /** 打赏时间（时间戳） */
        @NotNull(message = "打赏时间不能为空")
        private Long rechargeTime;

        /** 打赏类型 */
        private Integer rechargeType;

        /** 直播间ID */
        private Long liveRoomId;
    }
}
