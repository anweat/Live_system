# Docker 构建常见错误及解决方案

## 错误 1: JAR 文件未找到

### 错误信息
```
ERROR [service 3/3] COPY target/service-1.0.0.jar app.jar
failed to solve: "/target/service-1.0.0.jar": not found
```

### 原因
Docker 尝试复制 `target/` 目录下的 JAR 文件，但文件不存在。这意味着**项目还没有编译**。

### 解决方案

**方案一：使用管理脚本编译（推荐）**

运行 `manage.bat` 或 `manage.ps1`，选择：
- **选项 1**: Compile project - 仅编译
- **选项 3**: Rebuild project - 清理后重新编译

**方案二：手动 Maven 编译**

```powershell
# 进入 services 目录
cd services

# 先编译 common 模块
mvn clean install -pl common -am -DskipTests

# 编译所有服务
mvn clean package -DskipTests
```

**方案三：一键构建并启动**

运行管理脚本，选择 **选项 4**: Start all services
- 脚本会自动检测未编译的服务
- 提示是否编译
- 编译完成后自动启动

### 验证编译结果

检查以下文件是否存在：
```
services/anchor-service/target/anchor-service-1.0.0.jar
services/audience-service/target/audience-service-1.0.0.jar
services/redis-service/target/redis-service-1.0.0.jar
services/finance-service/target/finance-service-1.0.0.jar
services/mock-service/target/mock-service-1.0.0.jar
services/back-end-service/target/back-end-service-1.0.0.jar
```

---

## 错误 2: Maven 依赖下载失败

### 错误信息
```
Could not transfer artifact ... from/to central
```

### 原因
Maven 中央仓库访问受限或网络问题。

### 解决方案

**配置 Maven 镜像源**（推荐阿里云）

编辑 `~/.m2/settings.xml` 或 `services/settings.xml`：

```xml
<settings>
  <mirrors>
    <mirror>
      <id>aliyun</id>
      <mirrorOf>central</mirrorOf>
      <name>Aliyun Maven Mirror</name>
      <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
  </mirrors>
</settings>
```

---

## 错误 3: Docker 镜像拉取失败

### 错误信息
```
ERROR [internal] load metadata for docker.io/library/...
```

### 解决方案
参考 [DOCKER_MIRROR_CONFIG.md](DOCKER_MIRROR_CONFIG.md)

快速解决：
1. 配置 Docker 镜像加速器
2. 预拉取基础镜像：
   ```powershell
   docker pull eclipse-temurin:11-jre-jammy
   docker pull node:18-alpine
   docker pull redis:7-alpine
   ```

---

## 错误 4: 端口被占用

### 错误信息
```
Error starting service: bind: address already in use
```

### 原因
所需端口已被其他程序占用。

### 解决方案

**查看端口占用**：
```powershell
# Windows
netstat -ano | findstr :8081
netstat -ano | findstr :3306

# 查看进程详情
tasklist | findstr <PID>
```

**停止占用端口的服务**：
```powershell
# 停止进程
taskkill /PID <PID> /F

# 或停止 Docker 容器
docker ps
docker stop <container_name>
```

---

## 错误 5: 内存不足

### 错误信息
```
Error: Java heap space
OutOfMemoryError
```

### 解决方案

**方案一：增加 Docker 内存限制**

Docker Desktop → Settings → Resources → Memory
- 推荐设置：至少 8GB

**方案二：调整 Java 堆内存**

编辑各服务的 Dockerfile，修改 `JAVA_OPTS`：
```dockerfile
ENV JAVA_OPTS="-Xms512m -Xmx1024m"
```

---

## 错误 6: MySQL 连接失败

### 错误信息
```
Communications link failure
Access denied for user
```

### 原因
1. MySQL 容器未启动
2. 数据库配置错误
3. 网络连接问题

### 解决方案

**检查 MySQL 状态**：
```powershell
docker ps | findstr mysql
docker logs mysql
```

**验证连接**：
```powershell
docker exec -it mysql mysql -uroot -proot_password
```

**检查配置**：
- 确认 `docker-compose.yml` 中的 MySQL 配置
- 检查各服务的 `application.yml` 数据库连接信息

---

## 错误 7: Redis 连接失败

### 错误信息
```
Could not connect to Redis
Connection refused
```

### 解决方案

**检查 Redis 状态**：
```powershell
docker ps | findstr redis
docker logs shared-redis
```

**测试连接**：
```powershell
docker exec -it shared-redis redis-cli ping
# 应返回: PONG
```

---

## 错误 8: 编译失败 - 找不到 common 模块

### 错误信息
```
Could not resolve dependencies for project
com.liveroom:common:jar:1.0.0 not found
```

### 原因
common 模块未安装到本地 Maven 仓库。

### 解决方案

**先单独编译 common 模块**：
```powershell
cd services
mvn clean install -pl common -am -DskipTests
```

**然后编译其他服务**：
```powershell
mvn clean package -DskipTests
```

---

## 调试技巧

### 查看容器日志
```powershell
# 查看所有容器日志
docker-compose logs

# 查看特定服务日志
docker-compose logs -f service-name

# 查看最近100行日志
docker-compose logs --tail=100 service-name
```

### 进入容器调试
```powershell
# 进入容器
docker exec -it <container_name> bash

# 如果没有 bash，使用 sh
docker exec -it <container_name> sh

# 查看文件
ls -la /app
cat /app/application.yml
```

### 检查网络连通性
```powershell
# 从一个容器 ping 另一个
docker exec -it <container_name> ping mysql
docker exec -it <container_name> ping shared-redis
```

### 清理并重建
```powershell
# 停止所有容器
docker-compose down

# 清理所有资源（包括卷）
docker-compose down -v

# 清理 Docker 系统
docker system prune -a -f

# 重新构建
docker-compose build --no-cache
docker-compose up -d
```

---

## 预防措施

### 开发流程最佳实践

1. **首次启动**：
   ```
   1) 编译项目 (选项 1)
   2) 配置 Docker 镜像加速
   3) 启动基础设施 (选项 5)
   4) 启动业务服务 (选项 6)
   ```

2. **代码修改后**：
   ```
   1) 重新编译 (选项 1)
   2) 重启服务 (选项 8)
   ```

3. **完整重建**：
   ```
   1) 使用选项 9: Rebuild and restart services
   ```

### 定期维护

```powershell
# 每周清理一次未使用的资源
docker system prune -f

# 查看磁盘使用情况
docker system df
```

---

## 获取帮助

如果以上方案都无法解决问题：

1. **查看详细日志**：
   ```powershell
   docker-compose logs > logs.txt
   ```

2. **检查系统资源**：
   ```powershell
   docker stats
   docker system df
   ```

3. **验证环境**：
   ```powershell
   docker version
   docker-compose version
   mvn -version
   java -version
   ```

4. **查看 Docker 事件**：
   ```powershell
   docker events --since 1h
   ```
