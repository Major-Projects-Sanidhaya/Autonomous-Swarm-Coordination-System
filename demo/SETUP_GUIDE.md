# Complete Setup Guide - ASCS Demo

This guide covers **everything** you need to get the demo running, from fresh install to troubleshooting.

---

## Table of Contents

1. [System Requirements](#system-requirements)
2. [Installation Methods](#installation-methods)
3. [Running the Demo](#running-the-demo)
4. [Verification](#verification)
5. [Troubleshooting](#troubleshooting)
6. [Next Steps](#next-steps)

---

## System Requirements

### Minimum
- **OS:** macOS 10.13+, Windows 10+, or Linux
- **Java:** Version 11 or higher
- **RAM:** 2GB available
- **Display:** 1400x900 or higher

### Recommended
- **Java:** Version 17 or 21
- **RAM:** 4GB available
- **Maven:** 3.6+ (optional but makes life easier)

---

## Installation Methods

Choose one based on what you have installed:

### Method A: Maven Installed (Easiest) ⭐

**Step 1:** Verify Maven
```bash
mvn -version
```

If you see version 3.6+, you're good!

**Step 2:** Run the demo
```bash
cd demo
mvn clean javafx:run
```

**Done!** Skip to [Verification](#verification).

---

### Method B: No Maven, Want to Install It

**macOS (using Homebrew):**
```bash
brew install maven
```

**Windows (using Chocolatey):**
```powershell
choco install maven
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt install maven
```

**Manual Install (all platforms):**
1. Download from https://maven.apache.org/download.cgi
2. Extract to a folder (e.g., `C:\maven` or `/usr/local/maven`)
3. Add to PATH:
   - **Windows:** System Properties → Environment Variables → Path → Add `C:\maven\bin`
   - **macOS/Linux:** Add to `~/.bashrc` or `~/.zshrc`:
     ```bash
     export PATH="/usr/local/maven/bin:$PATH"
     ```
4. Restart terminal and verify: `mvn -version`

Then use Method A above.

---

### Method C: No Maven, Don't Want Maven

**Step 1:** Download JavaFX SDK

1. Go to https://gluonhq.com/products/javafx/
2. Download JavaFX SDK for your OS
3. Extract to a known location, e.g.:
   - **macOS:** `~/javafx-sdk-17`
   - **Windows:** `C:\javafx-sdk-17`
   - **Linux:** `~/javafx-sdk-17`

**Step 2:** Compile the demo

**On macOS/Linux:**
```bash
cd demo

# Using the compile script
./compile.sh ~/javafx-sdk-17/lib

# Or manually
javac --module-path ~/javafx-sdk-17/lib \
      --add-modules javafx.controls \
      -d out/production/demo \
      src/main/java/com/team6/swarm/demo/*.java
```

**On Windows:**
```powershell
cd demo

javac --module-path C:\javafx-sdk-17\lib ^
      --add-modules javafx.controls ^
      -d out\production\demo ^
      src\main\java\com\team6\swarm\demo\*.java
```

**Step 3:** Run the demo

**On macOS/Linux:**
```bash
# Using auto-generated script
./run-compiled.sh

# Or manually
java --module-path ~/javafx-sdk-17/lib \
     --add-modules javafx.controls \
     -cp out/production/demo \
     com.team6.swarm.demo.SwarmDemo
```

**On Windows:**
```powershell
java --module-path C:\javafx-sdk-17\lib ^
     --add-modules javafx.controls ^
     -cp out\production\demo ^
     com.team6.swarm.demo.SwarmDemo
```

---

## Running the Demo

Once installed, you have multiple options:

### Quick Run Scripts

**macOS/Linux:**
```bash
cd demo
./run.sh
```

**Windows:**
```powershell
cd demo
run.bat
```

### Maven Command
```bash
cd demo
mvn javafx:run
```

### Manual (after compilation)
```bash
cd demo
./run-compiled.sh  # macOS/Linux
```

---

## Verification

### Successful Launch

You should see:

1. **Console output:**
   ```
   ==========================================
     ASCS Demo - Swarm Visualization
   ==========================================
   ✓ Maven found: Apache Maven 3.x
   ✓ Java found: version 17
   🚀 Launching ASCS Demo...
   ```

2. **Demo window appears:**
   - Title: "Autonomous Swarm Coordination System - Interactive Demo"
   - Size: 1300x800 pixels
   - 12 cyan-colored agents moving on black canvas
   - Controls on the right side
   - Scenario buttons at the bottom

3. **Stats display:**
   - Agents: 12
   - FPS: 60 (or close to it)
   - Consensus: Idle
   - Network: Healthy (100%)

### Test the Demo

**Quick Test (30 seconds):**

1. Click **"A: Basic Flocking"** → Agents should move together smoothly
2. Click on the canvas → Agents should move toward the click point
3. Click **"Spawn Agent"** → Agent count increases to 13
4. Move **Separation slider** → Agent spacing should change

**Full Test (2 minutes):**

1. Click **"B: Consensus Vote"** → Agents turn orange, then green, result shown
2. Click **"C: Network Degradation"** → Network % drops and recovers
3. Click **"D: Formation Flying"** → Agents form line → V → circle → grid
4. Try all formation buttons → Agents move to formations

If all tests pass: **✅ Demo is working perfectly!**

---

## Troubleshooting

### Problem: "Command not found: mvn"

**Cause:** Maven not installed or not in PATH

**Solutions:**
1. Install Maven (see Method B above)
2. Use Method C (no Maven required)
3. Check PATH: `echo $PATH` (macOS/Linux) or `echo %PATH%` (Windows)

---

### Problem: "Module javafx.controls not found"

**Cause:** JavaFX not available to Java runtime

**Solutions:**

**If using Maven:**
```bash
mvn clean install  # Download dependencies
mvn javafx:run     # Run with proper module path
```

**If not using Maven:**
- Ensure you're using `--module-path` and `--add-modules` flags
- Verify JavaFX SDK path is correct
- Use the compile/run scripts provided

---

### Problem: "Error: JavaFX runtime components are missing"

**Cause:** Java runtime doesn't include JavaFX (Java 11+)

**Solution:** Use Maven method (auto-downloads JavaFX) or download JavaFX SDK manually

---

### Problem: Window doesn't appear / Black screen

**Possible causes and solutions:**

1. **Display issues:**
   - Check if window is behind other windows
   - Try different display if using multiple monitors
   - Check system display scaling settings

2. **Graphics drivers:**
   - Update graphics drivers
   - Try software rendering: Add `-Dprism.order=sw` flag
   ```bash
   java -Dprism.order=sw [rest of command]
   ```

3. **Java version:**
   - Ensure Java 11 or higher: `java -version`
   - Try a different Java version (17 or 21 recommended)

---

### Problem: Slow performance / Low FPS

**Solutions:**

1. **Reduce agent count:**
   - Click "Remove Agent" several times
   - Target 8-10 agents for older hardware

2. **Disable communication links:**
   - Uncheck "Show Communication Links"
   - Reduces rendering overhead

3. **Close other applications:**
   - Free up RAM and CPU
   - Especially close other Java applications

4. **Check Java version:**
   - Java 17+ has better performance than Java 11
   - Update if possible

5. **Hardware acceleration:**
   - Ensure graphics drivers are up to date
   - JavaFX uses GPU acceleration when available

---

### Problem: Compilation errors

**Common errors and fixes:**

1. **"error: package javafx.application does not exist"**
   - Missing `--module-path` or `--add-modules` flags
   - Verify JavaFX SDK path

2. **"error: cannot find symbol"**
   - Ensure you're in the `demo` directory
   - Check all source files are present: `ls src/main/java/com/team6/swarm/demo/`

3. **"error: invalid source release: 11"**
   - Java version too old
   - Update to Java 11 or higher

---

### Problem: "Address already in use" or similar

**Cause:** Another instance is running

**Solution:**
- Close any existing demo windows
- Kill Java processes: `killall java` (macOS/Linux) or Task Manager (Windows)

---

## Platform-Specific Notes

### macOS
- **Tested on:** macOS Big Sur, Monterey, Ventura
- **Java location:** Usually `/Library/Java/JavaVirtualMachines/`
- **Permission issue:** If `./run.sh` fails, run `chmod +x run.sh`

### Windows
- **Tested on:** Windows 10, Windows 11
- **Path separators:** Use `\` instead of `/`
- **PowerShell:** Recommended over Command Prompt
- **Execution policy:** If `.bat` fails, try running PowerShell as Administrator

### Linux
- **Tested on:** Ubuntu 20.04, 22.04
- **Dependencies:** May need `libgtk-3-0` for JavaFX
  ```bash
  sudo apt install libgtk-3-0
  ```
- **Wayland:** If issues, try X11 session

---

## Advanced Options

### Running with Custom JVM Arguments

**Increase memory:**
```bash
java -Xmx1G [rest of command]
```

**Enable detailed logging:**
```bash
java -Dprism.verbose=true [rest of command]
```

**Force software rendering:**
```bash
java -Dprism.order=sw [rest of command]
```

### Building a JAR

```bash
mvn clean package
java -jar target/ascs-demo-1.0-SNAPSHOT.jar
```

---

## Next Steps

Once the demo is running:

1. **Read the README.md** for full feature documentation
2. **Try all scenarios** (A, B, C, D)
3. **Experiment with controls** (sliders, formations)
4. **Explore the code** in `src/main/java/com/team6/swarm/demo/`
5. **Integrate into main project** (see DEMO_SUMMARY.md)

---

## Getting Help

**Still having issues?**

1. Check the [README.md](README.md) for detailed documentation
2. Review [QUICKSTART.md](QUICKSTART.md) for fast setup
3. Read [DEMO_SUMMARY.md](DEMO_SUMMARY.md) for feature overview
4. Open an issue on the main ASCS repository

---

## Quick Reference

### File Locations
```
demo/
├── pom.xml                  # Maven config
├── README.md                # Full documentation
├── QUICKSTART.md            # Fast setup
├── SETUP_GUIDE.md          # This file
├── DEMO_SUMMARY.md         # Feature checklist
├── run.sh                   # macOS/Linux launcher
├── run.bat                  # Windows launcher
├── compile.sh               # Manual compiler
└── src/main/java/com/team6/swarm/demo/
    ├── DemoAgent.java      # Agent implementation
    └── SwarmDemo.java      # Main application
```

### Common Commands

| Task | Command |
|------|---------|
| Run with Maven | `mvn clean javafx:run` |
| Run with script | `./run.sh` or `run.bat` |
| Compile manually | `./compile.sh /path/to/javafx/lib` |
| Run compiled | `./run-compiled.sh` |
| Clean build | `mvn clean` |
| Package JAR | `mvn clean package` |

---

**Good luck! You've got this! 🚀**
