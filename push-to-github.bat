@echo off
echo ========================================================
echo   Pushing House Rental Project to GitHub...
echo ========================================================

git add .
git commit -m "Update House Rental project"
git push -u origin main

echo.
echo [DONE] Code pushed to GitHub!
echo ========================================================
pause
