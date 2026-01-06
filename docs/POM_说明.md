# Maven POM 配置说明

## 📋 概述

项目采用 **多模块 Maven 结构**，包含两个关键的 pom.xml 文件：

1. **services/pom.xml** - 顶层 POM，管理所有子模块和依赖版本
2. **common/pom.xml** - common 公共模块 POM

## 🏗️ 项目结构

```
Live_system/
├── services/                    # 顶层模块聚合
│   ├── pom.xml                 # ⭐ 顶层 POM（管理所有子模块）
│   ├── common/                 # 公共模块（被所有服务依赖）
│   │   ├── pom.xml            # ⭐ common 模块 POM
│   │   └── src/
│   ├── db-service/            # 数据库服务
│   │   ├── pom.xml
│   │   └── src/
│   ├── anchor-service/        # 主播服务
│   │   ├── pom.xml
│   │   └── src/
│   ├── audience-service/      # 观众服务
│   │   ├── pom.xml
│   │   └── src/
│   ├── finance-service/       # 财务服务
│   │   ├── pom.xml
│   │   └── src/
│   └── ...                    # 其他服务
└── docs/                       # 项目文档
```

## 📄 services/pom.xml (顶层 POM)

### 作用

- 定义所有子模块
- 集中管理依赖版本（DependencyManagement）
- 集中管理构建插件版本（PluginManagement）
- 避免子模块重复定义依赖版本

### 关键部分

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

    <!-- 统一管理版本号 -->
    <properties>
        <spring-boot.version>2.7.0</spring-boot.version>
        <mysql.version>8.0.32</mysql.version>
        ...
    </properties>

    <!-- 集中管理依赖版本 -->
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring-boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            ...
        </dependencies>
    </dependencyManagement>

    <!-- 集中管理插件版本 -->
    <build>
        <pluginManagement>
            ...
        </pluginManagement>
    </build>
</project>
```

### 为什么需要？

✅ **版本一致性** - 所有子模块使用相同的依赖版本  
✅ **易于维护** - 版本号改变只需修改顶层 POM  
✅ **模块聚合** - 一次编译所有模块  
✅ **最佳实践** - 遵循 Maven 多模块项目规范

---

## 📄 common/pom.xml (模块 POM)

### 作用

- 定义 common 模块本身的信息
- 声明 common 的直接依赖
- 继承顶层 POM 的版本管理

### 关键部分

```xml
<project>
    <modelVersion>4.0.0</modelVersion>

    <!-- 继承顶层 POM -->
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.0</version>
    </parent>

    <!-- 模块自身的坐标 -->
    <groupId>com.liveroom</groupId>
    <artifactId>common</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <!-- 模块的依赖 -->
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <!-- ⭐ 注意：这里不需要指定版本号，因为已在顶层 POM 管理 -->
        ...
    </dependencies>
</project>
```

### 版本号继承链

```
Spring Boot 官方 POM
    ↓
services/pom.xml (顶层 POM)
    ↓
common/pom.xml (common 模块)
```

---

## 🔗 依赖继承关系

### 其他微服务如何依赖 common

```xml
<!-- 在 anchor-service/pom.xml 中 -->
<dependencies>
    <!-- 引入 common 公共模块 -->
    <dependency>
        <groupId>com.liveroom</groupId>
        <artifactId>common</artifactId>
        <!-- ⭐ 版本号由顶层 POM 管理，子模块可以不指定 -->
        <version>${project.version}</version>
    </dependency>

    <!-- 其他依赖 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
        <!-- ⭐ 版本号由 Spring Boot BOM 管理 -->
    </dependency>
</dependencies>
```

### 依赖关系图

```
顶层 POM (services/pom.xml)
├── common 模块
│   ├── Spring Boot Web
│   ├── Spring Boot Data JPA
│   ├── MyBatis
│   ├── MySQL Driver
│   └── 其他依赖
│
├── db-service
│   ├── 依赖 common 模块
│   ├── Spring Boot Web
│   └── ...
│
├── anchor-service
│   ├── 依赖 common 模块
│   ├── Spring Boot Web
│   └── ...
│
└── 其他服务
    ├── 依赖 common 模块
    └── ...
```

---

## 🛠️ Maven 命令使用

### 1. 在项目根目录编译所有模块

```bash
cd services
mvn clean compile
```

这会编译：

- common 模块
- 所有依赖 common 的服务

### 2. 编译特定模块

```bash
cd services
mvn -pl common clean compile
```

只编译 common 模块

### 3. 安装 common 到本地仓库

```bash
cd services/common
mvn clean install
```

其他模块可以引用本地仓库中的 common

### 4. 打包所有模块

```bash
cd services
mvn clean package
```

生成所有模块的 JAR 文件：

- `common/target/common-1.0.0.jar`
- `db-service/target/db-service-1.0.0.jar`
- 等等

### 5. 跳过测试编译和打包

```bash
mvn clean package -DskipTests
```

### 6. 查看依赖树

```bash
mvn dependency:tree
```

显示当前模块的所有依赖及版本

---

## ✅ 集成步骤

### 第 1 步：编译 common 模块

```bash
cd services/common
mvn clean install
```

这会将 common-1.0.0.jar 安装到本地 Maven 仓库

### 第 2 步：其他服务添加 common 依赖

编辑 `anchor-service/pom.xml`：

```xml
<dependencies>
    <!-- 添加 common 依赖 -->
    <dependency>
        <groupId>com.liveroom</groupId>
        <artifactId>common</artifactId>
        <version>${project.version}</version>
    </dependency>

    <!-- 其他依赖 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    ...
</dependencies>
```

### 第 3 步：编译和打包

```bash
cd services
mvn clean package
```

系统会：

1. 编译 common 模块
2. 将 common-1.0.0.jar 添加到各服务的类路径
3. 编译各个服务
4. 生成最终的 JAR 文件

### 第 4 步：验证依赖

```bash
mvn dependency:tree
```

确保 common 出现在依赖树中

---

## 🔍 常见问题

### Q: 为什么 common 中的代码改变后，其他服务没有生效？

A: 需要重新安装 common 到本地仓库：

```bash
cd services/common
mvn clean install
```

然后在其他服务中：

```bash
mvn clean compile
```

### Q: 如何跨越多个 Maven 项目？

A: 使用 `-rf`（resume from）标志：

```bash
mvn clean install -rf :module-name
```

这会从指定模块开始编译

### Q: 如何检查是否所有依赖都下载成功？

A: 查看本地仓库：

```bash
ls ~/.m2/repository/com/liveroom/
```

应该看到 `common/1.0.0/` 目录

### Q: 如何清除本地仓库中的缓存？

A: 删除 common 的版本：

```bash
rm -rf ~/.m2/repository/com/liveroom/common/1.0.0/
```

然后重新安装：

```bash
cd services/common
mvn clean install
```

---

## 📊 版本管理规范

### 版本号格式

遵循 **语义化版本** (Semantic Versioning)：

```
MAJOR.MINOR.PATCH-QUALIFIER
1.0.0-SNAPSHOT
↑     ↑     ↑
|     |     └─ 补丁版本 (bug 修复)
|     └─────── 次版本 (新增功能，向后兼容)
└───────────── 主版本 (破坏性改变)
```

### 版本说明

| 版本     | 说明             | 示例           |
| -------- | ---------------- | -------------- |
| SNAPSHOT | 开发版本，不稳定 | 1.0.0-SNAPSHOT |
| 正式版   | 生产发布版本     | 1.0.0          |
| BETA     | 测试版本         | 1.0.0-beta     |
| RC       | 候选版本         | 1.0.0-rc1      |

### 当前项目版本

- **当前版本**: 1.0.0 (正式发布版本)
- **Java 版本**: 11
- **Spring Boot 版本**: 2.7.0

---

## 🚀 最佳实践

### ✅ 推荐做法

1. **所有依赖版本在顶层 POM 管理**

   ```xml
   <!-- ✅ 好 -->
   <dependencyManagement>
       <dependencies>
           <dependency>
               <groupId>org.springframework.boot</groupId>
               <artifactId>spring-boot-starter-web</artifactId>
               <version>${spring-boot.version}</version>
           </dependency>
       </dependencies>
   </dependencyManagement>
   ```

2. **子模块不指定版本号**

   ```xml
   <!-- ✅ 好 -->
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-web</artifactId>
   </dependency>
   ```

3. **使用属性管理版本号**

   ```xml
   <!-- ✅ 好 -->
   <properties>
       <spring-boot.version>2.7.0</spring-boot.version>
   </properties>
   ```

4. **定期更新依赖**
   ```bash
   mvn versions:display-dependency-updates
   ```

### ❌ 避免做法

1. **不要在子模块中重复定义版本**

   ```xml
   <!-- ❌ 不好 -->
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-web</artifactId>
       <version>2.7.0</version>  <!-- 重复定义 -->
   </dependency>
   ```

2. **不要混用不同版本的 Spring Boot**
   ```xml
   <!-- ❌ 不好 -->
   <!-- service-1 中 -->
   <version>2.7.0</version>
   <!-- service-2 中 -->
   <version>2.6.0</version>
   ```

---

## 📚 相关文档

- [Maven 官方文档](https://maven.apache.org/)
- [Spring Boot Maven 插件](https://docs.spring.io/spring-boot/docs/current/maven-plugin/)
- [Common 模块 README](common/README.md)
- [项目结构说明](../../deployment/README.md)

---

## 🔧 IDE 配置

### IntelliJ IDEA

1. 打开项目根目录 `services`
2. IDE 会自动识别 Maven 多模块结构
3. 在 IDE 中可以看到所有模块

### Eclipse

1. File → Import → Existing Maven Projects
2. 选择 `services` 目录
3. Eclipse 会导入所有子模块

### VS Code

1. 安装 Extension Pack for Java
2. 打开 `services` 文件夹
3. VS Code 会自动识别 Maven 项目

---

## 📋 总结

| 文件         | 位置               | 用途                  |
| ------------ | ------------------ | --------------------- |
| 顶层 POM     | `services/pom.xml` | 定义模块、管理版本    |
| Common POM   | `common/pom.xml`   | common 模块本身的配置 |
| 其他服务 POM | `*/pom.xml`        | 各个微服务的配置      |

**关键点**:

- ✅ 顶层 POM 管理所有版本和插件
- ✅ 子模块不重复定义版本
- ✅ 统一使用 Spring Boot 2.7.0
- ✅ common 是被依赖的模块，需要先编译安装

---

**现在可以开始使用 Maven 编译和打包所有模块了！** 🚀

```bash
cd services
mvn clean install
```
