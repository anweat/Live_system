# 前端设计文档

## 技术选型

### 核心框架
- **Vue 3.3+**: Composition API + `<script setup>`
- **TypeScript 5.0+**: 类型安全
- **Vite 4.0+**: 构建工具

### UI组件库
- **Element Plus 2.4+**: 主UI组件库
- **@element-plus/icons-vue**: 图标库

### 状态管理与路由
- **Pinia 2.1+**: 状态管理
- **Vue Router 4.2+**: 路由管理

### 工具库
- **Axios 1.6+**: HTTP客户端
- **ECharts 5.4+**: 数据可视化
- **Day.js 1.11+**: 日期处理
- **NProgress**: 页面加载进度条

### 开发工具
- **ESLint + Prettier**: 代码规范
- **Sass**: CSS预处理器

## 项目结构

```
admin-console-frontend/
├── public/                      # 静态资源
├── src/
│   ├── api/                     # API接口定义
│   │   ├── anchor.ts           # 主播相关API
│   │   ├── audience.ts         # 观众相关API
│   │   ├── finance.ts          # 财务相关API
│   │   ├── analysis.ts         # 数据分析API
│   │   ├── mock.ts             # 模拟服务API
│   │   ├── docker.ts           # Docker管理API
│   │   ├── logs.ts             # 日志查询API
│   │   └── config.ts           # 配置管理API
│   ├── assets/                  # 资源文件
│   │   ├── styles/             # 全局样式
│   │   └── images/             # 图片资源
│   ├── components/              # 通用组件
│   │   ├── Layout/             # 布局组件
│   │   │   ├── AppHeader.vue   # 顶部导航
│   │   │   ├── AppSidebar.vue  # 侧边栏
│   │   │   └── AppFooter.vue   # 底部状态栏
│   │   ├── Common/             # 通用组件
│   │   │   ├── ApiResponse.vue # API响应展示
│   │   │   ├── StatusTag.vue   # 状态标签
│   │   │   └── DataTable.vue   # 数据表格
│   │   └── Charts/             # 图表组件
│   │       ├── LineChart.vue
│   │       ├── BarChart.vue
│   │       └── PieChart.vue
│   ├── composables/             # 组合式函数
│   │   ├── useApi.ts           # API调用封装
│   │   ├── useTable.ts         # 表格逻辑
│   │   └── useWebSocket.ts     # WebSocket连接
│   ├── router/                  # 路由配置
│   │   └── index.ts
│   ├── stores/                  # Pinia状态管理
│   │   ├── app.ts              # 应用状态
│   │   ├── user.ts             # 用户状态
│   │   └── env.ts              # 环境配置
│   ├── types/                   # TypeScript类型定义
│   │   ├── api.d.ts            # API类型
│   │   ├── models.d.ts         # 数据模型
│   │   └── common.d.ts         # 通用类型
│   ├── utils/                   # 工具函数
│   │   ├── request.ts          # Axios封装
│   │   ├── format.ts           # 格式化工具
│   │   └── validate.ts         # 验证工具
│   ├── views/                   # 页面组件
│   │   ├── Dashboard/          # 控制台首页
│   │   ├── Anchor/             # 主播管理
│   │   ├── Audience/           # 观众管理
│   │   ├── Finance/            # 财务管理
│   │   ├── Analysis/           # 数据分析
│   │   ├── Mock/               # 模拟测试
│   │   ├── Docker/             # Docker管理
│   │   ├── Logs/               # 日志查询
│   │   └── Settings/           # 系统设置
│   ├── App.vue                  # 根组件
│   └── main.ts                  # 入口文件
├── index.html
├── package.json
├── tsconfig.json
└── vite.config.ts
```

## 页面设计

### 1. 控制台首页 (Dashboard)
**路径**: `/dashboard`

**功能**:
- 关键指标卡片（总用户数、活跃用户、总收入等）
- 收入趋势图表
- 服务状态监控
- 最近操作日志

**组件**: `views/Dashboard/Index.vue`

---

### 2. 主播管理

#### 2.1 主播列表 (AnchorList)
**路径**: `/anchor/list`

**功能**:
- 分页表格展示主播列表
- 搜索、筛选、排序
- 查看详情、编辑、删除操作
- 快速操作：查看直播间、查看收益

**表格字段**: ID、昵称、性别、粉丝数、累计收益、状态、操作

#### 2.2 创建主播 (AnchorCreate)
**路径**: `/anchor/create`

**功能**:
- 表单输入（昵称、真实姓名、性别、签名等）
- 表单验证
- 提交后自动创建直播间

#### 2.3 直播间管理 (LiveRoomManage)
**路径**: `/anchor/liveroom/:anchorId`

**功能**:
- 直播间信息展示
- 开播/关播操作
- 实时数据监控（在线观众、累计观众、收入）
- 打赏记录列表

---

### 3. 观众管理

#### 3.1 观众列表 (AudienceList)
**路径**: `/audience/list`

**功能**:
- 分页表格
- 按消费等级筛选
- 查看详情、编辑、禁用操作

#### 3.2 打赏记录 (RechargeRecords)
**路径**: `/audience/recharge`

**功能**:
- 按观众/主播筛选
- 时间范围筛选
- 打赏金额统计

---

### 4. 财务管理

#### 4.1 提现审核 (WithdrawalAudit)
**路径**: `/finance/withdrawal`

**功能**:
- 待审核列表
- 批准/拒绝操作（带原因）
- 状态筛选
- 审核历史

#### 4.2 结算查询 (Settlement)
**路径**: `/finance/settlement`

**功能**:
- 按主播查询结算明细
- 可提取余额查询
- 手动触发结算

#### 4.3 分成配置 (CommissionRate)
**路径**: `/finance/commission`

**功能**:
- 主播分成比例配置
- 平台分成比例
- 批量设置

---

### 5. 数据分析

#### 5.1 收入分析 (IncomeAnalysis)
**路径**: `/analysis/income`

**功能**:
- 主播收入排行榜
- 时间范围选择
- 收入趋势图表
- TOP观众展示

#### 5.2 排行榜 (Ranking)
**路径**: `/analysis/ranking`

**功能**:
- 主播粉丝排行
- 主播收益排行
- 观众消费排行
- 可切换时间维度

#### 5.3 统计报表 (Statistics)
**路径**: `/analysis/statistics`

**功能**:
- 平台关键指标
- 每小时统计数据
- 数据导出

---

### 6. 模拟测试

#### 6.1 数据生成 (MockGenerate)
**路径**: `/mock/generate`

**功能**:
- 批量生成主播
- 批量生成观众
- 生成打赏记录
- 模拟直播活动

#### 6.2 批次管理 (BatchManage)
**路径**: `/mock/batch`

**功能**:
- 批次列表
- 批次详情
- 删除批次

---

### 7. Docker管理

#### 7.1 容器列表 (ContainerList)
**路径**: `/docker/containers`

**功能**:
- 容器状态展示
- 启动/停止/重启操作
- 查看日志按钮
- 容器资源使用情况

#### 7.2 容器详情 (ContainerDetail)
**路径**: `/docker/container/:id`

**功能**:
- 容器配置信息
- 环境变量
- 端口映射
- 挂载卷

---

### 8. 日志查询

#### 8.1 Docker日志 (DockerLogs)
**路径**: `/logs/docker`

**功能**:
- 选择容器
- 实时日志流（WebSocket）
- 日志筛选（级别、关键词）
- 导出日志

#### 8.2 应用日志 (AppLogs)
**路径**: `/logs/application`

**功能**:
- 选择服务
- 日志级别筛选
- 时间范围选择
- 全文搜索

---

### 9. 系统设置

#### 9.1 环境配置 (EnvConfig)
**路径**: `/settings/environment`

**功能**:
- 环境切换（开发/测试/生产）
- 服务地址配置
- 端口配置
- 配置保存

#### 9.2 服务配置 (ServiceConfig)
**路径**: `/settings/services`

**功能**:
- 各服务配置展示
- 配置文件编辑
- 配置验证
- 重启服务

---

## UI设计规范

### 色彩方案
- **主色**: `#409EFF` (Element Plus 主题色)
- **成功**: `#67C23A`
- **警告**: `#E6A23C`
- **错误**: `#F56C6C`
- **信息**: `#909399`

### 布局规范
- **侧边栏宽度**: 200px (展开) / 64px (收起)
- **顶部导航高度**: 60px
- **内容间距**: 20px
- **卡片圆角**: 4px

### 动画效果
- **页面过渡**: 300ms ease
- **按钮hover**: transform scale(1.05)
- **加载动画**: Element Plus loading
- **菜单展开**: 200ms cubic-bezier

### 响应式断点
- **大屏**: >= 1920px
- **桌面**: >= 1200px
- **平板**: >= 768px
- **移动**: < 768px

## 交互设计

### API请求展示
每个API调用显示：
- 请求方法和URL
- 请求参数
- 响应状态码
- 响应数据（可折叠）
- 响应时间

### 错误处理
- 网络错误：显示重试按钮
- 业务错误：显示具体错误信息
- 权限错误：跳转登录页
- 服务器错误：显示错误详情

### 加载状态
- 表格加载：骨架屏
- 按钮加载：loading状态
- 页面加载：顶部进度条
- 异步操作：全局loading

## 开发规范

### 命名规范
- **组件**: PascalCase (AnchorList.vue)
- **文件夹**: kebab-case (anchor-manage)
- **变量**: camelCase (anchorList)
- **常量**: UPPER_SNAKE_CASE (API_BASE_URL)

### 代码规范
- 使用 Composition API
- TypeScript严格模式
- Props定义类型
- 组件注释说明
- ESLint检查通过

### Git提交规范
- feat: 新功能
- fix: 修复bug
- docs: 文档更新
- style: 样式调整
- refactor: 代码重构
