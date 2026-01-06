#!/bin/bash

# ============================================================================
# 直播打赏系统 - Docker 管理工具快捷启动脚本
# ============================================================================

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# 管理脚本路径
MANAGE_SCRIPT="${SCRIPT_DIR}/deployment/docker/manage.sh"

# 检查管理脚本是否存在
if [ ! -f "$MANAGE_SCRIPT" ]; then
    echo "错误: 找不到管理脚本 $MANAGE_SCRIPT"
    exit 1
fi

# 确保脚本可执行
chmod +x "$MANAGE_SCRIPT"

# 执行管理脚本
exec "$MANAGE_SCRIPT" "$@"
