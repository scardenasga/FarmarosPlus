-- =====================================================
-- FARMAROSPLUS - ESQUEMA DE BASE DE DATOS SPRINT 2
-- Basado en schema_actualizado.sql + nuevas entidades:
-- - Compras / Proveedores con trazabilidad de pedido y recepción
-- - Medicamentos con prescripción
-- =====================================================

PRAGMA foreign_keys = OFF;

-- =====================================================
-- ELIMINAR TABLAS EXISTENTES (EN ORDEN POR DEPENDENCIAS)
-- =====================================================
DROP TABLE IF EXISTS pago_venta;
DROP TABLE IF EXISTS detalle_venta;
DROP TABLE IF EXISTS venta;
DROP TABLE IF EXISTS detalle_devolucion_proveedor;
DROP TABLE IF EXISTS devolucion_proveedor;
DROP TABLE IF EXISTS detalle_recepcion_compra;
DROP TABLE IF EXISTS recepcion_compra;
DROP TABLE IF EXISTS detalle_orden_compra;
DROP TABLE IF EXISTS nota_proveedor;
DROP TABLE IF EXISTS orden_compra;
DROP TABLE IF EXISTS proveedor_producto;
DROP TABLE IF EXISTS proveedor;
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
-- TABLA: proveedor (NUEVA)
-- Simplificada para el uso diario del negocio; los datos menos usados quedan fuera
-- =====================================================
CREATE TABLE proveedor
(
    id_proveedor       INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre             TEXT NOT NULL UNIQUE,
    nit                TEXT UNIQUE,                              -- NIT para identificación
    telefono           TEXT,
    email              TEXT,
    contacto           TEXT,                                     -- Nombre de contacto principal
    estado             TEXT NOT NULL DEFAULT 'ACTIVO' CHECK (estado IN ('ACTIVO', 'INACTIVO')),
    condicion_pago     TEXT,                                     -- Ej: "Neto 30", "Contra Entrega", etc.
    fecha_creacion     TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    fecha_modificacion TEXT,
    usuario_creacion   TEXT,
    usuario_modificacion TEXT
);

-- =====================================================
-- TABLA: producto (ACTUALIZADO)
-- Cambios: id_categoria nullable, porcentaje_iva, + requiere_prescripcion
-- =====================================================
CREATE TABLE producto
(
    UniqueID               INTEGER PRIMARY KEY AUTOINCREMENT,
    id_categoria           INTEGER,
    nombre                 TEXT NOT NULL,
    descripcion            TEXT,
    codigo_barras          TEXT UNIQUE,
    stock_minimo           INTEGER DEFAULT 0,
    stock_actual           INTEGER DEFAULT 0,
    costo                  REAL NOT NULL DEFAULT 0,
    precio_venta           REAL NOT NULL DEFAULT 0,
    margen_ganancia        REAL,
    porcentaje_iva         REAL NOT NULL DEFAULT 0,
    requiere_prescripcion  INTEGER NOT NULL DEFAULT 0,          -- NUEVO: 1 = requiere, 0 = no requiere
    estado                 TEXT NOT NULL DEFAULT 'ACTIVO' CHECK (estado IN ('ACTIVO', 'INACTIVO', 'DESCONTINUADO')),
    fecha_creacion         TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    usuario_creacion       TEXT,
    fecha_modificacion     TEXT,
    usuario_modificacion   TEXT,
    FOREIGN KEY (id_categoria) REFERENCES categoria (id_categoria)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

-- =====================================================
-- TABLA: proveedor_producto (NUEVA)
-- Relación directa para saber qué proveedor ofrece cada producto,
-- incluso si todavía no existe un lote o una orden previa
-- =====================================================
CREATE TABLE proveedor_producto
(
    id_proveedor_producto    INTEGER PRIMARY KEY AUTOINCREMENT,
    id_proveedor             INTEGER NOT NULL,
    id_producto              INTEGER NOT NULL,
    codigo_producto_proveedor TEXT,                              -- Código o referencia usada por el proveedor
    precio_referencia        REAL CHECK (precio_referencia IS NULL OR precio_referencia >= 0), -- Precio orientativo, no contractual
    estado                   TEXT NOT NULL DEFAULT 'ACTIVO' CHECK (estado IN ('ACTIVO', 'INACTIVO')),
    UNIQUE (id_proveedor, id_producto),
    FOREIGN KEY (id_proveedor) REFERENCES proveedor (id_proveedor)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    FOREIGN KEY (id_producto) REFERENCES producto (UniqueID)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- =====================================================
-- TABLA: lote
-- El lote conserva el origen del inventario y su vencimiento
-- =====================================================
CREATE TABLE lote
(
    id_lote            INTEGER PRIMARY KEY AUTOINCREMENT,
    numero_lote        TEXT NOT NULL,
    fecha_vencimiento  TEXT,
    cantidad           INTEGER NOT NULL DEFAULT 0 CHECK (cantidad >= 0),
    id_producto        INTEGER NOT NULL,
    id_proveedor       INTEGER,                                  -- Nuevo: relación con proveedor que lo suministró
    fecha_creacion     TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    fecha_modificacion TEXT,
    usuario_creacion   TEXT,
    usuario_modificacion TEXT,
    UNIQUE (id_producto, numero_lote, id_proveedor),
    FOREIGN KEY (id_producto) REFERENCES producto (UniqueID)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    FOREIGN KEY (id_proveedor) REFERENCES proveedor (id_proveedor)
        ON UPDATE CASCADE
        ON DELETE SET NULL
);

-- =====================================================
-- TABLA: movimiento_inventario
-- =====================================================
CREATE TABLE movimiento_inventario
(
    id_movimiento      INTEGER PRIMARY KEY AUTOINCREMENT,
    id_producto        INTEGER,
    nombre_producto    TEXT NOT NULL,
    id_lote            INTEGER,
    tipo_movimiento    TEXT NOT NULL CHECK (tipo_movimiento IN ('COMPRA', 'VENTA', 'AJUSTE', 'MERMA', 'ROBO', 'DEVOLUCION', 'VENCIMIENTO')),
    cantidad_anterior  INTEGER NOT NULL,
    cantidad_nueva     INTEGER NOT NULL,
    diferencia         INTEGER NOT NULL,
    motivo             TEXT,
    referencia_documento TEXT,
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
    nombre_producto     TEXT NOT NULL,
    precio_anterior     REAL NOT NULL,
    precio_nuevo        REAL NOT NULL,
    costo_anterior      REAL,
    costo_nuevo         REAL,
    motivo              TEXT,
    usuario_responsable TEXT NOT NULL,
    fecha_cambio        TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    FOREIGN KEY (id_producto) REFERENCES producto (UniqueID)
        ON UPDATE CASCADE
        ON DELETE SET NULL
);

-- =====================================================
-- TABLA: orden_compra (NUEVA)
-- Mantiene la planeación del pedido y el control básico del dinero comprometido
-- =====================================================
CREATE TABLE orden_compra
(
    id_orden           INTEGER PRIMARY KEY AUTOINCREMENT,
    id_proveedor       INTEGER NOT NULL,
    id_usuario         INTEGER NOT NULL,
    fecha_pedido       TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    fecha_esperada     TEXT,                                     -- Fecha estimada de entrega (DATO REAL de negocio)
    estado             TEXT NOT NULL DEFAULT 'PENDIENTE' CHECK (estado IN ('PENDIENTE', 'CERRADA', 'NO_RECIBIDA', 'CANCELADA')),
    total_esperado     REAL NOT NULL DEFAULT 0 CHECK (total_esperado >= 0),
    observaciones      TEXT,                                     -- Notas específicas de la orden
    fecha_creacion     TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    fecha_modificacion TEXT,
    usuario_creacion   TEXT,
    usuario_modificacion TEXT,
    FOREIGN KEY (id_proveedor) REFERENCES proveedor (id_proveedor)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

-- =====================================================
-- TABLA: detalle_orden_compra (NUEVA)
-- =====================================================
CREATE TABLE detalle_orden_compra
(
    id_detalle              INTEGER PRIMARY KEY AUTOINCREMENT,
    id_orden                INTEGER NOT NULL,
    id_producto             INTEGER,
    nombre_producto         TEXT NOT NULL,
    descripcion_producto    TEXT,
    cantidad_pedida         INTEGER NOT NULL CHECK (cantidad_pedida > 0),
    precio_unitario_pactado REAL NOT NULL CHECK (precio_unitario_pactado >= 0),
    FOREIGN KEY (id_orden) REFERENCES orden_compra (id_orden)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    FOREIGN KEY (id_producto) REFERENCES producto (UniqueID)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

-- =====================================================
-- TABLA: recepcion_compra (NUEVA)
-- Cabecera de recepción física; el detalle recibido vive por producto
-- =====================================================
CREATE TABLE recepcion_compra
(
    id_recepcion       INTEGER PRIMARY KEY AUTOINCREMENT,
    id_orden           INTEGER NOT NULL,
    id_usuario         INTEGER NOT NULL,
    fecha_recepcion    TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    observaciones      TEXT,                                     -- Diferencias, daños, etc.
    estado             TEXT NOT NULL DEFAULT 'PARCIAL' CHECK (estado IN ('PARCIAL', 'COMPLETA', 'RECHAZADA')),
    total_recepcion    REAL NOT NULL DEFAULT 0 CHECK (total_recepcion >= 0),
    estado_pago        TEXT NOT NULL DEFAULT 'PENDIENTE' CHECK (estado_pago IN ('PENDIENTE', 'PARCIAL', 'PAGADO')),
    monto_pagado       REAL NOT NULL DEFAULT 0 CHECK (monto_pagado >= 0),
    fecha_limite_pago  TEXT,
    fecha_creacion     TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    fecha_modificacion TEXT,
    usuario_creacion   TEXT,
    usuario_modificacion TEXT,
    FOREIGN KEY (id_orden) REFERENCES orden_compra (id_orden)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

-- =====================================================
-- TABLA: detalle_recepcion_compra (NUEVA)
-- Registra qué producto y cantidad llegaron en cada recepción
-- =====================================================
CREATE TABLE detalle_recepcion_compra
(
    id_detalle_recepcion INTEGER PRIMARY KEY AUTOINCREMENT,
    id_recepcion         INTEGER NOT NULL,
    id_detalle_orden     INTEGER NOT NULL,
    id_producto          INTEGER,
    cantidad_recibida    INTEGER NOT NULL CHECK (cantidad_recibida > 0),
    costo_unitario_real  REAL CHECK (costo_unitario_real IS NULL OR costo_unitario_real >= 0),
    observaciones        TEXT,
    FOREIGN KEY (id_recepcion) REFERENCES recepcion_compra (id_recepcion)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    FOREIGN KEY (id_detalle_orden) REFERENCES detalle_orden_compra (id_detalle)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    FOREIGN KEY (id_producto) REFERENCES producto (UniqueID)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

-- =====================================================
-- TABLA: devolucion_proveedor (NUEVA)
-- La devolución siempre nace desde una recepción, ya sea inmediata o posterior
-- =====================================================
CREATE TABLE devolucion_proveedor
(
    id_devolucion      INTEGER PRIMARY KEY AUTOINCREMENT,
    id_proveedor       INTEGER NOT NULL,
    id_recepcion       INTEGER NOT NULL,
    id_usuario         INTEGER NOT NULL,
    fecha_devolucion   TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    tipo_devolucion    TEXT NOT NULL CHECK (tipo_devolucion IN ('INMEDIATA_RECEPCION', 'POSTERIOR')),
    motivo_principal   TEXT NOT NULL CHECK (motivo_principal IN ('VENCIMIENTO', 'DANADO', 'DEFECTO', 'ERROR_DESPACHO', 'RETIRO_SANITARIO', 'OTRO')),
    estado             TEXT NOT NULL DEFAULT 'PENDIENTE' CHECK (estado IN ('PENDIENTE', 'ENVIADA', 'ACEPTADA', 'RECHAZADA', 'CERRADA')),
    observaciones      TEXT,
    FOREIGN KEY (id_proveedor) REFERENCES proveedor (id_proveedor)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    FOREIGN KEY (id_recepcion) REFERENCES recepcion_compra (id_recepcion)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

-- =====================================================
-- TABLA: detalle_devolucion_proveedor (NUEVA)
-- Permite devolver en recepción o después, incluyendo casos por vencimiento
-- =====================================================
CREATE TABLE detalle_devolucion_proveedor
(
    id_detalle_devolucion   INTEGER PRIMARY KEY AUTOINCREMENT,
    id_devolucion           INTEGER NOT NULL,
    id_detalle_recepcion    INTEGER NOT NULL,
    id_producto             INTEGER,
    id_lote                 INTEGER,
    cantidad_devuelta       INTEGER NOT NULL CHECK (cantidad_devuelta > 0),
    costo_unitario_referencia REAL CHECK (costo_unitario_referencia IS NULL OR costo_unitario_referencia >= 0),
    observaciones           TEXT,
    FOREIGN KEY (id_devolucion) REFERENCES devolucion_proveedor (id_devolucion)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    FOREIGN KEY (id_detalle_recepcion) REFERENCES detalle_recepcion_compra (id_detalle_recepcion)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    FOREIGN KEY (id_producto) REFERENCES producto (UniqueID)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    FOREIGN KEY (id_lote) REFERENCES lote (id_lote)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

-- =====================================================
-- TABLA: nota_proveedor (NUEVA)
-- Permite registrar comunicaciones, problemas, cambios con proveedores
-- =====================================================
CREATE TABLE nota_proveedor
(
    id_nota            INTEGER PRIMARY KEY AUTOINCREMENT,
    id_proveedor       INTEGER NOT NULL,
    tipo_nota          TEXT NOT NULL CHECK (tipo_nota IN ('RETRASO', 'PRODUCTO_DEFECTUOSO', 'CAMBIO_PRECIO', 'CAMBIO_CONTACTO', 'OTRO')),
    titulo             TEXT NOT NULL,
    descripcion        TEXT,
    id_orden_relacionada INTEGER,                               -- FK opcional a orden_compra si aplica
    fecha_creacion     TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    fecha_modificacion TEXT,
    usuario_creacion   TEXT,
    usuario_modificacion TEXT,
    FOREIGN KEY (id_proveedor) REFERENCES proveedor (id_proveedor)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    FOREIGN KEY (id_orden_relacionada) REFERENCES orden_compra (id_orden)
        ON UPDATE CASCADE
        ON DELETE SET NULL
);

-- =====================================================
-- TABLA: venta
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
    iva                REAL NOT NULL DEFAULT 0,
    total              REAL NOT NULL DEFAULT 0,
    cambio             REAL NOT NULL DEFAULT 0,
    fecha_creacion     TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    fecha_modificacion TEXT,
    usuario_creacion   TEXT,
    usuario_modificacion TEXT,
    FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

-- =====================================================
-- TABLA: detalle_venta
-- =====================================================
CREATE TABLE detalle_venta
(
    id_detalle              INTEGER PRIMARY KEY AUTOINCREMENT,
    id_venta                INTEGER NOT NULL,
    id_producto             INTEGER NOT NULL,
    id_lote                 INTEGER,
    cantidad                INTEGER NOT NULL CHECK (cantidad > 0),
    precio_unitario_aplicado REAL NOT NULL,
    subtotal_linea          REAL NOT NULL,
    iva_linea               REAL NOT NULL DEFAULT 0,
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
-- TABLA: pago_venta
-- =====================================================
CREATE TABLE pago_venta
(
    id_pago   INTEGER PRIMARY KEY AUTOINCREMENT,
    id_venta  INTEGER NOT NULL,
    tipo      TEXT NOT NULL CHECK (tipo IN ('EFECTIVO', 'TARJETA', 'TRANSFERENCIA')),
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
    accion             TEXT NOT NULL,
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

-- USUARIO
CREATE INDEX idx_usuario_rol ON usuario (rol);
CREATE INDEX idx_usuario_estado ON usuario (estado);

-- CATEGORIA
CREATE INDEX idx_categoria_nombre ON categoria (nombre);

-- PROVEEDOR (NUEVO)
CREATE INDEX idx_proveedor_nombre ON proveedor (nombre);
CREATE INDEX idx_proveedor_nit ON proveedor (nit);
CREATE INDEX idx_proveedor_estado ON proveedor (estado);

-- PRODUCTO
CREATE INDEX idx_producto_categoria ON producto (id_categoria);
CREATE INDEX idx_producto_codigo ON producto (codigo_barras);
CREATE INDEX idx_producto_estado ON producto (estado);
CREATE INDEX idx_producto_prescripcion ON producto (requiere_prescripcion);

-- PROVEEDOR_PRODUCTO (NUEVO)
CREATE INDEX idx_proveedor_producto_proveedor ON proveedor_producto (id_proveedor);
CREATE INDEX idx_proveedor_producto_producto ON proveedor_producto (id_producto);
CREATE INDEX idx_proveedor_producto_estado ON proveedor_producto (estado);

-- LOTE
CREATE INDEX idx_lote_producto ON lote (id_producto);
CREATE INDEX idx_lote_proveedor ON lote (id_proveedor);
CREATE INDEX idx_lote_vencimiento ON lote (fecha_vencimiento);

-- MOVIMIENTO INVENTARIO
CREATE INDEX idx_movimiento_producto ON movimiento_inventario (id_producto);
CREATE INDEX idx_movimiento_lote ON movimiento_inventario (id_lote);
CREATE INDEX idx_movimiento_fecha ON movimiento_inventario (fecha_movimiento);
CREATE INDEX idx_movimiento_tipo ON movimiento_inventario (tipo_movimiento);

-- HISTORIAL PRECIO PRODUCTO
CREATE INDEX idx_historial_precio_producto ON historial_precio_producto (id_producto);
CREATE INDEX idx_historial_precio_fecha ON historial_precio_producto (fecha_cambio);

-- ORDEN COMPRA (NUEVO)
CREATE INDEX idx_orden_proveedor ON orden_compra (id_proveedor);
CREATE INDEX idx_orden_usuario ON orden_compra (id_usuario);
CREATE INDEX idx_orden_fecha_pedido ON orden_compra (fecha_pedido);
CREATE INDEX idx_orden_fecha_esperada ON orden_compra (fecha_esperada);
CREATE INDEX idx_orden_estado ON orden_compra (estado);

-- DETALLE ORDEN COMPRA (NUEVO)
CREATE INDEX idx_detalle_orden ON detalle_orden_compra (id_orden);
CREATE INDEX idx_detalle_producto_compra ON detalle_orden_compra (id_producto);
CREATE INDEX idx_detalle_nombre_producto_compra ON detalle_orden_compra (nombre_producto);

-- RECEPCION COMPRA (NUEVO)
CREATE INDEX idx_recepcion_orden ON recepcion_compra (id_orden);
CREATE INDEX idx_recepcion_usuario ON recepcion_compra (id_usuario);
CREATE INDEX idx_recepcion_fecha ON recepcion_compra (fecha_recepcion);
CREATE INDEX idx_recepcion_estado ON recepcion_compra (estado);
CREATE INDEX idx_recepcion_estado_pago ON recepcion_compra (estado_pago);

-- DETALLE RECEPCION COMPRA (NUEVO)
CREATE INDEX idx_detalle_recepcion_recepcion ON detalle_recepcion_compra (id_recepcion);
CREATE INDEX idx_detalle_recepcion_detalle_orden ON detalle_recepcion_compra (id_detalle_orden);
CREATE INDEX idx_detalle_recepcion_producto ON detalle_recepcion_compra (id_producto);

-- DEVOLUCION PROVEEDOR (NUEVO)
CREATE INDEX idx_devolucion_proveedor ON devolucion_proveedor (id_proveedor);
CREATE INDEX idx_devolucion_recepcion ON devolucion_proveedor (id_recepcion);
CREATE INDEX idx_devolucion_usuario ON devolucion_proveedor (id_usuario);
CREATE INDEX idx_devolucion_fecha ON devolucion_proveedor (fecha_devolucion);
CREATE INDEX idx_devolucion_tipo ON devolucion_proveedor (tipo_devolucion);
CREATE INDEX idx_devolucion_estado ON devolucion_proveedor (estado);

-- DETALLE DEVOLUCION PROVEEDOR (NUEVO)
CREATE INDEX idx_detalle_devolucion_devolucion ON detalle_devolucion_proveedor (id_devolucion);
CREATE INDEX idx_detalle_devolucion_recepcion ON detalle_devolucion_proveedor (id_detalle_recepcion);
CREATE INDEX idx_detalle_devolucion_producto ON detalle_devolucion_proveedor (id_producto);
CREATE INDEX idx_detalle_devolucion_lote ON detalle_devolucion_proveedor (id_lote);

-- NOTA PROVEEDOR (NUEVO)
CREATE INDEX idx_nota_proveedor ON nota_proveedor (id_proveedor);
CREATE INDEX idx_nota_tipo ON nota_proveedor (tipo_nota);
CREATE INDEX idx_nota_fecha ON nota_proveedor (fecha_creacion);

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
-- RESUMEN DE CAMBIOS - SPRINT 2 (REVISADO)
-- =====================================================
-- NUEVAS TABLAS:
-- 1. proveedor - Catálogo de proveedores con datos de contacto
-- 2. proveedor_producto - Relación directa entre proveedores y productos
-- 3. orden_compra - Cabecera de órdenes de compra
-- 4. detalle_orden_compra - Líneas de compra con referencia al catálogo o descripción libre
-- 5. recepcion_compra - Cabecera de recepción física
-- 6. detalle_recepcion_compra - Detalle por producto recibido
-- 7. devolucion_proveedor - Cabecera de devoluciones al proveedor originadas en una recepción
-- 8. detalle_devolucion_proveedor - Detalle de productos/lotes devueltos al proveedor
-- 9. nota_proveedor - Registro de comunicaciones y problemas con proveedores
--
-- CAMBIOS EXISTENTES:
-- 1. producto: Agregado campo requiere_prescripcion (INTEGER, 0/1)
-- 2. lote: Agregado id_proveedor (FK a proveedor)
--
-- AJUSTES DE COHERENCIA:
-- 1. proveedor: se eliminan direccion y ciudad para reducir complejidad del registro
-- 2. proveedor_producto: se simplifica como relación de catálogo entre proveedor y producto
-- 3. orden_compra: mantiene fecha_pedido y total_esperado como datos de planeación
-- 4. detalle_orden_compra: permite pedir productos que aún no existen en inventario
-- 5. detalle_orden_compra: elimina cantidad_recibida porque ese dato pertenece a la recepción
-- 6. recepcion_compra: concentra el pago real de lo recibido
-- 7. detalle_recepcion_compra: registra la recepción real sin depender del lote
-- 8. devolucion_proveedor: solo nace desde recepcion_compra, no desde orden_compra
-- 9. devolucion_proveedor: soporta devolución inmediata y posterior, incluyendo vencimientos
-- 10. orden_compra: agrega estado NO_RECIBIDA para pedidos que nunca llegan
-- 11. recepcion_compra: maneja estados PARCIAL, COMPLETA o RECHAZADA
-- 12. auditoría redundante: se eliminan fechas técnicas en proveedor_producto, orden_compra, recepcion_compra y sus detalles
--
-- CASOS EDGE CUBIERTOS:
-- 1. ✓ Producto nuevo sin stock previo: puedo pedirlo aunque todavía no exista en inventario
-- 2. ✓ Recepciones parciales: cada entrega queda registrada en detalle_recepcion_compra
-- 3. ✓ Cambios de precio: precio_unitario_pactado es por detalle y costo_unitario_real puede registrar diferencias
-- 4. ✓ Producto descontinuado: puede venderse si hay lote, pero no permitir nueva orden
-- 5. ✓ Múltiples órdenes del mismo producto: cada una con precio/cantidades independientes
-- 6. ✓ Pedido nunca recibido: la orden puede marcarse como NO_RECIBIDA
-- 7. ✓ Cancelación de orden: cambiar estado, no revertir stock recibido
-- 8. ✓ Devolución inmediata: nace desde la recepción sin depender de la orden
-- 9. ✓ Devolución posterior por vencimiento u otras causas: queda vinculada a la recepción de origen
-- 10. ✓ Trazabilidad: proveedor_producto define oferta, orden_compra planea compra y detalle_recepcion_compra registra la llegada
-- 11. ✓ Productos no farmacéuticos: pueden comprarse y venderse sin lote cuando no se gestionan en la tabla lote
-- =====================================================
