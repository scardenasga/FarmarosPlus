<p align="center">
  <img src="farmarositaLogo.jpg" alt="Farmaros PLUS Logo" width="220" style="border-radius: 12px; box-shadow: 0 4px 8px rgba(0,0,0,0.1);" />
</p>

<h1 align="center">💊 Farmaros PLUS</h1>

<p align="center">
  <strong>Sistema Integral de Gestión Farmacéutica, Inventario y Punto de Venta (POS)</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-25-orange?logo=openjdk&logoColor=white" alt="Java 25" />
  <img src="https://img.shields.io/badge/Spring%20Boot-4.0.5-brightgreen?logo=springboot&logoColor=white" alt="Spring Boot" />
  <img src="https://img.shields.io/badge/Angular-19.2-red?logo=angular&logoColor=white" alt="Angular 19" />
  <img src="https://img.shields.io/badge/Database-SQLite%20%7C%20H2-blue?logo=sqlite&logoColor=white" alt="SQLite" />
  <img src="https://img.shields.io/badge/Desktop-jpackage%20(Windows)-0078D6?logo=windows&logoColor=white" alt="Windows Desktop" />
  <img src="https://img.shields.io/badge/API-OpenAPI%20%2F%20Swagger-green?logo=swagger&logoColor=white" alt="Swagger UI" />
</p>

---

## 📖 Descripción General

**Farmaros PLUS** es una solución de software integral, moderna y modular diseñada para optimizar las operaciones diarias de farmacias y droguerías. Desarrollada bajo una arquitectura monorepo desacoplada (**Spring Boot** + **Angular 19**), combina un punto de venta (POS) ágil, control estricto de inventario farmacéutico por lotes y vencimientos (FEFO), gestión de compras y proveedores, control de devoluciones, analítica de negocio y administración granular de permisos y seguridad.

La plataforma está optimizada para distribuirse de manera **"Zero-Config"** como aplicación de escritorio para Windows, empaquetando un runtime de Java optimizado y almacenamiento SQLite local sin requerir configuraciones complejas por parte del usuario final.

---

## 📸 Capturas del Sistema

A continuación se presentan las vistas principales de la aplicación en funcionamiento:

### 1. Panel Principal y Dashboard
> Vista general del negocio con métricas clave en tiempo real, alertas de stock mínimo, accesos rápidos y estado del sistema.

![Pantalla Principal](capturas/Pantalla%20Principal.png)

---

### 2. Punto de Venta (POS)
> Terminal de ventas intuitivo con búsqueda rápida de productos por nombre o código de barras, cálculo automático de impuestos y descuentos, carrito en vivo y múltiples opciones de liquidación.

![Ventana de Ventas](capturas/Ventana%20de%20Ventas.png)

---

### 3. Gestión de Inventario y Control de Lotes
> Catálogo detallado de medicamentos, trazabilidad por lote y fecha de vencimiento, alertas automáticas de caducidad y filtros de búsqueda avanzados.

![Ventana Inventario](capturas/Ventana%20Inventario.png)

---

### 4. Gestión de Compras y Proveedores
> Administración integral del ciclo de abastecimiento: creación y seguimiento de órdenes de compra, catálogo de distribuidores y recepción de pedidos.

![Ventana Gestión de Compras](capturas/Ventana%20Gestion%20Compras.png)

---

### 5. Control de Permisos y Roles de Usuario
> Matriz granular de seguridad que permite asignar y restringir accesos por usuario y rol en cada módulo crítico del sistema (ventas, compras, inventario, reportes y configuración).

![Panel Gestión de Permisos](capturas/Panel%20Gestion%20Permisos.png)

---

## ✨ Funcionalidades Destacadas

- 🛒 **Punto de Venta Ágil (POS):**
  - Búsqueda instantánea por código de barras, nombre o categoría.
  - Carrito de compras reactivo con desglose de subtotal, IVA y cambio.
  - Generación automática de comprobantes y facturas en PDF.
  - Anulación de ventas con auditoría y reversión automática de stock.
  - Cierre y arqueo de caja por turno.

- 📦 **Control de Inventario Farmacéutico:**
  - Gestión rigurosa de lotes y fechas de vencimiento.
  - Semáforo de alertas tempranas para productos próximos a vencer y stock crítico.
  - Categorización jerárquica y administración de presentaciones.
  - Historial de movimientos de almacén (entradas, salidas, ajustes).

- 🛍️ **Compras y Abastecimiento:**
  - Directorio completo de proveedores y laboratorios.
  - Emisión de órdenes de compra con cálculo de costos.
  - Recepción de mercancía con conciliación de lotes recibidos.
  - Cancelación controlada de órdenes sin afectación errónea de stock.

- 🔄 **Trazabilidad de Devoluciones:**
  - Devoluciones a proveedores por averías o inconsistencias.
  - Devoluciones de clientes con reincorporación controlada al inventario.

- 🔐 **Seguridad y Control de Accesos:**
  - Autenticación con cifrado robusto de contraseñas (BCrypt).
  - Control de accesos basado en roles (RBAC) y permisos individuales granulares.
  - Bloqueo y protección de rutas y componentes tanto a nivel de API como de interfaz.

- 📊 **Reportería y Analítica:**
  - Informes detallados de ventas por período, vendedor o producto.
  - Gráficos interactivos de tendencias comerciales con Chart.js.

- 🚀 **Portabilidad "Zero-Config":**
  - Distribución empaquetada con `jpackage` que incluye su propia JRE ligera (~60MB) y base de datos SQLite integrada. El cliente final no necesita instalar Java ni dependencias adicionales.

---

## 🛠️ Stack Tecnológico

| Capa | Tecnologías |
| :--- | :--- |
| **Backend** | • Java 25<br>• Spring Boot 4.0.5 (Web, Data JPA, Validation)<br>• Spring Security Crypto (BCrypt)<br>• SpringDoc OpenAPI / Swagger UI 3.0.2<br>• Lombok |
| **Frontend** | • Angular 19.2 (Standalone Components)<br>• TypeScript 5.7 & RxJS 7.8<br>• Chart.js 4.5 & ng2-charts 5.0<br>• HTML5 / SCSS / Diseño Responsivo |
| **Persistencia** | • SQLite (`org.xerial:sqlite-jdbc`) para ejecución local y producción<br>• Base de datos H2 en memoria para suite de pruebas automatizadas |
| **Testing** | • JUnit 5 & Mockito (`spring-boot-starter-test`)<br>• Karma & Jasmine para frontend |
| **Packaging & CI** | • Maven Wrapper (`mvnw`)<br>• Script automatizado PowerShell con `jpackage` y `jlink` |

---

## 📁 Estructura del Repositorio

```text
FarmarosPlus/
├── backend/                   # Microservicio Spring Boot (Java 25)
│   ├── src/main/java/         # Controllers, Services, Repositories, Entities y DTOs
│   ├── src/main/resources/    # Configuración (application.properties, profiles dev/prod)
│   ├── src/test/java/         # Suite de pruebas unitarias y de integración (H2)
│   └── pom.xml                # Gestión de dependencias Maven
│
├── frontend/                  # Single Page Application (Angular 19)
│   ├── src/app/               # Módulos: ventas, inventario, compras, auth, dashboard...
│   ├── package.json           # Dependencias y scripts npm
│   └── angular.json           # Configuración del compilador Angular
│
├── capturas/                  # Evidencias visuales de los módulos de la aplicación
├── database/                  # Base de datos SQLite de desarrollo (farmarosplus.db)
├── scripts/                   # Automatizaciones (empaquetado con jpackage, población de datos)
├── docs/                      # Diagramas de clases, entidad-relación y planes de prueba
└── README.md                  # Documentación principal del repositorio
```

---

## 🚀 Guía de Ejecución

### Opción A: Modo Usuario Final (Ejecutable Desktop)

Para ejecutar la aplicación compilada sin necesidad de instalar Java, Node ni bases de datos:

1. Descarga y descomprime el paquete de distribución (`FarmarosPlus-v1.0.x.zip`).
2. Ingresa a la carpeta extraída `FarmarosPlus/`.
3. Ejecuta **`Iniciar FarmarosPlus.bat`** (o `FarmarosPlus.exe`).
4. La aplicación inicializará el backend de forma transparente y abrirá automáticamente tu navegador en `http://localhost:8080`.
> *Nota: Los datos se conservan automáticamente en la carpeta de usuario (`~/.farmarosplus/farmarosplus.db`), lo que garantiza que las actualizaciones de versión no borren la información.*

---

### Opción B: Entorno de Desarrollo

#### Prerrequisitos
- **Java JDK 25** configurado en el sistema (`JAVA_HOME`).
- **Node.js 20+** y **npm** (para el frontend).

#### 1. Clonar el repositorio
```bash
git clone <URL_DEL_REPOSITORIO>
cd FarmarosPlus
```

#### 2. Levantar el Backend
El backend utiliza por defecto el perfil `dev`, apuntando a la base de datos de desarrollo `database/farmarosplus.db`:
```bash
# En Windows:
.\mvnw.cmd spring-boot:run

# En Linux / macOS:
./mvnw spring-boot:run
```
> La API REST y documentación interactiva quedarán disponibles en:
> - **API Base:** `http://localhost:8080/api`
> - **Swagger UI:** `http://localhost:8080/swagger-ui.html`

#### 3. Levantar el Frontend
En otra terminal:
```bash
cd frontend
npm install
npm start
```
> Accede a la aplicación web en desarrollo en `http://localhost:4200`.

---

## 🧪 Pruebas Automatizadas

El proyecto cuenta con una robusta suite de pruebas unitarias y de integración que se ejecutan sobre una base de datos **H2 en memoria**, asegurando cero efectos secundarios sobre la base SQLite real:

```bash
# Ejecutar todas las pruebas del backend
.\mvnw.cmd clean test

# Ejecutar una clase de prueba específica
.\mvnw.cmd -Dtest=CategoriaServiceTest test
```

---

## 📦 Generación del Empaquetado Desktop

Para compilar el frontend, empaquetar el backend en un JAR unificado y generar el instalador/distribuible autónomo para Windows:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/empaquetar.ps1 -Version 1.0.8
```

Este proceso generará automáticamente:
- `target-jpackage-output/FarmarosPlus/`: Directorio con el ejecutable nativo, scripts de inicio y runtime embebido.
- `FarmarosPlus-v1.0.8.zip`: Archivo comprimido listo para entregar al cliente.

---

## 📄 Licencia y Créditos

Desarrollado para la gestión y modernización de droguerías y farmacias.
Todos los derechos reservados.
