package com.liveroom.mock.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.*;
import java.util.List;

/**
 * 创建观众请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAudienceRequestDTO {

    @NotBlank(message = "观众昵称不能为空")
    @Size(max = 128, message = "昵称不能超过128个字符")
    private String nickname;

    @Min(value = 0, message = "性别值必须为0或1")
    @Max(value = 1, message = "性别值必须为0或1")
    private Integer gender;

    @Min(value = 0, message = "年龄不能小于0")
    @Max(value = 150, message = "年龄不能大于150")
    private Integer age;

    @Size(max = 200, message = "头像URL不能超过200个字符")
    private String avatarUrl;

    private List<String> tags;

    // 是否为Bot
    @Builder.Default
    private Boolean isBot = false;

    // 消费等级：0-低、1-中、2-高
    @Min(value = 0, message = "消费等级必须为0、1或2")
    @Max(value = 2, message = "消费等级必须为0、1或2")
    private Integer consumptionLevel;
}
