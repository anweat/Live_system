# 使用 Eclipse Temurin JRE 11 作为基础镜像
FROM eclipse-temurin:11-jre-jammy

# 设置工作目录
WORKDIR /app

# 设置环境变量
ENV JAVA_OPTS="-Xms512m -Xmx2048m"
ENV SPRING_PROFILES_ACTIVE=production

# 复制构建的 JAR 文件
COPY target/redis-service-1.0.0.jar app.jar

# 暴露端口
EXPOSE 8085

# 启动应用
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
