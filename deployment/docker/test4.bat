@echo off
echo Test 1: Before chcp
chcp 65001 >nul
echo Test 2: After chcp
setlocal enabledelayedexpansion
echo Test 3: After setlocal

:print_test
echo(
echo Testing header
echo(
goto :eof

call :print_test
echo Test 4: After function
pause
