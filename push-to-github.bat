@echo off
cd /d "%~dp0"
set "PATH=C:\Program Files\Git\cmd;%PATH%"
echo ========================================================
echo   Pushing House Rental Project to GitHub...
echo ========================================================

git add .
git commit -m "Update House Rental project"
git branch -M main
git push -u origin main

echo.
echo ========================================================
echo   Push complete! Check: https://github.com/akshitshri/House-rental-System
echo ========================================================
pause
