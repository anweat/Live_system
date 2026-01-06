# Docker 管理脚本使用指南

## 📋 概述

本目录包含用于管理直播打赏系统 Docker 部署的自动化脚本，提供交互式菜单操作，支持编译、部署、监控等全流程管理。

## 📁 文件说明

```
deployment/docker/
├── manage.sh      # Linux/Mac 管理脚本
├── manage.ps1     # Windows PowerShell 管理脚本 (推荐)
├── manage.bat     # Windows 批处理脚本 (已弃用)
└── README.md      # 使用文档（本文件）
```

## 🚀 快速开始

### Windows 系统

从项目根目录运行：
```batch
manage.bat
```

或直接运行PowerShell脚本：
```powershell
cd deployment\docker
.\manage.ps1
```

### Linux/Mac 系统

从项目根目录运行：
```bash
./manage.sh
```

或直接运行：
```bash
cd deployment/docker
./manage.sh
```

## 📖 功能说明

### 服务依赖关系

**基础设施层（必须）**：
- MySQL - 所有业务服务依赖
- Shared Redis - Redis服务和后端管理服务依赖
- Nginx - 独立运行，不强制依赖其他服务

**业务服务层**：
- Redis Service → 依赖 Shared Redis
- Anchor Service → 依赖 MySQL, Redis Service
- Audience Service → 依赖 MySQL, Redis Service
- Finance Service → 依赖 MySQL, Redis Service
- Back-end Service → 依赖 MySQL, Shared Redis
- Mock Service → 依赖 MySQL（可独立启动）

**推荐启动顺序**：
1. 基础设施（MySQL, Shared Redis, Nginx）
2. Redis Service
3. 业务服务（Anchor, Audience, Finance等）
4. Mock Service（可选，用于测试）

### 【编译管理】

#### 1) 编译项目
- 自动编译 `common` 模块
- 依次编译所有微服务
- 显示编译结果和 JAR 文件大小
- **注意**: 首次运行或修改代码后必须执行

#### 2) 清理编译缓存
- 清理 Maven target 目录
- 清理 Docker 构建缓存
- 释放磁盘空间

#### 3) 重新编译
- 组合操作：清理 + 编译
- 确保编译环境干净
- 解决编译缓存问题

### 【服务部署】

#### 4) 一键启动所有服务
- 自动检查编译状态
- 启动所有基础设施和业务服务
- 等待健康检查完成
- **推荐**: 首次部署使用此选项

#### 5) 启动基础设施
仅启动基础服务：
- MySQL (端口 3306)
- Shared Redis (端口 6379)
- Nginx (端口 80/443)

#### 6) 启动业务服务
仅启动应用服务：
- Anchor Service (端口 8081)
- Audience Service (端口 8082)
- Redis Service (端口 8085)

**注意**: 会自动检查基础设施是否运行

#### 7) 停止服务
交互式选择停止范围：
- 停止所有服务
- 停止业务服务（保留基础设施）
- 停止基础设施服务

#### 8) 重启服务
交互式选择重启范围：
- 重启所有服务
- 重启业务服务
- 重启基础设施服务
- 重启指定服务

### 【高级操作】

#### 9) 重新构建并重启
完整的重新部署流程：
1. 清理编译缓存
2. 重新编译项目
3. 重新构建 Docker 镜像 (--no-cache)
4. 重启所有业务服务

**适用场景**:
- 代码有重大修改
- Docker 镜像出现问题
- 依赖项更新

#### 10) 重置基础设施
⚠️ **危险操作** - 会删除所有数据！

执行步骤：
1. 停止并删除所有容器
2. 删除所有数据卷（MySQL、Redis 数据）
3. 重新启动基础设施

**适用场景**:
- 数据库需要重新初始化
- 系统出现严重故障
- 测试环境重置

**安全措施**: 需要输入 `YES` 确认

### 【监控诊断】

#### 11) 查看日志
交互式选择日志范围：
- 所有服务日志
- 业务服务日志
- 基础设施日志
- 指定服务日志

**提示**: 按 Ctrl+C 退出日志查看

#### 12) 检查服务状态
显示全面的系统信息：
- Docker Compose 服务列表
- 服务健康状态（健康/不健康/启动中）
- 端口映射信息
- CPU 和内存使用情况

### 【独立服务】

#### 13) 管理独立服务 (db-service)
管理使用独立 docker-compose.yml 的服务：
- 启动/停止/重启 db-service
- 查看 db-service 日志
- 检查 db-service 状态
- 重新构建 db-service

**说明**: DB Service 有自己的 docker-compose.yml 配置文件，位于 `services/db-service/` 目录下，独立于主配置文件进行管理。

## 🔧 环境要求

### 必需软件

1. **Docker Desktop** (Windows) 或 **Docker Engine** (Linux/Mac)
   - 版本要求: 20.10+
   - 下载地址: https://www.docker.com/products/docker-desktop

2. **Docker Compose**
   - 版本要求: 2.0+
   - Docker Desktop 已包含

3. **Maven** (仅编译功能需要)
   - 版本要求: 3.6+
   - 下载地址: https://maven.apache.org/download.cgi

### 可选工具

- **Git Bash** (Windows): 运行 `.sh` 脚本
- **curl**: 用于健康检查

## 📊 服务架构

### 基础设施服务

| 服务 | 端口 | 说明 |
|------|------|------|
| MySQL | 3306 | 主数据库 |
| Shared Redis | 6379 | 中央 Redis |
| Nginx | 80, 443 | 反向代理 |

### 业务服务

| 服务 | 端口 | 说明 |
|------|------|------|
| Anchor Service | 8081 | 主播服务 |
| Audience Service | 8082 | 观众服务 |
| Finance Service | 8083 | 财务服务 |
| Redis Service | 8085 | Redis 服务 |
| Mock Service | 8090 | 模拟测试服务 |

### 独立部署服务

| 服务 | 端口 | 说明 | 配置文件 |
|------|------|------|----------|
| DB Service | 8084 | 数据库服务 | services/db-service/docker-compose.yml |

> **注意**: DB Service 使用独立的 docker-compose.yml 配置文件，通过菜单选项 [13] 进行管理。

## 🎯 常见使用场景

### 场景 1: 首次部署

```bash
# 1. 编译项目
选择: 1

# 2. 一键启动所有服务
选择: 4

# 3. 检查服务状态
选择: 12
```

### 场景 2: 代码修改后重新部署

```bash
# 1. 重新编译
选择: 3

# 2. 重新构建并重启
选择: 9
```

### 场景 3: 仅修改单个服务

```bash
# 1. 编译项目
选择: 1

# 2. 重启服务 -> 重启指定服务
选择: 8 -> 4
# 输入服务名称，如: anchor-service
```

### 场景 4: 调试问题

```bash
# 1. 查看日志
选择: 11

# 2. 检查服务状态
选择: 12
```

### 场景 5: 清理环境

```bash
# 1. 停止服务 -> 停止所有服务
选择: 7 -> 1

# 2. 清理编译缓存
选择: 2
```

### 场景 6: 数据库初始化

```bash
# 重置基础设施（会删除所有数据）
选择: 10
# 输入 YES 确认
```

## ⚠️ 注意事项

### 编译相关

1. **Common 模块依赖**
   - 所有微服务都依赖 `common` 模块
   - 修改 `common` 后必须重新编译所有服务

2. **编译顺序**
   - 脚本会自动处理编译顺序
   - 先编译 `common`，再编译其他服务

3. **跳过测试**
   - 编译时使用 `-DskipTests` 跳过单元测试
   - 加快编译速度

### Docker 相关

1. **健康检查**
   - 服务启动后需要等待健康检查
   - 脚本会自动等待并显示状态

2. **端口冲突**
   - 确保端口未被占用: 3306, 6379, 80, 443, 8081-8085
   - Windows: `netstat -ano | findstr "端口"`
   - Linux: `lsof -i :端口`

3. **数据持久化**
   - MySQL 数据存储在 Docker Volume `mysql_data`
   - Redis 数据存储在 Docker Volume `redis_data`
   - 重置基础设施会删除这些数据卷

### 资源使用

1. **内存要求**
   - 建议至少 8GB 内存
   - 每个服务约占用 512MB-1GB

2. **磁盘空间**
   - 编译产物约 200MB
   - Docker 镜像约 500MB-1GB
   - 数据卷根据使用情况增长

## 🐛 故障排查

### 问题 1: Maven 编译失败

**症状**: 编译时报错 "Could not resolve dependencies"

**解决方案**:
```bash
# 1. 检查网络连接
# 2. 清理 Maven 缓存
rm -rf ~/.m2/repository  # Linux/Mac
rmdir /s %USERPROFILE%\.m2\repository  # Windows

# 3. 重新编译
选择: 3
```

### 问题 2: Docker 容器无法启动

**症状**: 服务显示 "Exited" 状态

**解决方案**:
```bash
# 1. 查看日志
选择: 11 -> 4
# 输入服务名称

# 2. 检查编译状态
选择: 1

# 3. 重新构建
选择: 9
```

### 问题 3: 端口被占用

**症状**: "Bind for 0.0.0.0:端口 failed: port is already allocated"

**解决方案**:
```bash
# Windows
netstat -ano | findstr "端口号"
taskkill /PID 进程ID /F

# Linux/Mac
lsof -i :端口号
kill -9 进程ID
```

### 问题 4: 健康检查失败

**症状**: 服务状态显示 "unhealthy"

**解决方案**:
```bash
# 1. 查看详细日志
选择: 11 -> 4

# 2. 检查数据库连接
docker exec -it mysql mysql -uroot -proot

# 3. 重启服务
选择: 8
```

### 问题 5: Docker Desktop 未启动

**症状**: "Cannot connect to the Docker daemon"

**解决方案**:
- Windows: 启动 Docker Desktop
- Linux: `sudo systemctl start docker`

## 📝 脚本维护

### 添加新服务

编辑脚本文件，修改服务列表：

```bash
# manage.sh
BUSINESS_SERVICES=("anchor-service" "audience-service" "redis-service" "new-service")

# manage.bat
set "BUSINESS_SERVICES=anchor-service audience-service redis-service new-service"
```

### 修改端口映射

编辑 `docker-compose.yml` 和脚本中的端口说明部分。

### 自定义等待时间

修改健康检查等待时间：

```bash
# manage.sh
sleep 10  # 改为需要的秒数

# manage.bat
timeout /t 10 /nobreak >nul  # 改为需要的秒数
```

## 🔗 相关文档

- [Docker Compose 配置](../docker-compose.yml)
- [系统架构文档](../docs/JavaEE%20架构与应用小组作业.md)
- [数据库设计](../services/db-service/docs/数据库设计文档.md)

## 📞 技术支持

遇到问题？

1. 检查本文档的故障排查部分
2. 查看服务日志: `选择 11`
3. 检查服务状态: `选择 12`

## 📄 许可证

本项目仅供学习使用。

---

**最后更新**: 2026-01-02
**版本**: 1.0.0
