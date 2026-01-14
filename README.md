# 🎥 直播打赏系统

基于 JavaEE 的分布式直播打赏系统，采用微服务架构，支持高并发场景下的直播打赏、财务结算、数据分析等功能。

## 📊 项目概述

本系统是一个完整的直播打赏解决方案，支持 **100个主播**、**30万观众**的高并发场景，实现了打赏不丢失、不重复的可靠性保证。

### 🎯 核心特性

- ✅ **高并发处理**: 单节点 >500 TPS，支持水平扩展
- ✅ **数据可靠性**: 幂等性设计，打赏数据不丢失、不重复
- ✅ **微服务架构**: Spring Cloud 生态，服务独立部署
- ✅ **容器化部署**: Docker Compose 一键启动
- ✅ **实时数据分析**: 多维度统计、用户画像、排行榜
- ✅ **智能标签系统**: 基于关联度的观众分配算法
- ✅ **完整的财务系统**: 结算、提现、分成管理

## 🏗️ 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                    前端/移动端 (Vue3)                         │
├─────────────────────────────────────────────────────────────┤
│                   Nginx API网关 (80/443)                     │
├──────────────┬──────────────┬──────────────┬────────────────┤
│ 主播服务      │ 观众服务      │ 财务服务      │ 数据分析服务    │
│ (8081)       │ (8082)       │ (8083)       │ (8084)         │
├──────────────┼──────────────┼──────────────┼────────────────┤
│ Redis服务     │ 数据库服务    │ Mock服务     │ 后台管理       │
│ (8085)       │ (8086)       │ (8087)       │ (8088)         │
└──────────────┴──────────────┴──────────────┴────────────────┘
                         ↓
                 ┌──────────────────┐
                 │ MySQL + Redis     │
                 │ 数据持久化层       │
                 └──────────────────┘
```

## 🎯 核心功能模块

### 🎤 主播服务 (Anchor Service)
- 主播账户管理（创建、查询、更新）
- 直播间生命周期管理（开启、结束、更新）
- 实时数据统计（观众数、收益、TOP10）
- 打赏记录查询
- 分成比例管理

### 👥 观众服务 (Audience Service)
- 观众注册与游客模式
- 打赏请求处理与记录
- 幂等性检查（防重复打赏）
- 消费统计与画像查询
- 批量数据同步

### 💰 财务服务 (Finance Service)
- 打赏结算与分成计算
- 提现申请与审核
- 收入统计分析
- 分成比例动态管理
- 财务数据同步接收

### 📈 数据分析服务 (Data Analysis Service)
- 多维度组合查询（时间、性别、地域）
- 观众消费画像计算（高/中/低消费）
- 小时级数据汇总
- 主播收入排行榜
- 标签关联度分析

### 🔧 辅助服务
- **Redis服务**: 分布式缓存、分布式锁、幂等性检查
- **数据库服务**: 数据库初始化、健康检查
- **Mock服务**: 测试数据生成、并发打赏模拟
- **后台管理服务**: 数据可视化、日志追踪、系统监控

## 🚀 快速开始

### 前置要求
- Java 11+
- Maven 3.6+
- Docker & Docker Compose
- MySQL 8.0+
- Redis 6.0+

### Windows 系统

```batch
# 双击运行或命令行执行
manage.bat
```

### Linux/Mac 系统

```bash
chmod +x manage.sh
./manage.sh
```

### 首次部署流程

```bash
# 1. 编译项目
选择菜单: 1

# 2. 一键启动所有服务
选择菜单: 4

# 3. 检查服务状态
选择菜单: 12

# 4. 访问系统
浏览器打开: http://localhost
```

## 📁 项目结构

```
Live_system/
├── manage.sh                 # 管理脚本快捷入口 (Linux/Mac)
├── manage.bat                # 管理脚本快捷入口 (Windows)
├── docker-compose.yml        # Docker Compose 主配置
├── deployment/               # 部署配置
│   ├── docker/              # Docker 管理脚本及文档
│   └── k8s/                 # Kubernetes 配置
├── services/                 # 微服务模块
│   ├── common/              # 公共模块（实体类、工具类）
│   ├── anchor-service/      # 主播服务
│   ├── audience-service/    # 观众服务
│   ├── finance-service/     # 财务服务
│   ├── data-analysis-service/ # 数据分析服务
│   ├── redis-service/       # Redis 服务
│   ├── db-service/          # 数据库服务
│   ├── mock-service/        # 模拟测试服务
│   ├── back_end-service/    # 后台管理服务
│   └── nginx/               # Nginx 网关
└── docs/                     # 项目文档
    ├── ApiSum/              # API接口文档
    ├── requirements/        # 需求与设计文档
    └── admin-console/       # 后台管理文档
```

## 📖 管理操作

### 常用管理命令

| 功能 | 菜单选项 | 说明 |
|------|----------|------|
| 编译项目 | 1 | Maven 编译所有服务 |
| 清理缓存 | 2 | 清理 Maven 缓存和构建目录 |
| 重新编译 | 3 | 清理后重新编译 |
| 一键启动 | 4 | 启动所有服务（基础设施+业务） |
| 启动基础设施 | 5 | 仅启动 MySQL + Redis + Nginx |
| 启动业务服务 | 6 | 启动所有业务微服务 |
| 停止服务 | 7 | 停止所有服务 |
| 重启服务 | 8 | 重启指定服务 |
| 重新构建 | 9 | 重新构建 Docker 镜像 |
| 重置基础设施 | 10 | 清空数据库和缓存 |
| 查看日志 | 11 | 查看服务实时日志 |
| 检查状态 | 12 | 检查所有服务运行状态 |
| 管理独立服务 | 13 | 单独启动/停止某个服务 |

## 🌐 服务端口与访问地址

| 服务 | 端口 | 访问地址 | 说明 |
|------|------|----------|------|
| **Nginx 网关** | 80 | http://localhost | API 统一入口 |
| **主播服务** | 8081 | http://localhost:8081 | 主播管理 API |
| **观众服务** | 8082 | http://localhost:8082 | 观众/打赏 API |
| **财务服务** | 8083 | http://localhost:8083 | 财务结算 API |
| **数据分析服务** | 8084 | http://localhost:8084 | 数据分析 API |
| **Redis服务** | 8085 | http://localhost:8085 | 缓存管理 API |
| **数据库服务** | 8086 | http://localhost:8086 | 数据库管理 API |
| **Mock服务** | 8087 | http://localhost:8087 | 测试数据 API |
| **后台管理** | 8088 | http://localhost:8088 | 管理控制台 |
| **MySQL** | 3306 | localhost:3306 | 数据库连接 |
| **Redis** | 6379 | localhost:6379 | 缓存连接 |

## 🔌 API 接口总览

### 主播服务 API
```
POST   /anchor/api/v1/anchors                     创建主播
GET    /anchor/api/v1/anchors/{anchorId}          查询主播信息
POST   /anchor/api/v1/live-rooms/{id}/start       开启直播
POST   /anchor/api/v1/live-rooms/{id}/end         结束直播
GET    /anchor/api/v1/anchors/{id}/recharges      查询打赏记录
```

### 观众服务 API
```
POST   /audience/api/v1/audiences                 创建观众
POST   /audience/api/v1/audiences/guest           创建游客
POST   /audience/api/v1/recharge                  创建打赏
GET    /audience/api/v1/audiences/{id}            查询观众信息
```

### 财务服务 API
```
POST   /finance/api/v1/withdrawal                 申请提现
GET    /finance/api/v1/withdrawal/{anchorId}      查询提现记录
PUT    /finance/api/v1/withdrawal/{id}/approve    批准提现
GET    /finance/api/v1/statistics/anchor/{id}     收入统计
```

### 数据分析服务 API
```
GET    /analysis/api/aggregation/metrics          关键指标统计
GET    /analysis/api/v1/anchor/income/{id}        主播收入分析
GET    /analysis/api/timeseries/daily             每日时间序列
GET    /analysis/api/v1/user/{id}                 用户画像
```

> 📘 详细 API 文档请查看 [docs/ApiSum/0-API-SUMMARY.md](docs/ApiSum/0-API-SUMMARY.md)

## 💡 核心技术特性

### 高并发处理
- **异步处理**: 线程池处理打赏请求，快速响应
- **批量同步**: 打赏数据批量推送，减少网络开销
- **幂等性设计**: traceId 唯一索引，防止重复提交

### 数据一致性
- **分布式锁**: Redis 实现，保证结算操作原子性
- **数据同步**: 主动推送 + 断点续传机制
- **状态机**: 防止重复处理，保证数据正确性

### 缓存策略
- **热点数据缓存**: 直播间、主播、观众信息（Redis）
- **查询优化**: TOP10 榜单、分析结果缓存
- **读写分离**: 主从模式提升查询性能

### 智能标签系统
- **标签关联度**: 基于观众行为计算标签关联性
- **智能分配**: 根据标签匹配度分配观众
- **动态更新**: 实时更新标签关联度

## 🧪 测试与Mock

### 使用 Mock 服务生成测试数据

```bash
# 1. 批量创建主播
curl -X POST http://localhost:8087/api/v1/mock/anchor/batch-create?count=10

# 2. 批量创建观众
curl -X POST http://localhost:8087/api/v1/mock/audience/batch-create?count=1000

# 3. 模拟打赏（高并发）
curl -X POST http://localhost:8087/api/v1/mock/recharge/batch-create?count=5000

# 4. 查询统计数据
curl http://localhost:8087/api/v1/mock/data/statistics
```

## 📊 性能指标

- **打赏处理**: 单节点 >500 TPS，双节点 >1000 TPS
- **接口响应**: 查询类 <2s，打赏类 <500ms
- **数据一致性**: 最终一致性 <5分钟
- **服务可用性**: 99.9% （支持故障转移）
- **启动时间**: 服务启动 <30s

## 📚 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 11+ | 开发语言 |
| Spring Boot | 2.7.x | 服务框架 |
| Spring Cloud | 2021.x | 微服务组件 |
| MySQL | 8.0+ | 关系型数据库 |
| Redis | 6.0+ | 分布式缓存 |
| Nginx | Latest | API网关/负载均衡 |
| Docker | Latest | 容器化部署 |
| Maven | 3.6+ | 项目构建 |

## 📖 文档导航

### API 文档
- [API 总览](docs/ApiSum/0-API-SUMMARY.md)
- [主播服务 API](docs/ApiSum/1-anchor-service-API.md)
- [观众服务 API](docs/ApiSum/2-audience-service-API.md)
- [财务服务 API](docs/ApiSum/3-finance-service-API.md)
- [数据分析服务 API](docs/ApiSum/4-data-analysis-service-API.md)

### 设计文档
- [系统设计文档](docs/requirements/系统设计文档.md)
- [数据库重构指南](docs/hulfway/DATABASE_REFACTORING_COMPLETE.md)
- [后台管理设计](docs/admin-console/0-OVERVIEW.md)

### 部署文档
- [Docker 快速启动](deployment/docker/QUICKSTART.md)
- [服务架构说明](deployment/docker/SERVICES_INFO.md)
- [故障排查指南](deployment/docker/TROUBLESHOOTING.md)

## 🔍 监控与日志

### 健康检查
```bash
# 检查所有服务状态
./manage.sh     # 选择 12

# 单独检查服务
curl http://localhost:8081/anchor/api/v1/health
curl http://localhost:8082/audience/api/v1/health
curl http://localhost:8083/finance/api/v1/health
```

### 日志追踪
- **traceId 机制**: 全链路日志追踪
- **集中日志**: 统一日志格式和收集
- **控制台查询**: 支持按 traceId 查询完整链路

## 🚨 常见问题

### Q: 服务启动失败？
A: 检查端口占用，确保 MySQL 和 Redis 已启动，查看日志排查错误

### Q: 打赏数据不同步？
A: 检查观众服务和财务服务的连接状态，查看同步任务日志

### Q: 如何重置所有数据？
A: 运行管理脚本选择"重置基础设施"（选项10）

### Q: 如何扩展服务节点？
A: 修改 docker-compose.yml，增加服务副本数，通过 Nginx 负载均衡

## 📝 开发团队

- **项目类型**: JavaEE 架构与应用课程项目
- **开发周期**: 4周（详见开发进度方案）
- **团队规模**: 2人
- **技术栈**: Spring Cloud 微服务架构

## 📄 许可证

本项目仅用于学习和研究目的。

---

**最后更新**: 2026年1月14日  
**文档版本**: v2.0
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
