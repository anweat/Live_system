# ============================================================================
# Live Streaming System - Docker Management Script (PowerShell)
# ============================================================================

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$ErrorActionPreference = "Continue"

$PROJECT_ROOT = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$SERVICES_DIR = Join-Path $PROJECT_ROOT "services"
$DOCKER_COMPOSE_FILE = Join-Path $PROJECT_ROOT "docker-compose.yml"

# Service Lists
$BUSINESS_SERVICES = @(
    "anchor-service", "audience-service", "redis-service",
    "finance-service", "mock-service", "back-end-service"
)
$INFRASTRUCTURE_SERVICES = @("mysql", "shared-redis", "nginx")
$STANDALONE_SERVICES = @("db-service")

# ============================================================================
# Utility Functions
# ============================================================================

function Print-Header {
    param([string]$Title)
    Write-Host ""
    Write-Host "================================================================" -ForegroundColor Cyan
    Write-Host "  $Title" -ForegroundColor Cyan
    Write-Host "================================================================" -ForegroundColor Cyan
    Write-Host ""
}

function Print-Success {
    param([string]$Message)
    Write-Host "[OK] $Message" -ForegroundColor Green
}

function Print-Error {
    param([string]$Message)
    Write-Host "[ERROR] $Message" -ForegroundColor Red
}

function Print-Warning {
    param([string]$Message)
    Write-Host "[WARNING] $Message" -ForegroundColor Yellow
}

function Print-Info {
    param([string]$Message)
    Write-Host "[INFO] $Message" -ForegroundColor Blue
}

function Check-Docker {
    if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
        Print-Error "Docker not installed. Please install Docker Desktop first"
        Read-Host "Press Enter to exit"
        exit 1
    }
    
    if (-not (Get-Command docker-compose -ErrorAction SilentlyContinue)) {
        $result = docker compose version 2>$null
        if ($LASTEXITCODE -ne 0) {
            Print-Error "Docker Compose not installed"
            Read-Host "Press Enter to exit"
            exit 1
        }
    }
}

function Check-Maven {
    if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
        Print-Warning "Maven not installed. Some features may be unavailable"
        return $false
    }
    return $true
}

function Invoke-DockerCompose {
    param([string[]]$Arguments)
    
    if (Get-Command docker-compose -ErrorAction SilentlyContinue) {
        & docker-compose @Arguments
    } else {
        & docker compose @Arguments
    }
}

# ============================================================================
# Build Management
# ============================================================================

function Clean-BuildCache {
    Print-Header "Clean Build Cache"
    
    Push-Location $PROJECT_ROOT
    
    if (Check-Maven) {
        Print-Info "Cleaning Maven build files..."
        mvn clean -f "$SERVICES_DIR\pom.xml" -q
        Print-Success "Maven build files cleaned"
    }
    
    Print-Info "Cleaning all target directories..."
    Get-ChildItem -Path $SERVICES_DIR -Recurse -Directory -Filter "target" | ForEach-Object {
        Remove-Item $_.FullName -Recurse -Force -ErrorAction SilentlyContinue
    }
    
    Print-Info "Cleaning Docker build cache..."
    docker builder prune -f | Out-Null
    
    Print-Success "All build files and cache cleaned"
    
    Pop-Location
    Write-Host ""
    Read-Host "Press Enter to continue"
}

function Compile-Project {
    Print-Header "Compile Project"
    
    if (-not (Check-Maven)) {
        Print-Error "Maven not installed. Cannot compile project"
        Read-Host "Press Enter to continue"
        return
    }
    
    Push-Location $SERVICES_DIR
    
    Print-Info "Compiling common module..."
    mvn clean install -pl common -am -DskipTests
    if ($LASTEXITCODE -ne 0) {
        Print-Error "Common module compilation failed"
        Pop-Location
        Read-Host "Press Enter to continue"
        return
    }
    Print-Success "Common module compiled successfully"
    
    Print-Info "Compiling all microservices..."
    mvn clean package -DskipTests
    if ($LASTEXITCODE -ne 0) {
        Print-Error "Project compilation failed"
        Pop-Location
        Read-Host "Press Enter to continue"
        return
    }
    
    Print-Success "Project compiled successfully"
    Write-Host ""
    Print-Info "Build artifacts location:"
    
    foreach ($service in $BUSINESS_SERVICES) {
        $jarFile = Join-Path $SERVICES_DIR "$service\target\$service-1.0.0.jar"
        if (Test-Path $jarFile) {
            Write-Host "  " -NoNewline
            Print-Success $service
        } else {
            Write-Host "  [MISSING] $service`: JAR file not found" -ForegroundColor Red
        }
    }
    
    Pop-Location
    Write-Host ""
    Read-Host "Press Enter to continue"
}

function Rebuild-Project {
    Print-Header "Rebuild Project"
    Clean-BuildCache
    Compile-Project
}

function Test-Compilation {
    Print-Info "Checking compilation status..."
    
    $allCompiled = $true
    $missingServices = @()
    
    foreach ($service in $BUSINESS_SERVICES) {
        $jarFile = Join-Path $SERVICES_DIR "$service\target\$service-1.0.0.jar"
        if (-not (Test-Path $jarFile)) {
            Print-Warning "$service not compiled (missing: $service-1.0.0.jar)"
            $allCompiled = $false
            $missingServices += $service
        }
    }
    
    if ($allCompiled) {
        Print-Success "All services compiled"
        return $true
    } else {
        Write-Host ""
        Write-Host "Missing JAR files for: $($missingServices -join ', ')" -ForegroundColor Yellow
        Write-Host "Please compile the project first (Option 1 or 3)" -ForegroundColor Yellow
    }
    return $false
}

# ============================================================================
# Docker Service Management
# ============================================================================

function Start-Infrastructure {
    Print-Header "Start Infrastructure Services"
    
    Push-Location $PROJECT_ROOT
    
    foreach ($service in $INFRASTRUCTURE_SERVICES) {
        Print-Info "Starting $service..."
        Invoke-DockerCompose -Arguments @("up", "-d", $service)
    }
    
    Write-Host ""
    Print-Info "Waiting for health checks..."
    Start-Sleep -Seconds 5
    
    Print-Success "Infrastructure services started"
    
    Pop-Location
    Write-Host ""
    Read-Host "Press Enter to continue"
}

function Start-BusinessServices {
    Print-Header "Start Business Services"
    
    if (-not (Test-Compilation)) {
        Write-Host ""
        $compile = Read-Host "Services not compiled. Compile now? [Y/n]"
        if ($compile -ne "n" -and $compile -ne "N") {
            Compile-Project
        } else {
            Print-Error "Cannot start uncompiled services"
            Read-Host "Press Enter to continue"
            return
        }
    }
    
    Push-Location $PROJECT_ROOT
    
    Print-Info "Checking infrastructure services..."
    foreach ($service in $INFRASTRUCTURE_SERVICES) {
        $running = docker ps --format "{{.Names}}" | Select-String -Pattern "^$service$" -Quiet
        if (-not $running) {
            Print-Warning "$service not running. Starting infrastructure..."
            Start-Infrastructure
            break
        }
    }
    
    Write-Host ""
    
    foreach ($service in $BUSINESS_SERVICES) {
        Print-Info "Starting $service..."
        Invoke-DockerCompose -Arguments @("up", "-d", $service)
    }
    
    Write-Host ""
    Print-Info "Waiting for health checks..."
    Start-Sleep -Seconds 10
    
    Print-Success "Business services started"
    
    Pop-Location
    Write-Host ""
    Read-Host "Press Enter to continue"
}

function Start-AllServices {
    Print-Header "Start All Services"
    
    if (-not (Test-Compilation)) {
        Write-Host ""
        $compile = Read-Host "Services not compiled. Compile now? [Y/n]"
        if ($compile -ne "n" -and $compile -ne "N") {
            Compile-Project
        } else {
            Print-Error "Cannot start uncompiled services"
            Read-Host "Press Enter to continue"
            return
        }
    }
    
    Push-Location $PROJECT_ROOT
    
    Print-Info "Starting all services..."
    Invoke-DockerCompose -Arguments @("up", "-d")
    
    Write-Host ""
    Print-Info "Waiting for health checks..."
    Start-Sleep -Seconds 10
    
    Print-Success "All services started"
    
    Pop-Location
    Write-Host ""
    Read-Host "Press Enter to continue"
}

function Stop-Services {
    Print-Header "Stop Services"
    
    Write-Host "Please select services to stop:"
    Write-Host "  1) Stop all services"
    Write-Host "  2) Stop business services (keep infrastructure)"
    Write-Host "  3) Stop infrastructure services"
    Write-Host "  4) Return to main menu"
    Write-Host ""
    
    $choice = Read-Host "Enter option [1-4]"
    
    Push-Location $PROJECT_ROOT
    
    switch ($choice) {
        "1" {
            Print-Info "Stopping all services..."
            Invoke-DockerCompose -Arguments @("down")
            Print-Success "All services stopped"
        }
        "2" {
            Print-Info "Stopping business services..."
            foreach ($service in $BUSINESS_SERVICES) {
                Invoke-DockerCompose -Arguments @("stop", $service)
            }
            Print-Success "Business services stopped"
        }
        "3" {
            Print-Info "Stopping infrastructure services..."
            foreach ($service in $INFRASTRUCTURE_SERVICES) {
                Invoke-DockerCompose -Arguments @("stop", $service)
            }
            Print-Success "Infrastructure services stopped"
        }
        "4" {
            Pop-Location
            return
        }
        default {
            Print-Error "Invalid option"
        }
    }
    
    Pop-Location
    Write-Host ""
    Read-Host "Press Enter to continue"
}

function Restart-Services {
    Print-Header "Restart Services"
    
    Write-Host "Please select services to restart:"
    Write-Host "  1) Restart all services"
    Write-Host "  2) Restart business services"
    Write-Host "  3) Restart infrastructure services"
    Write-Host "  4) Restart specific service"
    Write-Host "  5) Return to main menu"
    Write-Host ""
    
    $choice = Read-Host "Enter option [1-5]"
    
    Push-Location $PROJECT_ROOT
    
    switch ($choice) {
        "1" {
            Print-Info "Restarting all services..."
            Invoke-DockerCompose -Arguments @("restart")
            Print-Success "All services restarted"
        }
        "2" {
            Print-Info "Restarting business services..."
            foreach ($service in $BUSINESS_SERVICES) {
                Invoke-DockerCompose -Arguments @("restart", $service)
            }
            Print-Success "Business services restarted"
        }
        "3" {
            Print-Info "Restarting infrastructure services..."
            foreach ($service in $INFRASTRUCTURE_SERVICES) {
                Invoke-DockerCompose -Arguments @("restart", $service)
            }
            Print-Success "Infrastructure services restarted"
        }
        "4" {
            Write-Host ""
            Write-Host "Available services: $($BUSINESS_SERVICES + $INFRASTRUCTURE_SERVICES -join ', ')"
            $serviceName = Read-Host "Enter service name"
            $exists = docker ps -a --format "{{.Names}}" | Select-String -Pattern "^$serviceName$" -Quiet
            if ($exists) {
                Print-Info "Restarting $serviceName..."
                Invoke-DockerCompose -Arguments @("restart", $serviceName)
                Print-Success "$serviceName restarted"
            } else {
                Print-Error "Service not found: $serviceName"
            }
        }
        "5" {
            Pop-Location
            return
        }
        default {
            Print-Error "Invalid option"
        }
    }
    
    Pop-Location
    Write-Host ""
    Read-Host "Press Enter to continue"
}

function Rebuild-AndRestart {
    Print-Header "Rebuild and Restart"
    
    Write-Host "This operation will:"
    Write-Host "  1) Clean build cache"
    Write-Host "  2) Rebuild project"
    Write-Host "  3) Rebuild Docker images"
    Write-Host "  4) Restart services"
    Write-Host ""
    
    $confirm = Read-Host "Continue? [y/N]"
    if ($confirm -ne "y" -and $confirm -ne "Y") {
        Print-Info "Operation cancelled"
        Read-Host "Press Enter to continue"
        return
    }
    
    Rebuild-Project
    
    Write-Host ""
    Print-Header "Rebuild Docker Images"
    
    Push-Location $PROJECT_ROOT
    
    foreach ($service in $BUSINESS_SERVICES) {
        Print-Info "Building $service image..."
        Invoke-DockerCompose -Arguments @("build", "--no-cache", $service)
    }
    
    Write-Host ""
    Print-Info "Restarting services..."
    foreach ($service in $BUSINESS_SERVICES) {
        Invoke-DockerCompose -Arguments @("up", "-d", "--force-recreate", $service)
    }
    
    Write-Host ""
    Print-Info "Waiting for health checks..."
    Start-Sleep -Seconds 10
    
    Print-Success "Services rebuilt and restarted"
    
    Pop-Location
    Write-Host ""
    Read-Host "Press Enter to continue"
}

function Reset-Infrastructure {
    Print-Header "Reset Infrastructure"
    
    Write-Host "WARNING: This operation will DELETE ALL DATA!" -ForegroundColor Red
    Write-Host ""
    Write-Host "This operation will:"
    Write-Host "  1) Stop and remove all containers"
    Write-Host "  2) Delete all volumes (MySQL data, Redis data)"
    Write-Host "  3) Restart infrastructure"
    Write-Host ""
    
    $confirm = Read-Host "Continue? Type YES to confirm"
    if ($confirm -ne "YES") {
        Print-Info "Operation cancelled"
        Read-Host "Press Enter to continue"
        return
    }
    
    Push-Location $PROJECT_ROOT
    
    Print-Info "Stopping all services..."
    Invoke-DockerCompose -Arguments @("down", "-v")
    
    Write-Host ""
    Print-Info "Cleaning dangling images..."
    docker image prune -f | Out-Null
    
    Write-Host ""
    Print-Info "Restarting infrastructure..."
    Start-Infrastructure
    
    Print-Success "Infrastructure reset complete"
    
    Pop-Location
    Write-Host ""
    Read-Host "Press Enter to continue"
}

# ============================================================================
# Logging and Status
# ============================================================================

function Show-Logs {
    Print-Header "View Logs"
    
    Write-Host "Please select logs to view:"
    Write-Host "  1) All services logs"
    Write-Host "  2) Business services logs"
    Write-Host "  3) Infrastructure logs"
    Write-Host "  4) Specific service logs"
    Write-Host "  5) Return to main menu"
    Write-Host ""
    
    $choice = Read-Host "Enter option [1-5]"
    
    Push-Location $PROJECT_ROOT
    
    switch ($choice) {
        "1" {
            Print-Info "Viewing all services logs (Ctrl+C to exit)..."
            Invoke-DockerCompose -Arguments @("logs", "-f", "--tail=100")
        }
        "2" {
            Print-Info "Viewing business services logs (Ctrl+C to exit)..."
            Invoke-DockerCompose -Arguments (@("logs", "-f", "--tail=100") + $BUSINESS_SERVICES)
        }
        "3" {
            Print-Info "Viewing infrastructure logs (Ctrl+C to exit)..."
            Invoke-DockerCompose -Arguments (@("logs", "-f", "--tail=100") + $INFRASTRUCTURE_SERVICES)
        }
        "4" {
            Write-Host ""
            Write-Host "Available services: $($BUSINESS_SERVICES + $INFRASTRUCTURE_SERVICES -join ', ')"
            $serviceName = Read-Host "Enter service name"
            $exists = docker ps -a --format "{{.Names}}" | Select-String -Pattern "^$serviceName$" -Quiet
            if ($exists) {
                Print-Info "Viewing $serviceName logs (Ctrl+C to exit)..."
                Invoke-DockerCompose -Arguments @("logs", "-f", "--tail=100", $serviceName)
            } else {
                Print-Error "Service not found: $serviceName"
                Read-Host "Press Enter to continue"
            }
        }
        "5" {
            Pop-Location
            return
        }
        default {
            Print-Error "Invalid option"
            Read-Host "Press Enter to continue"
        }
    }
    
    Pop-Location
}

function Show-Status {
    Print-Header "System Status"
    
    Push-Location $PROJECT_ROOT
    
    Write-Host "Docker Compose Services:"
    Invoke-DockerCompose -Arguments @("ps")
    
    Write-Host ""
    Print-Info "Port Mappings:"
    Write-Host ""
    Write-Host "  Infrastructure:"
    Write-Host "    - MySQL:        3306"
    Write-Host "    - Shared Redis: 6379"
    Write-Host "    - Nginx:        80, 443"
    Write-Host ""
    Write-Host "  Business Services:"
    Write-Host "    - Anchor Service:   8081"
    Write-Host "    - Audience Service: 8082"
    Write-Host "    - Finance Service:  8083"
    Write-Host "    - Redis Service:    8085"
    Write-Host "    - Back-end Service: 8086 (API) / 8087 (Web)"
    Write-Host "    - Mock Service:     8090"
    Write-Host ""
    Write-Host "  Standalone Services:"
    Write-Host "    - DB Service:       8084 (uses separate docker-compose.yml)"
    
    Write-Host ""
    Print-Info "Resource Usage:"
    Write-Host ""
    docker stats --no-stream --format "table {{.Name}}`t{{.CPUPerc}}`t{{.MemUsage}}"
    
    Pop-Location
    Write-Host ""
    Read-Host "Press Enter to continue"
}

# ============================================================================
# Standalone Service Management
# ============================================================================

function Manage-StandaloneService {
    Print-Header "Manage Standalone Service (db-service)"
    
    $dbServiceDir = Join-Path $SERVICES_DIR "db-service"
    $dbComposeFile = Join-Path $dbServiceDir "docker-compose.yml"
    
    if (-not (Test-Path $dbComposeFile)) {
        Print-Error "db-service docker-compose.yml not found"
        Read-Host "Press Enter to continue"
        return
    }
    
    Write-Host "Please select operation:"
    Write-Host "  1) Start db-service"
    Write-Host "  2) Stop db-service"
    Write-Host "  3) Restart db-service"
    Write-Host "  4) View db-service logs"
    Write-Host "  5) Check db-service status"
    Write-Host "  6) Rebuild db-service"
    Write-Host "  7) Return to main menu"
    Write-Host ""
    
    $choice = Read-Host "Enter option [1-7]"
    
    Push-Location $dbServiceDir
    
    switch ($choice) {
        "1" {
            Print-Info "Starting db-service..."
            Invoke-DockerCompose -Arguments @("up", "-d")
            Print-Success "db-service started"
            Write-Host ""
            Read-Host "Press Enter to continue"
        }
        "2" {
            Print-Info "Stopping db-service..."
            Invoke-DockerCompose -Arguments @("down")
            Print-Success "db-service stopped"
            Write-Host ""
            Read-Host "Press Enter to continue"
        }
        "3" {
            Print-Info "Restarting db-service..."
            Invoke-DockerCompose -Arguments @("restart")
            Print-Success "db-service restarted"
            Write-Host ""
            Read-Host "Press Enter to continue"
        }
        "4" {
            Print-Info "Viewing db-service logs (Ctrl+C to exit)..."
            Invoke-DockerCompose -Arguments @("logs", "-f", "--tail=100")
        }
        "5" {
            Print-Info "db-service status:"
            Invoke-DockerCompose -Arguments @("ps")
            Write-Host ""
            Read-Host "Press Enter to continue"
        }
        "6" {
            Print-Info "Rebuilding db-service..."
            Invoke-DockerCompose -Arguments @("build", "--no-cache")
            Invoke-DockerCompose -Arguments @("up", "-d", "--force-recreate")
            Print-Success "db-service rebuilt"
            Write-Host ""
            Read-Host "Press Enter to continue"
        }
        "7" {
            Pop-Location
            return
        }
        default {
            Print-Error "Invalid option"
            Read-Host "Press Enter to continue"
        }
    }
    
    Pop-Location
}

# ============================================================================
# Main Menu
# ============================================================================

function Show-Menu {
    Clear-Host
    Write-Host ""
    Write-Host "================================================================" -ForegroundColor Cyan
    Write-Host "  Live Streaming System - Docker Management Tool" -ForegroundColor Cyan
    Write-Host "================================================================" -ForegroundColor Cyan
    Write-Host ""
    
    Write-Host "[Build Management]" -ForegroundColor Yellow
    Write-Host "  1) Compile project"
    Write-Host "  2) Clean build cache"
    Write-Host "  3) Rebuild project (clean + compile)"
    Write-Host ""
    Write-Host "[Service Deployment]" -ForegroundColor Yellow
    Write-Host "  4) Start all services (one-click)"
    Write-Host "  5) Start infrastructure (MySQL, Redis, Nginx)"
    Write-Host "  6) Start business services"
    Write-Host "  7) Stop services"
    Write-Host "  8) Restart services"
    Write-Host ""
    Write-Host "[Advanced Operations]" -ForegroundColor Yellow
    Write-Host "  9) Rebuild and restart services"
    Write-Host " 10) Reset infrastructure (DELETE ALL DATA)"
    Write-Host ""
    Write-Host "[Monitoring]" -ForegroundColor Yellow
    Write-Host " 11) View logs"
    Write-Host " 12) Check service status"
    Write-Host ""
    Write-Host "[Standalone Services]" -ForegroundColor Yellow
    Write-Host " 13) Manage standalone service (db-service)"
    Write-Host ""
    Write-Host "[Other]" -ForegroundColor Yellow
    Write-Host "  0) Exit"
    Write-Host ""
    Write-Host "================================================================" -ForegroundColor Cyan
}

function Main {
    Check-Docker
    
    while ($true) {
        Show-Menu
        $choice = Read-Host "Enter option [0-13]"
        Write-Host ""
        
        switch ($choice) {
            "1"  { Compile-Project }
            "2"  { Clean-BuildCache }
            "3"  { Rebuild-Project }
            "4"  { Start-AllServices }
            "5"  { Start-Infrastructure }
            "6"  { Start-BusinessServices }
            "7"  { Stop-Services }
            "8"  { Restart-Services }
            "9"  { Rebuild-AndRestart }
            "10" { Reset-Infrastructure }
            "11" { Show-Logs }
            "12" { Show-Status }
            "13" { Manage-StandaloneService }
            "0"  {
                Print-Info "Exiting management tool"
                exit 0
            }
            default {
                Print-Error "Invalid option. Please try again"
                Start-Sleep -Seconds 2
            }
        }
    }
}

# ============================================================================
# Script Entry Point
# ============================================================================

Main
