# ProductoController endpoints

Referencia de integracion para `src/main/java/co/edu/unbosque/backend/controller/ProductoController.java`.

Base path: `/api/productos`

## Resumen

| Method | Path | Description |
|---|---|---|
| POST | `/api/productos` | Crear producto con stock inicial. |
| PATCH | `/api/productos/{id}` | Editar un producto existente. |
| GET | `/api/productos/{id}/detalle` | Obtener un producto con sus lotes asociados. |
| POST | `/api/productos/codigo-barras/{codigoBarras}/ingresos` | Registrar ingreso de stock por codigo de barras. |
| GET | `/api/productos/{id}` | Obtener producto por id. |
| GET | `/api/productos/activos` | Listar productos activos. |
| GET | `/api/productos/stock-bajo` | Listar productos con stock bajo. |
| GET | `/api/productos/buscar?nombre=...&codigo=...` | Buscar productos activos por nombre o codigo. |
| PATCH | `/api/productos/codigo-barras/{codigoBarras}/precio` | Actualizar costo y precio de venta. |

## 1. POST `/api/productos`

Crea un producto nuevo. Si se envia `numeroLote` o `fechaVencimiento`, tambien se crea un lote inicial.

### Campos del body

Obligatorios:
- `nombre`
- `codigoBarras`
- `stockInicial`
- `costo`
- `precioVenta`
- `requierePrescripcion`

Opcionales:
- `categoriaId`
- `descripcion`
- `stockMinimo`
- `porcentajeIva`
- `fechaVencimiento`
- `numeroLote`

Validacion importante:
- `precioVenta` debe ser mayor que `costo * (1 + porcentajeIva / 100)`.
- Si `porcentajeIva` no se envia, se toma `0.0`.
- `estado` no se envia: el producto se crea como `ACTIVO`.
- Si `numeroLote` se envia, no puede repetirse en otro lote existente. El valor puede ser `null`, pero si viene informado debe ser unico.

### Ejemplo 1: producto sin lote

```json
{
  "nombre": "Acetaminofen 500mg",
  "codigoBarras": "7701234567890",
  "stockInicial": 20,
  "costo": 8500.0,
  "precioVenta": 12000.0,
  "porcentajeIva": 0.0,
  "requierePrescripcion": false,
  "fechaVencimiento": "2027-12-31"
}
```

### Ejemplo 2: producto con lote

```json
{
  "categoriaId": 1,
  "nombre": "Amoxicilina 500mg",
  "descripcion": "Caja por 20 capsulas",
  "codigoBarras": "7709876543210",
  "stockMinimo": 5,
  "stockInicial": 50,
  "costo": 15000.0,
  "precioVenta": 22000.0,
  "porcentajeIva": 19.0,
  "requierePrescripcion": true,
  "fechaVencimiento": "2027-12-31",
  "numeroLote": "AMX-2026-01"
}
```

## 2. PATCH `/api/productos/{id}`

Edita un producto existente. Este endpoint es para cambios de catalogo y tambien permite correccion manual de `stockActual`.

### Campos del body

Todos son opcionales, pero el request debe incluir al menos uno.

Editables:
- `categoriaId`
- `nombre`
- `descripcion`
- `stockMinimo`
- `stockActual`
- `costo`
- `precioVenta`
- `porcentajeIva`
- `requierePrescripcion`
- `estado`

No editables por este endpoint:
- `id`
- `stockInicial`
- `numeroLote`
- `fechaVencimiento`

Validacion importante:
- `stockActual` no puede ser negativo.
- Si cambias `costo`, `precioVenta` o `porcentajeIva`, se sigue validando que el precio de venta tenga sentido economico.
- `estado` solo acepta `ACTIVO`, `INACTIVO` o `DESCONTINUADO`.

### Ejemplo 1: cambio parcial de catalogo

```json

{
  "categoriaId": 2,
  "nombre": "Acetaminofen 650mg",
  "descripcion": "Caja por 20 tabletas recubiertas",
  "stockMinimo": 12,
  "stockActual": 120,
  "costo": 9000.0,
  "precioVenta": 13500.0,
  "porcentajeIva": 0.0,
  "requierePrescripcion": false,
  "estado": "ACTIVO"
}
```

### Ejemplo 2: correccion manual de stock

```json
{
  "stockActual": 120
}
```

### Ejemplo 3: cambio de estado y prescripcion

```json
{
  "requierePrescripcion": true,
  "estado": "INACTIVO"
}
```

## 3. GET `/api/productos/{id}/detalle`

Obtiene un producto por id con la misma informacion del detalle basico y una lista de lotes asociados.

### Respuesta

Devuelve:
- todos los campos de `ProductoResponse`
- `lotes`: lista resumida con `id`, `numeroLote`, `fechaVencimiento` y `cantidad`
- si el producto no tiene lotes, `lotes` devuelve una lista vacia y el producto igual se muestra

### Ejemplo de uso

```http
GET /api/productos/1/detalle
```

### Ejemplo de respuesta

```json
{
  "id": 1,
  "categoria": {
    "id": 2,
    "nombre": "Analgesicos",
    "descripcion": "Productos para dolor y fiebre"
  },
  "nombre": "Acetaminofen 500mg",
  "descripcion": "Caja por 20 tabletas",
  "codigoBarras": "7701234567890",
  "stockMinimo": 10,
  "stockActual": 20,
  "costo": 8500.0,
  "precioVenta": 12000.0,
  "margenGanancia": 41.18,
  "porcentajeIva": 0.0,
  "requierePrescripcion": false,
  "estado": "ACTIVO",
  "lotes": [
    {
      "id": 10,
      "numeroLote": "LOT-001",
      "fechaVencimiento": "2027-12-31",
      "cantidad": 12
    },
    {
      "id": 11,
      "numeroLote": null,
      "fechaVencimiento": "2028-01-15",
      "cantidad": 8
    }
  ]
}
```

## 4. POST `/api/productos/codigo-barras/{codigoBarras}/ingresos`

Aumenta el stock de un producto existente buscando por codigo de barras.

### Campos del body

Obligatorios:
- `cantidad`
- `fechaVencimiento`

Opcionales:
- `numeroLote`
- `nuevoCosto`
- `nuevoPrecioVenta`

Comportamiento:
- Siempre se crea un lote nuevo para el ingreso.
- Si `numeroLote` viene, no puede repetirse con otro lote existente.
- Si `numeroLote` no viene, el lote se guarda con `numeroLote = null`.
- Si `nuevoCosto` o `nuevoPrecioVenta` vienen, actualizan el producto y pueden generar historial de precio.

### Ejemplo 1: ingreso simple

```json
{
  "cantidad": 25,
  "fechaVencimiento": "2027-12-31"
}
```

### Ejemplo 2: ingreso con lote

```json
{
  "cantidad": 40,
  "numeroLote": "AMX-2026-02",
  "fechaVencimiento": "2027-12-31"
}
```

### Ejemplo 3: ingreso con cambio de precio

```json
{
  "cantidad": 40,
  "numeroLote": "AMX-2026-02",
  "nuevoCosto": 16000.0,
  "nuevoPrecioVenta": 23500.0,
  "fechaVencimiento": "2027-12-31"
}
```

## 5. GET `/api/productos/{id}`

Obtiene un producto por id.

### Ejemplo de uso

```http
GET /api/productos/1
```

No lleva body.

## 6. GET `/api/productos/activos`

Lista los productos con estado `ACTIVO`.

### Ejemplo de uso

```http
GET /api/productos/activos
```

No lleva body.
### Ejemplo de respuesta
```json
[
  {
    "id": 1,
    "categoria": null,
    "nombre": "Acetaminofen 500mg",
    "descripcion": null,
    "codigoBarras": "7701234567890",
    "stockMinimo": 10,
    "stockActual": 19,
    "costo": 8500,
    "precioVenta": 12000,
    "margenGanancia": 41.17647058823529,
    "porcentajeIva": 0,
    "requierePrescripcion": false,
    "estado": "ACTIVO"
  },
  {
    "id": 2,
    "categoria": {
      "id": 1,
      "nombre": "Dolor",
      "descripcion": "Para el dolor de cabeza"
    },
    "nombre": "Amoxicilina 500mg",
    "descripcion": null,
    "codigoBarras": "7709876543210",
    "stockMinimo": 5,
    "stockActual": 130,
    "costo": 16000,
    "precioVenta": 23500,
    "margenGanancia": 46.875,
    "porcentajeIva": 19,
    "requierePrescripcion": true,
    "estado": "ACTIVO"
  },
  {
    "id": 3,
    "categoria": {
      "id": 1,
      "nombre": "Dolor",
      "descripcion": "Para el dolor de cabeza"
    },
    "nombre": "Sevedol 500mg",
    "descripcion": null,
    "codigoBarras": "7709876543211",
    "stockMinimo": 5,
    "stockActual": 50,
    "costo": 15000,
    "precioVenta": 22000,
    "margenGanancia": 46.666666666666664,
    "porcentajeIva": 19,
    "requierePrescripcion": true,
    "estado": "ACTIVO"
  }
]
```

## 7. GET `/api/productos/stock-bajo`

Lista productos cuyo stock actual es menor o igual al stock minimo.

### Ejemplo de uso

```http
GET /api/productos/stock-bajo
```

No lleva body.

## 8. GET `/api/productos/buscar?nombre=...&codigo=...`

Busca productos activos por nombre o codigo de barras.

### Query params

Obligatorio:
- al menos uno de estos dos: `nombre` o `codigo`

Opcionales:
- el otro parametro puede omitirse

### Ejemplos de uso

```http
GET /api/productos/buscar?nombre=acetaminofen
```

```http
GET /api/productos/buscar?codigo=7701234567890
```

```http
GET /api/productos/buscar?nombre=acetaminofen&codigo=7701234567890
```

No lleva body.

## 9. PATCH `/api/productos/codigo-barras/{codigoBarras}/precio`

Actualiza el costo y el precio de venta de un producto por codigo de barras.

### Campos del body

Obligatorios:
- `nuevoCosto`
- `nuevoPrecioVenta`

Opcional:
- `motivo`

Validacion importante:
- `nuevoCosto` y `nuevoPrecioVenta` no pueden ser negativos.
- El nuevo precio debe seguir siendo coherente con el IVA del producto y dejar margen positivo.

### Ejemplo 1: cambio de precio

```json
{
  "nuevoCosto": 9000.0,
  "nuevoPrecioVenta": 13000.0,
  "motivo": "Ajuste por proveedor"
}
```

### Ejemplo 2: cambio sin motivo

```json
{
  "nuevoCosto": 9000.0,
  "nuevoPrecioVenta": 13000.0
}
```

## Notas de integracion

- Todas las solicitudes de escritura usan validacion con `@Valid`.
- Los errores de validacion retornan `400`.
- Los errores de negocio retornan `BusinessException` o `ResourceNotFoundException` segun el caso.
- Las respuestas usan `ProductoResponse`, `ProductoDetalleResponse`, `CategoriaResponse` y los DTOs resumidos de lote.
- El controlador no expone el campo `id` para editarlo, ni el `stockInicial` o los datos de lote desde el endpoint de edicion.

# CategoriaController endpoints

Referencia de integracion para `src/main/java/co/edu/unbosque/backend/controller/CategoriaController.java`.

Base path: `/api/categorias`

## Resumen

| Method | Path | Description |
|---|---|---|
| POST | `/api/categorias` | Crear categoria. |
| PUT | `/api/categorias/{id}` | Actualizar categoria. |
| DELETE | `/api/categorias/{id}` | Eliminar categoria (solo si no tiene productos activos). |
| GET | `/api/categorias/{id}` | Consultar categoria por id. |
| GET | `/api/categorias` | Listar categorias (ordenadas por nombre). |

## 1. POST `/api/categorias`

Crea una categoria nueva.

### Campos del body

Obligatorios:
- `nombre` (no puede venir vacío; se normaliza con `trim()`)

Opcionales:
- `descripcion` (si viene `null`, vacía o solo espacios, se guarda como `null`; también se normaliza con `trim()` y colapsa espacios múltiples)

Validacion importante:
- `nombre` debe ser único (comparación case-insensitive). Si ya existe, retorna error.

### Ejemplo de request

```json
{
  "nombre": "Analgesicos",
  "descripcion": "Productos para dolor y fiebre"
}
```

### Ejemplo de uso

```http
POST /api/categorias
```

### Respuesta (201)

```json
{
  "id": 1,
  "nombre": "Analgesicos",
  "descripcion": "Productos para dolor y fiebre"
}
```

## 2. PUT `/api/categorias/{id}`

Actualiza el nombre y la descripción de una categoria existente.

### Path params

Obligatorios:
- `id` (Long)

### Campos del body

Obligatorios:
- `nombre` (no puede venir vacío; se normaliza con `trim()`)

Opcionales:
- `descripcion` (misma normalización que en creación)

Validacion importante:
- `nombre` debe ser único (case-insensitive). Si el nombre ya existe en otra categoría distinta a `{id}`, retorna error.

### Ejemplo de request

```json
{
  "nombre": "Dolor",
  "descripcion": "Para el dolor de cabeza"
}
```

### Ejemplo de uso

```http
PUT /api/categorias/1
```

### Respuesta (200)

```json
{
  "id": 1,
  "nombre": "Dolor",
  "descripcion": "Para el dolor de cabeza"
}
```

## 3. DELETE `/api/categorias/{id}`

Elimina una categoria existente, solo si no tiene productos con estado `ACTIVO` asociados.

### Path params

Obligatorios:
- `id` (Long)

### Ejemplo de uso

```http
DELETE /api/categorias/1
```

No lleva body.

### Respuesta (204)

Sin body.

## 4. GET `/api/categorias/{id}`

Consulta una categoria por id.

### Path params

Obligatorios:
- `id` (Long)

### Ejemplo de uso

```http
GET /api/categorias/1
```

No lleva body.

### Respuesta (200)

```json
{
  "id": 1,
  "nombre": "Dolor",
  "descripcion": "Para el dolor de cabeza"
}
```

## 5. GET `/api/categorias`

Lista todas las categorias ordenadas por `nombre` ascendente.

### Ejemplo de uso

```http
GET /api/categorias
```

No lleva body.

### Respuesta (200)

```json
[
  {
    "id": 1,
    "nombre": "Analgesicos",
    "descripcion": "Productos para dolor y fiebre"
  },
  {
    "id": 2,
    "nombre": "Dolor",
    "descripcion": "Para el dolor de cabeza"
  }
]
```

## Notas de integracion

- Todas las solicitudes con body en estos endpoints usan validación con `@Valid`.
- Errores de validación (por ejemplo `nombre` vacío) retornan `400` con `ApiErrorResponse`.
- Errores de negocio (`BusinessException`) retornan `400` (por ejemplo: nombre duplicado o intento de eliminar una categoría con productos activos).
- Recursos inexistentes (`ResourceNotFoundException`) retornan `404`.

### Ejemplo de error (ApiErrorResponse)

```json
{
  "timestamp": "2026-05-13T14:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Ya existe una categoria con nombre Dolor",
  "path": "/api/categorias"
}
```

# ProveedorController endpoints

Referencia de integracion para `src/main/java/co/edu/unbosque/backend/controller/ProveedorController.java`.

Base path: `/api/proveedores`

## Resumen

| Method | Path | Description |
|---|---|---|
| POST | `/api/proveedores` | Crear proveedor. |
| GET | `/api/proveedores/{id}` | Consultar proveedor por id. |
| GET | `/api/proveedores/{id}/detalle` | Consultar proveedor por id con productos asociados. |
| GET | `/api/proveedores/activos` | Listar proveedores activos. |
| GET | `/api/proveedores` | Listar todos los proveedores. |
| PATCH | `/api/proveedores/{id}` | Actualizar datos del proveedor. |
| PATCH | `/api/proveedores/{id}/estado` | Actualizar estado del proveedor. |
| POST | `/api/proveedores/{id}/productos` | Asociar o reactivar un producto para el proveedor. |
| PATCH | `/api/proveedores/{id}/productos/{productoId}/estado` | Activar o inactivar una relación proveedor-producto. |
| DELETE | `/api/proveedores/{id}/productos/{productoId}` | Eliminar una relación proveedor-producto. |

## 1. POST `/api/proveedores`

Crea un proveedor nuevo.

### Campos del body

Obligatorios:
- `nombre`

Opcionales:
- `nit`
- `telefono`
- `email`
- `contacto`
- `condicionPago`

Validacion importante:
- `nombre` no puede venir vacio.
- `nombre` se normaliza con `trim()` y colapsa espacios multiples (ej: `"  Proveedor   ABC  "` se guarda como `"Proveedor ABC"`).
- `nombre` debe ser unico (comparacion case-insensitive). Si ya existe, retorna error.
- `estado` no se envia: el proveedor se crea como `ACTIVO`.
- Campos opcionales que vengan `null`, vacios o solo espacios se guardan como `null` (tambien se normalizan con `trim()` y colapsan espacios multiples).
- El backend acepta `condicionPago` y también los alias `condicionDePago` y `condicion_pago`.

### Ejemplo de request

```json
{
  "nombre": "Farmacéutica XYZ S.A.",
  "nit": "860123456-7",
  "telefono": "+57 1 1234567",
  "email": "contacto@farmaxyza.com",
  "contacto": "Juan Pérez",
  "condicionPago": "Neto 30"
}
```

### Ejemplo de uso

```http
POST /api/proveedores
```

### Respuesta (201)

Devuelve `ProveedorResponse`:
- `idProveedor`: id generado
- `estado`: `ACTIVO` por defecto
- `fechaCreacion` / `fechaModificacion`: fecha-hora en formato ISO (`LocalDateTime`)

```json
{
  "idProveedor": 1,
  "nombre": "Farmacéutica XYZ S.A.",
  "nit": "860123456-7",
  "telefono": "+57 1 1234567",
  "email": "contacto@farmaxyza.com",
  "contacto": "Juan Pérez",
  "estado": "ACTIVO",
  "condicionPago": "Neto 30"
}
```

### Posibles errores

- `400 Bad Request` si `nombre` viene vacio o si ya existe un proveedor con el mismo `nombre` (case-insensitive).
- `400 Bad Request` si el JSON es ilegible o invalido.

#### Ejemplo de error (ApiErrorResponse)

```json
{
  "timestamp": "2026-05-14T10:16:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Ya existe un proveedor con el nombre: Farmacéutica XYZ S.A.",
  "path": "/api/proveedores"
}
```

## 2. GET `/api/proveedores/{id}`

Consulta un proveedor por id.

### Path params

Obligatorios:
- `id` (Long)

### Ejemplo de uso

```http
GET /api/proveedores/1
```

No lleva body.

### Respuesta (200)

```json
{
  "idProveedor": 1,
  "nombre": "Farmacéutica XYZ S.A.",
  "nit": "860123456-7",
  "telefono": "+57 1 1234567",
  "email": "contacto@farmaxyza.com",
  "contacto": "Juan Pérez",
  "estado": "ACTIVO",
  "condicionPago": "Neto 30"
}
```

### Posibles errores

- `404 Not Found` si no existe proveedor con id `{id}`.

#### Ejemplo de error (ApiErrorResponse)

```json
{
  "timestamp": "2026-05-14T10:20:00",
  "status": 404,
  "error": "Not Found",
  "message": "No existe proveedor con id 999",
  "path": "/api/proveedores/999"
}
```

## 3. GET `/api/proveedores/activos`

Lista proveedores con estado `ACTIVO`, ordenados por `nombre` ascendente.

### Ejemplo de uso

```http
GET /api/proveedores/activos
```

No lleva body.

### Respuesta (200)

```json
[
  {
    "idProveedor": 1,
    "nombre": "Farmacéutica XYZ S.A.",
    "nit": "860123456-7",
    "telefono": "+57 1 1234567",
    "email": "contacto@farmaxyza.com",
    "contacto": "Juan Pérez",
    "estado": "ACTIVO",
    "condicionPago": "Neto 30"
  }
]
```

## 3.1. GET `/api/proveedores/{id}/detalle`

Consulta un proveedor por id y devuelve también los productos que tiene asociados en `proveedor_producto`.

### Path params

Obligatorios:
- `id` (Long)

### Ejemplo de uso

```http
GET /api/proveedores/1/detalle
```

### Respuesta (200)

```json
{
  "idProveedor": 1,
  "nombre": "Farmacéutica XYZ S.A.",
  "nit": "860123456-7",
  "telefono": "+57 1 1234567",
  "email": "contacto@farmaxyza.com",
  "contacto": "Juan Pérez",
  "estado": "ACTIVO",
  "condicionPago": "Neto 30",
  "productos": [
    {
      "id": 10,
      "nombre": "Acetaminofen 500mg",
      "descripcion": "Caja por 20 tabletas",
      "codigoBarras": "7701234567890",
      "estado": "ACTIVO",
      "codigoProductoProveedor": "PR-ACET-01",
      "precioReferencia": 8500.0,
      "estadoRelacion": "ACTIVO"
    }
  ]
}
```

### Notas

- Si el proveedor no tiene productos asociados, `productos` retorna una lista vacía.
- `estado` corresponde al estado del producto y `estadoRelacion` al estado de la fila en `proveedor_producto`.

## 4. GET `/api/proveedores`

Lista todos los proveedores.

Nota: este endpoint retorna `findAll()` (no filtra por estado y no garantiza orden).

### Ejemplo de uso

```http
GET /api/proveedores
```

No lleva body.

### Respuesta (200)

```json
[
  {
    "idProveedor": 1,
    "nombre": "Farmacéutica XYZ S.A.",
    "nit": "860123456-7",
    "telefono": "+57 1 1234567",
    "email": "contacto@farmaxyza.com",
    "contacto": "Juan Pérez",
    "estado": "ACTIVO",
    "condicionPago": "Neto 30"
  },
  {
    "idProveedor": 2,
    "nombre": "Proveedor ABC",
    "nit": null,
    "telefono": null,
    "email": null,
    "contacto": null,
    "estado": "INACTIVO",
    "condicionPago": null
  }
]
```

## 5. PATCH `/api/proveedores/{id}/estado`

Actualiza el `estado` del proveedor.

### Path params

Obligatorios:
- `id` (Long)

### Campos del body

Obligatorios:
- `estado`

Validacion importante:
- Valores validos: `ACTIVO` o `INACTIVO` (se normaliza con `trim().toUpperCase()`).

### Ejemplo de request

```json
{
  "estado": "INACTIVO"
}
```

### Ejemplo de uso

```http
PATCH /api/proveedores/1/estado
```

### Respuesta (200)

Retorna `ProveedorResponse` con el `estado` actualizado.

```json
{
  "idProveedor": 1,
  "nombre": "Farmacéutica XYZ S.A.",
  "nit": "860123456-7",
  "telefono": "+57 1 1234567",
  "email": "contacto@farmaxyza.com",
  "contacto": "Juan Pérez",
  "estado": "INACTIVO",
  "condicionPago": "Neto 30",
  "fechaCreacion": "2026-05-14T10:15:30",
  "fechaModificacion": "2026-05-14T10:45:00"
}
```

### Posibles errores

- `400 Bad Request` si `estado` viene vacio o no es uno de: `ACTIVO`, `INACTIVO`.
- `404 Not Found` si no existe proveedor con id `{id}`.

#### Ejemplo de error (ApiErrorResponse)

```json
{
  "timestamp": "2026-05-14T10:46:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Estado inválido. Valores válidos: ACTIVO, INACTIVO",
  "path": "/api/proveedores/1/estado"
}
```

## 6. PATCH `/api/proveedores/{id}`

Actualiza parcialmente los datos de un proveedor existente.

### Path params

Obligatorios:
- `id` (Long)

### Campos del body

Todos son opcionales, pero debe venir al menos uno:
- `nombre`
- `nit`
- `telefono`
- `email`
- `contacto`
- `condicionPago`

### Validacion importante

- `nombre`, si viene informado, no puede quedar vacío tras normalización.
- `nombre` sigue siendo único (comparación case-insensitive).
- Campos opcionales con texto vacío o solo espacios se guardan como `null`.
- Para condición de pago se aceptan `condicionPago`, `condicionDePago` y `condicion_pago`.

### Ejemplo de request

```json
{
  "nombre": "Farmacéutica XYZ Actualizada",
  "telefono": "+57 601 7654321",
  "email": "compras@farmaxyz.com",
  "contacto": "Ana Gómez",
  "condicionPago": "Contra Entrega"
}
```

### Ejemplo de uso

```http
PATCH /api/proveedores/1
```

### Respuesta (200)

```json
{
  "idProveedor": 1,
  "nombre": "Farmacéutica XYZ Actualizada",
  "nit": "860123456-7",
  "telefono": "+57 601 7654321",
  "email": "compras@farmaxyz.com",
  "contacto": "Ana Gómez",
  "estado": "ACTIVO",
  "condicionPago": "Contra Entrega",
  "fechaCreacion": "2026-05-14T10:15:30",
  "fechaModificacion": "2026-05-16T11:00:00"
}
```

## 7. POST `/api/proveedores/{id}/productos`

Crea una relación entre un proveedor y un producto. Si ya existía en estado `INACTIVO`, la reactiva y actualiza sus datos de referencia.

### Path params

Obligatorios:
- `id` (Long): id del proveedor

### Campos del body

Obligatorios:
- `productoId`

Opcionales:
- `codigoProductoProveedor`
- `precioReferencia`

### Validacion importante

- `precioReferencia` no puede ser negativo.
- Si la relación ya existe en estado `ACTIVO`, retorna error de negocio.

### Ejemplo de request

```json
{
  "productoId": 10,
  "codigoProductoProveedor": "PR-ACET-01",
  "precioReferencia": 8500.0
}
```

### Ejemplo de uso

```http
POST /api/proveedores/1/productos
```

### Respuesta (201)

```json
{
  "id": 10,
  "nombre": "Acetaminofen 500mg",
  "descripcion": "Caja por 20 tabletas",
  "codigoBarras": "7701234567890",
  "estado": "ACTIVO",
  "codigoProductoProveedor": "PR-ACET-01",
  "precioReferencia": 8500.0,
  "estadoRelacion": "ACTIVO"
}
```

## 8. PATCH `/api/proveedores/{id}/productos/{productoId}/estado`

Actualiza el estado de una relación proveedor-producto.

### Path params

Obligatorios:
- `id` (Long): id del proveedor
- `productoId` (Long): id del producto asociado

### Campos del body

Obligatorios:
- `estado`

### Validacion importante

- Valores válidos: `ACTIVO` o `INACTIVO`.

### Ejemplo de request

```json
{
  "estado": "INACTIVO"
}
```

### Ejemplo de uso

```http
PATCH /api/proveedores/1/productos/10/estado
```

### Respuesta (200)

```json
{
  "id": 10,
  "nombre": "Acetaminofen 500mg",
  "descripcion": "Caja por 20 tabletas",
  "codigoBarras": "7701234567890",
  "estado": "ACTIVO",
  "codigoProductoProveedor": "PR-ACET-01",
  "precioReferencia": 8500.0,
  "estadoRelacion": "INACTIVO"
}
```

## 9. DELETE `/api/proveedores/{id}/productos/{productoId}`

Elimina definitivamente la relación entre un proveedor y un producto.

### Path params

Obligatorios:
- `id` (Long): id del proveedor
- `productoId` (Long): id del producto asociado

### Ejemplo de uso

```http
DELETE /api/proveedores/1/productos/10
```

### Respuesta (204)

Sin body.

# CompraController endpoints

Referencia de integracion para `src/main/java/co/edu/unbosque/backend/controller/CompraController.java`.

Base path: `/api/compras`

## Resumen

| Method | Path | Description |
|---|---|---|
| POST | `/api/compras` | Registrar compra a proveedor. |
| GET | `/api/compras?idProveedor=...` | Listar compras, con filtro opcional por proveedor. |
| GET | `/api/compras/{id}` | Consultar compra por id. |

## 1. POST `/api/compras`

Registra una compra a proveedor con una o varias lineas de detalle.

### Campos del body

Obligatorios:
- `idProveedor`
- `usuarioResponsable`
- `detalles`

Opcionales:
- `numeroFactura`
- `notas`

Estructura de `detalles`:
- `idProducto` obligatorio
- `cantidad` obligatoria, minimo `1`
- `precioUnitario` obligatorio, no puede ser negativo

Validacion importante:
- `detalles` debe contener al menos un producto.
- El proveedor debe existir.
- Cada producto referenciado en `detalles` debe existir.
- `total` se calcula en backend como suma de `cantidad * precioUnitario` por cada linea.
- `fechaRecepcion` se genera en backend con la fecha-hora actual.

### Ejemplo de request

```json
{
  "idProveedor": 1,
  "usuarioResponsable": "admin",
  "numeroFactura": "FAC-2026-001",
  "notas": "Ingreso de medicamentos del pedido semanal",
  "detalles": [
    {
      "idProducto": 10,
      "cantidad": 20,
      "precioUnitario": 8500.0
    },
    {
      "idProducto": 11,
      "cantidad": 15,
      "precioUnitario": 12000.0
    }
  ]
}
```

### Ejemplo de uso

```http
POST /api/compras
```

### Respuesta (201)

Devuelve `CompraResponse` con el total calculado y el detalle registrado.

```json
{
  "id": 1,
  "idProveedor": 1,
  "nombreProveedor": "Farmacéutica XYZ S.A.",
  "usuarioResponsable": "admin",
  "numeroFactura": "FAC-2026-001",
  "notas": "Ingreso de medicamentos del pedido semanal",
  "fechaRecepcion": "2026-05-18T09:30:00",
  "total": 350000.0,
  "detalles": [
    {
      "idProducto": 10,
      "nombreProducto": "Acetaminofen 500mg",
      "cantidad": 20,
      "precioUnitario": 8500.0,
      "subtotal": 170000.0
    },
    {
      "idProducto": 11,
      "nombreProducto": "Amoxicilina 500mg",
      "cantidad": 15,
      "precioUnitario": 12000.0,
      "subtotal": 180000.0
    }
  ]
}
```

### Posibles errores

- `400 Bad Request` si faltan campos obligatorios, si `detalles` viene vacío, si `cantidad < 1` o si `precioUnitario` es negativo.
- `404 Not Found` si no existe el proveedor o alguno de los productos enviados.

## 2. GET `/api/compras?idProveedor=...`

Lista compras ordenadas por `fechaRecepcion` descendente.

### Query params

Opcionales:
- `idProveedor` para filtrar compras de un proveedor específico

Comportamiento:
- Si `idProveedor` no se envía, retorna todas las compras.
- Si `idProveedor` se envía, retorna solo las compras asociadas a ese proveedor.

### Ejemplos de uso

```http
GET /api/compras
```

```http
GET /api/compras?idProveedor=1
```

No lleva body.

### Respuesta (200)

```json
[
  {
    "id": 2,
    "idProveedor": 1,
    "nombreProveedor": "Farmacéutica XYZ S.A.",
    "usuarioResponsable": "admin",
    "numeroFactura": "FAC-2026-002",
    "notas": null,
    "fechaRecepcion": "2026-05-18T11:45:00",
    "total": 96000.0,
    "detalles": [
      {
        "idProducto": 12,
        "nombreProducto": "Ibuprofeno 400mg",
        "cantidad": 8,
        "precioUnitario": 12000.0,
        "subtotal": 96000.0
      }
    ]
  },
  {
    "id": 1,
    "idProveedor": 1,
    "nombreProveedor": "Farmacéutica XYZ S.A.",
    "usuarioResponsable": "admin",
    "numeroFactura": "FAC-2026-001",
    "notas": "Ingreso de medicamentos del pedido semanal",
    "fechaRecepcion": "2026-05-18T09:30:00",
    "total": 350000.0,
    "detalles": [
      {
        "idProducto": 10,
        "nombreProducto": "Acetaminofen 500mg",
        "cantidad": 20,
        "precioUnitario": 8500.0,
        "subtotal": 170000.0
      }
    ]
  }
]
```

## 3. GET `/api/compras/{id}`

Consulta una compra por id con su detalle completo.

### Path params

Obligatorios:
- `id` (Long)

### Ejemplo de uso

```http
GET /api/compras/1
```

No lleva body.

### Respuesta (200)

```json
{
  "id": 1,
  "idProveedor": 1,
  "nombreProveedor": "Farmacéutica XYZ S.A.",
  "usuarioResponsable": "admin",
  "numeroFactura": "FAC-2026-001",
  "notas": "Ingreso de medicamentos del pedido semanal",
  "fechaRecepcion": "2026-05-18T09:30:00",
  "total": 350000.0,
  "detalles": [
    {
      "idProducto": 10,
      "nombreProducto": "Acetaminofen 500mg",
      "cantidad": 20,
      "precioUnitario": 8500.0,
      "subtotal": 170000.0
    },
    {
      "idProducto": 11,
      "nombreProducto": "Amoxicilina 500mg",
      "cantidad": 15,
      "precioUnitario": 12000.0,
      "subtotal": 180000.0
    }
  ]
}
```

### Posibles errores

- `404 Not Found` si no existe la compra con id `{id}`.

## Notas de integracion

- Todas las solicitudes con body usan validación con `@Valid`.
- `usuarioResponsable` se exige como texto no vacío.
- Las respuestas usan `CompraResponse` y `DetalleCompraResponse`.
- Errores de recurso inexistente retornan `ResourceNotFoundException` con `404`.

# DevolucionController endpoints

Referencia de integracion para `src/main/java/co/edu/unbosque/backend/controller/DevolucionController.java`.

Base path: `/api/devoluciones`

## Resumen

| Method | Path | Description |
|---|---|---|
| POST | `/api/devoluciones` | Registrar devolución a proveedor. |
| GET | `/api/devoluciones` | Listar devoluciones. |
| GET | `/api/devoluciones/{id}` | Consultar devolución por id. |

## 1. POST `/api/devoluciones`

Registra una devolución de productos a proveedor y descuenta inventario por lote.

### Campos del body

Obligatorios:
- `idProveedor`
- `usuarioResponsable`
- `detalles`

Opcionales:
- `motivo`

Estructura de `detalles`:
- `idProducto` obligatorio
- `idLote` obligatorio
- `cantidad` obligatoria, minimo `1`

Validacion importante:
- `detalles` debe contener al menos un producto.
- El proveedor debe existir.
- El `usuarioResponsable` debe existir en el sistema.
- El lote debe existir.
- El producto debe existir.
- El lote debe pertenecer al producto indicado.
- La cantidad a devolver no puede superar el stock disponible en el lote.
- El backend descuenta stock tanto del producto como del lote y registra un movimiento de inventario tipo `DEVOLUCION`.

### Ejemplo de request

```json
{
  "idProveedor": 1,
  "usuarioResponsable": "admin",
  "motivo": "Producto próximo a vencer",
  "detalles": [
    {
      "idProducto": 10,
      "idLote": 100,
      "cantidad": 5
    },
    {
      "idProducto": 11,
      "idLote": 101,
      "cantidad": 2
    }
  ]
}
```

### Ejemplo de uso

```http
POST /api/devoluciones
```

### Respuesta (201)

Devuelve `DevolucionResponse` con el detalle de los productos devueltos.

```json
{
  "id": 1,
  "idProveedor": 1,
  "nombreProveedor": "Farmacéutica XYZ S.A.",
  "usuarioResponsable": "admin",
  "motivo": "Producto próximo a vencer",
  "fecha": "2026-05-18T14:10:00",
  "detalles": [
    {
      "idProducto": 10,
      "nombreProducto": "Acetaminofen 500mg",
      "numeroLote": "LOT-ACET-01",
      "cantidad": 5
    },
    {
      "idProducto": 11,
      "nombreProducto": "Amoxicilina 500mg",
      "numeroLote": "LOT-AMX-02",
      "cantidad": 2
    }
  ]
}
```

### Posibles errores

- `400 Bad Request` si faltan campos obligatorios o si `cantidad < 1`.
- `400 Bad Request` si el lote no pertenece al producto indicado.
- `400 Bad Request` si no hay stock suficiente en el lote.
- `404 Not Found` si no existe proveedor, usuario, lote o producto.

## 2. GET `/api/devoluciones`

Lista todas las devoluciones ordenadas por `fecha` descendente.

### Ejemplo de uso

```http
GET /api/devoluciones
```

No lleva body.

### Respuesta (200)

```json
[
  {
    "id": 2,
    "idProveedor": 1,
    "nombreProveedor": "Farmacéutica XYZ S.A.",
    "usuarioResponsable": "admin",
    "motivo": "Empaque deteriorado",
    "fecha": "2026-05-18T15:00:00",
    "detalles": [
      {
        "idProducto": 12,
        "nombreProducto": "Ibuprofeno 400mg",
        "numeroLote": "LOT-IBU-01",
        "cantidad": 3
      }
    ]
  },
  {
    "id": 1,
    "idProveedor": 1,
    "nombreProveedor": "Farmacéutica XYZ S.A.",
    "usuarioResponsable": "admin",
    "motivo": "Producto próximo a vencer",
    "fecha": "2026-05-18T14:10:00",
    "detalles": [
      {
        "idProducto": 10,
        "nombreProducto": "Acetaminofen 500mg",
        "numeroLote": "LOT-ACET-01",
        "cantidad": 5
      }
    ]
  }
]
```

## 3. GET `/api/devoluciones/{id}`

Consulta una devolución por id con sus detalles.

### Path params

Obligatorios:
- `id` (Long)

### Ejemplo de uso

```http
GET /api/devoluciones/1
```

No lleva body.

### Respuesta (200)

```json
{
  "id": 1,
  "idProveedor": 1,
  "nombreProveedor": "Farmacéutica XYZ S.A.",
  "usuarioResponsable": "admin",
  "motivo": "Producto próximo a vencer",
  "fecha": "2026-05-18T14:10:00",
  "detalles": [
    {
      "idProducto": 10,
      "nombreProducto": "Acetaminofen 500mg",
      "numeroLote": "LOT-ACET-01",
      "cantidad": 5
    },
    {
      "idProducto": 11,
      "nombreProducto": "Amoxicilina 500mg",
      "numeroLote": "LOT-AMX-02",
      "cantidad": 2
    }
  ]
}
```

### Posibles errores

- `404 Not Found` si no existe la devolución con id `{id}`.

## Notas de integracion

- Todas las solicitudes con body usan validación con `@Valid`.
- Las respuestas usan `DevolucionResponse` y `DetalleDevolucionResponse`.
- Este flujo afecta inventario: reduce `stockActual` del producto y `cantidad` del lote.
- También genera un movimiento de inventario con `tipoMovimiento = "DEVOLUCION"` y una referencia documental tipo `DEV-PROV-{id}`.

# OrdenCompraController endpoints

Referencia de integracion para `src/main/java/co/edu/unbosque/backend/controller/OrdenCompraController.java`.

Base path: `/api/ordenes-compra`

## Resumen

| Method | Path | Description |
|---|---|---|
| POST | `/api/ordenes-compra` | Crear orden de compra. |
| POST | `/api/ordenes-compra/{ordenId}/detalles` | Agregar detalle a una orden de compra pendiente. |
| GET | `/api/ordenes-compra/{id}` | Consultar orden de compra por id. |
| GET | `/api/ordenes-compra/resumen-seguimiento` | Obtener resumen de alertas y seguimiento de compras. |
| GET | `/api/ordenes-compra/pendientes` | Listar órdenes pendientes. |
| GET | `/api/ordenes-compra/proveedor/{proveedorId}` | Listar órdenes por proveedor. |
| GET | `/api/ordenes-compra` | Listar todas las órdenes. |
| GET | `/api/ordenes-compra/previsualizar-propuesta/{idProveedor}` | Generar previsualización automática de una orden sugerida. |

## 1. POST `/api/ordenes-compra`

Crea una orden de compra en estado inicial `PENDIENTE`.

### Campos del body

Obligatorios:
- `proveedorId`

Opcionales:
- `fechaEsperada`
- `totalEsperado`
- `observaciones`

Validacion importante:
- `proveedorId` debe existir.
- Si `totalEsperado` se envía, debe ser mayor a `0`.
- Si `totalEsperado` no se envía, el backend guarda `0.0`.
- `fechaPedido` se genera en backend con la fecha-hora actual.
- El usuario asociado a la orden se toma del usuario autenticado actual.
- La orden se crea con `estado = "PENDIENTE"`.

### Ejemplo de request

```json
{
  "proveedorId": 1,
  "fechaEsperada": "2026-05-25T00:00:00",
  "totalEsperado": 50000.0,
  "observaciones": "Envío urgente requerido"
}
```

### Ejemplo de uso

```http
POST /api/ordenes-compra
```

### Respuesta (201)

Devuelve `OrdenCompraResponse`.

```json
{
  "idOrden": 1,
  "proveedor": {
    "idProveedor": 1,
    "nombre": "Farmacéutica XYZ S.A.",
    "nit": "860123456-7",
    "telefono": "+57 1 1234567",
    "email": "contacto@farmaxyza.com",
    "contacto": "Juan Pérez",
    "estado": "ACTIVO",
    "condicionPago": "Neto 30",
    "fechaCreacion": "2026-05-10T08:00:00",
    "fechaModificacion": "2026-05-10T08:00:00"
  },
  "usuario": {
    "id": 3,
    "username": "admin",
    "nombreCompleto": "Administrador General",
    "rol": "ADMIN",
    "estado": "ACTIVO"
  },
  "fechaPedido": "2026-05-18T16:00:00",
  "fechaEsperada": "2026-05-25T00:00:00",
  "estado": "PENDIENTE",
  "totalEsperado": 50000.0,
  "observaciones": "Envío urgente requerido",
  "detalles": [],
  "fechaCreacion": "2026-05-18T16:00:00",
  "fechaModificacion": "2026-05-18T16:00:00"
}
```

### Posibles errores

- `400 Bad Request` si falta `proveedorId` o si `totalEsperado <= 0`.
- `404 Not Found` si no existe el proveedor.
- `404 Not Found` si no se encuentra el usuario autenticado actual.

## 2. POST `/api/ordenes-compra/{ordenId}/detalles`

Agrega una línea de detalle a una orden de compra existente.

### Path params

Obligatorios:
- `ordenId` (Long)

### Campos del body

Obligatorios:
- `productoId`
- `cantidadPedida`
- `precioUnitarioPactado`

Opcionales:
- `nombreProducto`
- `descripcionProducto`

Validacion importante:
- La orden debe existir.
- Solo se pueden agregar detalles si la orden está en estado `PENDIENTE`.
- El producto debe existir.
- `cantidadPedida` debe ser mayor a `0`.
- `precioUnitarioPactado` debe ser mayor a `0`.
- Al guardar el detalle, el backend incrementa `totalEsperado` de la orden con `cantidadPedida * precioUnitarioPactado`.

### Ejemplo de request

```json
{
  "productoId": 5,
  "nombreProducto": "Dipirona 500mg",
  "descripcionProducto": "Caja por 20 tabletas",
  "cantidadPedida": 100,
  "precioUnitarioPactado": 150.0
}
```

### Ejemplo de uso

```http
POST /api/ordenes-compra/1/detalles
```

### Respuesta (201)

Devuelve `DetalleOrdenCompraResponse`.

```json
{
  "idDetalle": 10,
  "productoId": 5,
  "nombreProducto": "Dipirona 500mg",
  "descripcionProducto": "Caja por 20 tabletas",
  "cantidadPedida": 100,
  "precioUnitarioPactado": 150.0,
  "subtotal": 15000.0
}
```

### Posibles errores

- `400 Bad Request` si faltan campos obligatorios o si `cantidadPedida <= 0` o `precioUnitarioPactado <= 0`.
- `400 Bad Request` si la orden no está en estado `PENDIENTE`.
- `404 Not Found` si no existe la orden o el producto.

## 3. GET `/api/ordenes-compra/{id}`

Consulta una orden de compra por id con su detalle completo.

### Path params

Obligatorios:
- `id` (Long)

### Ejemplo de uso

```http
GET /api/ordenes-compra/1
```

No lleva body.

### Respuesta (200)

```json
{
  "idOrden": 1,
  "proveedor": {
    "idProveedor": 1,
    "nombre": "Farmacéutica XYZ S.A.",
    "nit": "860123456-7",
    "telefono": "+57 1 1234567",
    "email": "contacto@farmaxyza.com",
    "contacto": "Juan Pérez",
    "estado": "ACTIVO",
    "condicionPago": "Neto 30",
    "fechaCreacion": "2026-05-10T08:00:00",
    "fechaModificacion": "2026-05-10T08:00:00"
  },
  "usuario": {
    "id": 3,
    "username": "admin",
    "nombreCompleto": "Administrador General",
    "rol": "ADMIN",
    "estado": "ACTIVO"
  },
  "fechaPedido": "2026-05-18T16:00:00",
  "fechaEsperada": "2026-05-25T00:00:00",
  "estado": "PENDIENTE",
  "totalEsperado": 65000.0,
  "observaciones": "Envío urgente requerido",
  "detalles": [
    {
      "idDetalle": 10,
      "productoId": 5,
      "nombreProducto": "Dipirona 500mg",
      "descripcionProducto": "Caja por 20 tabletas",
      "cantidadPedida": 100,
      "precioUnitarioPactado": 150.0,
      "subtotal": 15000.0
    }
  ],
  "fechaCreacion": "2026-05-18T16:00:00",
  "fechaModificacion": "2026-05-18T16:15:00"
}
```

### Posibles errores

- `404 Not Found` si no existe la orden de compra con id `{id}`.

## 4. GET `/api/ordenes-compra/resumen-seguimiento`

Retorna un resumen agregado para seguimiento de compras y una lista de alertas detalladas.

### Comportamiento

- `totalOrdenes`: cantidad total de órdenes existentes.
- `pendientesRecibirOPagar`: cuenta órdenes con `estado = "PENDIENTE"`.
- `vencidasAtrasadas`: cuenta órdenes con `estado = "NO_RECIBIDA"`.
- `alertasDetalladas`: incluye únicamente órdenes en estado `PENDIENTE` o `NO_RECIBIDA`.
- `codigoGenerado` se construye como `OC-{año}-{id con 3 dígitos}`.
- `fechaCreacionFormateada` se entrega en formato `dd/MM/yyyy`.
- `diasTranscurridos` se calcula desde `fechaPedido` hasta la fecha actual del servidor.

### Ejemplo de uso

```http
GET /api/ordenes-compra/resumen-seguimiento
```

No lleva body.

### Respuesta (200)

```json
{
  "totalOrdenes": 4,
  "pendientesRecibirOPagar": 2,
  "vencidasAtrasadas": 1,
  "alertasDetalladas": [
    {
      "idOrden": 1,
      "codigoGenerado": "OC-2026-001",
      "proveedorNombre": "Farmacéutica XYZ S.A.",
      "estadoActual": "PENDIENTE",
      "fechaCreacionFormateada": "18/05/2026",
      "diasTranscurridos": 0
    },
    {
      "idOrden": 2,
      "codigoGenerado": "OC-2026-002",
      "proveedorNombre": "Proveedor ABC",
      "estadoActual": "NO_RECIBIDA",
      "fechaCreacionFormateada": "10/05/2026",
      "diasTranscurridos": 8
    }
  ]
}
```

## 5. GET `/api/ordenes-compra/pendientes`

Lista órdenes de compra con estado `PENDIENTE`, ordenadas por `fechaPedido` descendente.

### Ejemplo de uso

```http
GET /api/ordenes-compra/pendientes
```

No lleva body.

### Respuesta (200)

Devuelve una lista de `OrdenCompraResumenResponse`.

```json
[
  {
    "idOrden": 3,
    "proveedorId": 2,
    "proveedorNombre": "Proveedor ABC",
    "fechaPedido": "2026-05-18T17:00:00",
    "fechaEsperada": "2026-05-24T00:00:00",
    "estado": "PENDIENTE",
    "totalEsperado": 180000.0
  },
  {
    "idOrden": 1,
    "proveedorId": 1,
    "proveedorNombre": "Farmacéutica XYZ S.A.",
    "fechaPedido": "2026-05-18T16:00:00",
    "fechaEsperada": "2026-05-25T00:00:00",
    "estado": "PENDIENTE",
    "totalEsperado": 65000.0
  }
]
```

## 6. GET `/api/ordenes-compra/proveedor/{proveedorId}`

Lista órdenes de compra de un proveedor específico, ordenadas por `fechaPedido` descendente.

### Path params

Obligatorios:
- `proveedorId` (Long)

### Ejemplo de uso

```http
GET /api/ordenes-compra/proveedor/1
```

No lleva body.

### Respuesta (200)

```json
[
  {
    "idOrden": 4,
    "proveedorId": 1,
    "proveedorNombre": "Farmacéutica XYZ S.A.",
    "fechaPedido": "2026-05-19T09:00:00",
    "fechaEsperada": "2026-05-26T00:00:00",
    "estado": "RECIBIDA",
    "totalEsperado": 210000.0
  },
  {
    "idOrden": 1,
    "proveedorId": 1,
    "proveedorNombre": "Farmacéutica XYZ S.A.",
    "fechaPedido": "2026-05-18T16:00:00",
    "fechaEsperada": "2026-05-25T00:00:00",
    "estado": "PENDIENTE",
    "totalEsperado": 65000.0
  }
]
```

## 7. GET `/api/ordenes-compra`

Lista todas las órdenes de compra.

Nota: este endpoint retorna `findAll()` y no garantiza orden específico.

### Ejemplo de uso

```http
GET /api/ordenes-compra
```

No lleva body.

### Respuesta (200)

```json
[
  {
    "idOrden": 1,
    "proveedorId": 1,
    "proveedorNombre": "Farmacéutica XYZ S.A.",
    "fechaPedido": "2026-05-18T16:00:00",
    "fechaEsperada": "2026-05-25T00:00:00",
    "estado": "PENDIENTE",
    "totalEsperado": 65000.0
  },
  {
    "idOrden": 2,
    "proveedorId": 2,
    "proveedorNombre": "Proveedor ABC",
    "fechaPedido": "2026-05-10T08:30:00",
    "fechaEsperada": "2026-05-15T00:00:00",
    "estado": "NO_RECIBIDA",
    "totalEsperado": 98000.0
  }
]
```

## 8. GET `/api/ordenes-compra/previsualizar-propuesta/{idProveedor}`

Genera una previsualización automática de orden sugerida para un proveedor, basada en productos activos con stock bajo.

### Path params

Obligatorios:
- `idProveedor` (Long)

### Comportamiento

- El proveedor debe existir.
- Solo considera productos con `estado = "ACTIVO"`.
- Solo considera productos cuyo `stockActual <= stockMinimo`.
- La `cantidadSugerida` se calcula como `(stockMinimo * 2) - stockActual`.
- `precioUnitario` toma el `costo` actual del producto.
- `subtotal` se calcula como `cantidadSugerida * precioUnitario`.
- `totalEstimado` es la suma de los subtotales sugeridos.
- El motivo de cada ítem es fijo: `Recomendación automática por stock bajo`.

### Ejemplo de uso

```http
GET /api/ordenes-compra/previsualizar-propuesta/1
```

No lleva body.

### Respuesta (200)

```json
{
  "proveedorId": 1,
  "proveedorNombre": "Farmacéutica XYZ S.A.",
  "items": [
    {
      "productoId": 10,
      "nombre": "Acetaminofen 500mg",
      "cantidadSugerida": 15,
      "precioUnitario": 8500.0,
      "subtotal": 127500.0,
      "motivo": "Recomendación automática por stock bajo"
    },
    {
      "productoId": 11,
      "nombre": "Amoxicilina 500mg",
      "cantidadSugerida": 8,
      "precioUnitario": 15000.0,
      "subtotal": 120000.0,
      "motivo": "Recomendación automática por stock bajo"
    }
  ],
  "totalEstimado": 247500.0
}
```

### Posibles errores

- `404 Not Found` si no existe el proveedor con id `{idProveedor}`.

## Notas de integracion

- Los endpoints con body usan validación con `@Valid`.
- `OrdenCompraResponse` incluye proveedor, usuario, detalles y campos de auditoría.
- `OrdenCompraResumenResponse` es una vista resumida para listados.
- El subtotal de cada detalle se calcula en backend como `cantidadPedida * precioUnitarioPactado`.
- El usuario de creación no se recibe por request: se resuelve desde el contexto de autenticación actual.
