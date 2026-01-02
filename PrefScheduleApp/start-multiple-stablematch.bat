@echo off
echo Starting multiple StableMatch instances...

:: Instance 1 on port 8084
start cmd /k "cd stablematch && mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8084"

:: Wait 10 seconds
timeout /t 10

:: Instance 2 on port 8086
start cmd /k "cd stablematch && mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8086"

:: Wait 10 seconds
timeout /t 10

:: Instance 3 on port 8087
start cmd /k "cd stablematch && mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8087"

echo All StableMatch instances started!