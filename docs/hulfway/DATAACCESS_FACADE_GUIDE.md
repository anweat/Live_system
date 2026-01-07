# DataAccessFacade 使用指南

**作者**: GitHub Copilot  
**创建日期**: 2026-01-06  
**版本**: 1.0

---

## 📌 核心原则

在 anchor-service 和 audience-service 中，**所有数据库操作都必须通过 `DataAccessFacade` 门面进行**，禁止直接使用 Repository。

---

## 🚀 快速开始

### ✅ 正确的做法

```java
@Service
public class MyService {
    
    @Autowired
    private DataAccessFacade dataAccessFacade;
    
    public void myMethod() {
        // 获取观众
        Audience audience = dataAccessFacade.audience().findById(id).orElse(null);
        
        // 创建观众
        Audience newAudience = dataAccessFacade.audience().createAudience(audience);
        
        // 更新观众
        dataAccessFacade.audience().updateAudience(audience);
        
        // 获取打赏记录
        Recharge recharge = dataAccessFacade.recharge().findById(id).orElse(null);
        
        // 创建打赏
        Recharge newRecharge = dataAccessFacade.recharge().createRecharge(recharge);
    }
}
```

### ❌ 错误的做法

```java
@Service
public class MyService {
    
    @Autowired
    private AudienceRepository audienceRepository;  // ❌ 禁止
    
    @Autowired
    private RechargeRepository rechargeRepository;  // ❌ 禁止
    
    public void myMethod() {
        // ❌ 这样做会违反架构规范
        Audience audience = audienceRepository.findById(id).orElse(null);
        audienceRepository.save(audience);
    }
}
```

---

## 📚 DataAccessFacade API 参考

### 观众相关 (`dataAccessFacade.audience()`)

```java
// 返回: AudienceService from common 模块

// 查询方法
Optional<Audience> findById(Long id);
Optional<Audience> findByNickname(String nickname);
List<Audience> findByConsumptionLevel(Integer level);
List<Audience> findByVipLevel(Integer level);
Page<Audience> findByConsumptionLevel(Integer level, Pageable pageable);
Page<Audience> findAll(Pageable pageable);
Page<Audience> searchByKeyword(String keyword, Pageable pageable);

// 创建/更新
Audience createAudience(Audience audience);
Audience updateAudience(Audience audience);

// 统计/其他
boolean isAudience(Long userId);
Long count();
```

### 打赏相关 (`dataAccessFacade.recharge()`)

```java
// 返回: RechargeService from common 模块

// 查询方法
Optional<Recharge> findById(Long id);
Optional<Recharge> findByTraceId(String traceId);
Page<Recharge> findByAnchorId(Long anchorId, Pageable pageable);
Page<Recharge> findByAudienceId(Long audienceId, Pageable pageable);
Page<Recharge> findByLiveRoomId(Long liveRoomId, Pageable pageable);
Page<Recharge> findTop10ByAnchorAndTimeRange(Long anchorId, LocalDateTime start, LocalDateTime end, Pageable pageable);
List<Recharge> findUnsyncedRecharges(Pageable pageable);

// 创建/更新
Recharge createRecharge(Recharge recharge);
Recharge updateRecharge(Recharge recharge);
```

### 主播相关 (`dataAccessFacade.anchor()`)

```java
// 返回: AnchorService from common 模块

// 查询方法
Optional<Anchor> findById(Long id);
Optional<Anchor> findByUserId(Long userId);

// 创建/更新
Anchor createAnchor(Anchor anchor);
Anchor updateAnchor(Anchor anchor);
```

### 直播间相关 (`dataAccessFacade.liveRoom()`)

```java
// 返回: LiveRoomService from common 模块

// 查询方法
Optional<LiveRoom> findById(Long id);
LiveRoom getLiveRoomInfo(Long id);
LiveRoom getLiveRoomByAnchor(Long anchorId);

// 创建/更新
LiveRoom createLiveRoom(LiveRoom liveRoom);
LiveRoom updateLiveRoom(LiveRoom liveRoom);

// 业务方法
void startBroadcast(Long liveRoomId);
void endBroadcast(Long liveRoomId);
```

### 同步进度相关 (`dataAccessFacade.syncProgress()`)

```java
// 返回: SyncProgressService from common 模块

// 查询方法
SyncProgress findBySourceServiceAndTargetService(String source, String target);
```

### 其他相关

```java
// 结算相关
dataAccessFacade.settlement()

// 提现相关
dataAccessFacade.withdrawal()

// 用户相关
dataAccessFacade.user()

// 分成比例相关
dataAccessFacade.commissionRate()
```

---

## 🔄 常见使用场景

### 场景1: 创建用户并关联数据

```java
@Service
public class UserService {
    
    @Autowired
    private DataAccessFacade dataAccessFacade;
    
    public Audience createAudienceWithStats(AudienceDTO dto) {
        // 1. 检查昵称是否存在
        if (dataAccessFacade.audience().findByNickname(dto.getNickname()).isPresent()) {
            throw new BusinessException("昵称已存在");
        }
        
        // 2. 创建观众
        Audience audience = new Audience();
        audience.setNickname(dto.getNickname());
        // ... 设置其他字段 ...
        Audience saved = dataAccessFacade.audience().createAudience(audience);
        
        return saved;
    }
}
```

### 场景2: 查询并更新

```java
public void updateAudienceConsumption(Long audienceId, BigDecimal amount) {
    // 1. 查询观众
    Audience audience = dataAccessFacade.audience().findById(audienceId)
        .orElseThrow(() -> new BusinessException("观众不存在"));
    
    // 2. 更新统计信息
    audience.setTotalRechargeAmount(
        audience.getTotalRechargeAmount().add(amount)
    );
    audience.setTotalRechargeCount(
        audience.getTotalRechargeCount() + 1
    );
    
    // 3. 保存回数据库
    dataAccessFacade.audience().updateAudience(audience);
}
```

### 场景3: 分页查询

```java
public Page<AudienceDTO> listAudiences(Integer page, Integer size) {
    Pageable pageable = PageRequest.of(page - 1, size);
    Page<Audience> audiences = dataAccessFacade.audience().findAll(pageable);
    return audiences.map(a -> convertToDTO(a));
}
```

### 场景4: 事务操作

```java
@Transactional
public void processReward(RechargeDTO rechargeDTO) {
    // 1. 创建打赏记录
    Recharge recharge = convertDTOToEntity(rechargeDTO);
    Recharge saved = dataAccessFacade.recharge().createRecharge(recharge);
    
    // 2. 更新观众消费统计
    dataAccessFacade.audience()
        .incrementRecharge(
            rechargeDTO.getAudienceId(),
            rechargeDTO.getAmount(),
            1L
        );
    
    // 3. 加入同步队列（如果需要）
    // ...
    
    // 事务在方法结束时自动提交
}
```

---

## ⚠️ 常见错误

### 错误1: 导入错误的 Service

```java
// ❌ 错误：导入了 audience-service 的 Service
import com.liveroom.audience.service.AudienceService;

// ✅ 正确：导入 common 的 DataAccessFacade
import common.service.DataAccessFacade;
```

### 错误2: 直接注入 Repository

```java
// ❌ 错误
@Autowired
private AudienceRepository repo;

// ✅ 正确
@Autowired
private DataAccessFacade facade;
```

### 错误3: 在错误的模块使用

```java
// ❌ 错误：在 finance-service 中直接使用 audience-service 的 Service
import com.liveroom.audience.service.AudienceService;
@Autowired
private AudienceService audienceService;

// ✅ 正确：使用 DataAccessFacade
@Autowired
private DataAccessFacade dataAccessFacade;
dataAccessFacade.audience().findById(id);
```

---

## 🔍 调试技巧

### 1. 检查依赖注入

```java
@Service
public class MyService {
    
    @Autowired
    private DataAccessFacade dataAccessFacade;
    
    @PostConstruct
    public void checkDependencies() {
        System.out.println("Audience Service: " + dataAccessFacade.audience());
        System.out.println("Recharge Service: " + dataAccessFacade.recharge());
    }
}
```

### 2. 追踪 SQL 操作

所有 SQL 操作都会通过门面层，可以在 DataAccessFacade 添加日志：

```java
public AudienceService audience() {
    log.debug("Accessing audience service");  // 会记录访问日志
    return audienceService;
}
```

### 3. 单元测试

```java
@RunWith(SpringRunner.class)
@SpringBootTest
public class MyServiceTest {
    
    @MockBean
    private DataAccessFacade dataAccessFacade;
    
    @Autowired
    private MyService myService;
    
    @Test
    public void testMyMethod() {
        // Mock DataAccessFacade 的行为
        Audience mockAudience = new Audience();
        mockAudience.setId(1L);
        
        when(dataAccessFacade.audience().findById(1L))
            .thenReturn(Optional.of(mockAudience));
        
        // 测试你的方法
        myService.doSomething(1L);
        
        // 验证调用
        verify(dataAccessFacade.audience()).findById(1L);
    }
}
```

---

## 📋 代码审查清单

在审查 audience-service 和 anchor-service 的代码时，检查以下项目：

- [ ] 是否直接注入了 Repository？应使用 DataAccessFacade
- [ ] 是否直接调用了 Repository？应通过 dataAccessFacade 进行
- [ ] 是否从其他模块导入了 Service？应使用 DataAccessFacade
- [ ] 是否正确处理了 Optional 返回值？
- [ ] 是否遵循了事务处理规范？
- [ ] 是否记录了操作日志？

---

## 📞 常见问题 (FAQ)

### Q1: 为什么不能直接使用 Repository？

**A**: 为了统一管理数据访问逻辑，包括：
- 缓存策略的统一实施
- 事务控制的统一管理
- 日志和审计的统一处理
- 数据验证的统一执行

### Q2: 如果 DataAccessFacade 没有我需要的方法怎么办？

**A**: 
1. 先检查底层 Service 是否有该方法
2. 如果没有，可以在 common 模块的对应 Service 中添加
3. 然后在 DataAccessFacade 中公开该方法
4. **不要**绕过门面直接使用 Repository

### Q3: DataAccessFacade 会不会成为性能瓶颈？

**A**: 不会。DataAccessFacade 只是一个访问入口，实际的数据库操作仍然是异步的。它反而可以通过统一的缓存策略提升性能。

### Q4: 是否所有微服务都要使用 DataAccessFacade？

**A**: 是的。所有依赖数据访问的微服务都应该使用 DataAccessFacade 来保持架构一致性。

---

## 🎓 学习资源

- 📖 `DATABASE_ACCESS_AUDIT_REPORT.md` - 详细的审计报告
- 📖 `REFACTORING_COMPLETE_SUMMARY.md` - 重构完成总结
- 📖 `VERIFICATION_CHECKLIST.md` - 验证检查清单

---

**最后更新**: 2026-01-06  
**维护者**: 架构团队

