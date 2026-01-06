# 微服务快速集成 Common 模块指南

## 🎯 5 分钟快速集成

### 前置条件

- ✅ 已编译并安装 common 模块到本地 Maven 仓库
- ✅ 待集成的微服务已存在

---

## 📝 集成步骤

### 第 1 步：编译并安装 common（如果还未安装）

```bash
cd services/common
mvn clean install
```

预期输出：

```
[INFO] Installing .../common/pom.xml to ~/.m2/repository/com/liveroom/common/1.0.0/common-1.0.0.pom
[INFO] Installing .../common/target/common-1.0.0.jar to ~/.m2/repository/com/liveroom/common/1.0.0/common-1.0.0.jar
```

### 第 2 步：编辑微服务的 pom.xml

以 `anchor-service` 为例，编辑 `services/anchor-service/pom.xml`：

```xml
<dependencies>

    <!-- ⭐ 添加 common 依赖（放在最前面） -->
    <dependency>
        <groupId>com.liveroom</groupId>
        <artifactId>common</artifactId>
        <version>1.0.0</version>
    </dependency>

    <!-- ⭐ 其他依赖 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <dependency>
        <groupId>org.mybatis.spring.boot</groupId>
        <artifactId>mybatis-spring-boot-starter</artifactId>
        <version>2.2.0</version>
    </dependency>

    <!-- ... 其他依赖 ... -->
</dependencies>
```

### 第 3 步：更新 IDE 和编译

在项目根目录执行：

```bash
cd services
mvn clean compile
```

或在 IDE 中刷新 Maven 项目

### 第 4 步：更新 Controller（示例）

编辑 `anchor-service/src/.../controller/AnchorController.java`：

**之前**:

```java
@RestController
@RequestMapping("/api/anchor")
public class AnchorController {

    @GetMapping("/{id}")
    public Map<String, Object> getAnchor(@PathVariable Long id) {
        Anchor anchor = service.getAnchor(id);

        // 手动构造响应
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("message", "成功");
        response.put("data", anchor);
        response.put("timestamp", System.currentTimeMillis());

        return response;
    }
}
```

**之后**:

```java
import common.response.BaseResponse;
import common.util.ResponseUtil;
import common.dto.AnchorDTO;
import common.exception.BusinessException;
import common.constant.ErrorConstants;

@RestController
@RequestMapping("/api/anchor")
public class AnchorController {

    @Autowired
    private AnchorService service;

    @GetMapping("/{id}")
    public BaseResponse<AnchorDTO> getAnchor(@PathVariable Long id) {
        // 调用服务
        Anchor anchor = service.getAnchor(id);
        if (anchor == null) {
            throw new BusinessException(
                ErrorConstants.ANCHOR_NOT_FOUND,
                "主播不存在"
            );
        }

        // 转换为 DTO
        AnchorDTO dto = convertToDTO(anchor);

        // 返回统一格式的响应
        return ResponseUtil.success(dto);
    }
}
```

### 第 5 步：更新 Service（示例）

编辑 `anchor-service/src/.../service/AnchorService.java`：

**之前**:

```java
@Service
public class AnchorService {

    public Anchor getAnchor(Long id) {
        Anchor anchor = mapper.selectById(id);
        if (anchor == null) {
            // 返回 null 或手动处理错误
            return null;
        }
        return anchor;
    }
}
```

**之后**:

```java
import common.exception.BusinessException;
import common.exception.SystemException;
import common.constant.ErrorConstants;
import common.logger.AppLoggerExt;

@Service
public class AnchorService {

    @Autowired
    private AnchorMapper mapper;

    public Anchor getAnchor(Long id) {
        try {
            Anchor anchor = mapper.selectById(id);

            // 业务异常 - 主播不存在
            if (anchor == null) {
                throw new BusinessException(
                    ErrorConstants.ANCHOR_NOT_FOUND,
                    "主播不存在"
                );
            }

            // 业务异常 - 主播已禁用
            if (anchor.getStatus() == StatusConstants.AccountStatus.BANNED) {
                throw new BusinessException(
                    ErrorConstants.ANCHOR_BANNED,
                    "该主播已被禁用"
                );
            }

            AppLoggerExt.logBusinessEvent("ANCHOR_QUERY", "id=" + id);
            return anchor;

        } catch (Exception e) {
            // 系统异常 - 数据库错误
            throw new SystemException(
                ErrorConstants.SYSTEM_ERROR,
                "查询主播信息失败",
                e
            );
        }
    }
}
```

---

## 📚 可用的类和方法

### 异常类

```java
import common.exception.*;

// 业务异常 - 用户不存在、主播不存在等
throw new BusinessException(errorCode, message);

// 验证异常 - 参数不合法
throw new ValidationException(errorCode, message);

// 系统异常 - 数据库错误、服务异常
throw new SystemException(errorCode, message, cause);
```

### 常量

```java
import common.constant.*;

// 状态常量
int type = StatusConstants.UserType.REGISTERED;
int status = StatusConstants.AccountStatus.NORMAL;
int roomStatus = StatusConstants.RoomStatus.LIVE;

// 错误码
int code = ErrorConstants.USER_NOT_FOUND;
String msg = ErrorConstants.getErrorMessage(code);

// 系统常量
int pageSize = SystemConstants.DEFAULT_PAGE_SIZE;
BigDecimal minAmount = SystemConstants.MIN_RECHARGE_AMOUNT;
boolean valid = SystemConstants.validateAmount(amount);
```

### 响应格式

```java
import common.response.*;
import common.util.ResponseUtil;

// 返回成功响应
BaseResponse<UserDTO> response = ResponseUtil.success(user);

// 返回失败响应
BaseResponse<Void> response = ResponseUtil.error(
    ErrorConstants.USER_NOT_FOUND
);

// 返回分页响应
PageResponse<UserDTO> response = ResponseUtil.pageSuccess(
    items, total, pageNo, pageSize
);
```

### DTO

```java
import common.dto.*;

// 所有 DTO 都支持 @Valid 验证
@PostMapping("/recharge")
public BaseResponse<Void> recharge(@Valid @RequestBody RechargeDTO dto) {
    // 如果验证失败，会自动返回验证错误
    service.recharge(dto);
    return ResponseUtil.success("充值成功");
}
```

---

## 🔍 验证集成成功

### 1. 编译成功

```bash
mvn clean compile
```

应该没有错误

### 2. 导入成功

在任何 Java 文件中添加：

```java
import common.exception.BusinessException;
import common.response.BaseResponse;
import common.util.ResponseUtil;
```

IDE 应该能正常识别这些类

### 3. 依赖树验证

```bash
mvn dependency:tree | grep common
```

应该看到：

```
[INFO] +- com.liveroom:common:jar:1.0.0:compile
```

---

## 🚀 完整示例

### 场景：获取主播信息

**Controller**:

```java
@RestController
@RequestMapping("/api/anchor")
public class AnchorController {

    @Autowired
    private AnchorService service;

    @GetMapping("/{id}")
    public BaseResponse<AnchorDTO> getAnchor(@PathVariable Long id) {
        AnchorDTO anchor = service.getAnchorDTO(id);
        return ResponseUtil.success(anchor);
    }
}
```

**Service**:

```java
@Service
public class AnchorService {

    @Autowired
    private AnchorMapper mapper;

    public AnchorDTO getAnchorDTO(Long id) {
        // 查询数据
        Anchor anchor = mapper.selectById(id);

        // 验证存在
        if (anchor == null) {
            throw new BusinessException(
                ErrorConstants.ANCHOR_NOT_FOUND,
                "主播不存在"
            );
        }

        // 验证状态
        if (!isAnchorActive(anchor)) {
            throw new BusinessException(
                ErrorConstants.ANCHOR_BANNED,
                "该主播已被禁用"
            );
        }

        // 转换并返回
        return convertToDTO(anchor);
    }

    private AnchorDTO convertToDTO(Anchor anchor) {
        return AnchorDTO.builder()
            .anchorId(anchor.getAnchorId())
            .userId(anchor.getUserId())
            .nickname(anchor.getNickname())
            .avatarUrl(anchor.getAvatarUrl())
            .level(anchor.getLevel())
            .totalIncome(anchor.getTotalIncome())
            .build();
    }
}
```

**DTO**:

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnchorDTO extends BaseDTO {
    private Long anchorId;
    private Long userId;
    private String nickname;
    private String avatarUrl;
    private Integer level;
    private BigDecimal totalIncome;
}
```

**响应示例**:

```json
{
  "code": 0,
  "message": "成功",
  "data": {
    "anchorId": 1,
    "userId": 10001,
    "nickname": "主播小王",
    "avatarUrl": "http://...",
    "level": 3,
    "totalIncome": 50000.0
  },
  "timestamp": 1634567890000,
  "traceId": "550e8400-e29b-41d4-a716-446655440000"
}
```

---

## 📋 集成检查清单

在集成 common 后，请逐一检查：

- [ ] pom.xml 中添加了 common 依赖
- [ ] 项目能正常编译
- [ ] 能正常导入 common 中的类
- [ ] 至少有一个 Controller 返回 BaseResponse
- [ ] 至少有一个 Service 抛出 BusinessException
- [ ] 项目能正常运行
- [ ] API 能正常返回统一格式的响应

---

## 🐛 常见问题

### Q: 编译时找不到 common 类

A: 需要先编译安装 common：

```bash
cd services/common
mvn clean install
```

### Q: 修改 common 后，其他服务没有生效

A: 需要重新编译安装 common：

```bash
cd services/common
mvn clean install
```

然后在其他服务中：

```bash
mvn clean compile
```

### Q: IDE 显示红线，但能编译成功

A: 刷新 Maven 项目：

- IntelliJ IDEA: 右键项目 → Maven → Reload projects
- Eclipse: 右键项目 → Maven → Update Project
- VS Code: 重新加载窗口

### Q: 想要修改 common 中的某个类

A: 直接在 `common/src/...` 中修改，然后：

```bash
cd services/common
mvn clean install
```

### Q: 需要为 common 添加新的异常或常量

A: 在 common 中添加，然后：

```bash
cd services/common
mvn clean install
```

---

## 📚 相关文档

- [Common 模块 README](common/README.md)
- [POM 配置说明](POM_说明.md)
- [快速参考](common/docs/快速参考.md)
- [详细使用指南](common/docs/响应和DTO使用指南.md)

---

## ✅ 总结

**集成 common 只需 3 步**:

1. 编译安装 common

   ```bash
   cd services/common && mvn clean install
   ```

2. 在服务 pom.xml 中添加依赖

   ```xml
   <dependency>
       <groupId>com.liveroom</groupId>
       <artifactId>common</artifactId>
       <version>1.0.0</version>
   </dependency>
   ```

3. 使用 common 中的类
   ```java
   return ResponseUtil.success(data);
   ```

**现在可以开始集成了！** 🚀
