package com.liveroom.anchor.controller;

import com.liveroom.anchor.dto.AnchorDTO;
import com.liveroom.anchor.service.AnchorService;
import common.annotation.Log;
import common.response.BaseResponse;
import common.exception.ValidationException;
import common.logger.TraceLogger;
import common.util.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.math.BigDecimal;

/**
 * 主播管理Controller
 * 提供主播相关的REST API接口
 * 
 * @author Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/anchors")
@Slf4j
@Validated
public class AnchorController {

    @Autowired
    private AnchorService anchorService;

    /**
     * 创建主播（注册）
     * POST /api/v1/anchors
     */
    @PostMapping
    @Log("创建主播")
    public BaseResponse<AnchorDTO> createAnchor(@Valid @RequestBody AnchorDTO anchorDTO) {
        TraceLogger.info("AnchorController", "createAnchor", 
            "创建主播请求: nickname=" + anchorDTO.getNickname());

        AnchorDTO result = anchorService.createAnchor(anchorDTO);
        return ResponseUtil.success(result);
    }

    /**
     * 查询主播信息
     * GET /api/v1/anchors/{anchorId}
     */
    @GetMapping("/{anchorId}")
    @Log("查询主播信息")
    public BaseResponse<AnchorDTO> getAnchor(@PathVariable Long anchorId) {
        if (anchorId == null || anchorId <= 0) {
            throw new ValidationException("主播ID不合法");
        }

        AnchorDTO anchor = anchorService.getAnchor(anchorId);
        return ResponseUtil.success(anchor);
    }

    /**
     * 更新主播信息
     * PUT /api/v1/anchors/{anchorId}
     */
    @PutMapping("/{anchorId}")
    @Log("更新主播信息")
    public BaseResponse<AnchorDTO> updateAnchor(
            @PathVariable Long anchorId,
            @Valid @RequestBody AnchorDTO anchorDTO) {
        
        if (anchorId == null || anchorId <= 0) {
            throw new ValidationException("主播ID不合法");
        }

        TraceLogger.info("AnchorController", "updateAnchor", 
            "更新主播信息: anchorId=" + anchorId);

        AnchorDTO result = anchorService.updateAnchor(anchorId, anchorDTO);
        return ResponseUtil.success(result);
    }

    /**
     * 查询主播列表（分页）
     * GET /api/v1/anchors?page=1&size=20&orderBy=fanCount&direction=desc
     */
    @GetMapping
    @Log("查询主播列表")
    public BaseResponse<java.util.List<AnchorDTO>> listAnchors(
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) Integer size,
            @RequestParam(required = false) String orderBy,
            @RequestParam(required = false, defaultValue = "desc") String direction) {

        java.util.List<AnchorDTO> anchors = anchorService.listAnchors(page, size, orderBy, direction);

        // 简化返回，前端自行分页
        return ResponseUtil.success(anchors);
    }

    /**
     * 查询TOP主播（按粉丝数）
     * GET /api/v1/anchors/top/fans?limit=10
     */
    @GetMapping("/top/fans")
    @Log("查询TOP主播（按粉丝数）")
    public BaseResponse<java.util.List<AnchorDTO>> listTopAnchorsByFans(
            @RequestParam(defaultValue = "10") @Min(1) Integer limit) {

        java.util.List<AnchorDTO> anchors = anchorService.listTopAnchorsByFans(limit);
        return ResponseUtil.success(anchors);
    }

    /**
     * 查询TOP主播（按收益）
     * GET /api/v1/anchors/top/earnings?limit=10
     */
    @GetMapping("/top/earnings")
    @Log("查询TOP主播（按收益）")
    public BaseResponse<java.util.List<AnchorDTO>> listTopAnchorsByEarnings(
            @RequestParam(defaultValue = "10") @Min(1) Integer limit) {

        java.util.List<AnchorDTO> anchors = anchorService.listTopAnchorsByEarnings(limit);
        return ResponseUtil.success(anchors);
    }

    /**
     * 更新主播统计数据（粉丝数、点赞数）
     * PATCH /api/v1/anchors/{anchorId}/stats
     */
    @PatchMapping("/{anchorId}/stats")
    @Log("更新主播统计数据")
    public BaseResponse<Void> updateAnchorStats(
            @PathVariable Long anchorId,
            @RequestParam(required = false) Long fanCountDelta,
            @RequestParam(required = false) Long likeCountDelta) {

        if (anchorId == null || anchorId <= 0) {
            throw new ValidationException("主播ID不合法");
        }

        if (fanCountDelta != null && fanCountDelta != 0) {
            anchorService.updateFanCount(anchorId, fanCountDelta);
        }

        if (likeCountDelta != null && likeCountDelta != 0) {
            anchorService.updateLikeCount(anchorId, likeCountDelta);
        }

        return ResponseUtil.success("统计数据更新成功");
    }

    /**
     * 更新主播累计收益
     * PATCH /api/v1/anchors/{anchorId}/earnings
     */
    @PatchMapping("/{anchorId}/earnings")
    @Log("更新主播累计收益")
    public BaseResponse<Void> updateTotalEarnings(
            @PathVariable Long anchorId,
            @RequestParam BigDecimal amount) {

        if (anchorId == null || anchorId <= 0) {
            throw new ValidationException("主播ID不合法");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            throw new ValidationException("金额不能为0");
        }

        anchorService.updateTotalEarnings(anchorId, amount);

        return ResponseUtil.success("累计收益更新成功");
    }

    /**
     * 查询主播可提取余额
     * GET /api/v1/anchors/{anchorId}/available-amount
     */
    @GetMapping("/{anchorId}/available-amount")
    @Log("查询主播可提取余额")
    public BaseResponse<BigDecimal> getAvailableAmount(@PathVariable Long anchorId) {

        if (anchorId == null || anchorId <= 0) {
            throw new ValidationException("主播ID不合法");
        }

        BigDecimal amount = anchorService.getAvailableAmount(anchorId);

        return ResponseUtil.success(amount);
    }

    /**
     * 更新主播可提取余额
     * PATCH /api/v1/anchors/{anchorId}/available-amount
     */
    @PatchMapping("/{anchorId}/available-amount")
    @Log("更新主播可提取余额")
    public BaseResponse<Void> updateAvailableAmount(
            @PathVariable Long anchorId,
            @RequestParam BigDecimal amount) {

        if (anchorId == null || anchorId <= 0) {
            throw new ValidationException("主播ID不合法");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            throw new ValidationException("金额不能为0");
        }

        anchorService.updateAvailableAmount(anchorId, amount);

        return ResponseUtil.success("可提取余额更新成功");
    }
}
