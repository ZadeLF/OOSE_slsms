@echo off
REM SLSMS - dev launcher (Windows)
REM Opens two separate terminal windows: backend on :8080, frontend on :5173.

set ROOT_DIR=%~dp0

start "SLSMS Backend (Spring Boot :8080)" cmd /k "cd /d %ROOT_DIR%backend && mvn spring-boot:run"
start "SLSMS Frontend (Vite :5173)" cmd /k "cd /d %ROOT_DIR%frontend && (if not exist node_modules npm install) && npm run dev"

echo.
echo Two terminals have been opened.
echo   - Backend  : http://localhost:8080
echo   - Frontend : http://localhost:5173
echo Close the terminals to stop the services.
