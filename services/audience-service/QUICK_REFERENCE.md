观众模块 - 验证完善 快速参考

========== 验证规则速查 ==========

【ID参数】
验证规则：if (id == null || id <= 0)
异常类型：new ValidationException("XXXID不合法")
示例：
  if (audienceId == null || audienceId <= 0) {
      throw new ValidationException("观众ID不合法");
  }

【分页参数】
Page验证：if (page == null || page < 1)
异常：new ValidationException("页码必须从1开始")

Size验证：if (size == null || size < 1 || size > 100)
异常：new ValidationException("每页大小必须在1-100之间")

【字符串参数】
非空验证：if (str == null || str.trim().isEmpty())
异常：new ValidationException("XXXX不能为空")

长度验证：if (str.length() > maxLength)
异常：new ValidationException("XXXX长度不能超过" + maxLength)

枚举验证：if (!str.matches("^(val1|val2|val3)$"))
异常：new ValidationException("XXXX必须为：val1、val2、val3")

【数值范围】
示例（limit 1-1000）：
  if (limit != null && (limit < 1 || limit > 1000)) {
      throw new ValidationException("查询数量必须在1-1000之间");
  }

========== 常见模式 ==========

模式1：ID验证和查询
```java
if (audienceId == null || audienceId <= 0) {
    throw new ValidationException("观众ID不合法");
}
AudienceDTO result = service.getAudience(audienceId);
if (result == null) {
    TraceLogger.info("观众不存在，audienceId=" + audienceId);
}
return ResponseUtil.success(result);
```

模式2：分页列表查询
```java
if (page == null || page < 1) {
    throw new ValidationException("页码必须从1开始");
}
if (size == null || size < 1 || size > 100) {
    throw new ValidationException("每页大小必须在1-100之间");
}
Page<DTO> result = service.listXxx(page, size);
return ResponseUtil.pageSuccess(result.getContent(), result.getTotalElements(), page, size);
```

模式3：修改操作
```java
if (id == null || id <= 0) {
    throw new ValidationException("XXXID不合法");
}
if (dto == null) {
    throw new ValidationException("XXXXX信息不能为空");
}
DTO result = service.updateXxx(id, dto);
return ResponseUtil.success(result, "修改成功");
```

模式4：枚举验证
```java
if (period == null || (!period.matches("^(day|week|month|all)$"))) {
    throw new ValidationException("时间段必须为：day、week、month、all");
}
```

========== 异常对照表 ==========

错误类型 | 异常类 | HTTP状态 | 示例
---------|--------|---------|--------
参数验证失败 | ValidationException | 400/422 | ID不合法、范围错误
业务逻辑失败 | BusinessException | 400 | 数据不存在、重复操作
系统异常 | SystemException | 500 | 数据库异常

========== 日志记录 ==========

空结果记录：
TraceLogger.info("观众不存在，audienceId=" + audienceId);
TraceLogger.info("暂无待同步的打赏记录，limit=" + limit);

异常自动记录（无需手动）：
throw new ValidationException("参数错误");
throw new BusinessException("业务错误");

========== 导入清单 ==========

必需导入：
import common.exception.ValidationException;
import common.logger.TraceLogger;
import common.response.ResponseUtil;
import common.response.BaseResponse;

========== 所有修改列表 ==========

AudienceController修改：
[✓] getAudience - ID验证
[✓] updateAudience - ID和DTO验证
[✓] getConsumptionStats - ID验证
[✓] disableAudience - ID和reason验证
[✓] enableAudience - ID验证
+ 前面已有：listAudiences, searchAudiences验证

RechargeController修改：
[✓] getRecharge - ID验证
[✓] getRechargeByTraceId - traceId验证
[✓] listAnchorRecharges - ID/page/size验证
[✓] listAudienceRecharges - ID/page/size验证
[✓] listLiveRoomRecharges - ID/page/size验证
[✓] getTop10Audiences - ID和period验证
[✓] listUnsyncedRecharges - limit验证
[✓] markRechargeAsSynced - 两个ID验证
+ 前面已有：createRecharge验证

========== 测试用例参考 ==========

单元测试示例：

1. 参数验证测试
@Test
public void testGetAudienceWithInvalidId() {
    assertThrows(ValidationException.class, 
        () -> controller.getAudience(-1L));
    assertThrows(ValidationException.class, 
        () -> controller.getAudience(null));
}

2. 业务逻辑测试
@Test
public void testGetAudienceNotFound() {
    when(service.getAudience(1L)).thenReturn(null);
    BaseResponse result = controller.getAudience(1L);
    assertNull(result.getData());
}

3. 异常处理测试
@Test
public void testValidationExceptionHandling() {
    Exception ex = assertThrows(ValidationException.class,
        () -> controller.getAudience(-1L));
    assertEquals("观众ID不合法", ex.getMessage());
}

========== 集成测试参考 ==========

API端点测试：

1. ID验证测试
GET /api/v1/audiences/-1
期望：400 或 422 错误，错误信息："观众ID不合法"

2. 分页验证测试
GET /api/v1/audiences?page=0&size=200
期望：400 或 422 错误

3. 成功查询测试
GET /api/v1/audiences/1
期望：200 成功，返回观众数据或null

========== 常见问题 ==========

Q1: 为什么同时在Controller和Service验证？
A: Controller验证请求格式，Service验证业务逻辑。
   双层验证确保数据安全和业务规则遵守。

Q2: 为什么null结果要记录日志？
A: 便于调试和性能分析。帮助发现数据异常或查询问题。

Q3: 分页大小为什么限制在1-100？
A: 防止恶意大查询导致内存溢出和性能问题。

Q4: traceId为什么最多64字符？
A: UUID标准长度为36字符，留余量防止溢出。

Q5: 为什么period必须是指定值？
A: 防止时间计算错误，只允许day/week/month/all。

========== 性能优化建议 ==========

1. 缓存热点查询
   - getAudience(audienceId)
   - getTop10Audiences()
   - 使用CacheKeyUtil生成缓存键

2. 分页查询优化
   - 建立索引：创建时间、修改时间
   - 使用limit限制结果数量
   - 考虑分区表处理大数据量

3. 异步处理
   - updateConsumptionStats异步执行
   - 数据同步任务定时执行

========== 安全加固建议 ==========

1. 参数白名单验证
   - 使用正则表达式验证枚举值
   - 检查字符串长度限制
   - ID必须是正整数

2. SQL注入防护
   - 已使用JPA参数绑定
   - 不拼接SQL字符串
   - 继续保持现有做法

3. 业务数据保护
   - 验证数据存在性
   - 检查操作权限（建议在后续完善）
   - 记录审计日志

========== 版本控制 ==========

修改版本：v1.0
修改日期：2024年
修改人：AI Assistant
修改内容：完善参数验证和业务逻辑验证
相关文档：
- VALIDATION_ENHANCEMENT.md
- VALIDATION_CHECKLIST.md
- ENHANCEMENT_REPORT.md
- CHANGES.md

========== 快速开始 ==========

1. 查看修改内容
   cat CHANGES.md

2. 查看详细文档
   cat docs/VALIDATION_ENHANCEMENT.md

3. 查看检查清单
   cat docs/VALIDATION_CHECKLIST.md

4. 查看总结报告
   cat docs/ENHANCEMENT_REPORT.md

5. 编译验证
   mvn clean compile

6. 运行测试
   mvn test

7. 查看修改的文件
   - src/main/java/com/liveroom/audience/controller/AudienceController.java
   - src/main/java/com/liveroom/audience/controller/RechargeController.java

========== 联系和反馈 ==========

如有问题或改进建议，请：
1. 查看VALIDATION_ENHANCEMENT.md获取详细说明
2. 查看VALIDATION_CHECKLIST.md验证覆盖范围
3. 参考ENHANCEMENT_REPORT.md了解设计思路
4. 参考本文档的常见问题部分

========== 验证完毕 ==========

✓ 所有参数验证已添加
✓ 所有异常处理已完成
✓ 所有日志记录已增强
✓ 代码编译无错误
✓ 文档已完成
✓ 准备就绪可以测试和部署

