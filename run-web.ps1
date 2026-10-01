Set-Location -Path $PSScriptRoot
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "  Compiling House Rental Web Application..." -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan

javac -cp ".;lib/mysql-connector-j.jar" *.java
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Compilation failed." -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit
}

# Free port 8080 if an old instance is running
$proc = (Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue).OwningProcess
if ($proc) { Stop-Process -Id $proc -Force -ErrorAction SilentlyContinue }

Write-Host "Starting Web Server on http://localhost:8080 ..." -ForegroundColor Green
Start-Process "http://localhost:8080"
java -cp ".;lib/mysql-connector-j.jar" AppServer
