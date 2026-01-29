#!/bin/bash
# Script rápido para reconstruir el ejecutable (sin volver a descargar el JRE)

set -e

echo "🔄 Recompilando LumiVault..."

# Limpiar y reconstruir
./gradlew clean shadowJar createExe

# Actualizar archivos en dist
cp build/launch4j/LumiVault.exe dist/LumiVault/
cp build/libs/lumiVault.jar dist/LumiVault/lib/

# Recrear ZIP
cd dist
rm -f LumiVault-Windows.zip
zip -r -q LumiVault-Windows.zip LumiVault/

echo "✅ Listo! ZIP actualizado en: dist/LumiVault-Windows.zip"
ls -lh LumiVault-Windows.zip
