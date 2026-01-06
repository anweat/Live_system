# 🎉 Common 模块完整部署指南

## 📌 三句话总结

1. **Common 模块** 包含了系统所有微服务都需要的基础类（异常、常量、响应格式、DTO 等）
2. **两个 POM 文件** 的作用：
   - `services/pom.xml` - 顶层 POM，聚合所有子模块，统一管理依赖版本
   - `common/pom.xml` - common 模块的 POM，定义 common 模块本身的配置和依赖
3. **3 步快速集成**：编译安装 common → 各微服务添加依赖 → 使用 common 中的类

---

## 🏗️ 两个 POM 的区别和关系

### services/pom.xml (顶层 POM)

```xml
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.liveroom</groupId>
    <artifactId>live-system</artifactId>
    <version>1.0.0</version>
    <packaging>pom</packaging>  <!-- ⭐ 聚合 POM，不生成 JAR -->

    <!-- 定义所有子模块 -->
    <modules>
        <module>common</module>
        <module>db-service</module>
        <module>anchor-service</module>
        ...
    </modules>

    <!-- 统一管理依赖版本 -->
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-parent</artifactId>
                <version>2.7.0</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            ...
        </dependencies>
    </dependencyManagement>

    <!-- 统一管理插件版本 -->
    <build>
        <pluginManagement>
            ...
        </pluginManagement>
    </build>
</project>
```

**作用**:

- ✅ 定义项目的所有子模块
- ✅ 统一管理依赖版本（DependencyManagement）
- ✅ 统一管理插件版本
- ✅ 避免依赖版本冲突
- ✅ 支持聚合编译（`mvn clean install` 编译所有模块）

**特点**:

- `<packaging>pom</packaging>` - 这是一个聚合 POM，不会生成 JAR 文件
- 子模块自动继承顶层 POM 的版本管理
- 一次修改，全局生效

---

### common/pom.xml (模块 POM)

```xml
<project>
    <modelVersion>4.0.0</modelVersion>

    <!-- 继承 Spring Boot 官方 POM，不继承 services/pom.xml -->
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.0</version>
    </parent>

    <!-- 模块自身的标识 -->
    <groupId>com.liveroom</groupId>
    <artifactId>common</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>  <!-- ⭐ 生成 JAR 文件 -->

    <!-- 模块的直接依赖 -->
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
            <!-- ⭐ 不需要指定版本，Spring Boot BOM 已管理 -->
        </dependency>
        ...
    </dependencies>
</project>
```

**作用**:

- ✅ 定义 common 模块本身的信息（groupId、artifactId、version）
- ✅ 声明 common 的直接依赖
- ✅ 定义 common 的构建配置
- ✅ 生成 `common-1.0.0.jar` 文件供其他模块使用

**特点**:

- `<packaging>jar</packaging>` - 生成可使用的 JAR 文件
- 继承 Spring Boot 官方 POM（不是 services/pom.xml）
- 其他微服务通过添加 common 作为依赖来使用它

---

## 🔗 POM 继承关系图

```
Spring Boot 官方 POM 2.7.0
    ↓
    ├─ services/pom.xml (顶层 POM)
    │   │
    │   └─ common/pom.xml (继承 Spring Boot)
    │       ├─ Spring Boot Web
    │       ├─ MyBatis
    │       ├─ Lombok
    │       └─ ... 其他依赖
    │
    ├─ db-service/pom.xml
    │   ├─ common 依赖
    │   ├─ Spring Boot Web (由 common 提供)
    │   └─ ...
    │
    └─ anchor-service/pom.xml
        ├─ common 依赖
        ├─ Spring Boot Web (由 common 提供)
        └─ ...
```

---

## ✅ 为什么需要两个 POM？

### services/pom.xml 的优势

| 优势         | 说明                                  |
| ------------ | ------------------------------------- |
| **聚合编译** | 一次编译所有模块：`mvn clean install` |
| **版本一致** | 所有模块使用相同的依赖版本            |
| **易于维护** | 版本号改变只需修改顶层 POM            |
| **最佳实践** | 遵循 Maven 多模块项目规范             |

### common/pom.xml 的优势

| 优势         | 说明                                                 |
| ------------ | ---------------------------------------------------- |
| **独立编译** | 可以单独编译和测试：`cd common && mvn clean install` |
| **独立发布** | 可以独立发布到中央仓库                               |
| **清晰依赖** | 清楚地列出 common 的直接依赖                         |
| **可重用**   | 其他项目可以引用 common                              |

---

## 🚀 如何使用这两个 POM？

### 场景 1：编译所有模块（推荐）

```bash
cd services
mvn clean install
```

**效果**:

1. 编译 common 模块
2. 将 `common-1.0.0.jar` 安装到本地 Maven 仓库
3. 编译所有其他微服务
4. 将所有 JAR 文件安装到本地 Maven 仓库

### 场景 2：只修改 common，重新编译

```bash
# 方法 1: 在 common 目录中编译（推荐）
cd services/common
mvn clean install

# 方法 2: 从 services 目录编译特定模块
cd services
mvn -pl common clean install
```

### 场景 3：编译特定的微服务

```bash
cd services
mvn -pl anchor-service clean compile
```

### 场景 4：跳过测试快速编译

```bash
cd services
mvn clean compile -DskipTests
```

---

## 📦 common 的依赖链

### common/pom.xml 中直接声明的依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <!-- 版本由 Spring Boot BOM 提供 -->
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
    <!-- 版本由 Spring Boot BOM 提供 -->
</dependency>

<!-- ... 其他 10+ 个依赖 ... -->
```

### common 被打包成 JAR 时，自动包含所有这些依赖

当其他微服务添加 common 依赖：

```xml
<dependency>
    <groupId>com.liveroom</groupId>
    <artifactId>common</artifactId>
    <version>1.0.0</version>
</dependency>
```

**会自动获得**:

- common 本身的代码
- Spring Boot Web
- Spring Boot Data JPA
- MyBatis
- MySQL Driver
- Lombok
- 等等所有 common 依赖的库

---

## 🎯 集成步骤详解

### 第 1 步：编译安装 common

```bash
cd services/common
mvn clean install
```

**发生了什么**:

1. Maven 读取 `common/pom.xml`
2. 检查 Spring Boot 官方 POM 中的依赖版本
3. 下载所有 common 需要的依赖
4. 编译 common 的所有 Java 源代码
5. 打包成 `common-1.0.0.jar`
6. **将 JAR 安装到本地 Maven 仓库** (`~/.m2/repository/com/liveroom/common/1.0.0/`)

### 第 2 步：其他服务添加 common 依赖

编辑 `services/db-service/pom.xml`：

```xml
<dependencies>
    <!-- ⭐ 添加这个依赖 -->
    <dependency>
        <groupId>com.liveroom</groupId>
        <artifactId>common</artifactId>
        <version>1.0.0</version>
    </dependency>

    <!-- 其他依赖 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <!-- ... -->
</dependencies>
```

**为什么不需要重复依赖**:

- db-service 已经通过 common 获得了 Spring Boot Web
- 无需重复添加相同的依赖

### 第 3 步：编译微服务

```bash
cd services
mvn clean compile
```

**发生了什么**:

1. Maven 读取 `services/pom.xml` (顶层 POM)
2. 编译 common 模块（如果未编译过）
3. 从本地 Maven 仓库获取 `common-1.0.0.jar`
4. 将 common JAR 添加到 db-service 的类路径
5. 编译 db-service 的所有 Java 源代码
6. 编译所有其他微服务

### 第 4 步：使用 common

```java
// 直接导入 common 中的类
import common.response.BaseResponse;
import common.util.ResponseUtil;
import common.exception.BusinessException;

@RestController
public class UserController {
    @GetMapping("/user/{id}")
    public BaseResponse<UserDTO> getUser(@PathVariable Long id) {
        UserDTO user = service.getUser(id);
        if (user == null) {
            throw new BusinessException(ErrorConstants.USER_NOT_FOUND, "用户不存在");
        }
        return ResponseUtil.success(user);
    }
}
```

---

## 📋 Maven 命令参考

| 命令                            | 用途                  |
| ------------------------------- | --------------------- |
| `mvn clean install`             | 编译并安装所有模块    |
| `mvn clean compile`             | 只编译，不安装        |
| `mvn clean package`             | 编译并打包成 JAR 文件 |
| `mvn dependency:tree`           | 显示依赖树            |
| `mvn -pl common clean install`  | 只编译 common 模块    |
| `mvn clean compile -DskipTests` | 跳过测试编译          |

---

## 🔍 常见问题

### Q: 修改 common 后，其他服务没有更新？

**A**: 需要重新安装 common：

```bash
cd services/common
mvn clean install  # ⭐ 重新生成 JAR 并安装到本地仓库
```

然后在其他服务中：

```bash
mvn clean compile  # ⭐ 重新编译
```

### Q: common 中的类在其他服务中不可见？

**A**: 检查以下几点：

1. 是否运行了 `mvn clean install`？
2. 是否在 pom.xml 中添加了 common 依赖？
3. 是否刷新了 IDE 的 Maven 项目？

### Q: 能否删除 services/pom.xml，只使用 common/pom.xml？

**A**: 可以，但不推荐。原因：

- ❌ 无法聚合编译所有模块
- ❌ 无法统一管理版本
- ❌ 各服务需要单独编译

### Q: 能否在 common/pom.xml 中继承 services/pom.xml？

**A**: 不推荐。原因：

- common 应该独立，不依赖 services 项目
- 会造成循环依赖的风险
- 不利于 common 的独立发布

---

## ✨ 最终总结

### common/pom.xml 的角色

```
定义 common 模块本身
    ↓
声明 common 的依赖
    ↓
生成 common-1.0.0.jar 文件
    ↓
其他微服务引用这个 JAR
```

### services/pom.xml 的角色

```
聚合所有微服务模块
    ↓
统一管理所有依赖版本
    ↓
统一管理所有插件版本
    ↓
支持一次编译所有模块
```

### 两者的关系

```
services/pom.xml (顶层)
    ├─ 聚合 common、db-service、anchor-service 等
    ├─ 版本管理（所有子模块使用相同的 Spring Boot 版本）
    └─ 插件管理
        ↓
    common/pom.xml (common 模块)
        ├─ 定义 common 的坐标和版本
        ├─ 声明 common 的直接依赖
        └─ 生成 common-1.0.0.jar
            ↓
        其他微服务
        ├─ 引用 common 依赖
        ├─ 自动获得 common 的所有依赖
        └─ 继承 Spring Boot 版本管理
```

---

## 🎓 核心概念

### POM 的三种角色

| 类型         | 用途         | 示例                 |
| ------------ | ------------ | -------------------- |
| **聚合 POM** | 管理多个模块 | services/pom.xml     |
| **模块 POM** | 定义单个模块 | common/pom.xml       |
| **父 POM**   | 定义通用配置 | Spring Boot 官方 POM |

### Maven 的版本管理

```
Spring Boot 官方 POM (最权威)
    ↓
services/pom.xml (项目级覆盖)
    ↓
common/pom.xml (模块级继承)
```

---

## 🚀 建议工作流程

### 日常开发

```bash
# 1. 整体编译（第一次或大改动时）
cd services
mvn clean install

# 2. 只编译修改过的模块（日常快速编译）
cd services/common
mvn clean install
# 或
cd services/anchor-service
mvn clean compile

# 3. 运行应用
cd services/db-service
mvn spring-boot:run
```

### 发布流程

```bash
# 1. 编译所有模块
cd services
mvn clean package

# 2. 生成的 JAR 文件位置
# services/common/target/common-1.0.0.jar
# services/db-service/target/db-service-1.0.0.jar
# ... 等等
```

---

**现在您完全理解了 POM 的设计和作用！** ✨

可以放心地使用这个多模块 Maven 结构了。😊
