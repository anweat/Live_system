# 直播打赏系统

基于 JavaEE 的分布式直播打赏系统，采用微服务架构，支持 Docker 容器化部署。

## 🚀 快速开始

### Windows 系统

双击运行根目录下的 `manage.bat`，或在命令行中执行：

```batch
manage.bat
```

### Linux/Mac 系统

```bash
chmod +x manage.sh
./manage.sh
```

## 📁 项目结构

```
Live_system/
├── manage.sh                 # 管理脚本快捷入口 (Linux/Mac)
├── manage.bat                # 管理脚本快捷入口 (Windows)
├── docker-compose.yml        # Docker Compose 主配置
├── deployment/               # 部署配置
│   ├── docker/              # Docker 管理脚本
│   │   ├── manage.sh        # 主管理脚本 (Linux/Mac)
│   │   ├── manage.bat       # 主管理脚本 (Windows)
│   │   ├── README.md        # 详细使用文档
│   │   ├── QUICKSTART.md    # 快速启动指南
│   │   └── SERVICES_INFO.md # 服务架构说明
│   └── k8s/                 # Kubernetes 配置
├── services/                 # 微服务模块
│   ├── common/              # 公共模块
│   ├── anchor-service/      # 主播服务
│   ├── audience-service/    # 观众服务
│   ├── finance-service/     # 财务服务
│   ├── redis-service/       # Redis 服务
│   ├── mock-service/        # 模拟测试服务
│   ├── back_end-service/    # 后台管理服务
│   ├── db-service/          # 数据库服务
│   └── nginx/               # Nginx 网关
└── docs/                     # 项目文档
```

## 🎯 核心功能

### 基础设施
- **MySQL** - 主数据库
- **Redis** - 分布式缓存
- **Nginx** - API 网关和负载均衡

### 业务服务
- **主播服务** (8081) - 主播信息管理、直播间管理
- **观众服务** (8082) - 观众信息管理、打赏处理
- **财务服务** (8083) - 打赏结算、分成管理、提现处理
- **Redis 服务** (8085) - 分布式缓存管理
- **模拟测试服务** (8090) - 测试数据生成、行为模拟
- **后台管理服务** (8086/8087) - 管理后台（前后端一体）

## 📖 使用指南

### 首次部署

```bash
# 1. 启动管理脚本
./manage.bat           # Windows
./manage.sh            # Linux/Mac

# 2. 编译项目（选项 1）
# 3. 一键启动所有服务（选项 4）
# 4. 检查服务状态（选项 12）
```

### 常用操作

| 功能 | 菜单选项 |
|------|----------|
| 编译项目 | 1 |
| 清理缓存 | 2 |
| 重新编译 | 3 |
| 一键启动 | 4 |
| 启动基础设施 | 5 |
| 启动业务服务 | 6 |
| 停止服务 | 7 |
| 重启服务 | 8 |
| 重新构建 | 9 |
| 重置基础设施 | 10 |
| 查看日志 | 11 |
| 检查状态 | 12 |
| 管理独立服务 | 13 |

## 🌐 访问地址

| 服务 | 端口 | 地址 |
|------|------|------|
| Nginx 网关 | 80 | http://localhost |
| 主播服务 | 8081 | http://localhost:8081 |
| 观众服务 | 8082 | http://localhost:8082 |
| 财务服务 | 8083 | http://localhost:8083 |
| Redis 服务 | 8085 | http://localhost:8085 |
| 后台管理 API | 8086 | http://localhost:8086 |
| 后台管理 Web | 8087 | http://localhost:8087 |
| 模拟测试服务 | 8090 | http://localhost:8090 |
| MySQL | 3306 | localhost:3306 (root/root) |
| Redis | 6379 | localhost:6379 |

## 🔧 环境要求

### 必需软件
- **Docker Desktop** (Windows) 或 **Docker Engine** (Linux/Mac)
  - 版本: 20.10+
- **Docker Compose**
  - 版本: 2.0+
- **Maven** (编译项目需要)
  - 版本: 3.6+
- **JDK** 11+

### 系统要求
- 内存: 8GB 以上
- 磁盘空间: 10GB 以上
- 操作系统: Windows 10+, macOS 10.15+, Linux (Ubuntu 20.04+)

## 📚 文档

- [详细使用文档](deployment/docker/README.md)
- [快速启动指南](deployment/docker/QUICKSTART.md)
- [服务架构说明](deployment/docker/SERVICES_INFO.md)
- [后台服务配置](deployment/docker/BACKEND_SERVICE_SETUP.md)
- [系统设计文档](docs/JavaEE%20架构与应用小组作业.md)

## 🛠️ 开发指南

### 编译项目

```bash
# 使用管理脚本（推荐）
./manage.bat  # 选择 1

# 或手动编译
cd services
mvn clean install -pl common -am
mvn clean package -DskipTests
```

### 本地开发

```bash
# 启动基础设施
./manage.bat  # 选择 5

# 本地 IDE 启动微服务进行调试
# 配置连接到 localhost:3306 (MySQL) 和 localhost:6379 (Redis)
```

### 添加新服务

1. 在 `services/` 下创建新服务目录
2. 创建 `Dockerfile` 和 `pom.xml`
3. 更新 `docker-compose.yml`
4. 更新管理脚本中的服务列表

## ⚠️ 注意事项

1. **首次运行**: 需要先编译项目（选项 1）
2. **端口冲突**: 确保端口未被占用
3. **资源使用**: 建议至少 8GB 内存
4. **Common 模块**: 修改后需重新编译所有服务
5. **数据持久化**: 重置基础设施会删除所有数据

## 🐛 故障排查

### 端口被占用

```bash
# Windows
netstat -ano | findstr "端口号"
taskkill /PID 进程ID /F

# Linux/Mac
lsof -i :端口号
kill -9 进程ID
```

### 编译失败

```bash
# 清理 Maven 缓存
rm -rf ~/.m2/repository  # Linux/Mac
rmdir /s %USERPROFILE%\.m2\repository  # Windows

# 重新编译
./manage.bat  # 选择 3
```

### 容器无法启动

```bash
# 查看日志
./manage.bat  # 选择 11

# 重新构建
./manage.bat  # 选择 9
```

## 📞 技术支持

- 查看详细文档: [deployment/docker/README.md](deployment/docker/README.md)
- 查看服务日志: 管理脚本选项 11
- 检查服务状态: 管理脚本选项 12

## 📄 许可证

本项目仅供学习使用。

---

**最后更新**: 2026-01-02  
**版本**: 1.0.0
