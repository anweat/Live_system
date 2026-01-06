# Web 前端目录

## 📋 说明

此目录用于存放 Vue.js 前端应用代码。

## 🚀 快速开始

### 初始化项目

```bash
# 方式一：使用 Vue CLI
npm init vue@latest

# 方式二：使用模板文件
cp package.json.template package.json
cp vite.config.js.template vite.config.js
cp index.html.template index.html
npm install
```

### 开发运行

```bash
npm run dev
```

访问: http://localhost:3000

### 生产构建

```bash
npm run build
```

构建产物将输出到 `dist/` 目录。

## 📁 推荐目录结构

```
web/
├── public/              # 静态资源
│   └── favicon.ico
├── src/
│   ├── assets/         # 资源文件
│   ├── components/     # 公共组件
│   ├── views/          # 页面组件
│   ├── router/         # 路由配置
│   ├── store/          # 状态管理
│   ├── api/            # API 接口
│   ├── utils/          # 工具函数
│   ├── App.vue         # 根组件
│   └── main.js         # 入口文件
├── index.html          # HTML 模板
├── package.json        # 依赖配置
├── vite.config.js      # Vite 配置
└── nginx.conf          # Nginx 配置（用于生产部署）
```

## 🔧 配置说明

### API 代理配置

开发环境下，Vite 会自动代理 `/api` 请求到后端服务：

```javascript
// vite.config.js
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8086',
      changeOrigin: true
    }
  }
}
```

### Nginx 配置

生产环境使用 Nginx 进行反向代理，配置文件已提供在 `nginx.conf`。

## 📦 Docker 构建

前端构建已集成到 Dockerfile 中：

```dockerfile
# Dockerfile.frontend
FROM node:16-alpine AS builder
WORKDIR /app
COPY web/package*.json ./
RUN npm install
COPY web/ ./
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY web/nginx.conf /etc/nginx/conf.d/default.conf
```

## 🎨 UI 框架

推荐使用 Element Plus：

```bash
npm install element-plus @element-plus/icons-vue
```

```javascript
// main.js
import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'

const app = createApp(App)
app.use(ElementPlus)
app.mount('#app')
```

## 📝 开发建议

1. **组件化开发**: 合理拆分组件，提高复用性
2. **状态管理**: 使用 Pinia 管理全局状态
3. **路由守卫**: 实现登录验证和权限控制
4. **API 封装**: 统一管理 API 请求
5. **错误处理**: 全局错误拦截和提示

## 🔗 相关文档

- [Vue 3 官方文档](https://cn.vuejs.org/)
- [Vite 官方文档](https://cn.vitejs.dev/)
- [Element Plus](https://element-plus.org/)

---

**当前状态**: ⏳ 待创建 - 请按照上述步骤初始化前端项目
