package com.liveroom.anchor.controller;

import com.liveroom.anchor.service.CommissionRateService;
import common.annotation.Log;
import common.exception.ValidationException;
import common.logger.TraceLogger;
import common.response.BaseResponse;
import common.response.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 分成比例查询Controller
 * 从财务服务动态查询主播分成比例
 * 
 * @author Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/commission")
@Slf4j
@Validated
public class CommissionRateController {

    @Autowired
    private CommissionRateService commissionRateService;

    /**
     * 查询主播当前生效的分成比例
     * GET /api/v1/commission/{anchorId}/current
     */
    @GetMapping("/{anchorId}/current")
    @Log("查询主播分成比例")
    public BaseResponse<Map<String, Object>> getCurrentCommissionRate(
            @PathVariable Long anchorId) {

        if (anchorId == null || anchorId <= 0) {
            throw new ValidationException("主播ID不合法");
        }

        TraceLogger.debug("CommissionRateController", "getCurrentCommissionRate",
                "查询主播分成比例: anchorId=" + anchorId);

        BigDecimal commissionRate = commissionRateService.getCurrentCommissionRate(anchorId);

        Map<String, Object> result = new HashMap<>();
        result.put("anchorId", anchorId);
        result.put("commissionRate", commissionRate);
        result.put("description", "当前生效的分成比例");

        return ResponseUtil.success(result);
    }

    /**
     * 清除主播分成比例缓存（管理接口）
     * DELETE /api/v1/commission/{anchorId}/cache
     */
    @DeleteMapping("/{anchorId}/cache")
    @Log("清除分成比例缓存")
    public BaseResponse<String> clearCommissionCache(@PathVariable Long anchorId) {

        if (anchorId == null || anchorId <= 0) {
            throw new ValidationException("主播ID不合法");
        }

        TraceLogger.info("CommissionRateController", "clearCommissionCache",
                "清除分成比例缓存: anchorId=" + anchorId);

        commissionRateService.clearCommissionCache(anchorId);

        return ResponseUtil.success("缓存已清除");
    }
}
