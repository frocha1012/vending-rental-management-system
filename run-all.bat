@echo off
setlocal

cd /d "%~dp0"

echo ============================================================
echo  Vending Rental System - Clean build + run
echo ============================================================

echo.
echo [1/3] Running clean build (skipping tests)...
call mvnw.cmd clean package -DskipTests
if errorlevel 1 (
    echo.
    echo Build FAILED. Aborting startup.
    pause
    exit /b 1
)

echo.
echo [2/3] Starting FRONTOFFICE (web app)...
start "Vending Rental - Frontoffice" cmd /k mvnw.cmd spring-boot:run

echo.
echo [3/3] Starting BACKOFFICE (JavaFX desktop)...
start "Vending Rental - Backoffice" cmd /k mvnw.cmd javafx:run

echo.
echo Both applications are starting in separate windows.
echo Close those windows to stop each application.

endlocal
