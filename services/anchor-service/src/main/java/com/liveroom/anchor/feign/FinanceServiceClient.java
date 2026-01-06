package com.liveroom.anchor.feign;

import common.bean.ApiResponse;
import common.response.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 财务服务Feign客户端
 * 用于主播服务调用财务服务的接口
 * 
 * @author Team
 * @version 1.0.0
 */
@FeignClient(name = "finance-service", fallback = FinanceServiceClientFallback.class)
public interface FinanceServiceClient {

    /**
     * 查询主播当前生效的分成比例
     * GET /api/v1/commission/{anchorId}/current
     */
    @GetMapping("/api/v1/commission/{anchorId}/current")
    BaseResponse<CommissionRateVO> getCurrentCommissionRate(@PathVariable("anchorId") Long anchorId);

    /**
     * 申请提现
     * POST /api/v1/withdrawal
     */
    @PostMapping("/api/v1/withdrawal")
    BaseResponse<WithdrawalVO> applyWithdrawal(@RequestBody WithdrawalRequestDTO requestDTO);

    /**
     * 查询提现记录
     * GET /api/v1/withdrawal/{anchorId}
     */
    @GetMapping("/api/v1/withdrawal/{anchorId}")
    BaseResponse<Object> listWithdrawals(
            @PathVariable("anchorId") Long anchorId,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size);

    /**
     * 根据traceId查询提现记录
     * GET /api/v1/withdrawal/by-trace-id/{traceId}
     */
    @GetMapping("/api/v1/withdrawal/by-trace-id/{traceId}")
    BaseResponse<WithdrawalVO> getWithdrawalByTraceId(@PathVariable("traceId") String traceId);

    /**
     * 分成比例VO（内部类）
     */
    class CommissionRateVO {
        private Long commissionRateId;
        private Long anchorId;
        private String anchorName;
        private Double commissionRate;
        private String effectiveTime;
        private String expireTime;
        private Integer status;
        private String remark;

        // Getters and Setters
        public Long getCommissionRateId() { return commissionRateId; }
        public void setCommissionRateId(Long commissionRateId) { this.commissionRateId = commissionRateId; }
        public Long getAnchorId() { return anchorId; }
        public void setAnchorId(Long anchorId) { this.anchorId = anchorId; }
        public String getAnchorName() { return anchorName; }
        public void setAnchorName(String anchorName) { this.anchorName = anchorName; }
        public Double getCommissionRate() { return commissionRate; }
        public void setCommissionRate(Double commissionRate) { this.commissionRate = commissionRate; }
        public String getEffectiveTime() { return effectiveTime; }
        public void setEffectiveTime(String effectiveTime) { this.effectiveTime = effectiveTime; }
        public String getExpireTime() { return expireTime; }
        public void setExpireTime(String expireTime) { this.expireTime = expireTime; }
        public Integer getStatus() { return status; }
        public void setStatus(Integer status) { this.status = status; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
    }

    /**
     * 提现请求DTO（内部类）
     */
    class WithdrawalRequestDTO {
        private String traceId;
        private Long anchorId;
        private String anchorName;
        private java.math.BigDecimal amount;
        private Integer withdrawalType;
        private String bankName;
        private String accountNumber;
        private String accountHolder;

        // Getters and Setters
        public String getTraceId() { return traceId; }
        public void setTraceId(String traceId) { this.traceId = traceId; }
        public Long getAnchorId() { return anchorId; }
        public void setAnchorId(Long anchorId) { this.anchorId = anchorId; }
        public String getAnchorName() { return anchorName; }
        public void setAnchorName(String anchorName) { this.anchorName = anchorName; }
        public java.math.BigDecimal getAmount() { return amount; }
        public void setAmount(java.math.BigDecimal amount) { this.amount = amount; }
        public Integer getWithdrawalType() { return withdrawalType; }
        public void setWithdrawalType(Integer withdrawalType) { this.withdrawalType = withdrawalType; }
        public String getBankName() { return bankName; }
        public void setBankName(String bankName) { this.bankName = bankName; }
        public String getAccountNumber() { return accountNumber; }
        public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
        public String getAccountHolder() { return accountHolder; }
        public void setAccountHolder(String accountHolder) { this.accountHolder = accountHolder; }
    }

    /**
     * 提现VO（内部类）
     */
    class WithdrawalVO {
        private Long withdrawalId;
        private Long anchorId;
        private String anchorName;
        private java.math.BigDecimal withdrawalAmount;
        private Integer withdrawalType;
        private String bankName;
        private String accountHolder;
        private Integer status;
        private String statusDesc;
        private String traceId;
        private String appliedTime;
        private String processedTime;
        private String rejectReason;

        // Getters and Setters
        public Long getWithdrawalId() { return withdrawalId; }
        public void setWithdrawalId(Long withdrawalId) { this.withdrawalId = withdrawalId; }
        public Long getAnchorId() { return anchorId; }
        public void setAnchorId(Long anchorId) { this.anchorId = anchorId; }
        public String getAnchorName() { return anchorName; }
        public void setAnchorName(String anchorName) { this.anchorName = anchorName; }
        public java.math.BigDecimal getWithdrawalAmount() { return withdrawalAmount; }
        public void setWithdrawalAmount(java.math.BigDecimal withdrawalAmount) { this.withdrawalAmount = withdrawalAmount; }
        public Integer getWithdrawalType() { return withdrawalType; }
        public void setWithdrawalType(Integer withdrawalType) { this.withdrawalType = withdrawalType; }
        public String getBankName() { return bankName; }
        public void setBankName(String bankName) { this.bankName = bankName; }
        public String getAccountHolder() { return accountHolder; }
        public void setAccountHolder(String accountHolder) { this.accountHolder = accountHolder; }
        public Integer getStatus() { return status; }
        public void setStatus(Integer status) { this.status = status; }
        public String getStatusDesc() { return statusDesc; }
        public void setStatusDesc(String statusDesc) { this.statusDesc = statusDesc; }
        public String getTraceId() { return traceId; }
        public void setTraceId(String traceId) { this.traceId = traceId; }
        public String getAppliedTime() { return appliedTime; }
        public void setAppliedTime(String appliedTime) { this.appliedTime = appliedTime; }
        public String getProcessedTime() { return processedTime; }
        public void setProcessedTime(String processedTime) { this.processedTime = processedTime; }
        public String getRejectReason() { return rejectReason; }
        public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
    }
}
