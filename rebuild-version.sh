#!/bin/bash
# Script para crear versiones numeradas

set -e

# Pedir versión
read -p "📝 Número de versión (ej: 1.0, 2.0, 2.1): " VERSION

if [ -z "$VERSION" ]; then
    echo "❌ Debes especificar una versión"
    exit 1
fi

echo "🔄 Construyendo LumiVault v${VERSION}..."

# Limpiar y reconstruir
./gradlew clean shadowJar createExe

# Actualizar archivos en dist
cp build/launch4j/LumiVault.exe dist/LumiVault/
cp build/libs/lumiVault.jar dist/LumiVault/lib/

# Crear archivo de versión
echo "LumiVault v${VERSION}" > dist/LumiVault/VERSION.txt
echo "Fecha: $(date '+%Y-%m-%d %H:%M')" >> dist/LumiVault/VERSION.txt

# Crear ZIP con versión en el nombre
cd dist
ZIP_NAME="LumiVault-v${VERSION}-Windows.zip"
rm -f "$ZIP_NAME"
zip -r -q "$ZIP_NAME" LumiVault/

echo "✅ Listo! Versión creada:"
ls -lh "$ZIP_NAME"
echo ""
echo "📦 Archivo: dist/$ZIP_NAME"
