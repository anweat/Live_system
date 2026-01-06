#!/bin/sh
# Docker 启动脚本
set -e

echo "========== 启动 Audience Service =========="

REDIS_ENABLED=${REDIS_ENABLED:-true}

if [ "$REDIS_ENABLED" = "true" ]; then
    echo "启动 Redis 服务..."
    redis-server --bind 127.0.0.1 --port 6379 --daemonize yes --logfile /var/log/redis/redis.log
    sleep 2
    
    if redis-cli ping | grep -q "PONG"; then
        echo "✓ Redis 启动成功"
    else
        echo "✗ Redis 启动失败"
        exit 1
    fi
fi

echo "启动 Spring Boot 应用..."
exec java ${JAVA_OPTS} -jar /app/app.jar
