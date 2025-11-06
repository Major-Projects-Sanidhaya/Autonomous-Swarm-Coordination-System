#!/bin/bash

# ASCS Demo Launcher for macOS/Linux
# Quick start script for the swarm visualization demo

echo "=========================================="
echo "  ASCS Demo - Swarm Visualization"
echo "=========================================="
echo ""

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Error: Maven is not installed"
    echo "Please install Maven: https://maven.apache.org/download.cgi"
    exit 1
fi

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Error: Java is not installed"
    echo "Please install Java 11+: https://adoptium.net/"
    exit 1
fi

echo "✓ Maven found: $(mvn -version | head -n 1)"
echo "✓ Java found: $(java -version 2>&1 | head -n 1)"
echo ""

# Check if dependencies are downloaded
if [ ! -d "target" ]; then
    echo "📦 First run detected - downloading dependencies..."
    echo "   This may take a minute..."
    mvn clean compile
    echo ""
fi

# Run the demo
echo "🚀 Launching ASCS Demo..."
echo ""
mvn javafx:run

# If Maven run fails, try alternative
if [ $? -ne 0 ]; then
    echo ""
    echo "⚠️  Maven run failed. Trying alternative method..."
    mvn clean package
    java -jar target/ascs-demo-1.0-SNAPSHOT.jar
fi
