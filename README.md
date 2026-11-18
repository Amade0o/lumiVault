<p align="center">
  <img src="src/main/resources/images/lumi-logo.png" alt="LumiVault" width="140">
</p>

<h1 align="center">LumiVault</h1>

<p align="center">
  Sistema de gestión de inventario para comercios de blanquería, decoración y regalería.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange" alt="Java 21">
  <img src="https://img.shields.io/badge/JavaFX-21-blue" alt="JavaFX 21">
  <img src="https://img.shields.io/badge/SQLite-3-lightgrey" alt="SQLite">
  <img src="https://img.shields.io/badge/Gradle-8-02303A" alt="Gradle">
</p>

---

## Descripción

LumiVault es una aplicación de escritorio para administrar el stock de un comercio pequeño. Funciona de forma local, sin conexión a internet ni servidor: los datos se guardan en una base SQLite junto a la aplicación.

## Funcionalidades

- **Productos**: alta, baja, búsqueda por nombre o ID y actualización de precios de costo y venta.
- **Categorías**: organización de productos por categoría.
- **Stock**: registro de entradas y salidas, con historial de movimientos por producto.
- **Resumen**: vista general del inventario y de la actividad reciente.
- **Backups automáticos**: en cada inicio se guarda una copia de la base en `data/backups/` (se conservan las últimas 20).

## Tecnologías

| Área        | Herramienta                          |
|-------------|--------------------------------------|
| Lenguaje    | Java 21                              |
| Interfaz    | JavaFX 21                            |
| Persistencia| SQLite (`sqlite-jdbc`)               |
| Build       | Gradle, Shadow, Launch4j             |
| Testing     | JUnit 5, Cucumber, Mockito, AssertJ, H2 |

## Requisitos

- JDK 21
- Para generar el ejecutable de Windows: `wget`, `unzip` y `zip` (los scripts están pensados para Linux)

## Uso

Ejecutar la aplicación en modo desarrollo:

```bash
./gradlew run
```

La base de datos se crea automáticamente en `data/lumivault.db` dentro del directorio de trabajo.

Ejecutar los tests:

```bash
./gradlew test
```

## Distribución para Windows

El proyecto genera un paquete portable que incluye su propio JRE, por lo que el usuario final no necesita tener Java instalado.

| Script                 | Descripción |
|------------------------|-------------|
| `build-windows.sh`     | Build completo: genera `LumiVault.exe`, descarga el JRE 21 y arma `dist/LumiVault/`. |
| `rebuild-quick.sh`     | Recompila y actualiza `dist/LumiVault-Windows.zip` sin volver a descargar el JRE. |
| `rebuild-version.sh`   | Igual que el anterior, pero genera un ZIP con número de versión. |

El usuario final solo tiene que descomprimir el ZIP y abrir `LumiVault.exe`.

## Estructura del proyecto

```
src/main/java/
├── controller/   Controlador y tipos de comando
├── database/     Conexión SQLite, creación de tablas y backups
├── model/        Entidades (Product, Category, Movement)
│   ├── command/  Operaciones del sistema (patrón Command)
│   └── factory/  Creación de productos (patrón Factory)
└── view/         Interfaz JavaFX y modo consola

src/test/
├── java/         Tests unitarios, de integración y de rendimiento
└── resources/features/   Escenarios Cucumber (BDD)
```

## Arquitectura

La aplicación sigue una estructura **MVC**. Cada acción que modifica el estado (agregar producto, registrar stock, etc.) se encapsula como un **Command**, que el `Controller` ejecuta a partir de un `CommandType`. La conexión a la base se gestiona con un **Singleton**.
