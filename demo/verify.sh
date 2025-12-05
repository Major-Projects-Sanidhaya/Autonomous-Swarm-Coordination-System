#!/bin/bash

# ASCS Demo Verification Script
# Verifies that the demo is properly set up and ready to run

echo "=========================================="
echo "  ASCS Demo - Verification Script"
echo "=========================================="
echo ""

# Color codes
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

ERRORS=0

# Check Java
echo -n "Checking Java installation... "
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | awk -F '"' '{print $2}')
    echo -e "${GREEN}✓${NC} Found Java $JAVA_VERSION"
else
    echo -e "${RED}✗${NC} Java not found"
    ((ERRORS++))
fi

# Check Maven
echo -n "Checking Maven installation... "
if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn -version 2>&1 | head -n 1 | awk '{print $3}')
    echo -e "${GREEN}✓${NC} Found Maven $MVN_VERSION"
else
    echo -e "${YELLOW}⚠${NC} Maven not found (optional)"
fi

# Check source files
echo ""
echo "Checking source files..."

echo -n "  - DemoAgent.java... "
if [ -f "src/main/java/com/team6/swarm/demo/DemoAgent.java" ]; then
    LINES=$(wc -l < "src/main/java/com/team6/swarm/demo/DemoAgent.java" | tr -d ' ')
    echo -e "${GREEN}✓${NC} ($LINES lines)"
else
    echo -e "${RED}✗${NC} Missing"
    ((ERRORS++))
fi

echo -n "  - SwarmDemo.java... "
if [ -f "src/main/java/com/team6/swarm/demo/SwarmDemo.java" ]; then
    LINES=$(wc -l < "src/main/java/com/team6/swarm/demo/SwarmDemo.java" | tr -d ' ')
    echo -e "${GREEN}✓${NC} ($LINES lines)"
else
    echo -e "${RED}✗${NC} Missing"
    ((ERRORS++))
fi

echo -n "  - pom.xml... "
if [ -f "pom.xml" ]; then
    echo -e "${GREEN}✓${NC}"
else
    echo -e "${RED}✗${NC} Missing"
    ((ERRORS++))
fi

# Check documentation
echo ""
echo "Checking documentation..."

DOCS=("README.md" "DEMO_SUMMARY.md" "ARCHITECTURE.md" "QUICKSTART.md" "INTEGRATION_COMPLETE.md")
for doc in "${DOCS[@]}"; do
    echo -n "  - $doc... "
    if [ -f "$doc" ]; then
        echo -e "${GREEN}✓${NC}"
    else
        echo -e "${YELLOW}⚠${NC} Missing (optional)"
    fi
done

# Check scripts
echo ""
echo "Checking run scripts..."

echo -n "  - run.sh... "
if [ -f "run.sh" ] && [ -x "run.sh" ]; then
    echo -e "${GREEN}✓${NC} (executable)"
elif [ -f "run.sh" ]; then
    echo -e "${YELLOW}⚠${NC} (not executable, fixing...)"
    chmod +x run.sh
    echo -e "    ${GREEN}✓${NC} Fixed"
else
    echo -e "${RED}✗${NC} Missing"
    ((ERRORS++))
fi

echo -n "  - compile.sh... "
if [ -f "compile.sh" ] && [ -x "compile.sh" ]; then
    echo -e "${GREEN}✓${NC} (executable)"
elif [ -f "compile.sh" ]; then
    echo -e "${YELLOW}⚠${NC} (not executable, fixing...)"
    chmod +x compile.sh
    echo -e "    ${GREEN}✓${NC} Fixed"
else
    echo -e "${YELLOW}⚠${NC} Missing (optional)"
fi

# Check build artifacts
echo ""
echo "Checking build artifacts..."

echo -n "  - Compiled classes... "
if [ -d "target/classes" ]; then
    CLASS_COUNT=$(find target/classes -name "*.class" 2>/dev/null | wc -l | tr -d ' ')
    if [ "$CLASS_COUNT" -gt 0 ]; then
        echo -e "${GREEN}✓${NC} ($CLASS_COUNT classes)"
    else
        echo -e "${YELLOW}⚠${NC} No classes found, needs compilation"
    fi
else
    echo -e "${YELLOW}⚠${NC} Not compiled yet"
fi

echo -n "  - JAR file... "
if [ -f "target/ascs-demo-1.0-SNAPSHOT.jar" ]; then
    JAR_SIZE=$(ls -lh target/ascs-demo-1.0-SNAPSHOT.jar | awk '{print $5}')
    echo -e "${GREEN}✓${NC} ($JAR_SIZE)"
else
    echo -e "${YELLOW}⚠${NC} Not built yet"
fi

# Test compilation (if Maven is available)
if command -v mvn &> /dev/null; then
    echo ""
    echo "Testing compilation..."
    echo -n "  - Running mvn compile... "

    if mvn compile -q &> /dev/null; then
        echo -e "${GREEN}✓${NC} Compilation successful"
    else
        echo -e "${RED}✗${NC} Compilation failed"
        ((ERRORS++))
        echo ""
        echo "  Running verbose compilation to show errors:"
        mvn compile
    fi
fi

# Summary
echo ""
echo "=========================================="
if [ $ERRORS -eq 0 ]; then
    echo -e "${GREEN}✓ VERIFICATION PASSED${NC}"
    echo ""
    echo "The demo is ready to run!"
    echo ""
    echo "To run the demo, use one of these methods:"
    echo ""
    echo "  Method 1 (Maven):"
    echo "    mvn clean javafx:run"
    echo ""
    echo "  Method 2 (Quick script):"
    echo "    ./run.sh"
    echo ""
else
    echo -e "${RED}✗ VERIFICATION FAILED${NC}"
    echo ""
    echo "$ERRORS error(s) found. Please fix the issues above."
fi
echo "=========================================="
