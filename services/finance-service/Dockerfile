FROM eclipse-temurin:11-jre-jammy

LABEL maintainer="liveroom-finance"
LABEL description="Finance Service - 财务服务"

# 设置工作目录
WORKDIR /app

# 复制jar包
COPY target/finance-service-1.0.0.jar /app/finance-service.jar

# 复制启动脚本
COPY docker-entrypoint.sh /app/docker-entrypoint.sh
RUN chmod +x /app/docker-entrypoint.sh

# 创建日志目录
RUN mkdir -p /app/logs

# 暴露端口
EXPOSE 8082

# 设置环境变量
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
ENV SPRING_PROFILES_ACTIVE="production"

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8082/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["/app/docker-entrypoint.sh"]
