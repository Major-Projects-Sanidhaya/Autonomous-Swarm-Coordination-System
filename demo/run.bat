@echo off
REM ASCS Demo Launcher for Windows
REM Quick start script for the swarm visualization demo

echo ==========================================
echo   ASCS Demo - Swarm Visualization
echo ==========================================
echo.

REM Check if Maven is installed
where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo Error: Maven is not installed
    echo Please install Maven: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)

REM Check if Java is installed
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo Error: Java is not installed
    echo Please install Java 11+: https://adoptium.net/
    pause
    exit /b 1
)

echo Maven found
mvn -version | findstr "Maven"

echo Java found
java -version 2>&1 | findstr "version"
echo.

REM Check if dependencies are downloaded
if not exist "target" (
    echo First run detected - downloading dependencies...
    echo This may take a minute...
    call mvn clean compile
    echo.
)

REM Run the demo
echo Launching ASCS Demo...
echo.
call mvn javafx:run

REM If Maven run fails, try alternative
if %errorlevel% neq 0 (
    echo.
    echo Maven run failed. Trying alternative method...
    call mvn clean package
    java -jar target\ascs-demo-1.0-SNAPSHOT.jar
)

pause
