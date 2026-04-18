-- =====================================================
-- CONFIGURACIÓN INICIAL
-- =====================================================
PRAGMA foreign_keys = ON;

-- =====================================================
-- TABLA: usuario (NUEVA - Necesaria para auditoría)
-- =====================================================
CREATE TABLE usuario
(
    id_usuario       INTEGER PRIMARY KEY AUTOINCREMENT,
    username         TEXT NOT NULL UNIQUE,
    password_hash    TEXT NOT NULL,
    nombre_completo  TEXT NOT NULL,
    rol              TEXT NOT NULL DEFAULT 'VENDEDOR' CHECK (rol IN ('ADMIN', 'REGENTE', 'VENDEDOR', 'ALMACENISTA')),
    estado           TEXT NOT NULL DEFAULT 'ACTIVO' CHECK (estado IN ('ACTIVO', 'INACTIVO')),
    fecha_creacion   TEXT DEFAULT (datetime('now', 'localtime')),
    fecha_modificacion TEXT,
    ultimo_acceso    TEXT
);

-- =====================================================
-- TABLA: categoria
-- =====================================================

CREATE TABLE categoria (
    id_categoria  INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre        TEXT NOT NULL UNIQUE,
    descripcion   TEXT,
    fecha_creacion     TEXT DEFAULT (datetime('now', 'localtime')),
    fecha_modificacion TEXT
);

-- =====================================================
-- TABLA: producto
-- =====================================================
CREATE TABLE producto
(
    UniqueID           INTEGER PRIMARY KEY AUTOINCREMENT,
    id_categoria       INTEGER NOT NULL,
    nombre             TEXT    NOT NULL,
    descripcion        TEXT,
    codigo_barras      TEXT UNIQUE,
    stock_minimo       INTEGER          DEFAULT 0,
    stock_actual       INTEGER          DEFAULT 0,
    costo              REAL    NOT NULL DEFAULT 0,
    precio_venta       REAL    NOT NULL DEFAULT 0,
    margen_ganancia    REAL,
    estado             TEXT    NOT NULL DEFAULT 'ACTIVO' CHECK (estado IN ('ACTIVO', 'INACTIVO', 'DESCONTINUADO')),
    -- Columnes de Auditoría Automática (Spring Data JPA)
    fecha_creacion     TEXT DEFAULT (datetime('now', 'localtime')),
    usuario_creacion   TEXT,
    fecha_modificacion TEXT,
    usuario_modificacion TEXT,
    FOREIGN KEY (id_categoria) REFERENCES categoria (id_categoria)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

-- =====================================================
-- TABLA: lote (Crítico en Farmacia)
-- =====================================================
CREATE TABLE lote
(
    id_lote            INTEGER PRIMARY KEY AUTOINCREMENT,
    numero_lote        TEXT    NOT NULL,
    fecha_vencimiento  TEXT,
    cantidad           INTEGER NOT NULL DEFAULT 0,
    id_producto        INTEGER NOT NULL,
    fecha_creacion      TEXT DEFAULT (datetime('now', 'localtime')),
--  ubicacion          TEXT,
    -- Auditoría
    usuario_creacion   TEXT,
    fecha_modificacion TEXT,
    usuario_modificacion TEXT,
    FOREIGN KEY (id_producto) REFERENCES producto (UniqueID)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- =====================================================
-- TABLA: movimiento_inventario (NUEVA - Auditoría de Negocio)
-- =====================================================
CREATE TABLE movimiento_inventario
(
    id_movimiento      INTEGER PRIMARY KEY AUTOINCREMENT,
    id_producto        INTEGER NOT NULL,
    nombre_producto    TEXT NOT NULL, -- Snapshot por si se borra el producto
    id_lote            INTEGER,       -- Opcional, si aplica al lote
    tipo_movimiento    TEXT NOT NULL CHECK (tipo_movimiento IN ('COMPRA', 'VENTA', 'AJUSTE', 'MERMA', 'ROBO', 'DEVOLUCION', 'VENCIMIENTO')),
    cantidad_anterior  INTEGER NOT NULL,
    cantidad_nueva     INTEGER NOT NULL,
    diferencia         INTEGER NOT NULL, -- Positivo (entrada) o Negativo (salida)
    motivo             TEXT,             -- Ej: "Factura #123", "Producto dañado"
    referencia_documento TEXT,           -- ID de venta o compra
    usuario_responsable TEXT NOT NULL,
    fecha_movimiento   TEXT DEFAULT (datetime('now', 'localtime')),
    FOREIGN KEY (id_producto) REFERENCES producto (UniqueID)
        ON UPDATE CASCADE
        ON DELETE SET NULL
);

-- =====================================================
-- TABLA: auditoria_sistema (Eventos Técnicos / Accesos) |||| FALTA POR REVISAR !!!!!!!
-- =====================================================
CREATE TABLE auditoria_sistema
(
    id_auditoria       INTEGER PRIMARY KEY AUTOINCREMENT,
    id_usuario         INTEGER,
    accion             TEXT NOT NULL, -- 'LOGIN', 'LOGOUT', 'EXPORTAR_REPORTE', 'CAMBIO_CLAVE'
    tabla_afectada     TEXT,
    registro_id        INTEGER,
    detalles           TEXT,
    ip_origen          TEXT,
    fecha_hora         TEXT DEFAULT (datetime('now', 'localtime')),
    FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
        ON UPDATE CASCADE
        ON DELETE SET NULL
);


CREATE TABLE historial_precio_producto
(
    id_historial        INTEGER PRIMARY KEY AUTOINCREMENT,
    id_producto         INTEGER NOT NULL,
    nombre_producto     TEXT NOT NULL,          -- snapshot, igual que en movimiento_inventario
    precio_anterior     REAL NOT NULL,
    precio_nuevo        REAL NOT NULL,
    costo_anterior      REAL,                   -- opcional pero útil para margen
    costo_nuevo         REAL,
    motivo              TEXT,                   -- "ajuste proveedor", "promoción", etc.
    usuario_responsable TEXT NOT NULL,
    fecha_cambio        TEXT DEFAULT (datetime('now', 'localtime')),
    FOREIGN KEY (id_producto) REFERENCES producto (UniqueID)
        ON UPDATE CASCADE ON DELETE SET NULL
);

-- =====================================================
-- TABLA: venta
-- =====================================================
CREATE TABLE venta
(
    id_venta           INTEGER PRIMARY KEY AUTOINCREMENT,
    id_usuario         INTEGER NOT NULL,
    fecha              TEXT    NOT NULL DEFAULT (datetime('now', 'localtime')),
    estado             TEXT    NOT NULL DEFAULT 'COMPLETADA'
                           CHECK (estado IN ('COMPLETADA', 'ANULADA')),
    motivo_anulacion   TEXT,
    subtotal           REAL    NOT NULL DEFAULT 0,
    descuento          REAL    NOT NULL DEFAULT 0,
    total              REAL    NOT NULL DEFAULT 0,
    -- Auditoría JPA
    usuario_creacion      TEXT,
    fecha_modificacion    TEXT,
    usuario_modificacion  TEXT,
    FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

-- =====================================================
-- TABLA: detalle_venta
-- =====================================================
CREATE TABLE detalle_venta
(
    id_detalle              INTEGER PRIMARY KEY AUTOINCREMENT,
    id_venta                INTEGER NOT NULL,
    id_producto             INTEGER NOT NULL,
    id_lote                 INTEGER NOT NULL,
    cantidad                INTEGER NOT NULL CHECK (cantidad > 0),
    precio_unitario_aplicado REAL   NOT NULL,
    subtotal_linea          REAL    NOT NULL,
    FOREIGN KEY (id_venta)    REFERENCES venta (id_venta)
        ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (id_producto) REFERENCES producto (UniqueID)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    FOREIGN KEY (id_lote)     REFERENCES lote (id_lote)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

-- =====================================================
-- TABLA: pago_venta
-- =====================================================
CREATE TABLE pago_venta
(
    id_pago   INTEGER PRIMARY KEY AUTOINCREMENT,
    id_venta  INTEGER NOT NULL,
    tipo      TEXT    NOT NULL CHECK (tipo IN ('EFECTIVO', 'TARJETA')),
    monto     REAL    NOT NULL CHECK (monto > 0),
    FOREIGN KEY (id_venta) REFERENCES venta (id_venta)
        ON UPDATE CASCADE ON DELETE CASCADE
);
```
Algunas decisiones de diseño que vale la pena explicar:

**Por qué `subtotal`, `descuento` y `total` en `venta` y además `subtotal_linea` en `detalle_venta`**

El descuento es global sobre el total, así que no vive en el detalle — vive en la cabecera.
Pero `subtotal_linea` en cada ítem te permite reconstruir el ticket completo sin recalcular, y es lo que necesitas para
los reportes de productos más vendidos. La relación es siempre: `subtotal = SUM(subtotal_linea)`,
`total = subtotal - descuento`. Esa consistencia la validas en el servicio antes de guardar.

**Por qué `id_lote` es NOT NULL en `detalle_venta`**

Dijiste que el vendedor puede vender de distintos lotes, lo que significa que el lote sí se captura por línea.
Si un producto viene de dos lotes diferentes en la misma venta, son dos filas en `detalle_venta` — una por cada lote.
Esto además permite que `movimiento_inventario` reciba la referencia exacta del lote afectado.

**Por qué `detalle_venta` no tiene campos de auditoría JPA**

Es una tabla de detalle inmutable — una vez creada la venta, las líneas no se modifican.
Si la venta se anula, el estado cambia en `venta` y el servicio revierte el stock,
pero las líneas quedan como registro histórico intacto. No hay nada que auditar a nivel de fila individual.

**El flujo completo en una transacción**

Cuando el servicio confirme una venta, todo esto ocurre junto o nada:

1. INSERT venta (estado = COMPLETADA)
2. Por cada línea del ticket:
   a. INSERT detalle_venta (id_lote, cantidad, precio_unitario_aplicado, subtotal_linea)
   b. UPDATE lote SET cantidad = cantidad - X WHERE id_lote = ?
   c. UPDATE producto SET stock_actual = stock_actual - X WHERE UniqueID = ?
   d. INSERT movimiento_inventario (tipo = VENTA, id_lote, diferencia = -X, referencia_documento = id_venta)
3. INSERT pago_venta (una o dos filas según sea simple o mixto)
```
-- =====================================================
-- ÍNDICES PARA MEJORAR RENDIMIENTO
-- =====================================================
-- PRODUCTO
CREATE INDEX idx_producto_categoria ON producto (id_categoria);
CREATE INDEX idx_producto_codigo ON producto (codigo_barras);
CREATE INDEX idx_producto_estado ON producto (estado);
-- LOTE
CREATE INDEX idx_lote_producto ON lote (id_producto);
CREATE INDEX idx_lote_vencimiento ON lote (fecha_vencimiento);
-- MOVIMIENTO INVENTARIO
CREATE INDEX idx_movimiento_producto ON movimiento_inventario (id_producto);
CREATE INDEX idx_movimiento_fecha ON movimiento_inventario (fecha_movimiento);
CREATE INDEX idx_movimiento_tipo ON movimiento_inventario (tipo_movimiento);
-- AUDITORIA SISTEMA
CREATE INDEX idx_auditoria_usuario ON auditoria_sistema (id_usuario);
CREATE INDEX idx_auditoria_fecha ON auditoria_sistema (fecha_hora);
-- HISTORIAL PRECIO PRODUCTO
CREATE INDEX idx_historial_precio_producto ON historial_precio_producto (id_producto);
CREATE INDEX idx_historial_precio_fecha    ON historial_precio_producto (fecha_cambio);
-- VENTA
CREATE INDEX idx_venta_usuario  ON venta (id_usuario);
CREATE INDEX idx_venta_fecha    ON venta (fecha);
CREATE INDEX idx_venta_estado   ON venta (estado);
-- DETALLE VENTA
CREATE INDEX idx_detalle_venta  ON detalle_venta (id_venta);
CREATE INDEX idx_detalle_producto ON detalle_venta (id_producto);
CREATE INDEX idx_detalle_lote   ON detalle_venta (id_lote);
-- PAGO VENTA
CREATE INDEX idx_pago_venta     ON pago_venta (id_venta);

-- =====================================================
-- TABLA: configuracion_alerta (singleton, id siempre = 1)
-- =====================================================
CREATE TABLE IF NOT EXISTS configuracion_alerta (
    id                       INTEGER PRIMARY KEY,
    dias_proximo_vencimiento INTEGER NOT NULL DEFAULT 30
);
INSERT OR IGNORE INTO configuracion_alerta (id, dias_proximo_vencimiento) VALUES (1, 30);

-- =====================================================
-- TABLA: alerta_inventario
-- =====================================================
CREATE TABLE IF NOT EXISTS alerta_inventario (
    id_alerta          INTEGER PRIMARY KEY AUTOINCREMENT,
    tipo               TEXT    NOT NULL CHECK (tipo IN ('STOCK_MINIMO', 'PROXIMO_VENCIMIENTO')),
    id_producto        INTEGER NOT NULL,
    nombre_producto    TEXT    NOT NULL,
    id_lote            INTEGER,
    numero_lote        TEXT,
    cantidad_actual    INTEGER,
    stock_minimo       INTEGER,
    fecha_vencimiento  TEXT,
    leida              INTEGER NOT NULL DEFAULT 0,
    fecha_generacion   TEXT    NOT NULL,
    FOREIGN KEY (id_producto) REFERENCES producto (UniqueID) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (id_lote)     REFERENCES lote (id_lote)      ON UPDATE CASCADE ON DELETE SET NULL
);
CREATE INDEX IF NOT EXISTS idx_alerta_leida   ON alerta_inventario (leida);
CREATE INDEX IF NOT EXISTS idx_alerta_tipo    ON alerta_inventario (tipo);
CREATE INDEX IF NOT EXISTS idx_alerta_producto ON alerta_inventario (id_producto);
