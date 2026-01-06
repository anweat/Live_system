#!/bin/bash
# db-service 启动脚本

set -e

APP_NAME="db-service"
APP_VERSION="1.0.0"
JAVA_HOME=${JAVA_HOME:-/usr/lib/jvm/java-11-openjdk}
JAVA=$JAVA_HOME/bin/java

# 项目目录
PROJECT_HOME=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
TARGET_DIR="$PROJECT_HOME/target"
JAR_FILE="$TARGET_DIR/$APP_NAME-$APP_VERSION.jar"

echo "=========================================="
echo "启动 $APP_NAME 服务"
echo "=========================================="
echo "项目目录: $PROJECT_HOME"
echo "JAR文件: $JAR_FILE"
echo "Java: $JAVA"
echo "=========================================="

# 检查JAR文件是否存在
if [ ! -f "$JAR_FILE" ]; then
    echo "错误: JAR文件不存在: $JAR_FILE"
    echo "请先执行: mvn clean package"
    exit 1
fi

# 启动参数
JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+ParallelRefProcEnabled"

# 启动应用
echo "启动应用..."
$JAVA $JAVA_OPTS -jar "$JAR_FILE" \
    --spring.config.location=classpath:application.yml \
    --logging.file.name=logs/db-service.log

echo "=========================================="
echo "服务已启动"
echo "访问地址: http://localhost:8081"
echo "健康检查: http://localhost:8081/actuator/health"
echo "=========================================="
