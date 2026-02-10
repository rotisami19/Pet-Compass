# Script to configure Modrinth credentials
# This sets environment variables for Modrinth API access

Write-Host "======================================" -ForegroundColor Cyan
Write-Host "Modrinth Credentials Configuration" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan

# Get API Token
Write-Host "`nGo to https://modrinth.com/settings/pats" -ForegroundColor Yellow
Write-Host "Create a new Personal Access Token and copy it below.`n" -ForegroundColor Yellow
$token = Read-Host "Enter your Modrinth API Token (or press Enter to skip)"

# Get Project ID
Write-Host "`nYour project ID is shown in your Modrinth project URL or settings:" -ForegroundColor Yellow
Write-Host "Example: https://modrinth.com/mod/pet-compass" -ForegroundColor Gray
Write-Host "The ID or slug is 'pet-compass'.`n" -ForegroundColor Yellow
$projectId = Read-Host "Enter your Modrinth Project ID (or press Enter to skip)"

Write-Host ""

# Validate inputs
if ([string]::IsNullOrWhiteSpace($token) -and [string]::IsNullOrWhiteSpace($projectId)) {
    Write-Host "No credentials provided. Exiting." -ForegroundColor Yellow
    exit 0
}

# Ask about scope
Write-Host "`nWhere would you like to set these variables?" -ForegroundColor Cyan
Write-Host "1. Current session only (temporary)" -ForegroundColor Gray
Write-Host "2. User profile (permanent, all future sessions)" -ForegroundColor Gray
Write-Host "3. System-wide (permanent, all users)" -ForegroundColor Gray
$scope = Read-Host "Enter your choice (1-3, default: 1)"

switch ($scope) {
    "2" {
        Write-Host "`nSetting environment variables for User profile..." -ForegroundColor Cyan
        if (-not [string]::IsNullOrWhiteSpace($token)) {
            [Environment]::SetEnvironmentVariable("MODRINTH_TOKEN", $token, "User")
            Write-Host "✓ MODRINTH_TOKEN set" -ForegroundColor Green
        }
        if (-not [string]::IsNullOrWhiteSpace($projectId)) {
            [Environment]::SetEnvironmentVariable("MODRINTH_PROJECT_ID", $projectId, "User")
            Write-Host "✓ MODRINTH_PROJECT_ID set" -ForegroundColor Green
        }
        Write-Host "`nNote: You may need to restart VS Code or PowerShell for changes to take effect." -ForegroundColor Yellow
    }
    
    "3" {
        Write-Host "`nSetting environment variables system-wide..." -ForegroundColor Cyan
        Write-Host "This requires Administrator privileges.`n" -ForegroundColor Yellow
        
        # Check if running as admin
        $isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
        
        if (-not $isAdmin) {
            Write-Host "ERROR: This operation requires Administrator privileges." -ForegroundColor Red
            Write-Host "Please run PowerShell as Administrator and try again." -ForegroundColor Red
            exit 1
        }
        
        if (-not [string]::IsNullOrWhiteSpace($token)) {
            [Environment]::SetEnvironmentVariable("MODRINTH_TOKEN", $token, "Machine")
            Write-Host "✓ MODRINTH_TOKEN set (system-wide)" -ForegroundColor Green
        }
        if (-not [string]::IsNullOrWhiteSpace($projectId)) {
            [Environment]::SetEnvironmentVariable("MODRINTH_PROJECT_ID", $projectId, "Machine")
            Write-Host "✓ MODRINTH_PROJECT_ID set (system-wide)" -ForegroundColor Green
        }
        Write-Host "`nNote: You may need to restart VS Code or PowerShell for changes to take effect." -ForegroundColor Yellow
    }
    
    default {
        Write-Host "`nSetting environment variables for current session..." -ForegroundColor Cyan
        if (-not [string]::IsNullOrWhiteSpace($token)) {
            $env:MODRINTH_TOKEN = $token
            Write-Host "✓ MODRINTH_TOKEN set (current session)" -ForegroundColor Green
        }
        if (-not [string]::IsNullOrWhiteSpace($projectId)) {
            $env:MODRINTH_PROJECT_ID = $projectId
            Write-Host "✓ MODRINTH_PROJECT_ID set (current session)" -ForegroundColor Green
        }
        Write-Host "`nThese variables will be lost when you close PowerShell." -ForegroundColor Yellow
    }
}

Write-Host "`nConfiguration complete!" -ForegroundColor Green
Write-Host "You can now run: .\publish-to-modrinth.ps1" -ForegroundColor Cyan
Write-Host "`nPress Enter to exit..." -ForegroundColor Gray
Read-Host
