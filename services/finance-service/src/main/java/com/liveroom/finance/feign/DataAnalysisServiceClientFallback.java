package com.liveroom.finance.feign;

import common.logger.TraceLogger;
import common.response.BaseResponse;
import common.response.ResponseUtil;
import org.springframework.stereotype.Component;

/**
 * 数据分析服务Feign客户端降级处理
 */
@Component
public class DataAnalysisServiceClientFallback implements DataAnalysisServiceClient {

    @Override
    public BaseResponse<Void> syncSettlementData(Long anchorId) {
        TraceLogger.warn("DataAnalysisServiceClientFallback", "syncSettlementData",
                "调用数据分析服务失败，进入降级处理，主播ID: " + anchorId);
        return ResponseUtil.error(500, "数据分析服务暂时不可用");
    }
}
