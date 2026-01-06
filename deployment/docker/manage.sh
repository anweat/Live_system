#!/bin/bash

# ============================================================================
# 直播打赏系统 - Docker 管理脚本
# ============================================================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 项目根目录
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
SERVICES_DIR="${PROJECT_ROOT}/services"
DOCKER_COMPOSE_FILE="${PROJECT_ROOT}/docker-compose.yml"

# 服务列表
BUSINESS_SERVICES=("anchor-service" "audience-service" "redis-service" "finance-service" "mock-service" "back-end-service")
INFRASTRUCTURE_SERVICES=("mysql" "shared-redis" "nginx")
ALL_SERVICES=("${INFRASTRUCTURE_SERVICES[@]}" "${BUSINESS_SERVICES[@]}")

# 有独立配置的服务
STANDALONE_SERVICES=("db-service")

# ============================================================================
# 工具函数
# ============================================================================

print_header() {
    echo -e "${CYAN}╔════════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${CYAN}║${NC}  ${PURPLE}$1${NC}"
    echo -e "${CYAN}╚════════════════════════════════════════════════════════════════╝${NC}"
}

print_success() {
    echo -e "${GREEN}✓${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

print_info() {
    echo -e "${BLUE}ℹ${NC} $1"
}

check_docker() {
    if ! command -v docker &> /dev/null; then
        print_error "Docker 未安装，请先安装 Docker"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        print_error "Docker Compose 未安装，请先安装 Docker Compose"
        exit 1
    fi
}

check_maven() {
    if ! command -v mvn &> /dev/null; then
        print_warning "Maven 未安装，某些功能可能不可用"
        return 1
    fi
    return 0
}

# Docker Compose 命令适配器
docker_compose_cmd() {
    if command -v docker-compose &> /dev/null; then
        docker-compose "$@"
    else
        docker compose "$@"
    fi
}

# ============================================================================
# 编译相关函数
# ============================================================================

clean_build_cache() {
    print_header "清理编译文件和缓存"
    
    cd "${PROJECT_ROOT}"
    
    # 清理 Maven 缓存
    if check_maven; then
        print_info "清理 Maven 编译文件..."
        mvn clean -f "${SERVICES_DIR}/pom.xml" -q
        print_success "Maven 编译文件已清理"
    fi
    
    # 清理 target 目录
    print_info "清理所有 target 目录..."
    find "${SERVICES_DIR}" -type d -name "target" -exec rm -rf {} + 2>/dev/null || true
    
    # 清理 Docker 缓存
    print_info "清理 Docker 构建缓存..."
    docker builder prune -f > /dev/null 2>&1 || true
    
    print_success "所有编译文件和缓存已清理"
}

compile_project() {
    print_header "编译项目"
    
    if ! check_maven; then
        print_error "Maven 未安装，无法编译项目"
        return 1
    fi
    
    cd "${SERVICES_DIR}"
    
    # 先编译 common 模块
    print_info "编译 common 模块..."
    if mvn clean install -pl common -am -DskipTests; then
        print_success "common 模块编译成功"
    else
        print_error "common 模块编译失败"
        return 1
    fi
    
    # 编译所有微服务
    print_info "编译所有微服务..."
    if mvn clean package -DskipTests; then
        print_success "项目编译成功"
        
        # 显示编译结果
        echo ""
        print_info "编译产物位置:"
        for service in "${BUSINESS_SERVICES[@]}"; do
            jar_file="${SERVICES_DIR}/${service}/target/${service}-1.0.0.jar"
            if [ -f "$jar_file" ]; then
                size=$(du -h "$jar_file" | cut -f1)
                echo -e "  ${GREEN}✓${NC} ${service}: ${size}"
            else
                echo -e "  ${RED}✗${NC} ${service}: 未找到 JAR 文件"
            fi
        done
    else
        print_error "项目编译失败"
        return 1
    fi
}

rebuild_project() {
    print_header "重新编译项目"
    
    # 清理缓存
    clean_build_cache
    
    echo ""
    
    # 重新编译
    compile_project
}

check_compilation() {
    print_info "检查编译状态..."
    
    local all_compiled=true
    for service in "${BUSINESS_SERVICES[@]}"; do
        jar_file="${SERVICES_DIR}/${service}/target/${service}-1.0.0.jar"
        if [ ! -f "$jar_file" ]; then
            print_warning "${service} 未编译"
            all_compiled=false
        fi
    done
    
    if [ "$all_compiled" = true ]; then
        print_success "所有服务已编译"
        return 0
    else
        return 1
    fi
}

# ============================================================================
# Docker 服务管理
# ============================================================================

start_infrastructure() {
    print_header "启动基础设施服务"
    
    cd "${PROJECT_ROOT}"
    
    for service in "${INFRASTRUCTURE_SERVICES[@]}"; do
        print_info "启动 ${service}..."
        docker_compose_cmd up -d "$service"
    done
    
    echo ""
    print_info "等待服务健康检查..."
    sleep 5
    
    check_health "${INFRASTRUCTURE_SERVICES[@]}"
    print_success "基础设施服务已启动"
}

start_business_services() {
    print_header "启动业务服务"
    
    # 检查编译状态
    if ! check_compilation; then
        echo ""
        read -p "$(echo -e ${YELLOW}服务未编译，是否现在编译？ [Y/n]: ${NC})" -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]] || [[ -z $REPLY ]]; then
            compile_project || return 1
        else
            print_error "无法启动未编译的服务"
            return 1
        fi
    fi
    
    cd "${PROJECT_ROOT}"
    
    # 确保基础设施已启动
    print_info "检查基础设施服务..."
    for service in "${INFRASTRUCTURE_SERVICES[@]}"; do
        if ! docker ps --format '{{.Names}}' | grep -q "^${service}$"; then
            print_warning "${service} 未运行，正在启动..."
            start_infrastructure
            break
        fi
    done
    
    echo ""
    
    # 启动业务服务
    for service in "${BUSINESS_SERVICES[@]}"; do
        print_info "启动 ${service}..."
        docker_compose_cmd up -d "$service"
    done
    
    echo ""
    print_info "等待服务健康检查..."
    sleep 10
    
    check_health "${BUSINESS_SERVICES[@]}"
    print_success "业务服务已启动"
}

start_all_services() {
    print_header "一键启动所有服务"
    
    # 检查编译
    if ! check_compilation; then
        echo ""
        read -p "$(echo -e ${YELLOW}服务未编译，是否现在编译？ [Y/n]: ${NC})" -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]] || [[ -z $REPLY ]]; then
            compile_project || return 1
        else
            print_error "无法启动未编译的服务"
            return 1
        fi
    fi
    
    cd "${PROJECT_ROOT}"
    
    print_info "启动所有服务..."
    docker_compose_cmd up -d
    
    echo ""
    print_info "等待服务健康检查..."
    sleep 10
    
    check_health "${ALL_SERVICES[@]}"
    print_success "所有服务已启动"
}

stop_services() {
    print_header "停止服务"
    
    cd "${PROJECT_ROOT}"
    
    echo "请选择要停止的服务:"
    echo "  1) 停止所有服务"
    echo "  2) 停止业务服务（保留基础设施）"
    echo "  3) 停止基础设施服务"
    echo "  4) 返回主菜单"
    echo ""
    
    read -p "请输入选项 [1-4]: " choice
    
    case $choice in
        1)
            print_info "停止所有服务..."
            docker_compose_cmd down
            print_success "所有服务已停止"
            ;;
        2)
            print_info "停止业务服务..."
            for service in "${BUSINESS_SERVICES[@]}"; do
                docker_compose_cmd stop "$service"
            done
            print_success "业务服务已停止"
            ;;
        3)
            print_info "停止基础设施服务..."
            for service in "${INFRASTRUCTURE_SERVICES[@]}"; do
                docker_compose_cmd stop "$service"
            done
            print_success "基础设施服务已停止"
            ;;
        4)
            return
            ;;
        *)
            print_error "无效选项"
            ;;
    esac
}

restart_services() {
    print_header "重启服务"
    
    cd "${PROJECT_ROOT}"
    
    echo "请选择要重启的服务:"
    echo "  1) 重启所有服务"
    echo "  2) 重启业务服务"
    echo "  3) 重启基础设施服务"
    echo "  4) 重启指定服务"
    echo "  5) 返回主菜单"
    echo ""
    
    read -p "请输入选项 [1-5]: " choice
    
    case $choice in
        1)
            print_info "重启所有服务..."
            docker_compose_cmd restart
            print_success "所有服务已重启"
            ;;
        2)
            print_info "重启业务服务..."
            for service in "${BUSINESS_SERVICES[@]}"; do
                docker_compose_cmd restart "$service"
            done
            print_success "业务服务已重启"
            ;;
        3)
            print_info "重启基础设施服务..."
            for service in "${INFRASTRUCTURE_SERVICES[@]}"; do
                docker_compose_cmd restart "$service"
            done
            print_success "基础设施服务已重启"
            ;;
        4)
            echo ""
            echo "可用服务: ${ALL_SERVICES[*]}"
            read -p "请输入服务名称: " service_name
            if docker ps -a --format '{{.Names}}' | grep -q "^${service_name}$"; then
                print_info "重启 ${service_name}..."
                docker_compose_cmd restart "$service_name"
                print_success "${service_name} 已重启"
            else
                print_error "服务不存在: ${service_name}"
            fi
            ;;
        5)
            return
            ;;
        *)
            print_error "无效选项"
            ;;
    esac
}

rebuild_and_restart() {
    print_header "重新构建并重启"
    
    echo "此操作将:"
    echo "  1) 清理编译缓存"
    echo "  2) 重新编译项目"
    echo "  3) 重新构建 Docker 镜像"
    echo "  4) 重启服务"
    echo ""
    
    read -p "$(echo -e ${YELLOW}确认继续？ [y/N]: ${NC})" -n 1 -r
    echo
    
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        print_info "操作已取消"
        return
    fi
    
    # 重新编译
    rebuild_project || return 1
    
    echo ""
    
    # 重新构建 Docker 镜像
    print_header "重新构建 Docker 镜像"
    cd "${PROJECT_ROOT}"
    
    for service in "${BUSINESS_SERVICES[@]}"; do
        print_info "构建 ${service} 镜像..."
        docker_compose_cmd build --no-cache "$service"
    done
    
    echo ""
    
    # 重启服务
    print_info "重启服务..."
    for service in "${BUSINESS_SERVICES[@]}"; do
        docker_compose_cmd up -d --force-recreate "$service"
    done
    
    echo ""
    print_info "等待服务健康检查..."
    sleep 10
    
    check_health "${BUSINESS_SERVICES[@]}"
    print_success "服务已重新构建并启动"
}

reset_infrastructure() {
    print_header "重置基础设施"
    
    echo -e "${RED}警告: 此操作将删除所有数据！${NC}"
    echo ""
    echo "此操作将:"
    echo "  1) 停止并删除所有容器"
    echo "  2) 删除所有数据卷（MySQL 数据、Redis 数据）"
    echo "  3) 重新启动基础设施"
    echo ""
    
    read -p "$(echo -e ${RED}确认继续？ 输入 YES 确认: ${NC})" confirm
    
    if [ "$confirm" != "YES" ]; then
        print_info "操作已取消"
        return
    fi
    
    cd "${PROJECT_ROOT}"
    
    print_info "停止所有服务..."
    docker_compose_cmd down -v
    
    echo ""
    print_info "清理悬空镜像..."
    docker image prune -f > /dev/null 2>&1 || true
    
    echo ""
    print_info "重新启动基础设施..."
    start_infrastructure
    
    print_success "基础设施已重置"
}

# ============================================================================
# 日志和状态检查
# ============================================================================

view_logs() {
    print_header "查看日志"
    
    cd "${PROJECT_ROOT}"
    
    echo "请选择要查看的日志:"
    echo "  1) 所有服务日志"
    echo "  2) 业务服务日志"
    echo "  3) 基础设施日志"
    echo "  4) 指定服务日志"
    echo "  5) 返回主菜单"
    echo ""
    
    read -p "请输入选项 [1-5]: " choice
    
    case $choice in
        1)
            print_info "查看所有服务日志 (Ctrl+C 退出)..."
            docker_compose_cmd logs -f --tail=100
            ;;
        2)
            print_info "查看业务服务日志 (Ctrl+C 退出)..."
            docker_compose_cmd logs -f --tail=100 "${BUSINESS_SERVICES[@]}"
            ;;
        3)
            print_info "查看基础设施日志 (Ctrl+C 退出)..."
            docker_compose_cmd logs -f --tail=100 "${INFRASTRUCTURE_SERVICES[@]}"
            ;;
        4)
            echo ""
            echo "可用服务: ${ALL_SERVICES[*]}"
            read -p "请输入服务名称: " service_name
            if docker ps -a --format '{{.Names}}' | grep -q "^${service_name}$"; then
                print_info "查看 ${service_name} 日志 (Ctrl+C 退出)..."
                docker_compose_cmd logs -f --tail=100 "$service_name"
            else
                print_error "服务不存在: ${service_name}"
            fi
            ;;
        5)
            return
            ;;
        *)
            print_error "无效选项"
            ;;
    esac
}

check_health() {
    local services=("$@")
    local all_healthy=true
    
    echo ""
    print_info "服务健康状态:"
    echo ""
    
    for service in "${services[@]}"; do
        if docker ps --format '{{.Names}}' | grep -q "^${service}$"; then
            local status=$(docker inspect --format='{{.State.Health.Status}}' "$service" 2>/dev/null || echo "unknown")
            
            if [ "$status" = "healthy" ]; then
                echo -e "  ${GREEN}●${NC} ${service}: 健康"
            elif [ "$status" = "starting" ]; then
                echo -e "  ${YELLOW}●${NC} ${service}: 启动中"
                all_healthy=false
            elif [ "$status" = "unhealthy" ]; then
                echo -e "  ${RED}●${NC} ${service}: 不健康"
                all_healthy=false
            elif [ "$status" = "unknown" ] || [ -z "$status" ]; then
                # 没有健康检查，检查是否运行
                local running=$(docker inspect --format='{{.State.Running}}' "$service" 2>/dev/null || echo "false")
                if [ "$running" = "true" ]; then
                    echo -e "  ${BLUE}●${NC} ${service}: 运行中 (无健康检查)"
                else
                    echo -e "  ${RED}●${NC} ${service}: 已停止"
                    all_healthy=false
                fi
            else
                echo -e "  ${RED}●${NC} ${service}: 未知状态"
                all_healthy=false
            fi
        else
            echo -e "  ${RED}○${NC} ${service}: 未运行"
            all_healthy=false
        fi
    done
    
    echo ""
    
    if [ "$all_healthy" = true ]; then
        print_success "所有服务运行正常"
    else
        print_warning "部分服务存在问题"
    fi
}

show_status() {
    print_header "系统状态"
    
    cd "${PROJECT_ROOT}"
    
    # 显示 Docker Compose 服务状态
    echo -e "${CYAN}Docker Compose 服务:${NC}"
    docker_compose_cmd ps
    
    echo ""
    
    # 显示详细健康状态
    check_health "${ALL_SERVICES[@]}"
    
    echo ""
    
    # 显示端口映射
    print_info "端口映射:"
    echo ""
    echo "  基础设施:"
    echo "    - MySQL:        3306"
    echo "    - Shared Redis: 6379"
    echo "    - Nginx:        80, 443"
    echo ""
    echo "  业务服务:"
    echo "    - Anchor Service:   8081"
    echo "    - Audience Service: 8082"
    echo "    - Finance Service:  8083"
    echo "    - Redis Service:    8085"
    echo "    - Back-end Service: 8086 (API) / 8087 (Web)"
    echo "    - Mock Service:     8090"
    echo ""
    echo "  独立部署服务:"
    echo "    - DB Service:       8084 (使用独立 docker-compose.yml)"
    
    echo ""
    
    # 显示资源使用情况
    print_info "资源使用情况:"
    echo ""
    docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}" $(docker ps --format '{{.Names}}' | grep -E "$(IFS=\|; echo "${ALL_SERVICES[*]}")")
}

# ============================================================================
# 独立服务管理
# ============================================================================

manage_standalone_service() {
    print_header "管理独立服务 (db-service)"
    
    local DB_SERVICE_DIR="${SERVICES_DIR}/db-service"
    
    if [ ! -f "${DB_SERVICE_DIR}/docker-compose.yml" ]; then
        print_error "db-service 的 docker-compose.yml 不存在"
        return 1
    fi
    
    echo "请选择操作:"
    echo "  1) 启动 db-service"
    echo "  2) 停止 db-service"
    echo "  3) 重启 db-service"
    echo "  4) 查看 db-service 日志"
    echo "  5) 检查 db-service 状态"
    echo "  6) 重新构建 db-service"
    echo "  7) 返回主菜单"
    echo ""
    
    read -p "请输入选项 [1-7]: " choice
    
    cd "${DB_SERVICE_DIR}"
    
    case $choice in
        1)
            print_info "启动 db-service..."
            docker_compose_cmd up -d
            print_success "db-service 已启动"
            ;;
        2)
            print_info "停止 db-service..."
            docker_compose_cmd down
            print_success "db-service 已停止"
            ;;
        3)
            print_info "重启 db-service..."
            docker_compose_cmd restart
            print_success "db-service 已重启"
            ;;
        4)
            print_info "查看 db-service 日志 (Ctrl+C 退出)..."
            docker_compose_cmd logs -f --tail=100
            ;;
        5)
            print_info "db-service 服务状态:"
            docker_compose_cmd ps
            ;;
        6)
            print_info "重新构建 db-service..."
            docker_compose_cmd build --no-cache
            docker_compose_cmd up -d --force-recreate
            print_success "db-service 已重新构建"
            ;;
        7)
            return
            ;;
        *)
            print_error "无效选项"
            ;;
    esac
}

# ============================================================================
# 主菜单
# ============================================================================

show_menu() {
    clear
    print_header "直播打赏系统 - Docker 管理工具"
    echo ""
    echo -e "${CYAN}【编译管理】${NC}"
    echo "  1) 编译项目"
    echo "  2) 清理编译缓存"
    echo "  3) 重新编译（清理+编译）"
    echo ""
    echo -e "${CYAN}【服务部署】${NC}"
    echo "  4) 一键启动所有服务"
    echo "  5) 启动基础设施（MySQL、Redis、Nginx）"
    echo "  6) 启动业务服务"
    echo "  7) 停止服务"
    echo "  8) 重启服务"
    echo ""
    echo -e "${CYAN}【高级操作】${NC}"
    echo "  9) 重新构建并重启服务"
    echo " 10) 重置基础设施（删除所有数据）"
    echo ""
    echo -e "${CYAN}【监控诊断】${NC}"
    echo " 11) 查看日志"
    echo " 12) 检查服务状态"
    echo ""
    echo -e "${CYAN}【独立服务】${NC}"
    echo " 13) 管理独立服务 (db-service)"
    echo ""
    echo -e "${CYAN}【其他】${NC}"
    echo "  0) 退出"
    echo ""
    echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
}

main() {
    # 检查 Docker 环境
    check_docker
    
    while true; do
        show_menu
        read -p "请输入选项 [0-13]: " choice
        echo ""
        
        case $choice in
            1)
                compile_project
                ;;
            2)
                clean_build_cache
                ;;
            3)
                rebuild_project
                ;;
            4)
                start_all_services
                ;;
            5)
                start_infrastructure
                ;;
            6)
                start_business_services
                ;;
            7)
                stop_services
                ;;
            8)
                restart_services
                ;;
            9)
                rebuild_and_restart
                ;;
            10)
                reset_infrastructure
                ;;
            11)
                view_logs
                ;;
            12)
                show_status
                ;;
            13)
                manage_standalone_service
                ;;
            0)
                print_info "退出管理工具"
                exit 0
                ;;
            *)
                print_error "无效选项，请重新输入"
                ;;
        esac
        
        echo ""
        read -p "按 Enter 键继续..." -r
    done
}

# ============================================================================
# 脚本入口
# ============================================================================

# 如果直接运行脚本，显示菜单
if [ "${BASH_SOURCE[0]}" = "${0}" ]; then
    main
fi
