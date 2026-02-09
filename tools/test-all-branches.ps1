# Script pour tester le serveur Minecraft sur toutes les branches
# Usage: Depuis la racine du projet, lancer: .\tools\test-all-branches.ps1

$ProjectRoot = Get-Location
$LogDir = Join-Path $ProjectRoot "server-smoketest-logs"

# Creer le dossier de logs s'il n'existe pas
if (-not (Test-Path $LogDir)) {
    New-Item -ItemType Directory -Path $LogDir | Out-Null
}

# Sauvegarder la branche courante
$OriginalBranch = & git rev-parse --abbrev-ref HEAD 2>$null
if (-not $OriginalBranch) { $OriginalBranch = "main" }
Write-Host "=== Branche originale: $OriginalBranch ===" -ForegroundColor Cyan

# Liste des branches a tester
$Branches = @(
    "1.20.x-forge",
    "1.20.x-neoforge", 
    "main"
)

# Resultats
$Results = @{}

function Test-ServerBranch {
    param(
        [string]$Branch
    )
    
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Yellow
    Write-Host "=== Test de la branche: $Branch ===" -ForegroundColor Yellow
    Write-Host "========================================" -ForegroundColor Yellow
    
    # Checkout de la branche
    Write-Host "-> Checkout $Branch..." -ForegroundColor Cyan
    
    # Fetch d'abord
    & git fetch origin $Branch 2>$null
    
    # Essayer checkout local, sinon depuis origin
    $checkoutResult = & git checkout $Branch 2>&1
    if ($LASTEXITCODE -ne 0) {
        # Essayer de creer depuis origin
        & git checkout -b $Branch origin/$Branch 2>&1
        if ($LASTEXITCODE -ne 0) {
            # Peut-etre que la branche locale existe mais pointe vers autre chose
            & git branch -D $Branch 2>$null
            & git checkout -b $Branch origin/$Branch 2>&1
        }
    }
    
    $currentBranch = & git rev-parse --abbrev-ref HEAD 2>$null
    if ($currentBranch -ne $Branch) {
        Write-Host "ERREUR: Impossible de checkout la branche $Branch (actuellement sur $currentBranch)" -ForegroundColor Red
        return $false
    }
    
    Write-Host "-> Checkout reussi! (sur $currentBranch)" -ForegroundColor Green
    
    # Clean et Build
    Write-Host "-> Build en cours... (cela peut prendre quelques minutes)" -ForegroundColor Cyan
    
    $gradlew = Join-Path $ProjectRoot "gradlew.bat"
    $BuildLogFile = Join-Path $LogDir "$Branch.build.log"
    
    & $gradlew clean build -x test 2>&1 | Tee-Object -FilePath $BuildLogFile
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "ERREUR: Build echoue pour $Branch" -ForegroundColor Red
        return $false
    }
    
    Write-Host "-> Build reussi!" -ForegroundColor Green
    
    # Lancer le serveur en mode test
    Write-Host "-> Demarrage du serveur (test smoke, max 120 secondes)..." -ForegroundColor Cyan
    
    $OutLog = Join-Path $LogDir "$Branch.out.log"
    $ErrLog = Join-Path $LogDir "$Branch.err.log"
    
    # Accepter l'EULA si necessaire
    $eulaPath = Join-Path $ProjectRoot "run\eula.txt"
    if (-not (Test-Path $eulaPath)) {
        $runDir = Join-Path $ProjectRoot "run"
        if (-not (Test-Path $runDir)) { New-Item -ItemType Directory -Path $runDir | Out-Null }
    }
    "eula=true" | Out-File -FilePath $eulaPath -Encoding ASCII -Force
    
    # Creer un fichier de sortie pour capturer le log
    "" | Out-File -FilePath $OutLog -Encoding UTF8 -Force
    
    # Lancer le serveur en background
    $pinfo = New-Object System.Diagnostics.ProcessStartInfo
    $pinfo.FileName = $gradlew
    $pinfo.Arguments = "runServer"
    $pinfo.WorkingDirectory = $ProjectRoot
    $pinfo.RedirectStandardOutput = $true
    $pinfo.RedirectStandardError = $true
    $pinfo.UseShellExecute = $false
    $pinfo.CreateNoWindow = $true
    
    $process = New-Object System.Diagnostics.Process
    $process.StartInfo = $pinfo
    
    # Event handlers pour capturer la sortie
    $outputBuilder = New-Object System.Text.StringBuilder
    $errorBuilder = New-Object System.Text.StringBuilder
    
    $process.Start() | Out-Null
    
    $Timeout = 120
    $StartTime = Get-Date
    $ServerStarted = $false
    $ServerCrashed = $false
    
    Write-Host "-> Attente du demarrage du serveur..." -ForegroundColor Gray
    
    while (((Get-Date) - $StartTime).TotalSeconds -lt $Timeout) {
        Start-Sleep -Milliseconds 2000
        
        # Lire la sortie disponible
        if (-not $process.HasExited) {
            # Lire ligne par ligne de maniere asynchrone
            while (-not $process.StandardOutput.EndOfStream) {
                $line = $process.StandardOutput.ReadLine()
                if ($line) {
                    $outputBuilder.AppendLine($line) | Out-Null
                    # Afficher la progression
                    if ($line -match "Loading|Preparing|Starting|Done") {
                        Write-Host "   $line" -ForegroundColor DarkGray
                    }
                    # Verifier si demarre
                    if ($line -match "Done \(" -or $line -match "For help, type") {
                        $ServerStarted = $true
                        break
                    }
                    # Verifier crash
                    if ($line -match "FAILED" -or $line -match "Exception" -or $line -match "Error loading") {
                        $ServerCrashed = $true
                    }
                }
            }
        }
        
        if ($ServerStarted) { break }
        if ($process.HasExited) { 
            $ServerCrashed = $true
            break 
        }
    }
    
    # Sauvegarder les logs
    $outputBuilder.ToString() | Out-File -FilePath $OutLog -Encoding UTF8 -Force
    
    # Arreter le serveur
    Write-Host "-> Arret du serveur..." -ForegroundColor Gray
    if (-not $process.HasExited) {
        try {
            $process.Kill()
            $process.WaitForExit(5000)
        }
        catch {}
    }
    
    # Tuer les processus Java restants
    Get-Process -Name "java" -ErrorAction SilentlyContinue | ForEach-Object {
        try { $_.Kill() } catch {}
    }
    
    Start-Sleep -Seconds 2
    
    if ($ServerStarted) {
        Write-Host "=== SUCCES: $Branch ===" -ForegroundColor Green
        return $true
    }
    else {
        $reason = if ($ServerCrashed) { "crash" } else { "timeout" }
        Write-Host "=== ECHEC: $Branch ($reason) ===" -ForegroundColor Red
        Write-Host "    Voir les logs: $OutLog" -ForegroundColor Gray
        return $false
    }
}

# Tester chaque branche
foreach ($Branch in $Branches) {
    try {
        $Success = Test-ServerBranch -Branch $Branch
        $Results[$Branch] = $Success
    }
    catch {
        Write-Host "ERREUR lors du test de $Branch : $_" -ForegroundColor Red
        $Results[$Branch] = $false
    }
}

# Revenir a la branche originale
Write-Host ""
Write-Host "-> Retour a la branche originale: $OriginalBranch" -ForegroundColor Cyan
& git checkout $OriginalBranch 2>&1 | Out-Null

# Afficher le resume
Write-Host ""
Write-Host "========================================" -ForegroundColor Magenta
Write-Host "=== RESUME DES TESTS ===" -ForegroundColor Magenta
Write-Host "========================================" -ForegroundColor Magenta

foreach ($Branch in $Branches) {
    $Status = if ($Results[$Branch]) { "SUCCES" } else { "ECHEC" }
    $Color = if ($Results[$Branch]) { "Green" } else { "Red" }
    Write-Host "$Branch : $Status" -ForegroundColor $Color
}

Write-Host ""
Write-Host "Les logs sont disponibles dans: $LogDir" -ForegroundColor Cyan

# Retourner le code de sortie
$FailedCount = ($Results.Values | Where-Object { -not $_ }).Count
if ($FailedCount -gt 0) {
    Write-Host ""
    Write-Host "ATTENTION: $FailedCount branche(s) ont echoue!" -ForegroundColor Red
    exit 1
}
Write-Host ""
Write-Host "Tous les tests ont reussi!" -ForegroundColor Green
exit 0
