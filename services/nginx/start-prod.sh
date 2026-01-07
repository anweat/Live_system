#!/bin/sh
# 生产环境启动脚本（需提前准备好证书、静态资源等）

docker-compose -f docker-compose.yml up -d --build

