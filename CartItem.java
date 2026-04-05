@echo off
REM E-Shopping Cart Application Launcher
REM This script compiles and runs the application

setlocal enabledelayedexpansion

echo ========================================
echo  E-Shopping Cart - Application Launcher
echo ========================================
echo.

REM Check if Java is installed
java -version >nul 2>&1
if errorlevel 1 (
    echo Error: Java is not installed or not in PATH
    echo Please install Java and add it to your PATH environment variable
    pause
    exit /b 1
)

REM Navigate to project directory
cd /d "%~dp0"

echo Compiling source files...
javac -d build\classes -sourcepath src -cp "lib\*" src\com\eshop\**\*.java 2>nul

if errorlevel 1 (
    echo.
    echo Warning: Some files may not have compiled, but attempting to run anyway...
    echo.
)

echo.
echo Starting E-Shopping Cart application...
echo Please login to continue.
echo.
java -cp "build\classes;lib\*" com.eshop.ui.LoginFrame

pause
