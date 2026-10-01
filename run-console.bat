@echo off
cd /d "%~dp0"
echo ========================================================
echo   Starting House Rental Console Application...
echo ========================================================
javac -cp ".;lib/mysql-connector-j.jar" *.java
if errorlevel 1 (
    echo [ERROR] Compilation failed.
    pause
    exit /b 1
)
java -cp ".;lib/mysql-connector-j.jar" ConsoleApp
pause
