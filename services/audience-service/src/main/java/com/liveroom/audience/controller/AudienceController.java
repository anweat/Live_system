package com.liveroom.audience.controller;

import java.util.List;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import common.annotation.Idempotent;
import common.annotation.Log;
import common.annotation.ValidateParam;
import common.exception.ValidationException;
import common.response.BaseResponse;
import common.response.PageResponse;
import common.response.ResponseUtil;
import common.logger.TraceLogger;
import com.liveroom.audience.dto.AudienceDTO;
import com.liveroom.audience.dto.ConsumptionStatsDTO;
import com.liveroom.audience.service.AudienceService;

/**
 * 观众相关API接口
 * 提供观众管理、查询等相关操作
 */
@RestController
@RequestMapping("/api/v1/audiences")
@Slf4j
public class AudienceController {

    @Autowired
    private AudienceService audienceService;

    /**
     * 创建观众（注册用户）
     */
    @PostMapping
    @Log("创建观众")
    @ValidateParam
    @Idempotent(key = "#audienceDTO.nickname", timeout = 30)
    public BaseResponse<AudienceDTO> createAudience(@Valid @RequestBody AudienceDTO audienceDTO) {
        AudienceDTO result = audienceService.createAudience(audienceDTO);
        return ResponseUtil.success(result, "观众创建成功");
    }

    /**
     * 创建游客观众
     */
    @PostMapping("/guest")
    @Log("创建游客观众")
    public BaseResponse<AudienceDTO> createGuestAudience(@RequestBody(required = false) AudienceDTO audienceDTO) {
        if (audienceDTO == null) {
            audienceDTO = new AudienceDTO();
        }
        AudienceDTO result = audienceService.createGuestAudience(audienceDTO);
        return ResponseUtil.success(result, "游客观众创建成功");
    }

    /**
     * 获取观众信息
     */
    @GetMapping("/{audienceId}")
    @Log("查询观众信息")
    public BaseResponse<AudienceDTO> getAudience(@PathVariable Long audienceId) {
        if (audienceId == null || audienceId <= 0) {
            throw new ValidationException("观众ID不合法");
        }
        AudienceDTO result = audienceService.getAudience(audienceId);
        if (result == null) {
            TraceLogger.info("观众不存在，audienceId=" + audienceId);
        }
        return ResponseUtil.success(result);
    }

    /**
     * 修改观众信息
     */
    @PutMapping("/{audienceId}")
    @Log("修改观众信息")
    @ValidateParam
    public BaseResponse<AudienceDTO> updateAudience(
            @PathVariable Long audienceId,
            @Valid @RequestBody AudienceDTO audienceDTO) {
        if (audienceId == null || audienceId <= 0) {
            throw new ValidationException("观众ID不合法");
        }
        if (audienceDTO == null) {
            throw new ValidationException("观众信息不能为空");
        }
        AudienceDTO result = audienceService.updateAudience(audienceId, audienceDTO);
        return ResponseUtil.success(result, "观众信息修改成功");
    }

    /**
     * 查询观众列表
     */
    @GetMapping
    @Log("查询观众列表")
    public BaseResponse<PageResponse<AudienceDTO>> listAudiences(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Integer consumptionLevel) {
        // 参数验证
        if (page == null || page < 1) {
            throw new ValidationException("页码必须从1开始");
        }
        if (size == null || size < 1 || size > 100) {
            throw new ValidationException("每页大小必须在1-100之间");
        }
        if (consumptionLevel != null && (consumptionLevel < 0 || consumptionLevel > 2)) {
            throw new ValidationException("消费等级只能是0、1、2");
        }

        Page<AudienceDTO> pageResult = audienceService.listAudiences(page, size, consumptionLevel);
        if (pageResult.getContent().isEmpty()) {
            TraceLogger.info("AudienceController", "listAudiences", "查询结果为空");
        }
        return ResponseUtil.pageSuccess(pageResult.getContent(), pageResult.getTotalElements(), page, size);
    }

    /**
     * 搜索观众
     */
    @GetMapping("/search")
    @Log("搜索观众")
    public BaseResponse<PageResponse<AudienceDTO>> searchAudiences(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        // 参数验证
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new ValidationException("搜索关键词不能为空");
        }
        if (keyword.length() > 50) {
            throw new ValidationException("搜索关键词长度不能超过50");
        }
        if (page == null || page < 1) {
            throw new ValidationException("页码必须从1开始");
        }
        if (size == null || size < 1 || size > 100) {
            throw new ValidationException("每页大小必须在1-100之间");
        }

        Page<AudienceDTO> pageResult = audienceService.searchAudiences(keyword.trim(), page, size);
        if (pageResult.getContent().isEmpty()) {
            TraceLogger.info("AudienceController", "searchAudiences", "搜索无结果: " + keyword);
        }
        return ResponseUtil.pageSuccess(pageResult.getContent(), pageResult.getTotalElements(), page, size);
    }

    /**
     * 获取观众消费统计
     */
    @GetMapping("/{audienceId}/consumption-stats")
    @Log("查询观众消费统计")
    public BaseResponse<ConsumptionStatsDTO> getConsumptionStats(@PathVariable Long audienceId) {
        if (audienceId == null || audienceId <= 0) {
            throw new ValidationException("观众ID不合法");
        }
        ConsumptionStatsDTO result = audienceService.getConsumptionStats(audienceId);
        if (result == null) {
            TraceLogger.info("观众消费统计为空，audienceId=" + audienceId);
        }
        return ResponseUtil.success(result);
    }

    /**
     * 禁用观众账户
     */
    @PutMapping("/{audienceId}/disable")
    @Log("禁用观众账户")
    public BaseResponse<Void> disableAudience(
            @PathVariable Long audienceId,
            @RequestParam(required = false) String reason) {
        if (audienceId == null || audienceId <= 0) {
            throw new ValidationException("观众ID不合法");
        }
        if (reason != null && reason.length() > 200) {
            throw new ValidationException("禁用原因长度不能超过200");
        }
        audienceService.disableAudience(audienceId, reason);
        return ResponseUtil.success(null, "观众账户已禁用");
    }

    /**
     * 启用观众账户
     */
    @PutMapping("/{audienceId}/enable")
    @Log("启用观众账户")
    public BaseResponse<Void> enableAudience(@PathVariable Long audienceId) {
        if (audienceId == null || audienceId <= 0) {
            throw new ValidationException("观众ID不合法");
        }
        audienceService.enableAudience(audienceId);
        return ResponseUtil.success(null, "观众账户已启用");
    }
}
