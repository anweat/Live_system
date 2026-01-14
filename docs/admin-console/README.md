# 后端管理控制台文档索引

## 文档概览

本目录包含直播系统后端管理控制台的完整设计文档,涵盖架构设计、功能规划、UI原型和实施指南。

---

## 文档列表

### [0-OVERVIEW.md](./0-OVERVIEW.md) - 总体设计概览
**内容摘要**:
- 项目简介和核心功能模块
- 技术架构(前端Vue3 + 后端Spring Boot)
- 部署架构和页面布局设计
- 菜单结构和关键特性
- 开发计划和文档导航

**适用对象**: 项目负责人、架构师、全体开发人员

---

### [1-FRONTEND-DESIGN.md](./1-FRONTEND-DESIGN.md) - 前端设计文档
**内容摘要**:
- 技术选型(Vue 3, TypeScript, Element Plus等)
- 项目结构和目录组织
- 9大功能模块页面设计
  - 控制台首页、主播管理、观众管理
  - 财务管理、数据分析、模拟测试
  - Docker管理、日志查询、系统设置
- UI设计规范和交互设计
- 开发规范和命名约定

**适用对象**: 前端开发人员、UI设计师

---

### [2-BACKEND-API-DESIGN.md](./2-BACKEND-API-DESIGN.md) - 后端API设计文档
**内容摘要**:
- 服务信息和API设计原则
- 统一响应格式
- 7大模块API接口定义(50+接口)
  - 主播管理、直播间管理
  - 观众管理、打赏管理
  - 财务管理、数据分析
  - 模拟服务
- 数据模型(VO)定义
- 错误码和开发注意事项

**适用对象**: 后端开发人员、接口对接人员

---

### [3-DEPLOYMENT-MANAGEMENT.md](./3-DEPLOYMENT-MANAGEMENT.md) - 部署管理功能设计
**内容摘要**:
- 多种部署策略支持(Docker/K8s/原生)
- Docker容器化部署详细实现
  - Docker Java API集成
  - 容器管理API(启动、停止、重启、删除)
  - 镜像管理和服务快捷操作
  - Docker Compose操作
- K8s集群部署设计要点(预留)
  - Deployment/Service/Pod管理
  - 自动扩缩容和负载均衡
- 原生服务器部署设计要点(预留)
  - SSH远程管理
  - Systemd服务管理
- 部署策略接口设计和工厂模式
- 服务映射配置、错误处理和安全考虑
- 扩展功能(监控、日志流、资源统计)

**适用对象**: 后端开发人员、运维人员

---

### [4-LOG-MANAGEMENT.md](./4-LOG-MANAGEMENT.md) - 日志管理功能设计
**内容摘要**:
- 日志类型(Docker日志 + 应用日志)
- 日志查询API设计
- Docker日志读取实现
- 应用日志解析和格式化
- WebSocket实时日志推送
- 前端日志查看器组件
- 性能优化和扩展功能
- 日志分析和告警

**适用对象**: 后端开发人员、运维人员

---

### [5-CONFIG-MANAGEMENT.md](./5-CONFIG-MANAGEMENT.md) - 配置管理功能设计
**内容摘要**:
- 配置文件结构(application.yml)
- 配置查询和修改API
- 环境管理(dev/test/prod切换)
- 配置模板和验证
- 配置备份与恢复
- YAML文件读写实现
- 前端配置编辑器
- 安全考虑和扩展功能

**适用对象**: 后端开发人员、运维人员

---

### [6-UI-PROTOTYPE.md](./6-UI-PROTOTYPE.md) - 页面原型设计
**内容摘要**:
- 设计规范和配色方案
- 整体布局(顶部导航+侧边栏+内容区)
- 9个主要页面原型
  - 控制台首页、主播列表、创建主播
  - 直播间管理、提现审核、收入分析
  - 容器列表、日志查询、环境配置
- 交互动画效果
- 响应式设计和移动端适配

**适用对象**: 前端开发人员、UI设计师、产品经理

---

### [7-IMPLEMENTATION-GUIDE.md](./7-IMPLEMENTATION-GUIDE.md) - 开发实施指南
**内容摘要**:
- 开发顺序(5个阶段)
- 快速启动指南
  - 后端服务创建和启动
  - 前端项目创建和配置
- 环境配置(dev/test/prod)
- Docker部署配置
- API测试方法
- 常见问题和解决方案
- 性能优化建议
- 安全建议
- 监控与日志配置

**适用对象**: 全体开发人员、运维人员

---

## 阅读顺序建议

### 对于项目经理/产品经理
1. 0-OVERVIEW.md (了解整体架构)
2. 6-UI-PROTOTYPE.md (了解页面设计)
3. 7-IMPLEMENTATION-GUIDE.md (了解开发计划)

### 对于前端开发人员
1. 0-OVERVIEW.md (了解整体架构)
2. 1-FRONTEND-DESIGN.md (前端技术栈和页面设计)
3. 2-BACKEND-API-DESIGN.md (了解后端接口)
4. 6-UI-PROTOTYPE.md (UI原型参考)
5. 7-IMPLEMENTATION-GUIDE.md (开发指南)

### 对于后端开发人员
1. 0-OVERVIEW.md (了解整体架构)
2. 2-BACKEND-API-DESIGN.md (API设计)
3. 3-DEPLOYMENT-MANAGEMENT.md (部署管理-多策略支持)
4. 4-LOG-MANAGEMENT.md (日志功能)
5. 5-CONFIG-MANAGEMENT.md (配置功能)
6. 7-IMPLEMENTATION-GUIDE.md (开发指南)

### 对于运维人员
1. 0-OVERVIEW.md (了解整体架构)
2. 3-DEPLOYMENT-MANAGEMENT.md (部署管理-多策略)
3. 4-LOG-MANAGEMENT.md (日志查询)
4. 5-CONFIG-MANAGEMENT.md (配置管理)
5. 7-IMPLEMENTATION-GUIDE.md (部署指南)

---

## 技术栈总览

### 前端技术
- **框架**: Vue 3.3+ (Composition API)
- **语言**: TypeScript 5.0+
- **UI库**: Element Plus 2.4+
- **状态管理**: Pinia 2.1+
- **路由**: Vue Router 4.2+
- **HTTP**: Axios 1.6+
- **图表**: ECharts 5.4+
- **构建**: Vite 4.0+

### 后端技术
- **框架**: Spring Boot 2.7+
- **语言**: Java 17
- **Docker**: Docker Java API 3.3+
- **配置**: SnakeYAML
- **WebSocket**: Spring WebSocket
- **HTTP客户端**: RestTemplate / WebClient

---

## 功能清单

### 业务管理 (5个模块)
- ✅ 主播管理 (创建、查询、更新、TOP排行)
- ✅ 观众管理 (注册、查询、禁用、消费统计)
- ✅ 直播间管理 (开播、关播、实时监控)
- ✅ 财务管理 (提现审核、结算查询、分成配置)
- ✅ 数据分析 (收入分析、排行榜、统计报表)

### 测试管理 (1个模块)
- ✅ 模拟服务 (批量生成数据、批次管理、测试场景)

### 运维管理 (3个模块)
- ✅ 部署管理 (多策略支持: Docker⭐/K8s/原生)
- ✅ 日志查询 (Docker日志、应用日志、实时流)
- ✅ 配置管理 (在线编辑、环境切换、备份恢复)

---

## 快速链接

- **项目根目录**: `../../`
- **API文档目录**: `../ApiSum/`
- **服务部署文档**: `../deployment/docker/`
- **数据库文档**: `../requirements/`

---

## 更新日志

### 2026-01-07
- ✅ 创建完整设计文档
- ✅ 定义7个文档模块
- ✅ 完成API接口设计(50+接口)
- ✅ 完成UI原型设计(9个页面)
- ✅ 编写实施指南

---

## 联系方式

如有疑问或建议,请联系:
- **项目负责人**: [待定]
- **技术负责人**: [待定]
- **文档维护**: [待定]

---

**注意**: 本文档为设计阶段文档,具体实现时可根据实际情况调整。所有设计遵循现有服务的API规范,确保系统一致性。
