@echo off
echo Starting House Rental Console Application...
javac -cp ".;lib/mysql-connector-j.jar" *.java
java -cp ".;lib/mysql-connector-j.jar" ConsoleApp
pause
