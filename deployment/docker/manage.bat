@echo off
REM ============================================================================
REM Live Streaming System - Docker Management Script (Windows)
REM ============================================================================

chcp 65001 >nul
setlocal enabledelayedexpansion

set "PROJECT_ROOT=%~dp0..\.."
set "SERVICES_DIR=%PROJECT_ROOT%\services"
set "DOCKER_COMPOSE_FILE=%PROJECT_ROOT%\docker-compose.yml"

REM Service Lists
set "BUSINESS_SERVICES=anchor-service audience-service redis-service finance-service mock-service back-end-service"
set "INFRASTRUCTURE_SERVICES=mysql shared-redis nginx"
set "STANDALONE_SERVICES=db-service"

REM ============================================================================
REM Utility Functions
REM ============================================================================

:print_header
echo(
echo ================================================================
echo   %~1
echo ================================================================
echo(
goto :eof

:print_success
echo [92m[OK][0m %~1
goto :eof

:print_error
echo [91m[ERROR][0m %~1
goto :eof

:print_warning
echo [93m[WARNING][0m %~1
goto :eof

:print_info
echo [94m[INFO][0m %~1
goto :eof

:check_docker
where docker >nul 2>&1
if errorlevel 1 (
    call :print_error "Docker not installed. Please install Docker Desktop first"
    pause
    exit /b 1
)

where docker-compose >nul 2>&1
if errorlevel 1 (
    docker compose version >nul 2>&1
    if errorlevel 1 (
        call :print_error "Docker Compose not installed"
        pause
        exit /b 1
    )
)
goto :eof

:check_maven
where mvn >nul 2>&1
if errorlevel 1 (
    call :print_warning "Maven not installed. Some features may be unavailable"
    exit /b 1
)
exit /b 0

:docker_compose_cmd
where docker-compose >nul 2>&1
if errorlevel 1 (
    docker compose %*
) else (
    docker-compose %*
)
goto :eof

REM ============================================================================
REM Build Management
REM ============================================================================

:clean_build_cache
call :print_header "Clean Build Cache"

cd /d "%PROJECT_ROOT%"

call :check_maven
if not errorlevel 1 (
    call :print_info "Cleaning Maven build files..."
    call mvn clean -f "%SERVICES_DIR%\pom.xml" -q
    call :print_success "Maven build files cleaned"
)

call :print_info "Cleaning all target directories..."
for /d /r "%SERVICES_DIR%" %%d in (target) do (
    if exist "%%d" (
        rmdir /s /q "%%d" 2>nul
    )
)

call :print_info "Cleaning Docker build cache..."
docker builder prune -f >nul 2>&1

call :print_success "All build files and cache cleaned"
echo(
pause
goto :eof

:compile_project
call :print_header "Compile Project"

call :check_maven
if errorlevel 1 (
    call :print_error "Maven not installed. Cannot compile project"
    pause
    goto :eof
)

cd /d "%SERVICES_DIR%"

call :print_info "Compiling common module..."
call mvn clean install -pl common -am -DskipTests
if errorlevel 1 (
    call :print_error "Common module compilation failed"
    pause
    goto :eof
)
call :print_success "Common module compiled successfully"

call :print_info "Compiling all microservices..."
call mvn clean package -DskipTests
if errorlevel 1 (
    call :print_error "Project compilation failed"
    pause
    goto :eof
)

call :print_success "Project compiled successfully"
echo(
call :print_info "Build artifacts location:"

for %%s in (%BUSINESS_SERVICES%) do (
    set "jar_file=%SERVICES_DIR%\%%s\target\%%s-1.0.0.jar"
    if exist "!jar_file!" (
        echo   [92m[OK][0m %%s
    ) else (
        echo   [91m[MISSING][0m %%s: JAR file not found
    )
)

echo(
pause
goto :eof

:rebuild_project
call :print_header "Rebuild Project"
call :clean_build_cache
call :compile_project
goto :eof

:check_compilation
call :print_info "Checking compilation status..."

set "all_compiled=1"
for %%s in (%BUSINESS_SERVICES%) do (
    set "jar_file=%SERVICES_DIR%\%%s\target\%%s-1.0.0.jar"
    if not exist "!jar_file!" (
        call :print_warning "%%s not compiled"
        set "all_compiled=0"
    )
)

if !all_compiled! equ 1 (
    call :print_success "All services compiled"
    exit /b 0
) else (
    exit /b 1
)

REM ============================================================================
REM Docker Service Management
REM ============================================================================

:start_infrastructure
call :print_header "Start Infrastructure Services"

cd /d "%PROJECT_ROOT%"

for %%s in (%INFRASTRUCTURE_SERVICES%) do (
    call :print_info "Starting %%s..."
    call :docker_compose_cmd up -d %%s
)

echo(
call :print_info "Waiting for health checks..."
timeout /t 5 /nobreak >nul

call :print_success "Infrastructure services started"
echo(
pause
goto :eof

:start_business_services
call :print_header "Start Business Services"

call :check_compilation
if errorlevel 1 (
    echo(
    set /p "compile=Services not compiled. Compile now? [Y/n]: "
    if /i "!compile!"=="n" (
        call :print_error "Cannot start uncompiled services"
        pause
        goto :eof
    )
    call :compile_project
)

cd /d "%PROJECT_ROOT%"

call :print_info "Checking infrastructure services..."
for %%s in (%INFRASTRUCTURE_SERVICES%) do (
    docker ps --format "{{.Names}}" | findstr /x "%%s" >nul
    if errorlevel 1 (
        call :print_warning "%%s not running. Starting..."
        call :start_infrastructure
        goto :start_business_continue
    )
)

:start_business_continue
echo(

for %%s in (%BUSINESS_SERVICES%) do (
    call :print_info "Starting %%s..."
    call :docker_compose_cmd up -d %%s
)

echo(
call :print_info "Waiting for health checks..."
timeout /t 10 /nobreak >nul

call :print_success "Business services started"
echo(
pause
goto :eof

:start_all_services
call :print_header "Start All Services"

call :check_compilation
if errorlevel 1 (
    echo(
    set /p "compile=Services not compiled. Compile now? [Y/n]: "
    if /i "!compile!"=="n" (
        call :print_error "Cannot start uncompiled services"
        pause
        goto :eof
    )
    call :compile_project
)

cd /d "%PROJECT_ROOT%"

call :print_info "Starting all services..."
call :docker_compose_cmd up -d

echo(
call :print_info "Waiting for health checks..."
timeout /t 10 /nobreak >nul

call :print_success "All services started"
echo(
pause
goto :eof

:stop_services
call :print_header "Stop Services"

echo Please select services to stop:
echo   1^) Stop all services
echo   2^) Stop business services (keep infrastructure)
echo   3^) Stop infrastructure services
echo   4^) Return to main menu
echo(

set /p "choice=Enter option [1-4]: "

cd /d "%PROJECT_ROOT%"

if "!choice!"=="1" (
    call :print_info "Stopping all services..."
    call :docker_compose_cmd down
    call :print_success "All services stopped"
) else if "!choice!"=="2" (
    call :print_info "Stopping business services..."
    for %%s in (%BUSINESS_SERVICES%) do (
        call :docker_compose_cmd stop %%s
    )
    call :print_success "Business services stopped"
) else if "!choice!"=="3" (
    call :print_info "Stopping infrastructure services..."
    for %%s in (%INFRASTRUCTURE_SERVICES%) do (
        call :docker_compose_cmd stop %%s
    )
    call :print_success "Infrastructure services stopped"
) else if "!choice!"=="4" (
    goto :eof
) else (
    call :print_error "Invalid option"
)

echo(
pause
goto :eof

:restart_services
call :print_header "Restart Services"

echo Please select services to restart:
echo   1^) Restart all services
echo   2^) Restart business services
echo   3^) Restart infrastructure services
echo   4^) Restart specific service
echo   5^) Return to main menu
echo(

set /p "choice=Enter option [1-5]: "

cd /d "%PROJECT_ROOT%"

if "!choice!"=="1" (
    call :print_info "Restarting all services..."
    call :docker_compose_cmd restart
    call :print_success "All services restarted"
) else if "!choice!"=="2" (
    call :print_info "Restarting business services..."
    for %%s in (%BUSINESS_SERVICES%) do (
        call :docker_compose_cmd restart %%s
    )
    call :print_success "Business services restarted"
) else if "!choice!"=="3" (
    call :print_info "Restarting infrastructure services..."
    for %%s in (%INFRASTRUCTURE_SERVICES%) do (
        call :docker_compose_cmd restart %%s
    )
    call :print_success "Infrastructure services restarted"
) else if "!choice!"=="4" (
    echo(
    echo Available services: %BUSINESS_SERVICES% %INFRASTRUCTURE_SERVICES%
    set /p "service_name=Enter service name: "
    docker ps -a --format "{{.Names}}" | findstr /x "!service_name!" >nul
    if not errorlevel 1 (
        call :print_info "Restarting !service_name!..."
        call :docker_compose_cmd restart !service_name!
        call :print_success "!service_name! restarted"
    ) else (
        call :print_error "Service not found: !service_name!"
    )
) else if "!choice!"=="5" (
    goto :eof
) else (
    call :print_error "Invalid option"
)

echo(
pause
goto :eof

:rebuild_and_restart
call :print_header "Rebuild and Restart"

echo This operation will:
echo   1^) Clean build cache
echo   2^) Rebuild project
echo   3^) Rebuild Docker images
echo   4^) Restart services
echo(

set /p "confirm=Continue? [y/N]: "
if /i not "!confirm!"=="y" (
    call :print_info "Operation cancelled"
    pause
    goto :eof
)

call :rebuild_project

echo(
call :print_header "Rebuild Docker Images"

cd /d "%PROJECT_ROOT%"

for %%s in (%BUSINESS_SERVICES%) do (
    call :print_info "Building %%s image..."
    call :docker_compose_cmd build --no-cache %%s
)

echo(
call :print_info "Restarting services..."
for %%s in (%BUSINESS_SERVICES%) do (
    call :docker_compose_cmd up -d --force-recreate %%s
)

echo(
call :print_info "Waiting for health checks..."
timeout /t 10 /nobreak >nul

call :print_success "Services rebuilt and restarted"
echo(
pause
goto :eof

:reset_infrastructure
call :print_header "Reset Infrastructure"

echo [91mWARNING: This operation will DELETE ALL DATA![0m
echo(
echo This operation will:
echo   1^) Stop and remove all containers
echo   2^) Delete all volumes (MySQL data, Redis data)
echo   3^) Restart infrastructure
echo(

set /p "confirm=Continue? Type YES to confirm: "
if not "!confirm!"=="YES" (
    call :print_info "Operation cancelled"
    pause
    goto :eof
)

cd /d "%PROJECT_ROOT%"

call :print_info "Stopping all services..."
call :docker_compose_cmd down -v

echo(
call :print_info "Cleaning dangling images..."
docker image prune -f >nul 2>&1

echo(
call :print_info "Restarting infrastructure..."
call :start_infrastructure

call :print_success "Infrastructure reset complete"
echo(
pause
goto :eof

REM ============================================================================
REM Logging and Status
REM ============================================================================

:view_logs
call :print_header "View Logs"

echo Please select logs to view:
echo   1^) All services logs
echo   2^) Business services logs
echo   3^) Infrastructure logs
echo   4^) Specific service logs
echo   5^) Return to main menu
echo(

set /p "choice=Enter option [1-5]: "

cd /d "%PROJECT_ROOT%"

if "!choice!"=="1" (
    call :print_info "Viewing all services logs (Ctrl+C to exit)..."
    call :docker_compose_cmd logs -f --tail=100
) else if "!choice!"=="2" (
    call :print_info "Viewing business services logs (Ctrl+C to exit)..."
    call :docker_compose_cmd logs -f --tail=100 %BUSINESS_SERVICES%
) else if "!choice!"=="3" (
    call :print_info "Viewing infrastructure logs (Ctrl+C to exit)..."
    call :docker_compose_cmd logs -f --tail=100 %INFRASTRUCTURE_SERVICES%
) else if "!choice!"=="4" (
    echo(
    echo Available services: %BUSINESS_SERVICES% %INFRASTRUCTURE_SERVICES%
    set /p "service_name=Enter service name: "
    docker ps -a --format "{{.Names}}" | findstr /x "!service_name!" >nul
    if not errorlevel 1 (
        call :print_info "Viewing !service_name! logs (Ctrl+C to exit)..."
        call :docker_compose_cmd logs -f --tail=100 !service_name!
    ) else (
        call :print_error "Service not found: !service_name!"
        pause
    )
) else if "!choice!"=="5" (
    goto :eof
) else (
    call :print_error "Invalid option"
    pause
)

goto :eof

:show_status
call :print_header "System Status"

cd /d "%PROJECT_ROOT%"

echo Docker Compose Services:
call :docker_compose_cmd ps

echo(
call :print_info "Port Mappings:"
echo(
echo   Infrastructure:
echo     - MySQL:        3306
echo     - Shared Redis: 6379
echo     - Nginx:        80, 443
echo(
echo   Business Services:
echo     - Anchor Service:   8081
echo     - Audience Service: 8082
echo     - Finance Service:  8083
echo     - Redis Service:    8085
echo     - Back-end Service: 8086 (API) / 8087 (Web)
echo     - Mock Service:     8090
echo(
echo   Standalone Services:
echo     - DB Service:       8084 (uses separate docker-compose.yml)

echo(
call :print_info "Resource Usage:"
echo(
docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}"

echo(
pause
goto :eof

REM ============================================================================
REM Standalone Service Management
REM ============================================================================

:manage_standalone_service
call :print_header "Manage Standalone Service (db-service)"

set "DB_SERVICE_DIR=%SERVICES_DIR%\db-service"

if not exist "%DB_SERVICE_DIR%\docker-compose.yml" (
    call :print_error "db-service docker-compose.yml not found"
    pause
    goto :eof
)

echo Please select operation:
echo   1^) Start db-service
echo   2^) Stop db-service
echo   3^) Restart db-service
echo   4^) View db-service logs
echo   5^) Check db-service status
echo   6^) Rebuild db-service
echo   7^) Return to main menu
echo(

set /p "choice=Enter option [1-7]: "

cd /d "%DB_SERVICE_DIR%"

if "!choice!"=="1" (
    call :print_info "Starting db-service..."
    call :docker_compose_cmd up -d
    call :print_success "db-service started"
    echo(
    pause
) else if "!choice!"=="2" (
    call :print_info "Stopping db-service..."
    call :docker_compose_cmd down
    call :print_success "db-service stopped"
    echo(
    pause
) else if "!choice!"=="3" (
    call :print_info "Restarting db-service..."
    call :docker_compose_cmd restart
    call :print_success "db-service restarted"
    echo(
    pause
) else if "!choice!"=="4" (
    call :print_info "Viewing db-service logs (Ctrl+C to exit)..."
    call :docker_compose_cmd logs -f --tail=100
) else if "!choice!"=="5" (
    call :print_info "db-service status:"
    call :docker_compose_cmd ps
    echo(
    pause
) else if "!choice!"=="6" (
    call :print_info "Rebuilding db-service..."
    call :docker_compose_cmd build --no-cache
    call :docker_compose_cmd up -d --force-recreate
    call :print_success "db-service rebuilt"
    echo(
    pause
) else if "!choice!"=="7" (
    goto :eof
) else (
    call :print_error "Invalid option"
    pause
)

goto :eof

REM ============================================================================
REM Main Menu
REM ============================================================================

:show_menu
cls
echo(
echo ================================================================
echo   Live Streaming System - Docker Management Tool
echo ================================================================
echo(
echo(
echo [Build Management]
echo   1^) Compile project
echo   2^) Clean build cache
echo   3^) Rebuild project (clean + compile)
echo(
echo [Service Deployment]
echo   4^) Start all services (one-click)
echo   5^) Start infrastructure (MySQL, Redis, Nginx)
echo   6^) Start business services
echo   7^) Stop services
echo   8^) Restart services
echo(
echo [Advanced Operations]
echo   9^) Rebuild and restart services
echo  10^) Reset infrastructure (DELETE ALL DATA)
echo(
echo [Monitoring]
echo  11^) View logs
echo  12^) Check service status
echo(
echo [Standalone Services]
echo  13^) Manage standalone service (db-service)
echo(
echo [Other]
echo   0^) Exit
echo(
echo ================================================================
goto :eof

:main
call :check_docker
if errorlevel 1 exit /b 1

:menu_loop
call :show_menu
set /p "choice=Enter option [0-13]: "
echo(

if "!choice!"=="1" (
    call :compile_project
) else if "!choice!"=="2" (
    call :clean_build_cache
) else if "!choice!"=="3" (
    call :rebuild_project
) else if "!choice!"=="4" (
    call :start_all_services
) else if "!choice!"=="5" (
    call :start_infrastructure
) else if "!choice!"=="6" (
    call :start_business_services
) else if "!choice!"=="7" (
    call :stop_services
) else if "!choice!"=="8" (
    call :restart_services
) else if "!choice!"=="9" (
    call :rebuild_and_restart
) else if "!choice!"=="10" (
    call :reset_infrastructure
) else if "!choice!"=="11" (
    call :view_logs
) else if "!choice!"=="12" (
    call :show_status
) else if "!choice!"=="13" (
    call :manage_standalone_service
) else if "!choice!"=="0" (
    call :print_info "Exiting management tool"
    exit /b 0
) else (
    call :print_error "Invalid option. Please try again"
    pause
)

goto menu_loop

REM ============================================================================
REM Script Entry Point
REM ============================================================================

call :main
