package com.liveroom.mock.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 批量创建Bot观众请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchCreateBotsRequest {

    @NotNull(message = "创建数量不能为空")
    @Min(value = 1, message = "至少创建1个观众")
    @Max(value = 5000, message = "单次最多创建5000个观众")
    private Integer count;

    private Boolean assignRandomTags;

    private Boolean assignConsumptionLevel;

    @Min(value = 0, message = "男性比例不能小于0")
    @Max(value = 100, message = "男性比例不能大于100")
    private Integer malePercentage;

    @Min(value = 1, message = "最小年龄不能小于1")
    private Integer minAge;

    @Max(value = 120, message = "最大年龄不能大于120")
    private Integer maxAge;
}
