package com.liveroom.analysis.controller;

import com.liveroom.analysis.dto.*;
import com.liveroom.analysis.service.*;
import common.logger.TraceLogger;
import common.response.BaseResponse;
import common.response.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户画像分析Controller
 */
@RestController
@RequestMapping("/api/v1/analysis/user")
@RequiredArgsConstructor
@Validated
public class UserPortraitController {

    private final UserPortraitService userPortraitService;

    /**
     * 获取用户画像
     */
    @GetMapping("/portrait/{audienceId}")
    public BaseResponse<UserPortraitDTO> getUserPortrait(
            @PathVariable @NotNull Long audienceId) {
        
        TraceLogger.info("UserPortraitController", "getUserPortrait", audienceId);

        UserPortraitDTO portrait = userPortraitService.getUserPortrait(audienceId);
        return ResponseUtil.success(portrait);
    }

    /**
     * 批量获取用户画像
     */
    @PostMapping("/portraits/batch")
    public BaseResponse<List<UserPortraitDTO>> batchGetUserPortraits(
            @RequestBody List<Long> audienceIds) {
        
        TraceLogger.info("UserPortraitController", "batchGetUserPortraits", 
            null, "count", audienceIds.size());

        List<UserPortraitDTO> portraits = userPortraitService.batchGetUserPortraits(audienceIds);
        return ResponseUtil.success(portraits);
    }
}
