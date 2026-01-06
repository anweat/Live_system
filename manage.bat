@echo off
REM ============================================================================
REM Live Streaming System - Docker Management Tool Launcher (Windows)
REM ============================================================================

setlocal

REM Get script directory
set "SCRIPT_DIR=%~dp0"

REM Management script path
set "MANAGE_SCRIPT=%SCRIPT_DIR%deployment\docker\manage.ps1"

REM Check if management script exists
if not exist "%MANAGE_SCRIPT%" (
    echo Error: Management script not found %MANAGE_SCRIPT%
    pause
    exit /b 1
)

REM Execute PowerShell management script
powershell -ExecutionPolicy Bypass -File "%MANAGE_SCRIPT%"

endlocal
