package com.liveroom.finance.service;

import com.liveroom.finance.config.RedisLockUtil;
import com.liveroom.finance.dto.WithdrawalRequestDTO;
import common.bean.Withdrawal;
import common.constant.ErrorConstants;
import common.constant.StatusConstants;
import common.dto.WithdrawalDTO;
import common.exception.BusinessException;
import common.exception.SystemException;
import common.logger.TraceLogger;
import common.repository.WithdrawalRepository;
import common.util.EncryptUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 提现服务
 * 处理主播提现申请、审核等业务（幂等性设计）
 */
@Service
@Slf4j
public class WithdrawalService {

    @Autowired
    private WithdrawalRepository withdrawalRepository;

    @Autowired
    private SettlementService settlementService;

    @Autowired
    private RedisLockUtil redisLockUtil;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String WITHDRAWAL_CACHE_KEY = "finance:withdrawal:trace:";
    private static final String WITHDRAWAL_LOCK_KEY = "withdrawal:anchor:";

    /**
     * 申请提现（幂等性保证）
     */
    @Transactional(rollbackFor = Exception.class)
    public WithdrawalDTO applyWithdrawal(WithdrawalRequestDTO requestDTO) {
        String traceId = requestDTO.getTraceId();
        Long anchorId = requestDTO.getAnchorId();

        TraceLogger.info("WithdrawalService", "applyWithdrawal",
                "申请提现，主播ID: " + anchorId + ", traceId: " + traceId + ", 金额: " + requestDTO.getAmount());

        // 1. 参数验证
        if (!requestDTO.validate()) {
            throw new BusinessException(ErrorConstants.VALIDATION_FAILED, "提现参数校验失败");
        }

        // 2. 幂等性检查 - Redis缓存
        String cacheKey = WITHDRAWAL_CACHE_KEY + traceId;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof WithdrawalDTO) {
            TraceLogger.warn("WithdrawalService", "applyWithdrawal",
                    "提现申请已存在（缓存），traceId: " + traceId);
            return (WithdrawalDTO) cached;
        }

        // 3. 幂等性检查 - 数据库
        Withdrawal existing = withdrawalRepository.findByTraceId(traceId).orElse(null);
        if (existing != null) {
            TraceLogger.warn("WithdrawalService", "applyWithdrawal",
                    "提现申请已存在（数据库），traceId: " + traceId);
            WithdrawalDTO dto = convertToDTO(existing);
            // 更新缓存
            redisTemplate.opsForValue().set(cacheKey, dto, 24, TimeUnit.HOURS);
            return dto;
        }

        // 4. 获取分布式锁（防止并发提现）
        String lockKey = WITHDRAWAL_LOCK_KEY + anchorId;
        if (!redisLockUtil.tryLock(lockKey, 30)) {
            throw new BusinessException(ErrorConstants.SERVICE_ERROR, "系统繁忙，请稍后重试");
        }

        try {
            // 5. 再次检查数据库（双重检查）
            existing = withdrawalRepository.findByTraceId(traceId).orElse(null);
            if (existing != null) {
                WithdrawalDTO dto = convertToDTO(existing);
                redisTemplate.opsForValue().set(cacheKey, dto, 24, TimeUnit.HOURS);
                return dto;
            }

            // 6. 验证提现金额限制
            if (requestDTO.getAmount().compareTo(new BigDecimal("1.00")) < 0) {
                throw new BusinessException(ErrorConstants.INVALID_AMOUNT, "提现金额不能小于1.00元");
            }
            if (requestDTO.getAmount().compareTo(new BigDecimal("99999.99")) > 0) {
                throw new BusinessException(ErrorConstants.WITHDRAWAL_AMOUNT_EXCEEDS_LIMIT, "提现金额不能超过99999.99元");
            }

            // 7. 扣减可提取金额（使用悲观锁）
            settlementService.deductAvailableAmount(anchorId, requestDTO.getAmount());

            // 8. 创建提现记录
            Withdrawal withdrawal = Withdrawal.builder()
                    .anchorId(anchorId)
                    .anchorName(requestDTO.getAnchorName())
                    .withdrawalAmount(requestDTO.getAmount())
                    .withdrawalType(requestDTO.getWithdrawalType())
                    .bankName(requestDTO.getBankName())
                    .bankCardEncrypted(EncryptUtil.base64Encode(requestDTO.getAccountNumber()))
                    .accountHolder(requestDTO.getAccountHolder())
                    .status(StatusConstants.WithdrawalStatus.APPLYING) // 0-申请中
                    .traceId(traceId)
                    .appliedTime(LocalDateTime.now())
                    .version(0)
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();

            withdrawal = withdrawalRepository.save(withdrawal);

            // 9. 转换为DTO并缓存
            WithdrawalDTO dto = convertToDTO(withdrawal);
            redisTemplate.opsForValue().set(cacheKey, dto, 24, TimeUnit.HOURS);

            TraceLogger.info("WithdrawalService", "applyWithdrawal",
                    "提现申请成功，提现ID: " + withdrawal.getWithdrawalId());

            return dto;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            TraceLogger.error("WithdrawalService", "applyWithdrawal",
                    "提现申请失败，traceId: " + traceId, e);
            throw new SystemException(ErrorConstants.SYSTEM_ERROR, "提现申请失败", e);
        } finally {
            redisLockUtil.unlock(lockKey);
        }
    }

    /**
     * 查询提现记录
     */
    public Page<WithdrawalDTO> listWithdrawals(Long anchorId, Integer status, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Withdrawal> withdrawalPage = withdrawalRepository
                .findByAnchorIdAndStatus(anchorId, status, pageable);

        return withdrawalPage.map(this::convertToDTO);
    }

    /**
     * 根据traceId查询提现记录
     */
    public WithdrawalDTO getWithdrawalByTraceId(String traceId) {
        // 1. 先从缓存查询
        String cacheKey = WITHDRAWAL_CACHE_KEY + traceId;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof WithdrawalDTO) {
            return (WithdrawalDTO) cached;
        }

        // 2. 从数据库查询
        Withdrawal withdrawal = withdrawalRepository.findByTraceId(traceId)
                .orElse(null);

        if (withdrawal == null) {
            return null;
        }

        WithdrawalDTO dto = convertToDTO(withdrawal);
        // 更新缓存
        redisTemplate.opsForValue().set(cacheKey, dto, 24, TimeUnit.HOURS);

        return dto;
    }

    /**
     * 审核通过提现申请
     */
    @Transactional(rollbackFor = Exception.class)
    public void approveWithdrawal(Long withdrawalId) {
        Withdrawal withdrawal = withdrawalRepository.findById(withdrawalId)
                .orElseThrow(() -> new BusinessException(ErrorConstants.WITHDRAWAL_NOT_FOUND, "提现记录不存在"));

        if (withdrawal.getStatus() != StatusConstants.WithdrawalStatus.APPLYING) {
            throw new BusinessException(ErrorConstants.WITHDRAWAL_ALREADY_EXISTS, "提现状态不正确，无法审核");
        }

        withdrawal.setStatus(StatusConstants.WithdrawalStatus.PROCESSING); // 1-处理中
        withdrawal.setProcessedTime(LocalDateTime.now());
        withdrawal.setUpdateTime(LocalDateTime.now());
        withdrawalRepository.save(withdrawal);

        // 清除缓存
        String cacheKey = WITHDRAWAL_CACHE_KEY + withdrawal.getTraceId();
        redisTemplate.delete(cacheKey);

        TraceLogger.info("WithdrawalService", "approveWithdrawal",
                "提现审核通过，提现ID: " + withdrawalId);
    }

    /**
     * 拒绝提现申请
     */
    @Transactional(rollbackFor = Exception.class)
    public void rejectWithdrawal(Long withdrawalId, String reason) {
        Withdrawal withdrawal = withdrawalRepository.findById(withdrawalId)
                .orElseThrow(() -> new BusinessException(ErrorConstants.WITHDRAWAL_NOT_FOUND, "提现记录不存在"));

        if (withdrawal.getStatus() != StatusConstants.WithdrawalStatus.APPLYING) {
            throw new BusinessException(ErrorConstants.WITHDRAWAL_ALREADY_EXISTS, "提现状态不正确，无法拒绝");
        }

        withdrawal.setStatus(StatusConstants.WithdrawalStatus.REJECTED); // 4-已拒绝
        withdrawal.setRejectReason(reason);
        withdrawal.setProcessedTime(LocalDateTime.now());
        withdrawal.setUpdateTime(LocalDateTime.now());
        withdrawalRepository.save(withdrawal);

        // 退回金额到可提取余额
        // 注意：需要使用分布式锁防止并发
        String lockKey = WITHDRAWAL_LOCK_KEY + withdrawal.getAnchorId();
        if (redisLockUtil.tryLock(lockKey, 30)) {
            try {
                // 这里应该调用settlementService的方法退回金额
                // settlementService.refundAvailableAmount(withdrawal.getAnchorId(), withdrawal.getWithdrawalAmount());
            } finally {
                redisLockUtil.unlock(lockKey);
            }
        }

        // 清除缓存
        String cacheKey = WITHDRAWAL_CACHE_KEY + withdrawal.getTraceId();
        redisTemplate.delete(cacheKey);

        TraceLogger.info("WithdrawalService", "rejectWithdrawal",
                "提现已拒绝，提现ID: " + withdrawalId + ", 原因: " + reason);
    }

    /**
     * 转换为DTO
     */
    private WithdrawalDTO convertToDTO(Withdrawal withdrawal) {
        return WithdrawalDTO.builder()
                .withdrawalId(withdrawal.getWithdrawalId())
                .anchorId(withdrawal.getAnchorId())
                .anchorName(withdrawal.getAnchorName())
                .amount(withdrawal.getWithdrawalAmount())
                .withdrawalType(withdrawal.getWithdrawalType())
                .bankName(withdrawal.getBankName())
                .accountHolder(withdrawal.getAccountHolder())
                .status(withdrawal.getStatus())
                .statusDesc(getStatusDesc(withdrawal.getStatus()))
                .traceId(withdrawal.getTraceId())
                .rejectReason(withdrawal.getRejectReason())
                .transferSerialNumber(withdrawal.getTransferSerialNumber())
                .appliedTime(withdrawal.getAppliedTime())
                .processedTime(withdrawal.getProcessedTime())
                .createTime(withdrawal.getCreateTime())
                .updateTime(withdrawal.getUpdateTime())
                .build();
    }

    /**
     * 获取状态描述
     */
    private String getStatusDesc(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "申请中";
            case 1: return "处理中";
            case 2: return "已打款";
            case 3: return "失败";
            case 4: return "已拒绝";
            default: return "未知";
        }
    }
}
