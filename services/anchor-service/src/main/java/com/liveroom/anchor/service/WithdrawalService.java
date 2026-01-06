package com.liveroom.anchor.service;

import com.liveroom.anchor.feign.FinanceServiceClient;
import com.liveroom.anchor.repository.AnchorRepository;
import common.bean.user.Anchor;
import common.constant.ErrorConstants;
import common.exception.BusinessException;
import common.logger.TraceLogger;
import common.response.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * 主播提现服务
 * 直接对接财务服务处理提现逻辑
 * 
 * @author Team
 * @version 1.0.0
 */
@Service
@Slf4j
public class WithdrawalService {

    @Autowired
    private AnchorRepository anchorRepository;

    @Autowired
    private FinanceServiceClient financeServiceClient;

    /**
     * 主播申请提现（直接调用财务服务）
     */
    public FinanceServiceClient.WithdrawalVO applyWithdrawal(
            Long anchorId,
            BigDecimal amount,
            Integer withdrawalType,
            String bankName,
            String accountNumber,
            String accountHolder) {

        long startTime = System.currentTimeMillis();
        TraceLogger.info("WithdrawalService", "applyWithdrawal",
                String.format("主播申请提现: anchorId=%d, amount=%s, type=%d",
                        anchorId, amount, withdrawalType));

        // 1. 验证主播存在
        Optional<Anchor> anchorOpt = anchorRepository.findById(anchorId);
        if (!anchorOpt.isPresent()) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_FOUND, "主播不存在");
        }
        Anchor anchor = anchorOpt.get();

        // 2. 构建提现请求DTO
        FinanceServiceClient.WithdrawalRequestDTO requestDTO = 
                new FinanceServiceClient.WithdrawalRequestDTO();
        requestDTO.setTraceId("WD-" + UUID.randomUUID().toString());
        requestDTO.setAnchorId(anchorId);
        requestDTO.setAnchorName(anchor.getNickname());
        requestDTO.setAmount(amount);
        requestDTO.setWithdrawalType(withdrawalType);
        requestDTO.setBankName(bankName);
        requestDTO.setAccountNumber(accountNumber);
        requestDTO.setAccountHolder(accountHolder);

        // 3. 调用财务服务提现接口
        BaseResponse<FinanceServiceClient.WithdrawalVO> response = 
                financeServiceClient.applyWithdrawal(requestDTO);

        // 4. 检查响应
        if (response.getCode() != 0) {
            TraceLogger.error("WithdrawalService", "applyWithdrawal",
                    "财务服务提现失败: " + response.getMessage());
            throw new BusinessException(ErrorConstants.SYSTEM_ERROR, "提现失败: " + response.getMessage());
        }

        long endTime = System.currentTimeMillis();
        TraceLogger.info("WithdrawalService", "applyWithdrawal",
                String.format("提现申请成功: anchorId=%d, withdrawalId=%d, 耗时=%dms",
                        anchorId, response.getData().getWithdrawalId(), (endTime - startTime)));

        return response.getData();
    }

    /**
     * 查询提现记录
     */
    public BaseResponse<Object> listWithdrawals(Long anchorId, Integer status, 
                                                Integer page, Integer size) {
        TraceLogger.debug("WithdrawalService", "listWithdrawals",
                String.format("查询提现记录: anchorId=%d, status=%s", anchorId, status));

        // 验证主播存在
        if (!anchorRepository.existsById(anchorId)) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_FOUND, "主播不存在");
        }

        // 调用财务服务查询接口
        BaseResponse<Object> response = financeServiceClient.listWithdrawals(
                anchorId, status, page, size);

        if (response.getCode() != 0) {
            TraceLogger.error("WithdrawalService", "listWithdrawals",
                    "财务服务查询失败: " + response.getMessage());
            throw new BusinessException(ErrorConstants.SYSTEM_ERROR, "查询失败: " + response.getMessage());
        }

        return response;
    }

    /**
     * 根据traceId查询提现记录
     */
    public FinanceServiceClient.WithdrawalVO getWithdrawalByTraceId(String traceId) {
        TraceLogger.debug("WithdrawalService", "getWithdrawalByTraceId",
                "查询提现记录: traceId=" + traceId);

        // 调用财务服务查询接口
        BaseResponse<FinanceServiceClient.WithdrawalVO> response = 
                financeServiceClient.getWithdrawalByTraceId(traceId);

        if (response.getCode() != 0) {
            TraceLogger.error("WithdrawalService", "getWithdrawalByTraceId",
                    "财务服务查询失败: " + response.getMessage());
            throw new BusinessException(ErrorConstants.SYSTEM_ERROR, "查询失败: " + response.getMessage());
        }

        return response.getData();
    }
}
