FROM eclipse-temurin:11-jre-jammy

WORKDIR /app

# 安装必要工具
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# 复制jar包
COPY target/mock-service-1.0.0.jar app.jar

# 暴露端口
EXPOSE 8090

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8090/mock/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=production", "app.jar"]
