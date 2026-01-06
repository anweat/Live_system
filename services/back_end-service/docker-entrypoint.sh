#!/bin/bash

# ============================================================================
# 后端服务启动脚本
# ============================================================================

set -e

echo "==========================================="
echo "Starting Backend Service"
echo "==========================================="

# 等待数据库就绪
if [ -n "$MYSQL_HOST" ]; then
    echo "Waiting for MySQL at $MYSQL_HOST:${MYSQL_PORT:-3306}..."
    timeout=60
    while ! nc -z $MYSQL_HOST ${MYSQL_PORT:-3306}; do
        timeout=$((timeout-1))
        if [ $timeout -le 0 ]; then
            echo "Error: MySQL connection timeout"
            exit 1
        fi
        sleep 1
    done
    echo "MySQL is ready"
fi

# 等待 Redis 就绪
if [ -n "$REDIS_HOST" ]; then
    echo "Waiting for Redis at $REDIS_HOST:${REDIS_PORT:-6379}..."
    timeout=60
    while ! nc -z $REDIS_HOST ${REDIS_PORT:-6379}; do
        timeout=$((timeout-1))
        if [ $timeout -le 0 ]; then
            echo "Error: Redis connection timeout"
            exit 1
        fi
        sleep 1
    done
    echo "Redis is ready"
fi

# 启动 Nginx（如果配置了）
if command -v nginx &> /dev/null; then
    echo "Starting Nginx..."
    nginx
    echo "Nginx started"
fi

# 启动 Spring Boot 应用
echo "Starting Spring Boot application..."
echo "Java Options: $JAVA_OPTS"
echo "Active Profile: $SPRING_PROFILES_ACTIVE"

# 检查 jar 文件是否存在
if [ -f "/app/back_end-service.jar" ]; then
    exec java $JAVA_OPTS -jar /app/back_end-service.jar
else
    echo "Warning: back_end-service.jar not found!"
    echo "This is a placeholder. Please build the application first."
    # 保持容器运行以便调试
    tail -f /dev/null
fi
