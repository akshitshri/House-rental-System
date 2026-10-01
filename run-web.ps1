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

Write-Host "Starting Web Server on http://localhost:8080 ..." -ForegroundColor Green
Start-Process "http://localhost:8080"
java -cp ".;lib/mysql-connector-j.jar" AppServer
