# Finance-Service 重构 - 快速参考指南

## 🎯 快速查找

### Repository位置变更

| 功能 | 原位置 | 新位置 |
|------|--------|--------|
| 提现管理 | com.liveroom.finance.repository.WithdrawalRepository | **common.repository.WithdrawalRepository** ✨ |
| 结算管理 | com.liveroom.finance.repository.SettlementRepository | **common.repository.SettlementRepository** |
| 结算明细 | com.liveroom.finance.repository.SettlementDetailRepository | **common.repository.SettlementDetailRepository** |
| 打赏记录 | com.liveroom.finance.repository.RechargeRecordRepository | **common.repository.RechargeRecordRepository** ✨ |
| 同步进度 | com.liveroom.finance.repository.SyncProgressRepository | **common.repository.SyncProgressRepository** |

> ✨ = 新增或新增方法

### 常用Repository方法速查

#### WithdrawalRepository
```java
// 按traceId查询（幂等性）
Optional<Withdrawal> findByTraceId(String traceId);

// 检查traceId是否存在
boolean existsByTraceId(String traceId);

// 按主播ID和状态查询（分页）
Page<Withdrawal> findByAnchorIdAndStatus(Long anchorId, Integer status, Pageable pageable);

// ✨ 新增：按主播ID查询所有提现（分页，按申请时间倒序）
Page<Withdrawal> findByAnchorIdOrderByAppliedTimeDesc(Long anchorId, Pageable pageable);

// ✨ 新增：统计提现总额
Double sumWithdrawnAmountByAnchorId(Long anchorId);
```

#### RechargeRecordRepository (新增)
```java
// 按traceId查询（幂等性）
Optional<RechargeRecord> findByTraceId(String traceId);

// 查询指定主播待结算的记录
List<RechargeRecord> findUnsettledRecordsByAnchor(Long anchorId);

// 统计主播时间段打赏总额
BigDecimal sumAmountByAnchorAndTime(Long anchorId, LocalDateTime startTime, LocalDateTime endTime);

// 统计主播时间段打赏笔数
Long countByAnchorAndTime(Long anchorId, LocalDateTime startTime, LocalDateTime endTime);

// 批量更新结算状态
int batchUpdateSettlementStatus(List<Long> recordIds, Integer status, 
        LocalDateTime settlementTime, Double commissionRate, 
        BigDecimal settlementAmount, LocalDateTime updateTime);

// 查询小时级统计
List<Object[]> getHourlyStatistics(Long anchorId, LocalDateTime startTime, LocalDateTime endTime);
```

#### SettlementDetailRepository
```java
// 按主播ID查询结算明细（分页）
Page<SettlementDetail> findByAnchorIdOrderBySettlementStartTimeDesc(Long anchorId, Pageable pageable);

// 按主播ID和时间范围查询
Page<SettlementDetail> findByAnchorIdAndTimeRange(Long anchorId, LocalDateTime startTime, 
        LocalDateTime endTime, Pageable pageable);

// 按结算ID查询所有明细
List<SettlementDetail> findBySettlementIdOrderBySettlementStartTimeDesc(Long settlementId);
```

#### SettlementRepository
```java
// 按主播ID查询结算记录
Optional<Settlement> findByAnchorId(Long anchorId);

// ✨ 使用悲观锁查询（防并发）
Optional<Settlement> findByAnchorIdWithLock(Long anchorId);

// 按状态查询
List<Settlement> findByStatus(Integer status);
```

#### SyncProgressRepository
```java
// 按源和目标服务查询
Optional<SyncProgress> findBySourceServiceAndTargetService(String sourceService, String targetService);

// 检查批次ID是否存在（幂等性）
boolean existsByBatchId(String batchId);
```

---

## 💻 代码示例

### WithdrawalService 使用示例
```java
@Service
public class WithdrawalService {
    @Autowired
    private WithdrawalRepository withdrawalRepository;  // ✅ 使用common中的Repository
    
    @Transactional
    public WithdrawalDTO applyWithdrawal(WithdrawalRequestDTO request) {
        // 1. 幂等性检查
        Withdrawal existing = withdrawalRepository.findByTraceId(request.getTraceId()).orElse(null);
        if (existing != null) {
            TraceLogger.warn("WithdrawalService", "applyWithdrawal", 
                "提现申请已存在，traceId: " + request.getTraceId());
            return convertToDTO(existing);
        }
        
        // 2. 创建提现记录
        Withdrawal withdrawal = buildWithdrawal(request);
        withdrawal = withdrawalRepository.save(withdrawal);
        
        TraceLogger.info("WithdrawalService", "applyWithdrawal", 
            "提现申请成功，提现ID: " + withdrawal.getWithdrawalId());
        
        return convertToDTO(withdrawal);
    }
}
```

### SettlementService 使用示例
```java
@Service
public class SettlementService {
    @Autowired
    private RechargeRecordRepository rechargeRecordRepository;  // ✅ 使用common中的Repository
    @Autowired
    private SettlementRepository settlementRepository;
    
    @Transactional
    public void settleForAnchor(Long anchorId) {
        // 1. 查询待结算记录
        List<RechargeRecord> records = rechargeRecordRepository
            .findUnsettledRecordsByAnchor(anchorId);
        
        // 2. 计算结算金额
        BigDecimal totalAmount = records.stream()
            .map(RechargeRecord::getRechargeAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 3. 使用悲观锁更新结算记录
        Settlement settlement = settlementRepository
            .findByAnchorIdWithLock(anchorId)  // ✅ 防并发
            .orElseThrow(() -> new BusinessException(
                ErrorConstants.SETTLEMENT_NOT_FOUND, "结算记录不存在"));
        
        settlement.setSettlementAmount(settlement.getSettlementAmount().add(totalAmount));
        settlementRepository.save(settlement);
        
        TraceLogger.info("SettlementService", "settleForAnchor", 
            "结算完成，主播ID: " + anchorId + ", 金额: " + totalAmount);
    }
}
```

### StatisticsService 使用示例
```java
@Service
public class StatisticsService {
    @Autowired
    private RechargeRecordRepository rechargeRecordRepository;  // ✅ 使用common中的Repository
    
    public AnchorRevenueVO getAnchorRevenue(Long anchorId, 
            LocalDateTime startTime, LocalDateTime endTime) {
        // 查询总额
        BigDecimal totalAmount = rechargeRecordRepository
            .sumAmountByAnchorAndTime(anchorId, startTime, endTime);
        
        // 查询笔数
        Long count = rechargeRecordRepository
            .countByAnchorAndTime(anchorId, startTime, endTime);
        
        return AnchorRevenueVO.builder()
            .anchorId(anchorId)
            .totalAmount(totalAmount)
            .totalCount(count)
            .startTime(startTime)
            .endTime(endTime)
            .build();
    }
}
```

---

## 🔧 异常处理速查

### 标准异常使用
```java
// ✅ 业务异常（业务规则违反）
throw new BusinessException(ErrorConstants.VALIDATION_FAILED, "参数验证失败");
throw new BusinessException(ErrorConstants.WITHDRAWAL_NOT_FOUND, "提现记录不存在");
throw new BusinessException(ErrorConstants.WITHDRAWAL_AMOUNT_EXCEEDS_LIMIT, "提现金额超过限额");
throw new BusinessException(ErrorConstants.INSUFFICIENT_WITHDRAWAL_BALANCE, "可提取余额不足");

// ✅ 系统异常（系统错误）
throw new SystemException(ErrorConstants.SYSTEM_ERROR, "系统内部错误", e);
throw new SystemException(ErrorConstants.SERVICE_ERROR, "系统繁忙，请稍后重试");

// ✅ 验证异常（参数验证）
throw new ValidationException(ErrorConstants.INVALID_AMOUNT, "金额无效");
```

### 错误码快速查找

| 错误码 | 值 | 场景 |
|--------|-----|------|
| VALIDATION_FAILED | 100005 | 参数验证失败 |
| SERVICE_ERROR | 100007 | 系统繁忙/服务不可用 |
| INVALID_AMOUNT | 210301 | 金额无效 |
| WITHDRAWAL_NOT_FOUND | 300501 | 提现记录不存在 |
| WITHDRAWAL_AMOUNT_EXCEEDS_LIMIT | 300504 | 提现金额超过限额 |
| INSUFFICIENT_WITHDRAWAL_BALANCE | 300503 | 可提取余额不足 |
| SETTLEMENT_NOT_FOUND | 300401 | 结算记录不存在 |
| OPERATION_NOT_ALLOWED | 100003 | 操作不被允许 |

---

## 📝 日志使用速查

### TraceLogger 使用示例
```java
import common.logger.TraceLogger;

// ✅ 信息级别
TraceLogger.info("WithdrawalService", "applyWithdrawal", 
    "提现申请成功，提现ID: " + withdrawalId);

// ✅ 警告级别
TraceLogger.warn("WithdrawalService", "applyWithdrawal", 
    "提现申请已存在，traceId: " + traceId);

// ✅ 错误级别（带异常）
TraceLogger.error("WithdrawalService", "applyWithdrawal", 
    "提现申请失败，anchorId: " + anchorId, e);

// ✅ 调试级别
TraceLogger.debug("WithdrawalService", "applyWithdrawal", 
    "调试信息");
```

---

## 📊 导入变更快速对照

### Before (旧导入)
```java
import com.liveroom.finance.repository.WithdrawalRepository;
import com.liveroom.finance.repository.SettlementRepository;
import com.liveroom.finance.repository.SettlementDetailRepository;
import com.liveroom.finance.repository.RechargeRecordRepository;
import com.liveroom.finance.repository.SyncProgressRepository;
import common.exception.BusinessException;  // ✅ 这个不变
import common.logger.TraceLogger;           // ✅ 这个不变
```

### After (新导入)
```java
import common.repository.WithdrawalRepository;        // ✅ 从local改为common
import common.repository.SettlementRepository;        // ✅ 从local改为common
import common.repository.SettlementDetailRepository;  // ✅ 从local改为common
import common.repository.RechargeRecordRepository;    // ✅ 新增
import common.repository.SyncProgressRepository;      // ✅ 从local改为common
import common.exception.BusinessException;            // ✅ 无变化
import common.logger.TraceLogger;                     // ✅ 无变化
```

---

## 🚀 快速开发检查清单

在修改finance-service时，请确保：

- [ ] 使用 `common.repository.*` 而不是 `com.liveroom.finance.repository.*`
- [ ] 使用 `common.exception.BusinessException` 或 `SystemException`
- [ ] 使用 `common.logger.TraceLogger` 记录日志
- [ ] 使用 `common.constant.ErrorConstants` 中定义的错误码
- [ ] 所有新增Repository方法在common中定义
- [ ] 幂等性使用 `traceId + Redis缓存 + 数据库双重检查`
- [ ] 并发控制使用 `findByAnchorIdWithLock()` (悲观锁) 或 `RedisLockUtil` (分布式锁)
- [ ] 事务使用 `@Transactional(rollbackFor = Exception.class)`

---

## 🔗 相关文档链接

- [重构完成报告](REFACTOR_COMPLETION_REPORT.md) - 详细的重构过程
- [重构总结](REFACTOR_COMPLETE_SUMMARY.md) - 重构目标和成果
- [验证清单](MIGRATION_CHECKLIST.md) - 分阶段的验证步骤
- [Common异常指南](../common/docs/exception-guide.md) - 异常处理详细说明
- [Common日志指南](../common/docs/logger-guide.md) - 日志记录详细说明

---

## ❓ 常见问题

### Q1: Repository在哪里定义？
A: 从finance-service迁移后，所有Repository都定义在 `common/src/main/java/common/repository/` 中。

### Q2: 如何使用新的WithdrawalRepository方法？
A: 
```java
@Autowired
private WithdrawalRepository withdrawalRepository;

// 新增方法：按申请时间倒序分页查询
Page<Withdrawal> page = withdrawalRepository
    .findByAnchorIdOrderByAppliedTimeDesc(anchorId, PageRequest.of(0, 10));

// 新增方法：统计提现总额
Double total = withdrawalRepository.sumWithdrawnAmountByAnchorId(anchorId);
```

### Q3: RechargeRecordRepository中的方法有哪些？
A: 详见上面的"常用Repository方法速查"中的RechargeRecordRepository部分。

### Q4: 如何处理幂等性？
A: 使用三层检查：
```java
// 1. Redis缓存检查
String cacheKey = CACHE_KEY + traceId;
if (redisTemplate.hasKey(cacheKey)) return cached;

// 2. 数据库检查
if (repository.existsByTraceId(traceId)) return existing;

// 3. 分布式锁
if (!redisLockUtil.tryLock(lockKey, timeout)) throw error;
```

### Q5: 如何处理并发更新？
A: 使用悲观锁：
```java
Settlement settlement = settlementRepository
    .findByAnchorIdWithLock(anchorId)  // 获得排他锁
    .orElseThrow(...);

// 更新操作
settlement.setAmount(...);
settlementRepository.save(settlement);  // 提交释放锁
```

---

## 📞 获取帮助

遇到问题？按以下顺序查找：
1. 本快速参考指南（QUICK_REFERENCE.md）
2. [重构完成报告](REFACTOR_COMPLETION_REPORT.md)
3. [验证清单](MIGRATION_CHECKLIST.md)
4. Common模块文档
5. 代码注释和git提交历史

---

**最后更新**: 2026-01-06  
**版本**: 1.0  
**状态**: ✅ FINAL

