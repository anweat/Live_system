@echo off
echo DEBUG: Script started
where docker >nul 2>&1
if errorlevel 1 (
    echo DEBUG: Docker not found
) else (
    echo DEBUG: Docker found
)
echo DEBUG: Script finished
