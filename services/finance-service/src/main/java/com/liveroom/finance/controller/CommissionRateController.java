package com.liveroom.finance.controller;

import com.liveroom.finance.dto.CommissionRateDTO;
import com.liveroom.finance.service.CommissionRateService;
import common.annotation.Log;
import common.annotation.ValidateParam;
import common.exception.ValidationException;
import common.response.BaseResponse;
import common.response.PageResponse;
import common.response.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 分成比例管理Controller
 */
@RestController
@RequestMapping("/api/v1/commission")
@Slf4j
public class CommissionRateController {

    @Autowired
    private CommissionRateService commissionRateService;

    /**
     * 创建/更新主播分成比例
     * POST /api/v1/commission
     */
    @PostMapping
    @Log("创建/更新分成比例")
    @ValidateParam
    public BaseResponse<CommissionRateDTO> createOrUpdateCommissionRate(
            @Valid @RequestBody CommissionRateDTO dto) {
        CommissionRateDTO result = commissionRateService.createOrUpdateCommissionRate(dto);
        return ResponseUtil.success("操作成功",result  );
    }

    /**
     * 查询主播当前生效的分成比例
     * GET /api/v1/commission/{anchorId}/current
     */
    @GetMapping("/{anchorId}/current")
    @Log("查询当前分成比例")
    public BaseResponse<CommissionRateDTO> getCurrentCommissionRate(@PathVariable Long anchorId) {
        if (anchorId == null || anchorId <= 0) {
            throw new ValidationException("主播ID不合法");
        }
        CommissionRateDTO result = commissionRateService.getCurrentCommissionRate(anchorId);
        return ResponseUtil.success(result);
    }

    /**
     * 查询主播分成比例历史
     * GET /api/v1/commission/{anchorId}/history
     */
    @GetMapping("/{anchorId}/history")
    @Log("查询分成比例历史")
    public BaseResponse<PageResponse<CommissionRateDTO>> getCommissionRateHistory(
            @PathVariable Long anchorId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        if (anchorId == null || anchorId <= 0) {
            throw new ValidationException("主播ID不合法");
        }
        if (page == null || page < 1) {
            throw new ValidationException("页码必须从1开始");
        }
        if (size == null || size < 1 || size > 100) {
            throw new ValidationException("每页大小必须在1-100之间");
        }

        Page<CommissionRateDTO> pageResult = commissionRateService
                .getCommissionRateHistory(anchorId, page, size);
        return ResponseUtil.success(PageResponse.of(pageResult.getContent(),
                pageResult.getTotalElements(), page, size));
    }
}
