@echo off
cd /d "%~dp0"
echo ========================================================
echo   Compiling House Rental Web Application...
echo ========================================================
javac -cp ".;lib/mysql-connector-j.jar" *.java
if errorlevel 1 (
    echo [ERROR] Compilation failed.
    pause
    exit /b 1
)
echo Starting Web Server on http://localhost:8080 ...
start http://localhost:8080
java -cp ".;lib/mysql-connector-j.jar" AppServer
pause
