@echo off
echo Compiling House Rental Web Application...
javac -cp ".;lib/mysql-connector-j.jar" *.java
echo Starting Web Server on http://localhost:8080 ...
start http://localhost:8080
java -cp ".;lib/mysql-connector-j.jar" AppServer
pause
