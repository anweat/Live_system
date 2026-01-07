package com.liveroom.mock.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 创建主播请求（发送给主播服务）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAnchorRequest {

    @NotBlank(message = "主播名称不能为空")
    private String nickname;

    @NotNull(message = "性别不能为空")
    private Integer gender;

    private Integer age;

    private String avatarUrl;

    private String bio;

    private List<String> tags;
}
