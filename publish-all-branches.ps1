# Script to publish all branches to CurseForge
# Usage: .\publish-all-branches.ps1
# Requires: CURSEFORGE_API_KEY and CURSEFORGE_PROJECT_ID environment variables

param(
    [switch]$DryRun = $false
)

# Colors for output
$colors = @{
    Success = 'Green'
    Error   = 'Red'
    Warning = 'Yellow'
    Info    = 'Cyan'
}

function Write-Status {
    param([string]$Message, [string]$Type = 'Info')
    $color = if ($colors.ContainsKey($Type)) { $colors[$Type] } else { 'White' }
    Write-Host "[$([datetime]::Now.ToString('HH:mm:ss'))] $Message" -ForegroundColor $color
}

function Write-Section {
    param([string]$Title)
    Write-Host ""
    Write-Host ("="*60) -ForegroundColor Cyan
    Write-Host $Title -ForegroundColor Cyan
    Write-Host ("="*60) -ForegroundColor Cyan
}

# Check for required environment variables
Write-Section "Checking Prerequisites"
if (-not $env:CURSEFORGE_API_KEY) {
    Write-Status "ERROR: CURSEFORGE_API_KEY environment variable not set" Error
    Write-Status "Get your API key from: https://authors.curseforge.com/account/api-tokens" Warning
    exit 1
}
if (-not $env:CURSEFORGE_PROJECT_ID) {
    Write-Status "ERROR: CURSEFORGE_PROJECT_ID environment variable not set" Error
    exit 1
}

Write-Status "[OK] API Key found" Success
Write-Status "[OK] Project ID found: $env:CURSEFORGE_PROJECT_ID" Success

# Get current branch
$currentBranch = git rev-parse --abbrev-ref HEAD 2>$null
Write-Status "Current branch: $currentBranch" Info

# Array of branches to publish
$branches = @(
    @{ name = 'main'; minecraftVersion = '1.21.1'; loader = 'NeoForge' },
    @{ name = '1.20.x-neoforge'; minecraftVersion = '1.20.1'; loader = 'NeoForge' },
    @{ name = '1.20.x-forge'; minecraftVersion = '1.20.1'; loader = 'Forge' }
)

$failedBranches = @()
$successBranches = @()

foreach ($branch in $branches) {
    Write-Section "Processing: $($branch.name) [MC $($branch.minecraftVersion), $($branch.loader)]"
    
    try {
        # Stash any uncommitted changes
        Write-Status "Stashing uncommitted changes..." Info
        git stash --include-untracked | Out-Null
        
        # Checkout branch
        Write-Status "Checking out branch: $($branch.name)" Info
        git checkout $branch.name 2>&1 | Out-Null
        if ($LASTEXITCODE -ne 0) {
            throw "Failed to checkout branch"
        }
        
        # Update from remote
        Write-Status "Pulling latest from remote..." Info
        git pull origin $branch.name 2>&1 | Out-Null
        if ($LASTEXITCODE -ne 0) {
            Write-Status "Warning: Could not pull from remote, continuing with local version" Warning
        }
        
        # Clean previous builds
        Write-Status "Running Gradle build..." Info
        if ($DryRun) {
            Write-Status "[DRY RUN] Would run: ./gradlew.bat build --no-daemon -q" Warning
        } else {
            $buildOutput = & .\gradlew.bat build --no-daemon -q 2>&1
            if ($LASTEXITCODE -ne 0) {
                throw "Build failed for $($branch.name)"
            }
            Write-Status "[OK] Build successful" Success
        }
        
        # Publish to CurseForge
        Write-Status "Publishing to CurseForge..." Info
        if ($DryRun) {
            Write-Status "[DRY RUN] Would run: ./gradlew.bat curseforge --no-daemon -q" Warning
        } else {
            $publishOutput = & .\gradlew.bat curseforge --no-daemon -q 2>&1
            if ($LASTEXITCODE -ne 0) {
                throw "CurseForge upload failed for $($branch.name)"
            }
            Write-Status "[OK] Published successfully" Success
        }
        
        $successBranches += $branch.name
        
    } catch {
        Write-Status "ERROR: $_" Error
        $failedBranches += $branch.name
    }
}

# Return to original branch
Write-Section "Cleanup"
Write-Status "Returning to original branch: $currentBranch" Info
git checkout $currentBranch 2>&1 | Out-Null

# Restore stashed changes
Write-Status "Restoring stashed changes..." Info
git stash pop | Out-Null

# Summary
Write-Section "Publication Summary"
Write-Status "Successfully published: $($successBranches.Count) branch(es)" Success
foreach ($branch in $successBranches) {
    Write-Host "  [OK] $branch" -ForegroundColor Green
}

if ($failedBranches.Count -gt 0) {
    Write-Status "Failed to publish: $($failedBranches.Count) branch(es)" Error
    foreach ($branch in $failedBranches) {
        Write-Host "  [FAIL] $branch" -ForegroundColor Red
    }
    exit 1
} else {
    Write-Status 'All branches published successfully!' Success
}
