#!/bin/bash
# Finance-Service Repository 重构 - 验证脚本
# 用于验证重构是否完成和编译是否正确

echo "========================================="
echo "Finance-Service Repository 重构验证"
echo "========================================="
echo ""

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 初始化计数器
PASS=0
FAIL=0

# 函数：检查条件
check() {
    if eval "$1"; then
        echo -e "${GREEN}✓${NC} $2"
        ((PASS++))
    else
        echo -e "${RED}✗${NC} $2"
        ((FAIL++))
    fi
}

echo "1️⃣  检查本地Repository文件是否已删除..."
check "! find services/finance-service/src/main/java/com/liveroom/finance/repository -name '*.java' 2>/dev/null | grep -q ." "本地Repository文件已删除"

echo ""
echo "2️⃣  检查Service层导入..."
check "! grep -r 'import com.liveroom.finance.repository' services/finance-service/src/main/java/com/liveroom/finance/service/ 2>/dev/null" "Service层无本地repository导入"
check "grep -r 'import common.repository' services/finance-service/src/main/java/com/liveroom/finance/service/ | grep -q ." "Service层正确使用common.repository"

echo ""
echo "3️⃣  检查异常处理..."
check "grep -r 'import common.exception.BusinessException' services/finance-service/src/main/java/com/liveroom/finance/service/ | grep -q ." "使用BusinessException"
check "grep -r 'import common.exception.SystemException' services/finance-service/src/main/java/com/liveroom/finance/service/ | grep -q ." "使用SystemException"

echo ""
echo "4️⃣  检查日志管理..."
check "grep -r 'import common.logger.TraceLogger' services/finance-service/src/main/java/com/liveroom/finance/service/ | grep -q ." "使用TraceLogger"

echo ""
echo "5️⃣  检查错误码使用..."
check "grep -r 'ErrorConstants\.' services/finance-service/src/main/java/com/liveroom/finance/service/ | wc -l | grep -qE '^[0-9]+$'" "使用ErrorConstants"

echo ""
echo "6️⃣  检查启动类配置..."
check "grep -q 'common.repository' services/finance-service/src/main/java/com/liveroom/finance/FinanceServiceApplication.java" "启动类添加了common.repository扫描"

echo ""
echo "7️⃣  检查Common模块的Repository..."
check "[ -f services/common/src/main/java/common/repository/RechargeRecordRepository.java ]" "RechargeRecordRepository已创建"
check "grep -q 'findUnsettledRecordsByAnchor' services/common/src/main/java/common/repository/RechargeRecordRepository.java" "RechargeRecordRepository包含必要方法"

echo ""
echo "8️⃣  检查文档..."
check "[ -f services/finance-service/REFACTOR_COMPLETION_REPORT.md ]" "完成报告已生成"
check "[ -f services/finance-service/MIGRATION_CHECKLIST.md ]" "迁移清单已生成"
check "[ -f services/finance-service/QUICK_REFERENCE.md ]" "快速参考已生成"

echo ""
echo "========================================="
echo "编译验证"
echo "========================================="
echo ""
echo "开始编译finance-service模块..."
cd services/finance-service
mvn clean compile -DskipTests > /tmp/finance-compile.log 2>&1
COMPILE_RESULT=$?

if [ $COMPILE_RESULT -eq 0 ]; then
    echo -e "${GREEN}✓${NC} finance-service编译成功"
    ((PASS++))
else
    echo -e "${RED}✗${NC} finance-service编译失败"
    echo "错误日志："
    tail -50 /tmp/finance-compile.log
    ((FAIL++))
fi

echo ""
echo "========================================="
echo "验证结果汇总"
echo "========================================="
echo ""
echo -e "通过: ${GREEN}$PASS${NC}"
echo -e "失败: ${RED}$FAIL${NC}"
echo ""

if [ $FAIL -eq 0 ]; then
    echo -e "${GREEN}✅ 所有验证都通过了！重构完成！${NC}"
    exit 0
else
    echo -e "${RED}❌ 有$FAIL项验证未通过，请检查日志${NC}"
    exit 1
fi

