# 使用 Alpine 构建阶段（包含 Redis）
FROM redis:7-alpine AS redis-base

# 使用 Eclipse Temurin JRE 11 作为主镜像
FROM eclipse-temurin:11-jre-jammy

# 复制 Redis 可执行文件
COPY --from=redis-base /usr/local/bin/redis-server /usr/local/bin/redis-server
COPY --from=redis-base /usr/local/bin/redis-cli /usr/local/bin/redis-cli

# 创建 Redis 用户和目录
RUN mkdir -p /data /var/log/redis && \
    useradd -r redis || true

# 设置工作目录
WORKDIR /app

# 设置环境变量
ENV JAVA_OPTS="-Xms512m -Xmx512m"
ENV SPRING_PROFILES_ACTIVE=production
ENV REDIS_ENABLED=true

# 复制启动脚本
COPY docker-entrypoint.sh /app/docker-entrypoint.sh
RUN chmod +x /app/docker-entrypoint.sh

# 复制构建的 JAR 文件（使用 ARG 支持动态镜像）
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# 暴露端口
EXPOSE 8081 6379

# 启动脚本
ENTRYPOINT ["/app/docker-entrypoint.sh"]
