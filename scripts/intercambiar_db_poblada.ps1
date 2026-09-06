# intercambiar_db_poblada.ps1 - intercambia DB temporal poblada sin recompilar
# Uso:
#   powershell -ExecutionPolicy Bypass -File scripts/intercambiar_db_poblada.ps1          # modo DEV
#   powershell -ExecutionPolicy Bypass -File scripts/intercambiar_db_poblada.ps1 -Prod     # modo PROD (copia a %USERPROFILE%\.farmarosplus)
#   powershell -ExecutionPolicy Bypass -File scripts/intercambiar_db_poblada.ps1 -Zip      # parchea el ZIP ya generado

param(
  [switch]$Prod,
  [switch]$Zip,
  [string]$Version = "1.0.0"
)

$ErrorActionPreference = "Stop"
$ROOT = (Resolve-Path "$PSScriptRoot/..").Path
$SRC_DB = Join-Path $ROOT "database\farmarosplus_poblada.db"
$SRC_UPLOADS = Join-Path $ROOT "uploads_poblada\productos"

if (-not (Test-Path $SRC_DB)) { throw "No existe $SRC_DB. Ejecuta primero: python scripts/poblar_larebaja.py" }
if (-not (Test-Path $SRC_UPLOADS)) { throw "No existe $SRC_UPLOADS" }

if ($Zip) {
  $OUTPUT = Join-Path $ROOT "target-jpackage-output\FarmarosPlus"
  if (-not (Test-Path $OUTPUT)) { throw "No existe $OUTPUT. Ejecuta primero scripts/empaquetar.ps1" }
  $DST_UPLOADS = Join-Path $OUTPUT "uploads\productos"
  # En el ZIP actual la DB no va dentro, va en %USERPROFILE%\.farmarosplus. Solo parcheamos uploads dentro del ZIP
  # y dejamos la DB poblada al lado del ZIP para que el usuario la copie manual a %USERPROFILE%
  Write-Host "[ZIP] Copiando imagenes a $DST_UPLOADS" -ForegroundColor Yellow
  New-Item -ItemType Directory -Path $DST_UPLOADS -Force | Out-Null
  Copy-Item -Path (Join-Path $SRC_UPLOADS "*") -Destination $DST_UPLOADS -Force
  Copy-Item $SRC_DB (Join-Path $ROOT "FarmarosPlus_poblada-v$Version.db") -Force
  Write-Host "Imagenes parcheadas en ZIP. DB poblada copiada a FarmarosPlus_poblada-v$Version.db (copiar manualmente a %USERPROFILE%\.farmarosplus\farmarosplus.db)" -ForegroundColor Green
  exit 0
}

if ($Prod) {
  $DST_DB = Join-Path $env:USERPROFILE ".farmarosplus\farmarosplus.db"
  $DST_UPLOADS_PROD = Join-Path $ROOT "target-jpackage-output\FarmarosPlus\uploads\productos"
  Write-Host "[PROD] Copiando DB a $DST_DB" -ForegroundColor Yellow
  New-Item -ItemType Directory -Path (Split-Path $DST_DB) -Force | Out-Null
  Copy-Item $SRC_DB $DST_DB -Force
  if (Test-Path $DST_UPLOADS_PROD) {
    New-Item -ItemType Directory -Path $DST_UPLOADS_PROD -Force | Out-Null
    Copy-Item -Path (Join-Path $SRC_UPLOADS "*") -Destination $DST_UPLOADS_PROD -Force
    Write-Host "Imagenes copiadas a $DST_UPLOADS_PROD" -ForegroundColor Green
  } else {
    Write-Host "Aviso: no existe $DST_UPLOADS_PROD (aun no has ejecutado empaquetar.ps1). Solo se copio la DB PROD." -ForegroundColor DarkYellow
    Write-Host "Copia manual: xcopy /E /I uploads_poblada <ruta_ZIP>\FarmarosPlus\uploads\" -ForegroundColor DarkYellow
  }
  Write-Host "LISTO PROD" -ForegroundColor Green
  exit 0
}

# DEV por defecto
$DST_DB_DEV = Join-Path $ROOT "database\farmarosplus.db"
$DST_UPLOADS_DEV = Join-Path $ROOT "uploads\productos"
Write-Host "[DEV] Copiando DB a $DST_DB_DEV" -ForegroundColor Yellow
Copy-Item $SRC_DB $DST_DB_DEV -Force
New-Item -ItemType Directory -Path $DST_UPLOADS_DEV -Force | Out-Null
Copy-Item -Path (Join-Path $SRC_UPLOADS "*") -Destination $DST_UPLOADS_DEV -Force
Write-Host "DB y ${SRC_UPLOADS} -> uploads/productos copiados. Reinicia la app (./mvnw spring-boot:run)" -ForegroundColor Green
