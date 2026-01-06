package com.liveroom.mock.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.*;
import java.util.List;

/**
 * 创建主播请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAnchorRequestDTO {

    @NotBlank(message = "主播名称不能为空")
    @Size(max = 128, message = "主播名称不能超过128个字符")
    private String anchorName;

    @Min(value = 0, message = "性别值必须为0或1")
    @Max(value = 1, message = "性别值必须为0或1")
    private Integer gender;

    @Size(max = 500, message = "个人简介不能超过500个字符")
    private String bio;

    @Size(max = 200, message = "头像URL不能超过200个字符")
    private String avatarUrl;

    private List<String> tags;
}
