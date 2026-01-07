# Nginx Docker化使用说明

## 目录结构建议
```
services/nginx/
├── Dockerfile
├── docker-compose.yml
├── docker/
│   └── nginx.conf
├── certs/           # 生产环境证书（可选）
├── logs/            # 日志挂载目录（可选）
└── dist/            # 前端静态资源（可选）
```

## 构建与启动

### 构建镜像
```sh
docker build -t nginx-gateway .
```

### 启动服务（推荐用compose）
```sh
docker-compose up -d
```

## 开发与生产环境建议
- 开发环境可直接挂载配置、静态资源，便于热更新。
- 生产环境建议：
  - 挂载证书目录，启用HTTPS（修改nginx.conf和Dockerfile）。
  - 日志目录挂载到宿主机，便于日志收集。
  - 可用CI/CD自动构建镜像并推送。

## 其他
- 如需动态渲染nginx.conf，可自定义entrypoint脚本并在Dockerfile中启用。
- 详细nginx路由见`docker/nginx.conf`。

