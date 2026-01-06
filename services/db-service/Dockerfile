FROM eclipse-temurin:11-jre-jammy

# 安装 curl 用于健康检查
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# 复制JAR文件
COPY target/db-service-1.0.0.jar db-service.jar
COPY src/main/resources/application.yml config/application.yml

# 创建日志目录
RUN mkdir -p logs

# 暴露端口
EXPOSE 8081

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["java", \
    "-Xms512m", \
    "-Xmx1024m", \
    "-XX:+UseG1GC", \
    "-XX:+ParallelRefProcEnabled", \
    "-jar", \
    "db-service.jar", \
    "--spring.config.location=file:config/application.yml"]
