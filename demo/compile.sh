#!/bin/bash

# Simple compilation script without Maven
# For users who don't have Maven installed

echo "=========================================="
echo "  ASCS Demo - Simple Compiler"
echo "=========================================="
echo ""

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Error: Java is not installed"
    echo "Please install Java 11+: https://adoptium.net/"
    exit 1
fi

echo "✓ Java found: $(java -version 2>&1 | head -n 1)"
echo ""

# Detect JavaFX location
JAVAFX_PATH=""

# Common JavaFX locations on macOS
JAVAFX_LOCATIONS=(
    "/Library/Java/JavaVirtualMachines/*/Contents/Home/lib"
    "/usr/local/opt/openjfx/lib"
    "$HOME/javafx-sdk-*/lib"
)

echo "🔍 Looking for JavaFX installation..."
for location in "${JAVAFX_LOCATIONS[@]}"; do
    if [ -d "$location" ] && [ -f "$location/javafx.controls.jar" ]; then
        JAVAFX_PATH="$location"
        echo "✓ Found JavaFX at: $JAVAFX_PATH"
        break
    fi
done

if [ -z "$JAVAFX_PATH" ]; then
    echo ""
    echo "⚠️  JavaFX not found automatically."
    echo ""
    echo "Please download JavaFX SDK from:"
    echo "https://gluonhq.com/products/javafx/"
    echo ""
    echo "Then run this script with the path:"
    echo "./compile.sh /path/to/javafx-sdk/lib"
    echo ""
    exit 1
fi

# Allow manual path override
if [ ! -z "$1" ]; then
    JAVAFX_PATH="$1"
    echo "Using provided JavaFX path: $JAVAFX_PATH"
fi

# Create output directory
mkdir -p out/production/demo

echo ""
echo "📦 Compiling ASCS Demo..."

# Compile
javac --module-path "$JAVAFX_PATH" \
      --add-modules javafx.controls \
      -d out/production/demo \
      src/main/java/com/team6/swarm/demo/*.java

if [ $? -eq 0 ]; then
    echo "✅ Compilation successful!"
    echo ""
    echo "To run the demo:"
    echo "  java --module-path \"$JAVAFX_PATH\" \\"
    echo "       --add-modules javafx.controls \\"
    echo "       -cp out/production/demo \\"
    echo "       com.team6.swarm.demo.SwarmDemo"
    echo ""
    echo "Or simply run: ./run-compiled.sh"

    # Create run script
    cat > run-compiled.sh << EOF
#!/bin/bash
java --module-path "$JAVAFX_PATH" \\
     --add-modules javafx.controls \\
     -cp out/production/demo \\
     com.team6.swarm.demo.SwarmDemo
EOF
    chmod +x run-compiled.sh

else
    echo "❌ Compilation failed"
    exit 1
fi
