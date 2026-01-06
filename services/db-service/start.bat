@echo off
REM db-service Windows 启动脚本

setlocal enabledelayedexpansion

set APP_NAME=db-service
set APP_VERSION=1.0.0
set PROJECT_HOME=%~dp0
set TARGET_DIR=%PROJECT_HOME%target
set JAR_FILE=%TARGET_DIR%\%APP_NAME%-%APP_VERSION%.jar

if "%JAVA_HOME%"=="" (
    set JAVA=java
) else (
    set JAVA=%JAVA_HOME%\bin\java.exe
)

echo ==========================================
echo 启动 %APP_NAME% 服务
echo ==========================================
echo 项目目录: %PROJECT_HOME%
echo JAR文件: %JAR_FILE%
echo Java: %JAVA%
echo ==========================================

if not exist "%JAR_FILE%" (
    echo 错误: JAR文件不存在: %JAR_FILE%
    echo 请先执行: mvn clean package
    pause
    exit /b 1
)

REM 启动参数
set JAVA_OPTS=-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+ParallelRefProcEnabled

REM 启动应用
echo 启动应用...
%JAVA% %JAVA_OPTS% -jar "%JAR_FILE%" ^
    --spring.config.location=classpath:application.yml ^
    --logging.file.name=logs/db-service.log

echo ==========================================
echo 服务已启动
echo 访问地址: http://localhost:8081
echo 健康检查: http://localhost:8081/actuator/health
echo ==========================================

pause
