#!/usr/bin/env pwsh
# E-Shopping Cart Application Launcher
# This script compiles and runs the application

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " E-Shopping Cart - Application Launcher" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check if Java is installed
try {
    java -version 2>&1 | Out-Null
} catch {
    Write-Host "Error: Java is not installed or not in PATH" -ForegroundColor Red
    Write-Host "Please install Java and add it to your PATH environment variable"
    Read-Host "Press Enter to exit"
    exit 1
}

# Navigate to project directory
$projectDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectDir

Write-Host "Compiling source files..." -ForegroundColor Yellow
javac -d build/classes -sourcepath src -cp "lib/*" (Get-ChildItem -Path src -Filter *.java -Recurse).FullName 2>$null

if ($LASTEXITCODE -eq 1) {
    Write-Host "Warning: Some files may not have compiled, but attempting to run anyway..." -ForegroundColor Yellow
    Write-Host ""
}

Write-Host ""
Write-Host "Starting E-Shopping Cart application..." -ForegroundColor Green
Write-Host "Please login to continue." -ForegroundColor Green
Write-Host ""

java -cp "build/classes;lib/*" com.eshop.ui.LoginFrame
Read-Host "Press Enter to exit"
