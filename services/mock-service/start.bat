@echo off
echo ========================================
echo Starting Mock Service...
echo ========================================

cd /d %~dp0

echo Building project...
call mvn clean package -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo Build failed!
    pause
    exit /b %ERRORLEVEL%
)

echo Starting application...
java -jar target\mock-service-1.0.0.jar

pause
