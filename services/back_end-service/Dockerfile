# ============================================================================
# 后端服务完整部署 Dockerfile（包含前后端）
# 适用于一体化部署场景
# ============================================================================

# 阶段 1: 前端构建
FROM node:18-alpine AS frontend-builder

WORKDIR /frontend

# 复制前端源码（待实现）
# COPY web/package*.json ./
# RUN npm install
# COPY web/ ./
# RUN npm run build

# 创建占位
RUN mkdir -p /frontend/dist && \
    echo '<html><head><title>Live System</title></head><body><h1>Frontend Placeholder</h1></body></html>' > /frontend/dist/index.html

# 阶段 2: 后端 + Nginx
FROM eclipse-temurin:11-jre-jammy

# 安装 Nginx
RUN apt-get update && \
    apt-get install -y nginx curl && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# 复制后端 jar（待实现）
# COPY target/back_end-service-1.0.0.jar /app/app.jar

# 复制前端构建产物
COPY --from=frontend-builder /frontend/dist /usr/share/nginx/html

# 创建 Nginx 配置
RUN mkdir -p /etc/nginx/sites-available && \
    echo 'server { \
    listen 80; \
    server_name _; \
    \
    # 前端静态资源 \
    location / { \
        root /usr/share/nginx/html; \
        index index.html; \
        try_files $uri $uri/ /index.html; \
    } \
    \
    # 后端 API 代理 \
    location /api { \
        proxy_pass http://localhost:8086; \
        proxy_set_header Host $host; \
        proxy_set_header X-Real-IP $remote_addr; \
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for; \
        proxy_set_header X-Forwarded-Proto $scheme; \
    } \
}' > /etc/nginx/sites-available/default && \
    ln -sf /etc/nginx/sites-available/default /etc/nginx/sites-enabled/default

# 创建启动脚本
RUN echo '#!/bin/bash\n\
set -e\n\
\n\
# 启动 Nginx\n\
nginx\n\
\n\
# 启动后端应用（待实现）\n\
# java $JAVA_OPTS -jar /app/app.jar\n\
\n\
# 占位：保持容器运行\n\
echo "Backend service placeholder. Nginx is running."\n\
tail -f /dev/null\n\
' > /app/start.sh && chmod +x /app/start.sh

# 暴露端口
EXPOSE 80 8086

# 环境变量
ENV JAVA_OPTS="-Xms512m -Xmx1024m"
ENV SPRING_PROFILES_ACTIVE="production"

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost/ || exit 1

# 启动服务
CMD ["/app/start.sh"]
