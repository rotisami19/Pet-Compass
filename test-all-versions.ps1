# PetCompass - Test all versions before release
# Runs a build on each branch (1.20.x NeoForge, 1.20.x Forge, 1.21.x NeoForge)
# Auto-stashes local changes for checkout, then restores at the end.

$ErrorActionPreference = "Stop"
$branches = @("1.20.x-neoforge", "1.20.x-forge", "1.21.x-neoforge")
$results = @{}
$startBranch = (git branch --show-current 2>$null)
$hadStash = $false

# Stash local changes (including untracked) to allow checkout
$status = git status --porcelain 2>$null
if ($status -and $status.Trim() -ne "") {
    Write-Host "Local changes detected -> auto stash (-u)..." -ForegroundColor Yellow
    $ErrorActionPreference = "SilentlyContinue"
    git stash push -u -m "test-all-versions-auto" 2>&1 | Out-Null
    $ErrorActionPreference = "Stop"
    if ($LASTEXITCODE -eq 0) { $hadStash = $true }
}

function Get-MinecraftVersion {
    param([string]$branch)
    try {
        $gp = git show "${branch}:gradle.properties" 2>$null
        if (-not $gp) { return "?" }
        $m = [regex]::Match($gp, "minecraft_version=([\d.]+)")
        if ($m.Success) { return $m.Groups[1].Value.Trim() }
    } catch {}
    return "?"
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  PetCompass - Multi-version test" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

foreach ($branch in $branches) {
    $mc = Get-MinecraftVersion $branch
    Write-Host "[$branch] MC $mc" -ForegroundColor Yellow
    try {
        $ErrorActionPreference = "SilentlyContinue"
        $checkout = git checkout $branch 2>&1 | Out-String
        $checkoutExit = $LASTEXITCODE
        $ErrorActionPreference = "Stop"
        if ($checkoutExit -ne 0) {
            $results[$branch] = "CHECKOUT_FAIL"
            Write-Host "  Cannot checkout branch: $($checkout.Trim())" -ForegroundColor Red
            Write-Host ""
            continue
        }
        $ErrorActionPreference = "SilentlyContinue"
        $build = & .\gradlew.bat build --no-daemon -q 2>&1
        $buildExit = $LASTEXITCODE
        $ErrorActionPreference = "Stop"
        if ($buildExit -eq 0) {
            $results[$branch] = "OK"
            Write-Host "  Build OK" -ForegroundColor Green
        } else {
            $results[$branch] = "FAIL"
            Write-Host "  Build FAIL" -ForegroundColor Red
            Write-Host $build
        }
        $ErrorActionPreference = "Stop"
    } catch {
        $results[$branch] = "ERROR"
        Write-Host "  Error: $_" -ForegroundColor Red
    }
    Write-Host ""
}

# Return to starting branch and restore stash
$ErrorActionPreference = "SilentlyContinue"
$current = git branch --show-current 2>$null
if ($current -and $current -ne $startBranch) { git checkout $startBranch 2>$null | Out-Null }
if ($hadStash) {
    Write-Host "Restoring stash..." -ForegroundColor Yellow
    git stash pop 2>&1 | Out-Null
    if ($LASTEXITCODE -ne 0) { Write-Host "  (Possible conflict: check with 'git status')" -ForegroundColor Gray }
}
$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
foreach ($b in $branches) {
    $r = $results[$b]
    $color = if ($r -eq "OK") { "Green" } else { "Red" }
    Write-Host "  $b : $r" -ForegroundColor $color
}
$failed = (@($results.Values) | Where-Object { $_ -ne "OK" }).Count
if ($failed -gt 0) {
    Write-Host ""
    Write-Host "Warning: $failed version(s) failed. Fix before publishing." -ForegroundColor Red
    exit 1
}
Write-Host ""
Write-Host "All builds succeeded. Run the client (runClient) and validate the manual checklist." -ForegroundColor Green
