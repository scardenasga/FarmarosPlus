-- =====================================================
-- FARMAROSPLUS - ESQUEMA DE BASE DE DATOS ACTUALIZADO
-- Basado en schema.sql + modificaciones de entidades JPA
-- =====================================================

PRAGMA foreign_keys = OFF;

-- =====================================================
-- ELIMINAR TABLAS EXISTENTES (EN ORDEN POR DEPENDENCIAS)
-- =====================================================
DROP TABLE IF EXISTS pago_venta;
DROP TABLE IF EXISTS detalle_venta;
DROP TABLE IF EXISTS venta;
DROP TABLE IF EXISTS movimiento_inventario;
DROP TABLE IF EXISTS historial_precio_producto;
DROP TABLE IF EXISTS lote;
DROP TABLE IF EXISTS producto;
DROP TABLE IF EXISTS categoria;
DROP TABLE IF EXISTS auditoria_sistema;
DROP TABLE IF EXISTS usuario;

-- =====================================================
-- CONFIGURACIÓN INICIAL
-- =====================================================
PRAGMA foreign_keys = ON;

-- =====================================================
-- TABLA: usuario
-- =====================================================
CREATE TABLE usuario
(
    id_usuario       INTEGER PRIMARY KEY AUTOINCREMENT,
    username         TEXT NOT NULL UNIQUE,
    password_hash    TEXT NOT NULL,
    nombre_completo  TEXT NOT NULL,
    rol              TEXT NOT NULL DEFAULT 'VENDEDOR' CHECK (rol IN ('ADMIN', 'REGENTE', 'VENDEDOR', 'ALMACENISTA')),
    estado           TEXT NOT NULL DEFAULT 'ACTIVO' CHECK (estado IN ('ACTIVO', 'INACTIVO')),
    fecha_creacion   TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    fecha_modificacion TEXT,
    ultimo_acceso    TEXT,
    usuario_creacion TEXT,
    usuario_modificacion TEXT
);

-- =====================================================
-- TABLA: categoria
-- =====================================================
CREATE TABLE categoria
(
    id_categoria       INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre             TEXT NOT NULL UNIQUE,
    descripcion        TEXT,
    fecha_creacion     TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    fecha_modificacion TEXT,
    usuario_creacion   TEXT,
    usuario_modificacion TEXT
);

-- =====================================================
-- TABLA: producto (ACTUALIZADO)
-- Cambios: id_categoria ahora es nullable, agregado porcentaje_iva
-- =====================================================
CREATE TABLE producto
(
    UniqueID           INTEGER PRIMARY KEY AUTOINCREMENT,
    id_categoria       INTEGER,                               -- Nullable según entidad JPA
    nombre             TEXT NOT NULL,
    descripcion        TEXT,
    codigo_barras      TEXT UNIQUE,
    stock_minimo       INTEGER DEFAULT 0,
    stock_actual       INTEGER DEFAULT 0,
    costo              REAL NOT NULL DEFAULT 0,
    precio_venta       REAL NOT NULL DEFAULT 0,
    margen_ganancia    REAL,
    porcentaje_iva     REAL NOT NULL DEFAULT 0,              -- NUEVO: % IVA aplicable (0.0 para medicamentos, 19.0 para otros)
    estado             TEXT NOT NULL DEFAULT 'ACTIVO' CHECK (estado IN ('ACTIVO', 'INACTIVO', 'DESCONTINUADO')),
    fecha_creacion     TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    usuario_creacion   TEXT,
    fecha_modificacion TEXT,
    usuario_modificacion TEXT,
    FOREIGN KEY (id_categoria) REFERENCES categoria (id_categoria)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

-- =====================================================
-- TABLA: lote
-- =====================================================
CREATE TABLE lote
(
    id_lote            INTEGER PRIMARY KEY AUTOINCREMENT,
    numero_lote        TEXT NOT NULL,
    fecha_vencimiento  TEXT,
    cantidad           INTEGER NOT NULL DEFAULT 0,
    id_producto        INTEGER NOT NULL,
    fecha_creacion     TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    fecha_modificacion TEXT,
    usuario_creacion   TEXT,
    usuario_modificacion TEXT,
    FOREIGN KEY (id_producto) REFERENCES producto (UniqueID)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- =====================================================
-- TABLA: movimiento_inventario
-- =====================================================
CREATE TABLE movimiento_inventario
(
    id_movimiento      INTEGER PRIMARY KEY AUTOINCREMENT,
    id_producto        INTEGER,
    nombre_producto    TEXT NOT NULL,                         -- Snapshot por si se borra el producto
    id_lote            INTEGER,                               -- Opcional, si aplica al lote
    tipo_movimiento    TEXT NOT NULL CHECK (tipo_movimiento IN ('COMPRA', 'VENTA', 'AJUSTE', 'MERMA', 'ROBO', 'DEVOLUCION', 'VENCIMIENTO')),
    cantidad_anterior  INTEGER NOT NULL,
    cantidad_nueva     INTEGER NOT NULL,
    diferencia         INTEGER NOT NULL,                       -- Positivo (entrada) o Negativo (salida)
    motivo             TEXT,                                   -- Ej: "Factura #123", "Producto dañado"
    referencia_documento TEXT,                                 -- ID de venta o compra
    usuario_responsable TEXT NOT NULL,
    fecha_movimiento   TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    FOREIGN KEY (id_producto) REFERENCES producto (UniqueID)
        ON UPDATE CASCADE
        ON DELETE SET NULL,
    FOREIGN KEY (id_lote) REFERENCES lote (id_lote)
        ON UPDATE CASCADE
        ON DELETE SET NULL
);

-- =====================================================
-- TABLA: historial_precio_producto
-- =====================================================
CREATE TABLE historial_precio_producto
(
    id_historial        INTEGER PRIMARY KEY AUTOINCREMENT,
    id_producto         INTEGER,
    nombre_producto     TEXT NOT NULL,                        -- Snapshot del nombre al momento del cambio
    precio_anterior     REAL NOT NULL,
    precio_nuevo        REAL NOT NULL,
    costo_anterior      REAL,                                  -- Opcional pero útil para margen
    costo_nuevo         REAL,
    motivo              TEXT,                                  -- "ajuste proveedor", "promoción", etc.
    usuario_responsable TEXT NOT NULL,
    fecha_cambio        TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    FOREIGN KEY (id_producto) REFERENCES producto (UniqueID)
        ON UPDATE CASCADE
        ON DELETE SET NULL
);

-- =====================================================
-- TABLA: venta (ACTUALIZADO)
-- Cambios: agregado iva, cambio, fecha_creacion
-- =====================================================
CREATE TABLE venta
(
    id_venta           INTEGER PRIMARY KEY AUTOINCREMENT,
    id_usuario         INTEGER NOT NULL,
    fecha              TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    estado             TEXT NOT NULL DEFAULT 'COMPLETADA' CHECK (estado IN ('COMPLETADA', 'ANULADA')),
    motivo_anulacion   TEXT,
    subtotal           REAL NOT NULL DEFAULT 0,
    descuento          REAL NOT NULL DEFAULT 0,
    iva                REAL NOT NULL DEFAULT 0,             -- NUEVO: Total de IVA de la venta
    total              REAL NOT NULL DEFAULT 0,
    cambio             REAL NOT NULL DEFAULT 0,              -- NUEVO: Cambio devuelto al cliente
    fecha_creacion     TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    fecha_modificacion TEXT,
    usuario_creacion   TEXT,
    usuario_modificacion TEXT,
    FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

-- =====================================================
-- TABLA: detalle_venta (ACTUALIZADO)
-- Cambios: agregado iva_linea
-- =====================================================
CREATE TABLE detalle_venta
(
    id_detalle              INTEGER PRIMARY KEY AUTOINCREMENT,
    id_venta                INTEGER NOT NULL,
    id_producto             INTEGER NOT NULL,
    id_lote                 INTEGER NOT NULL,
    cantidad                INTEGER NOT NULL CHECK (cantidad > 0),
    precio_unitario_aplicado REAL NOT NULL,
    subtotal_linea          REAL NOT NULL,
    iva_linea               REAL NOT NULL DEFAULT 0,          -- NUEVO: IVA calculado para esta línea
    FOREIGN KEY (id_venta) REFERENCES venta (id_venta)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    FOREIGN KEY (id_producto) REFERENCES producto (UniqueID)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    FOREIGN KEY (id_lote) REFERENCES lote (id_lote)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

-- =====================================================
-- TABLA: pago_venta (ACTUALIZADO)
-- Cambios: agregado TRANSFERENCIA al CHECK
-- =====================================================
CREATE TABLE pago_venta
(
    id_pago   INTEGER PRIMARY KEY AUTOINCREMENT,
    id_venta  INTEGER NOT NULL,
    tipo      TEXT NOT NULL CHECK (tipo IN ('EFECTIVO', 'TARJETA', 'TRANSFERENCIA')),  -- ACTUALIZADO: Agregado TRANSFERENCIA
    monto     REAL NOT NULL CHECK (monto > 0),
    FOREIGN KEY (id_venta) REFERENCES venta (id_venta)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- =====================================================
-- TABLA: auditoria_sistema
-- =====================================================
CREATE TABLE auditoria_sistema
(
    id_auditoria       INTEGER PRIMARY KEY AUTOINCREMENT,
    id_usuario         INTEGER,
    accion             TEXT NOT NULL,                         -- LOGIN | LOGOUT | EXPORTAR_REPORTE | CAMBIO_CLAVE
    tabla_afectada     TEXT,
    registro_id        INTEGER,
    detalles           TEXT,
    fecha_hora         TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
        ON UPDATE CASCADE
        ON DELETE SET NULL
);

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
CREATE INDEX idx_movimiento_lote ON movimiento_inventario (id_lote);
CREATE INDEX idx_movimiento_fecha ON movimiento_inventario (fecha_movimiento);
CREATE INDEX idx_movimiento_tipo ON movimiento_inventario (tipo_movimiento);

-- HISTORIAL PRECIO PRODUCTO
CREATE INDEX idx_historial_precio_producto ON historial_precio_producto (id_producto);
CREATE INDEX idx_historial_precio_fecha ON historial_precio_producto (fecha_cambio);

-- VENTA
CREATE INDEX idx_venta_usuario ON venta (id_usuario);
CREATE INDEX idx_venta_fecha ON venta (fecha);
CREATE INDEX idx_venta_estado ON venta (estado);

-- DETALLE VENTA
CREATE INDEX idx_detalle_venta ON detalle_venta (id_venta);
CREATE INDEX idx_detalle_producto ON detalle_venta (id_producto);
CREATE INDEX idx_detalle_lote ON detalle_venta (id_lote);

-- PAGO VENTA
CREATE INDEX idx_pago_venta ON pago_venta (id_venta);

-- AUDITORIA SISTEMA
CREATE INDEX idx_auditoria_usuario ON auditoria_sistema (id_usuario);
CREATE INDEX idx_auditoria_fecha ON auditoria_sistema (fecha_hora);

-- =====================================================
-- DATOS INICIALES (OPCIONAL)
-- =====================================================

-- Usuario administrador por defecto (password: admin123)
-- INSERT INTO usuario (username, password_hash, nombre_completo, rol, estado)
-- VALUES ('admin', '$2a$10$hash_aqui', 'Administrador del Sistema', 'ADMIN', 'ACTIVO');

-- =====================================================
-- RESUMEN DE CAMBIOS RESPECTO AL ESQUEMA ORIGINAL
-- =====================================================
-- 1. producto: id_categoria cambió a nullable (opcional)
-- 2. producto: agregado porcentaje_iva (REAL NOT NULL DEFAULT 0)
-- 3. venta: agregado iva (REAL NOT NULL DEFAULT 0)
-- 4. venta: agregado cambio (REAL NOT NULL DEFAULT 0)
-- 5. venta: fecha_creacion ahora NOT NULL (heredado de Auditable)
-- 6. detalle_venta: agregado iva_linea (REAL NOT NULL DEFAULT 0)
-- 7. pago_venta: agregado TRANSFERENCIA al CHECK constraint
-- 8. Todos los campos de auditoría JPA sincronizados con entidades
-- =====================================================
