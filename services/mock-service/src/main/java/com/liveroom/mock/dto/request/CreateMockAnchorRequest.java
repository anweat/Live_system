package com.liveroom.mock.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 创建模拟主播请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMockAnchorRequest {

    private String anchorName;

    private Integer gender;

    private String bio;

    private List<String> tags;
}
