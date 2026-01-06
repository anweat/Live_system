#!/bin/sh

echo "========================================="
echo "  Starting Finance Service"
echo "========================================="

# 等待数据库服务启动
if [ -n "$DB_HOST" ]; then
  echo "Waiting for database at $DB_HOST:$DB_PORT..."
  while ! nc -z $DB_HOST ${DB_PORT:-3306}; do
    sleep 1
  done
  echo "Database is ready!"
fi

# 等待Redis服务启动
if [ -n "$REDIS_HOST" ]; then
  echo "Waiting for Redis at $REDIS_HOST:$REDIS_PORT..."
  while ! nc -z $REDIS_HOST ${REDIS_PORT:-6379}; do
    sleep 1
  done
  echo "Redis is ready!"
fi

# 等待Consul服务启动
if [ -n "$CONSUL_HOST" ]; then
  echo "Waiting for Consul at $CONSUL_HOST:$CONSUL_PORT..."
  while ! nc -z $CONSUL_HOST ${CONSUL_PORT:-8500}; do
    sleep 1
  done
  echo "Consul is ready!"
fi

# 启动应用
echo "Starting Finance Service..."
exec java $JAVA_OPTS \
  -Djava.security.egd=file:/dev/./urandom \
  -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-production} \
  -jar /app/finance-service.jar
