@echo off
echo ========================================================
echo   Backing up House Rental Project...
echo ========================================================

set BACKUP_DIR=..\HouseRental_Backup_Temp
set ZIP_DEST=..\HouseRental_Backup.zip

if exist "%BACKUP_DIR%" rmdir /s /q "%BACKUP_DIR%"
mkdir "%BACKUP_DIR%"

xcopy /E /I /Y . "%BACKUP_DIR%" /EXCLUDE:exclude_backup.txt 2>nul
if errorlevel 1 (
    robocopy . "%BACKUP_DIR%" /E /XD .git lib /XF *.zip
)

powershell -Command "Compress-Archive -Path '%BACKUP_DIR%\*' -DestinationPath '%ZIP_DEST%' -Force"

if exist "%BACKUP_DIR%" rmdir /s /q "%BACKUP_DIR%"

echo.
echo [SUCCESS] Backup created at: %ZIP_DEST%
echo ========================================================
pause
