#!/bin/bash
set -e

echo "🚀 LumiVault - Construcción de Ejecutable para Windows"
echo "======================================================"

# Colores
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # Sin color

# Limpiar construcciones anteriores
echo -e "\n${BLUE}[1/6]${NC} Limpiando construcciones anteriores..."
rm -rf build/launch4j
rm -rf build/libs
rm -rf dist
./gradlew clean

# Construir el JAR con todas las dependencias
echo -e "\n${BLUE}[2/6]${NC} Construyendo JAR con todas las dependencias..."
./gradlew shadowJar

# Crear el ejecutable .exe
echo -e "\n${BLUE}[3/6]${NC} Generando ejecutable LumiVault.exe..."
./gradlew createExe

# Crear estructura de distribución
echo -e "\n${BLUE}[4/6]${NC} Creando estructura de distribución..."
mkdir -p dist/LumiVault/lib
mkdir -p dist/LumiVault/data

# Copiar archivos
cp build/launch4j/LumiVault.exe dist/LumiVault/
cp build/libs/lumiVault.jar dist/LumiVault/lib/

# Descargar JRE de Windows (si no existe)
JRE_DIR="dist/LumiVault/jre"
if [ ! -d "$JRE_DIR" ]; then
    echo -e "\n${BLUE}[5/6]${NC} Descargando JRE 21 para Windows..."
    echo -e "${YELLOW}⚠️  Esto puede tardar varios minutos (descarga ~100 MB)${NC}"
    
    # URL del JRE de Adoptium (Temurin)
    JRE_URL="https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.2%2B13/OpenJDK21U-jre_x64_windows_hotspot_21.0.2_13.zip"
    JRE_ZIP="jre-temp.zip"
    
    # Descargar
    wget -q --show-progress "$JRE_URL" -O "$JRE_ZIP"
    
    # Extraer
    echo -e "${BLUE}Extrayendo JRE...${NC}"
    unzip -q "$JRE_ZIP" -d dist/LumiVault/
    
    # Renombrar la carpeta del JRE
    mv dist/LumiVault/jdk-21.0.2+13-jre dist/LumiVault/jre
    
    # Limpiar archivo temporal
    rm "$JRE_ZIP"
    
    echo -e "${GREEN}✓ JRE descargado e instalado${NC}"
else
    echo -e "\n${BLUE}[5/6]${NC} JRE ya existe, omitiendo descarga..."
fi

# Crear archivo README para el usuario
echo -e "\n${BLUE}[6/6]${NC} Creando documentación..."
cat > dist/LumiVault/README.txt << 'EOF'
╔══════════════════════════════════════════════════════════════╗
║                      LUMIVAULT v1.0                          ║
║              Sistema de Gestión de Inventario                ║
╚══════════════════════════════════════════════════════════════╝

INSTRUCCIONES DE USO:
────────────────────────────────────────────────────────────────

1. Haz doble clic en "LumiVault.exe" para iniciar la aplicación

2. Para crear un acceso directo en el escritorio:
   - Clic derecho sobre "LumiVault.exe"
   - Selecciona "Crear acceso directo"
   - Arrastra el acceso directo a tu escritorio

3. Las bases de datos se guardan automáticamente en la carpeta "data"

4. IMPORTANTE: No borres ni muevas la carpeta "jre" - es necesaria
   para que el programa funcione.

5. Puedes copiar toda la carpeta "LumiVault" a otro PC o USB
   y funcionará sin necesidad de instalar nada.

────────────────────────────────────────────────────────────────
CONTENIDO DE LA CARPETA:
────────────────────────────────────────────────────────────────

LumiVault.exe    → Ejecutable principal (haz doble clic aquí)
jre/             → Java Runtime (no tocar)
lib/             → Librerías de la aplicación (no tocar)
data/            → Tus bases de datos se guardan aquí
README.txt       → Este archivo

────────────────────────────────────────────────────────────────

¡Disfruta usando LumiVault!

EOF

echo -e "\n${GREEN}✓ Construcción completada exitosamente!${NC}"
echo -e "\n📦 El ejecutable está en: ${BLUE}dist/LumiVault/${NC}"
echo -e "\n📝 Próximos pasos:"
echo -e "   1. Comprime la carpeta ${BLUE}dist/LumiVault/${NC} en un ZIP"
echo -e "   2. Envía el ZIP a tus usuarios de Windows"
echo -e "   3. Ellos solo deben descomprimir y hacer doble clic en ${BLUE}LumiVault.exe${NC}"
echo ""
