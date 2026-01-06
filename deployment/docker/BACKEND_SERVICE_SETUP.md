# Back-end Service 部署配置完成

## ✅ 已创建文件

### Docker 配置
- ✅ `Dockerfile` - 一体化部署（前后端集成）
- ✅ `Dockerfile.backend` - 仅后端部署
- ✅ `Dockerfile.frontend` - 仅前端部署
- ✅ `docker-entrypoint.sh` - 容器启动脚本

### 前端配置（web 目录）
- ✅ `nginx.conf` - Nginx 配置（生产环境）
- ✅ `package.json.template` - npm 依赖模板
- ✅ `vite.config.js.template` - Vite 配置模板
- ✅ `index.html.template` - HTML 入口模板
- ✅ `README.md` - 前端开发指南

### 文档
- ✅ `README.md` - 服务说明和使用指南

## 📦 部署方式

### 方式一：一体化部署（推荐）
使用 `Dockerfile`，在单个容器中运行前后端：
- 端口 80: 前端 Vue 应用
- 端口 8086: 后端 Spring Boot API

```yaml
back-end-service:
  ports:
    - "8087:80"      # Web 前端
    - "8086:8086"    # API 后端
```

### 方式二：分离部署
- 后端: `Dockerfile.backend`
- 前端: `Dockerfile.frontend`

## 🎯 当前状态

### ✅ 已完成
- Docker 容器配置（占位）
- Nginx 反向代理配置
- 前端构建流程配置
- 健康检查配置
- 环境变量配置
- 启动脚本

### ⏳ 待实现
- 后端 Spring Boot 应用代码
- 前端 Vue 应用代码
- pom.xml Maven 配置
- 数据库表结构
- API 接口开发

## 🚀 激活步骤

### 1. 初始化前端项目

```bash
cd services/back_end-service/web

# 使用模板文件
cp package.json.template package.json
cp vite.config.js.template vite.config.js
cp index.html.template index.html

# 安装依赖
npm install

# 开发运行
npm run dev
```

### 2. 创建后端项目

```bash
cd services/back_end-service

# 创建 pom.xml（参考其他服务）
# 创建 src/main/java 目录结构
# 实现 Spring Boot 应用
```

### 3. 更新 Dockerfile

取消注释实际的构建命令：

```dockerfile
# 前端构建
COPY web/package*.json ./
RUN npm install
COPY web/ ./
RUN npm run build

# 后端打包
COPY target/back_end-service-1.0.0.jar /app/app.jar
```

### 4. 测试部署

```bash
# 使用管理脚本
cd deployment
./manage.sh
# 选择 [1] 编译项目
# 选择 [4] 一键启动所有服务
```

## 📊 服务访问

### 开发环境
- 前端开发服务器: http://localhost:3000
- 后端 API: http://localhost:8086

### Docker 部署
- 前端 Web: http://localhost:8087
- 后端 API: http://localhost:8086/api
- 主网关: http://localhost (通过 Nginx)

## 🔗 服务集成

已添加到主 docker-compose.yml：

```yaml
back-end-service:
  - 依赖: mysql, shared-redis
  - 网络: live-network
  - 端口: 8087 (Web), 8086 (API)
  - 健康检查: ✅
  - 自动重启: ✅
```

已添加到管理脚本：
- ✅ manage.sh
- ✅ manage.bat
- ✅ 端口映射说明
- ✅ 服务状态监控

## 📝 开发建议

### 后端技术栈
- Spring Boot 2.7+
- MyBatis / JPA
- Redis (缓存)
- MySQL (数据库)
- Spring Security (认证授权)

### 前端技术栈
- Vue 3
- Vite
- Element Plus
- Vue Router
- Pinia
- Axios

### 功能模块
- 用户管理
- 权限控制
- 数据统计
- 系统配置
- 日志查询
- 实时监控

## ⚠️ 注意事项

1. **端口配置**: 8086 (API) 和 8087 (Web) 需要确保未被占用
2. **Nginx 配置**: API 代理路径为 `/api`
3. **数据库**: 需要创建 `live_backend_db` 数据库
4. **占位容器**: 当前容器仅输出占位信息，不会报错
5. **前端路由**: 使用 History 模式，需要 Nginx `try_files` 配置

## 📚 相关文档

- [服务 README](../services/back_end-service/README.md)
- [前端开发指南](../services/back_end-service/web/README.md)
- [部署指南](README.md)
- [服务架构说明](SERVICES_INFO.md)

---

**创建日期**: 2026-01-02  
**状态**: 🔄 占位阶段 - 配置完成，待开发实现
