@echo off
setlocal enabledelayedexpansion

:print_header
echo.
echo ================================================================
echo   %~1
echo ================================================================
echo.
goto :eof

:show_menu
cls
echo.
echo ================================================================
echo   Live Streaming System - Docker Management Tool
echo ================================================================
echo.
goto :eof

call :show_menu
pause
