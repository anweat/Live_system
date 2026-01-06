# 术语说明文档 - Recharge vs Top-up

## 🚨 重要：命名澄清

本项目存在一个**命名不一致**的问题，需要特别注意：

### 术语对照表

| 中文 | 正确的英文 | 项目中使用的命名 | 说明 |
|------|-----------|----------------|------|
| **充值** | Top-up / Deposit / Recharge | `topUpBalance()` | 观众往自己钱包**充钱**，增加余额 |
| **打赏** | Reward / Tip / Gift | `Recharge` 类、`createRecharge()` | 观众给主播**送钱**，扣除余额 |

### 问题根源

在本项目中，**`Recharge` 类实际上表示"打赏记录"，而不是"充值记录"**！

这是因为：
- 英文 `recharge` 的本意是"充值/再充电"
- 但在项目设计时，`recharge` 表被用来存储**打赏明细记录**
- 数据库注释明确写着："打赏明细记录表"（不是充值记录表）

### 代码中的体现

#### 1. 数据库表设计

```sql
-- DB1: 观众服务数据库
CREATE TABLE IF NOT EXISTS recharge (
    recharge_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '打赏ID',
    live_room_id BIGINT NOT NULL COMMENT '直播间ID',
    anchor_id BIGINT NOT NULL COMMENT '主播ID',
    audience_id BIGINT NOT NULL COMMENT '观众ID',
    recharge_amount DECIMAL(15, 2) NOT NULL COMMENT '打赏金额',  -- 注意：是"打赏金额"
    recharge_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '打赏时间',
    ...
) ENGINE = InnoDB COMMENT = '打赏明细记录表，核心业务表';  -- 明确注释为"打赏"

-- DB2: 财务服务数据库
CREATE TABLE IF NOT EXISTS recharge_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    original_recharge_id BIGINT NOT NULL COMMENT '原始打赏记录ID(来自DB1)',
    recharge_amount DECIMAL(15, 2) NOT NULL COMMENT '打赏金额',
    ...
) ENGINE = InnoDB COMMENT = '打赏记录表（财务服务持久化），用于结算计算和统计分析';
```

#### 2. Java实体类

```java
/**
 * 打赏记录表实体
 * 记录每一笔打赏的详细信息，支持幂等性控制
 */
@Entity
@Table(name = "recharge")
public class Recharge implements Serializable {
    private Long rechargeId;      // 打赏ID
    private Long anchorId;         // 主播ID（接收打赏的人）
    private Long audienceId;       // 观众ID（打赏的人）
    private BigDecimal rechargeAmount;  // 打赏金额
    private LocalDateTime rechargeTime; // 打赏时间
    ...
}
```

#### 3. Service层方法

```java
/**
 * 打赏业务逻辑服务（Reward/Tip Service）
 * 
 * 重要说明：在本项目中，"Recharge" 表示 "打赏/礼物"，不是 "充值"
 * - createRecharge() = 创建打赏记录（观众给主播打赏）
 * - topUpBalance() = 充值余额（给观众钱包充钱）
 */
@Service
public class RechargeService {
    
    /**
     * 创建打赏记录（Reward/Tip）- 观众给主播打赏
     * 流程：余额检查 → 扣除余额 → 保存打赏记录 → 同步队列
     */
    public RechargeDTO createRecharge(RechargeDTO rechargeDTO) {
        // 扣除观众余额（支付打赏）
        checkAndDeductBalance(audienceId, rechargeAmount);
        
        // 保存打赏记录
        Recharge recharge = new Recharge();
        recharge.setRechargeAmount(rechargeAmount);  // 打赏金额
        ...
    }
    
    /**
     * 充值虚拟余额（Top-up）- 给观众钱包充钱（测试用）
     */
    public void topUpBalance(Long audienceId, BigDecimal amount) {
        // 增加观众余额
        BigDecimal newBalance = currentBalance.add(amount);
        ...
    }
}
```

### 资金流向对比

#### 充值（Top-up）
```
观众钱包余额：1000元
      ↓ 充值500元（topUpBalance）
观众钱包余额：1500元
```

#### 打赏（Reward）
```
观众钱包余额：1000元
      ↓ 打赏主播100元（createRecharge）
观众钱包余额：900元
主播收益：+100元（通过结算系统）
```

### API端点对照

#### 充值相关（给观众钱包充钱）
```bash
# 充值 - 增加观众余额
POST /api/v1/test/wallet/topup?audienceId=1001&amount=500

# 查询余额
GET /api/v1/test/wallet/balance/1001
```

#### 打赏相关（观众给主播送钱）
```bash
# 创建打赏 - 扣除观众余额，给主播打赏
POST /api/v1/recharge
{
  "anchorId": 2001,
  "audienceId": 1001,
  "rechargeAmount": 100.00  // 打赏金额
}

# 查询打赏记录
GET /api/v1/recharge/{rechargeId}
```

### 为什么不改名？

理论上，应该将 `Recharge` 改名为 `Reward` 或 `Tip` 更合适，但：

1. **数据库表已创建**：`recharge` 表已经在使用
2. **大量代码依赖**：涉及多个服务、Repository、DTO
3. **向后兼容性**：如果改名需要数据迁移
4. **文档明确说明**：通过注释和文档澄清命名

### 最佳实践建议

#### ✅ 推荐做法

1. **在代码中使用清晰的注释**
```java
/**
 * Recharge = 打赏记录（不是充值！）
 * rechargeAmount = 打赏金额（观众给主播的金额）
 */
```

2. **方法命名要明确**
```java
topUpBalance()      // 充值（给自己钱包充钱）
createRecharge()    // 打赏（给主播送钱）
deductBalance()     // 扣款（消费打赏）
```

3. **文档要说明清楚**
- 在README中添加术语说明
- 在API文档中标注清楚
- 在测试指南中强调区别

#### ❌ 避免的错误

1. **混淆充值和打赏**
```java
// 错误示例：
rechargeBalance()  // 模糊！是充值还是打赏？

// 正确示例：
topUpBalance()     // 明确：充值
createRecharge()   // 明确：打赏（虽然命名不完美，但有注释）
```

2. **在用户界面使用错误术语**
```
❌ "充值给主播"  // 错误！应该是"打赏主播"
✅ "打赏主播"

❌ "打赏你的账户" // 错误！应该是"充值账户"
✅ "充值账户"
```

### 新项目建议

如果从零开始设计，建议使用以下命名：

| 功能 | 推荐命名 | 表名 | 实体类 |
|------|---------|------|-------|
| 充值 | Deposit | `wallet_deposit` | `WalletDeposit` |
| 打赏 | Reward/Tip/Gift | `reward` 或 `gift` | `Reward` 或 `Gift` |
| 提现 | Withdrawal | `withdrawal` | `Withdrawal` ✅ |
| 结算 | Settlement | `settlement` | `Settlement` ✅ |

注意：本项目的 `Withdrawal` 和 `Settlement` 命名是正确的！

### 总结

- **本项目约定**：`Recharge` = 打赏记录（观众给主播送钱）
- **标准英文含义**：`Recharge` = 充值（给自己账户充钱）
- **解决方案**：通过注释、文档、清晰的方法命名来区分
- **测试时注意**：
  - `topUpBalance()` = 充值（给观众钱包充钱）
  - `createRecharge()` = 打赏（观众给主播送钱）

希望这份文档能帮助理解本项目的命名约定！👍
