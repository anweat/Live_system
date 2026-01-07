@echo off
REM Finance-Service Repository 重构 - 验证脚本 (Windows)
REM 用于验证重构是否完成和编译是否正确

setlocal enabledelayedexpansion

echo =========================================
echo Finance-Service Repository 重构验证
echo =========================================
echo.

REM 初始化计数器
set PASS=0
set FAIL=0

REM 函数：检查条件并输出结果
REM 用法: call :check "条件命令" "描述文字"

echo 1^) 检查本地Repository文件是否已删除...
REM 检查是否有.java文件在repository目录中
if not exist "services\finance-service\src\main\java\com\liveroom\finance\repository" (
    echo [OK] 本地Repository目录已删除
    set /a PASS+=1
) else (
    dir /s /b "services\finance-service\src\main\java\com\liveroom\finance\repository\*.java" >nul 2>&1
    if errorlevel 1 (
        echo [OK] 本地Repository文件已删除
        set /a PASS+=1
    ) else (
        echo [FAIL] 本地Repository文件仍存在
        set /a FAIL+=1
    )
)

echo.
echo 2^) 检查Service层导入...
findstr /r /m "import com\.liveroom\.finance\.repository" "services\finance-service\src\main\java\com\liveroom\finance\service\*.java" >nul 2>&1
if errorlevel 1 (
    echo [OK] Service层无本地repository导入
    set /a PASS+=1
) else (
    echo [FAIL] Service层仍有本地repository导入
    set /a FAIL+=1
)

findstr /r /m "import common\.repository" "services\finance-service\src\main\java\com\liveroom\finance\service\*.java" >nul 2>&1
if not errorlevel 1 (
    echo [OK] Service层正确使用common.repository
    set /a PASS+=1
) else (
    echo [FAIL] Service层缺少common.repository导入
    set /a FAIL+=1
)

echo.
echo 3^) 检查异常处理...
findstr /r /m "import common\.exception\.BusinessException" "services\finance-service\src\main\java\com\liveroom\finance\service\*.java" >nul 2>&1
if not errorlevel 1 (
    echo [OK] 使用BusinessException
    set /a PASS+=1
) else (
    echo [WARN] 未找到BusinessException导入
)

findstr /r /m "import common\.exception\.SystemException" "services\finance-service\src\main\java\com\liveroom\finance\service\*.java" >nul 2>&1
if not errorlevel 1 (
    echo [OK] 使用SystemException
    set /a PASS+=1
) else (
    echo [WARN] 未找到SystemException导入
)

echo.
echo 4^) 检查日志管理...
findstr /r /m "import common\.logger\.TraceLogger" "services\finance-service\src\main\java\com\liveroom\finance\service\*.java" >nul 2>&1
if not errorlevel 1 (
    echo [OK] 使用TraceLogger
    set /a PASS+=1
) else (
    echo [WARN] 未找到TraceLogger导入
)

echo.
echo 5^) 检查启动类配置...
findstr "common.repository" "services\finance-service\src\main\java\com\liveroom\finance\FinanceServiceApplication.java" >nul 2>&1
if not errorlevel 1 (
    echo [OK] 启动类添加了common.repository扫描
    set /a PASS+=1
) else (
    echo [FAIL] 启动类缺少common.repository扫描
    set /a FAIL+=1
)

echo.
echo 6^) 检查Common模块的Repository...
if exist "services\common\src\main\java\common\repository\RechargeRecordRepository.java" (
    echo [OK] RechargeRecordRepository已创建
    set /a PASS+=1
) else (
    echo [FAIL] RechargeRecordRepository未创建
    set /a FAIL+=1
)

findstr "findUnsettledRecordsByAnchor" "services\common\src\main\java\common\repository\RechargeRecordRepository.java" >nul 2>&1
if not errorlevel 1 (
    echo [OK] RechargeRecordRepository包含必要方法
    set /a PASS+=1
) else (
    echo [FAIL] RechargeRecordRepository缺少必要方法
    set /a FAIL+=1
)

echo.
echo 7^) 检查文档...
if exist "services\finance-service\REFACTOR_COMPLETION_REPORT.md" (
    echo [OK] 完成报告已生成
    set /a PASS+=1
) else (
    echo [WARN] 完成报告未生成
)

if exist "services\finance-service\MIGRATION_CHECKLIST.md" (
    echo [OK] 迁移清单已生成
    set /a PASS+=1
) else (
    echo [WARN] 迁移清单未生成
)

if exist "services\finance-service\QUICK_REFERENCE.md" (
    echo [OK] 快速参考已生成
    set /a PASS+=1
) else (
    echo [WARN] 快速参考未生成
)

echo.
echo =========================================
echo 编译验证
echo =========================================
echo.
echo 开始编译finance-service模块...
cd services\finance-service
call mvn clean compile -DskipTests >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] finance-service编译成功
    set /a PASS+=1
) else (
    echo [FAIL] finance-service编译失败
    call mvn clean compile -DskipTests
    set /a FAIL+=1
)
cd ..\..

echo.
echo =========================================
echo 验证结果汇总
echo =========================================
echo.
echo 通过: %PASS%
echo 失败: %FAIL%
echo.

if %FAIL% equ 0 (
    echo 【成功】所有验证都通过了！重构完成！
    exit /b 0
) else (
    echo 【失败】有%FAIL%项验证未通过，请检查日志
    exit /b 1
)

