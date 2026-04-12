@echo off
set BASE=C:\suresh\banking_project

echo ============================================
echo   Banking Microservices - Starting Services
echo ============================================
echo.

echo [1/8] Starting Eureka Server on port 8761...
start "Eureka Server" cmd /k "cd /d %BASE%\eureka-server && mvn spring-boot:run"
echo      Waiting 30s for Eureka to be ready...
timeout /t 30 /nobreak > nul

echo [2/8] Starting Config Server on port 8888...
start "Config Server" cmd /k "cd /d %BASE%\config-server && mvn spring-boot:run"
echo      Waiting 20s for Config Server to be ready...
timeout /t 20 /nobreak > nul

echo [3/8] Starting Auth Service on port 8081...
start "Auth Service" cmd /k "cd /d %BASE%\auth-service && mvn spring-boot:run"
echo      Waiting 20s...
timeout /t 20 /nobreak > nul

echo [4/8] Starting Registration Service on port 8082...
start "Registration Service" cmd /k "cd /d %BASE%\registration-service && mvn spring-boot:run"
echo      Waiting 15s...
timeout /t 15 /nobreak > nul

echo [5/8] Starting Login Service on port 8083...
start "Login Service" cmd /k "cd /d %BASE%\login-service && mvn spring-boot:run"
echo      Waiting 15s...
timeout /t 15 /nobreak > nul

echo [6/8] Starting Account Service on port 8084...
start "Account Service" cmd /k "cd /d %BASE%\account-service && mvn spring-boot:run"
echo      Waiting 15s...
timeout /t 15 /nobreak > nul

echo [7/8] Starting Transaction Service on port 8085...
start "Transaction Service" cmd /k "cd /d %BASE%\transaction-service && mvn spring-boot:run"
echo      Waiting 15s...
timeout /t 15 /nobreak > nul

echo [8/8] Starting API Gateway on port 8090...
start "API Gateway" cmd /k "cd /d %BASE%\api-gateway && mvn spring-boot:run"
echo      Waiting 20s...
timeout /t 20 /nobreak > nul

echo.
echo ============================================
echo   All services launched!
echo ============================================
echo.
echo   Eureka Dashboard : http://localhost:8761
echo   API Gateway      : http://localhost:8090
echo   Swagger UI       : http://localhost:8090/swagger-ui.html
echo.
echo   Start frontend:
echo   cd %BASE%\banking-frontend
echo   npm install
echo   npm start
echo.
echo   Check each service window for startup errors.
echo ============================================
pause
