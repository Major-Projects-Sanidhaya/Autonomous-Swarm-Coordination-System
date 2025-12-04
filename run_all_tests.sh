#!/bin/bash

echo "==============================================="
echo "   Running All Integration Tests (Week 3-8)"
echo "==============================================="
echo

cd SwarmCoordination/src/main/java

echo "Compiling all core classes..."
javac com/team6/swarm/core/*.java 2>&1 | grep -E "error|warning" | head -20

echo
echo "==============================================="
echo "   Running Week 3 Integration Test"
echo "==============================================="
java com.team6.swarm.core.Week3IntegrationTest

echo
echo "==============================================="
echo "   Running Week 4 Integration Test"
echo "==============================================="
java com.team6.swarm.core.Week4IntegrationTest

echo
echo "==============================================="
echo "   Running Week 7 Integration Test"
echo "==============================================="
java com.team6.swarm.core.Week7IntegrationTest

echo
echo "==============================================="
echo "   Running Week 8 Integration Test"
echo "==============================================="
java com.team6.swarm.core.Week8IntegrationTest

echo
echo "==============================================="
echo "   All Tests Complete!"
echo "==============================================="
