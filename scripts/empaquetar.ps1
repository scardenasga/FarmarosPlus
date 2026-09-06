# empaquetar.ps1 - FarmarosPlus
# Script simple: crea un instalador .exe/.msi con todo incluido
# Uso: powershell -ExecutionPolicy Bypass -File scripts/empaquetar.ps1
# Requisitos: Node, Java 25 (ya los tienes)

param(
  [string]$Version = "1.0.0",
  [switch]$SoloJar  # si pones -SoloJar, no crea instalador, solo el JAR
)

$ErrorActionPreference = "Stop"

# --- Rutas ---
$ROOT = (Resolve-Path "$PSScriptRoot/..").Path
$FRONTEND = Join-Path $ROOT "frontend"
$BACKEND = Join-Path $ROOT "backend"
$JAVA_HOME = "C:\Users\cgseb\.jdks\ms-25.0.3"
$JPACKAGE = Join-Path $JAVA_HOME "bin\jpackage.exe"
$JLINK = Join-Path $JAVA_HOME "bin\jlink.exe"

Write-Host "=== FarmarosPlus - Empaquetar ===" -ForegroundColor Cyan
Write-Host "Version: $Version"

# 1. Compilar frontend
Write-Host "`n[1/4] Compilando frontend..." -ForegroundColor Yellow
Push-Location $FRONTEND
npm run build
if ($LASTEXITCODE -ne 0) { throw "Fallo ng build" }
Pop-Location

# Buscar donde quedo el build
$NG_DIST = Join-Path $FRONTEND "dist/frontend/browser"
if (-not (Test-Path $NG_DIST)) { $NG_DIST = Join-Path $FRONTEND "dist/frontend" }
Write-Host "  Listo: $NG_DIST" -ForegroundColor Green

# 2. Copiar frontend al backend
Write-Host "`n[2/4] Copiando frontend al backend..." -ForegroundColor Yellow
$STATIC = Join-Path $BACKEND "src/main/resources/static"
if (Test-Path $STATIC) { Remove-Item -Recurse -Force $STATIC }
New-Item -ItemType Directory -Path $STATIC -Force | Out-Null
Copy-Item -Path (Join-Path $NG_DIST "*") -Destination $STATIC -Recurse -Force
Write-Host "  Listo" -ForegroundColor Green

# 3. Compilar backend
Write-Host "`n[3/4] Compilando backend..." -ForegroundColor Yellow
Push-Location $BACKEND
& ./mvnw.cmd clean package -DskipTests
if ($LASTEXITCODE -ne 0) { throw "Fallo mvn package" }
Pop-Location
$JAR = Join-Path $BACKEND "target/backend-0.0.1-SNAPSHOT.jar"
Write-Host "  Listo: $JAR" -ForegroundColor Green

if ($SoloJar) {
  Write-Host "`nSolo JAR generado. Ubicacion: $JAR" -ForegroundColor Green
  exit 0
}

# 4. Crear instalador con jpackage
Write-Host "`n[4/4] Creando instalador (jpackage)..." -ForegroundColor Yellow

# Carpeta temporal para jpackage
$INPUT_DIR = Join-Path $ROOT "target-jpackage-input"
$OUTPUT_DIR = Join-Path $ROOT "target-jpackage-output"
$RUNTIME_DIR = Join-Path $ROOT "target-jpackage-runtime"
if (Test-Path $INPUT_DIR) { Remove-Item -Recurse -Force $INPUT_DIR }
if (Test-Path $OUTPUT_DIR) { Remove-Item -Recurse -Force $OUTPUT_DIR }
if (Test-Path $RUNTIME_DIR) { Remove-Item -Recurse -Force $RUNTIME_DIR }
New-Item -ItemType Directory -Path $INPUT_DIR -Force | Out-Null
New-Item -ItemType Directory -Path $OUTPUT_DIR -Force | Out-Null

# Copiar JAR a input
Copy-Item $JAR (Join-Path $INPUT_DIR "farmarosplus.jar") -Force

# Crear runtime minimo con jlink
# Nota: java.compiler es obligatorio para Spring Data AOT (javax.lang.model.SourceVersion)
Write-Host "  Creando JRE minimo..." -ForegroundColor DarkYellow
$MODULES = "java.base,java.compiler,java.desktop,java.instrument,java.logging,java.management,java.naming,java.net.http,java.prefs,java.security.jgss,java.sql,java.transaction.xa,java.xml,jdk.unsupported"
& $JLINK --output $RUNTIME_DIR --add-modules $MODULES --strip-debug --no-header-files --no-man-pages --compress=2
if ($LASTEXITCODE -ne 0) { throw "Fallo jlink" }

# Crear instalador (sin consola negra, con acceso directo en escritorio)
Write-Host "  Creando EXE/MSI..." -ForegroundColor DarkYellow
& $JPACKAGE `
  --type app-image `
  --input $INPUT_DIR `
  --name FarmarosPlus `
  --main-jar farmarosplus.jar `
  --main-class org.springframework.boot.loader.launch.JarLauncher `
  --runtime-image $RUNTIME_DIR `
  --dest $OUTPUT_DIR `
  --app-version $Version `
  --vendor "FarmarosPlus" `
  --copyright "FarmarosPlus 2026"

if ($LASTEXITCODE -ne 0) { throw "Fallo jpackage" }

# Tambien crear MSI si se puede
Write-Host "  Creando MSI..." -ForegroundColor DarkYellow
& $JPACKAGE `
  --type msi `
  --input $INPUT_DIR `
  --name FarmarosPlus `
  --main-jar farmarosplus.jar `
  --main-class org.springframework.boot.loader.launch.JarLauncher `
  --runtime-image $RUNTIME_DIR `
  --dest $OUTPUT_DIR `
  --app-version $Version `
  --vendor "FarmarosPlus" `
  --win-shortcut `
  --win-menu `
  --win-dir-chooser

if ($LASTEXITCODE -ne 0) {
  Write-Host "  MSI no se pudo crear (normal si falta WiX), pero el EXE si esta listo" -ForegroundColor DarkYellow
}

# Crear lanzador .bat alternativo (no lo bloquea Device Guard / SmartScreen)
$BAT = Join-Path $OUTPUT_DIR "FarmarosPlus\Iniciar FarmarosPlus.bat"
"@echo off`r`nstart http://localhost:8080`r`n`"%~dp0runtime\bin\javaw.exe`" -jar `"%~dp0app\farmarosplus.jar`"" | Set-Content -Path $BAT -Encoding ASCII
Write-Host "  Lanzador BAT creado: $BAT" -ForegroundColor Green

# Crear ZIP para enviar al cliente (todo incluido, no necesita instalar nada)
$ZIP = Join-Path $ROOT "FarmarosPlus-v$Version.zip"
if (Test-Path $ZIP) { Remove-Item $ZIP -Force }
Compress-Archive -Path (Join-Path $OUTPUT_DIR "FarmarosPlus\*") -DestinationPath $ZIP -Force
Write-Host "  ZIP para cliente: $ZIP" -ForegroundColor Green

Write-Host "`n=== LISTO ===" -ForegroundColor Green
Write-Host "Carpeta: $OUTPUT_DIR\FarmarosPlus"
Write-Host "ZIP: $ZIP"
Write-Host "`nPara el cliente: envia el ZIP. No necesita Node, Java ni SQLite (todo va dentro)."
Write-Host "El cliente descomprime y doble click en 'Iniciar FarmarosPlus.bat' (o FarmarosPlus.exe si no lo bloquea Defender)"
Write-Host "La base de datos se guarda en C:\Users\Usuario\.farmarosplus\farmarosplus.db (no se borra al actualizar)"
