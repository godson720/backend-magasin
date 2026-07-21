# ============================================================
# Script de demarrage automatique - GestionMagasin
# Demarre MySQL80 puis le backend Node.js/Express
# Doit etre execute en tant qu'Administrateur
# ============================================================

# --- Verification des droits administrateur ---
$currentPrincipal = New-Object Security.Principal.WindowsPrincipal([Security.Principal.WindowsIdentity]::GetCurrent())
if (-not $currentPrincipal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)) {
    Write-Host "Ce script doit etre execute en tant qu'Administrateur." -ForegroundColor Red
    Write-Host "Relancement en mode administrateur..." -ForegroundColor Yellow
    Start-Process powershell.exe "-NoProfile -ExecutionPolicy Bypass -File `"$PSCommandPath`"" -Verb RunAs
    exit
}

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Demarrage GestionMagasin - $(Get-Date)" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan

# --- CONFIGURATION (a adapter si besoin) ---
$backendPath = "C:\Users\hp\backend-magasin"
$mysqlServiceName = "MySQL80"

# --- ETAPE 1 : Demarrer MySQL80 ---
Write-Host "`n[1/3] Verification du service $mysqlServiceName..." -ForegroundColor Yellow
$service = Get-Service -Name $mysqlServiceName -ErrorAction SilentlyContinue

if ($null -eq $service) {
    Write-Host "ERREUR : Le service $mysqlServiceName est introuvable." -ForegroundColor Red
    Read-Host "Appuyez sur Entree pour quitter"
    exit 1
}

if ($service.Status -eq 'Running') {
    Write-Host "MySQL80 est deja demarre." -ForegroundColor Green
} else {
    Write-Host "Demarrage de MySQL80..." -ForegroundColor Yellow
    try {
        Start-Service -Name $mysqlServiceName -ErrorAction Stop
        Start-Sleep -Seconds 3
        $service.Refresh()
        if ($service.Status -eq 'Running') {
            Write-Host "MySQL80 demarre avec succes." -ForegroundColor Green
        } else {
            throw "Le service n'a pas demarre correctement."
        }
    } catch {
        Write-Host "ERREUR lors du demarrage de MySQL80 : $_" -ForegroundColor Red
        Read-Host "Appuyez sur Entree pour quitter"
        exit 1
    }
}

# --- ETAPE 2 : Afficher l'adresse IP locale (pour RetrofitClient.kt) ---
Write-Host "`n[2/3] Adresse(s) IP locale(s) de ce PC :" -ForegroundColor Yellow
$ips = Get-NetIPAddress -AddressFamily IPv4 | Where-Object {
    $_.IPAddress -notlike "127.*" -and $_.IPAddress -notlike "169.254.*"
}
foreach ($ip in $ips) {
    Write-Host "   -> $($ip.IPAddress)  ($($ip.InterfaceAlias))" -ForegroundColor White
}
Write-Host "   Verifiez que RetrofitClient.kt utilise bien l'une de ces IP." -ForegroundColor DarkYellow

# --- ETAPE 3 : Demarrer le backend Node.js ---
Write-Host "`n[3/3] Demarrage du backend Node.js..." -ForegroundColor Yellow

if (-not (Test-Path $backendPath)) {
    Write-Host "ERREUR : Dossier backend introuvable : $backendPath" -ForegroundColor Red
    Read-Host "Appuyez sur Entree pour quitter"
    exit 1
}

# Lance le backend dans une nouvelle fenetre pour garder les logs visibles
Start-Process powershell.exe -ArgumentList "-NoExit", "-Command", "cd '$backendPath'; Write-Host 'Backend GestionMagasin en cours...' -ForegroundColor Green; npm start"

Write-Host "`n============================================" -ForegroundColor Cyan
Write-Host "  Tout est demarre !" -ForegroundColor Green
Write-Host "  - MySQL80 : actif" -ForegroundColor Green
Write-Host "  - Backend : lance dans une nouvelle fenetre" -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Cyan
Start-Sleep -Seconds 5
