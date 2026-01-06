package com.liveroom.mock.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.*;

/**
 * 批量创建Bot观众请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchCreateBotsRequestDTO {

    @NotNull(message = "创建数量不能为空")
    @Min(value = 1, message = "至少创建1个Bot")
    @Max(value = 500, message = "单次最多创建500个Bot")
    private Integer count;

    @Builder.Default
    private Boolean assignRandomTags = true;

    @Builder.Default
    private Boolean assignConsumptionLevel = true;

    // 性别分布（男性占比%，null表示随机）
    @Min(value = 0, message = "性别占比不能小于0")
    @Max(value = 100, message = "性别占比不能大于100")
    private Integer malePercentage;

    // 年龄范围
    @Min(value = 1, message = "最小年龄不能小于1")
    @Builder.Default
    private Integer minAge = 18;

    @Max(value = 100, message = "最大年龄不能大于100")
    @Builder.Default
    private Integer maxAge = 50;
}
