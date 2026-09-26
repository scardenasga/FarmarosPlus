#!/usr/bin/env python3
"""
poblar_larebaja_clean.py — genera DB LIMPIA solo con datos del CSV + defaults
No copia ventas, compras, devoluciones, etc. Solo usuarios/permisos/categorías + productos CSV.

- Lee esquema REAL de database/farmarosplus.db (via sqlite_master) para no depender de .sql desactualizados
- Crea database/farmarosplus_poblada.db desde cero (tablas vacías)
- Inserta seeds mínimos que crea el JAR al compilar (DataInitializer + Configuracion)
- Inserta categorías (10) y productos del CSV (740 únicos) con lote + movimiento_inventario COMPRA
- Copia imágenes a uploads_poblada/productos/producto-{id}.ext (1:1 con nuevo id)

Uso: python scripts/poblar_larebaja_clean.py
"""
import csv
import shutil
import sqlite3
from pathlib import Path
from datetime import datetime

ROOT = Path(__file__).resolve().parent.parent
SRC_DB = ROOT / "database" / "farmarosplus.db"
DST_DB = ROOT / "database" / "farmarosplus_poblada.db"
CSV_PATH = (ROOT.parent / "datos" / "productos_final.csv").resolve()
IMG_DIR = (ROOT.parent / "datos" / "imagenes_larebaja").resolve()
UPLOADS_POBLADA = ROOT / "uploads_poblada" / "productos"
UPLOADS_REAL = ROOT / "uploads" / "productos"

STOCK_INICIAL = 20
STOCK_MINIMO = 5
FECHA_VENC_MILLIS = int(datetime(2027, 12, 31).timestamp() * 1000)

def main():
    if not SRC_DB.exists():
        raise SystemExit(f"No existe SRC_DB {SRC_DB}")
    if not CSV_PATH.exists():
        raise SystemExit(f"No existe CSV {CSV_PATH}")

    # 1. Crear DB limpia replicando esquema del SRC (CREATE TABLE + INDEX)
    if DST_DB.exists():
        DST_DB.unlink()
    print(f"[1/4] Creando DB limpia {DST_DB} desde esquema de {SRC_DB}")
    src = sqlite3.connect(str(SRC_DB))
    dst = sqlite3.connect(str(DST_DB))
    src_cur = src.cursor()
    dst_cur = dst.cursor()
    dst_cur.execute("PRAGMA foreign_keys=OFF;")

    # copiar orden de creación (sqlite_master ordenado por rowid ya respeta dependencias)
    for sql in src_cur.execute("SELECT sql FROM sqlite_master WHERE type='table' AND sql IS NOT NULL ORDER BY rowid"):
        dst_cur.execute(sql[0])
    for sql in src_cur.execute("SELECT sql FROM sqlite_master WHERE type='index' AND sql IS NOT NULL ORDER BY rowid"):
        try:
            dst_cur.execute(sql[0])
        except sqlite3.OperationalError as e:
            # índices auto de PK ya existen
            if "already exists" not in str(e):
                raise
    dst.commit()
    print("  Tablas creadas:", [r[0] for r in src_cur.execute('SELECT name FROM sqlite_master WHERE type="table" ORDER BY name')])

    # 2. Seeds mínimos (exactamente lo que crea DataInitializer + config por defecto)
    print("[2/4] Insertando seeds mínimos (usuarios, permisos, categorías, config)")
    now = datetime.now().strftime("%Y-%m-%dT%H:%M:%S.%f")[:-3]

    # copiar usuarios/permisos/rol_permiso/categoria/config desde SRC (son los defaults creados al compilar)
    for tbl in ["usuario", "permiso", "rol_permiso", "categoria", "configuracion_sistema"]:
        rows = list(src_cur.execute(f'SELECT * FROM "{tbl}"'))
        if not rows:
            continue
        cols = [d[0] for d in src_cur.description]
        placeholders = ",".join(["?"] * len(cols))
        dst_cur.executemany(f'INSERT INTO "{tbl}" ({",".join(cols)}) VALUES ({placeholders})', rows)
        print(f"  {tbl}: {len(rows)}")

    # limpiar auditoría innecesaria que pudiera venir copiada por error
    # (no copiamos venta, lote, producto, etc. pero por si quedó algo)
    dst.commit()

    # 3. Leer CSV deduplicado y insertar productos
    print(f"[3/4] Leyendo CSV {CSV_PATH}")
    rows_by_code = {}
    with open(CSV_PATH, encoding="utf-8-sig") as f:
        r = csv.DictReader(f)
        for row in r:
            code = row["codigo_barras"].strip()
            if not code:
                continue
            if code in rows_by_code:
                # quedarse con el que tiene precio si hay duplicado
                if not rows_by_code[code]["precio"].strip() and row["precio"].strip():
                    rows_by_code[code] = row
                continue
            rows_by_code[code] = row
    print(f"  Únicos: {len(rows_by_code)}")

    # mapa categorias para FK (usar las 10 existentes)
    cat_map = {row[0]: row[0] for row in dst_cur.execute("SELECT id_categoria FROM categoria")}
    # fallback id=1
    default_cat = next(iter(cat_map), 1)

    # preparar uploads
    if UPLOADS_POBLADA.exists():
        shutil.rmtree(UPLOADS_POBLADA)
    UPLOADS_POBLADA.mkdir(parents=True, exist_ok=True)
    # limpiar uploads real también para que DEV quede limpio (opcional: no borrar todo, solo agregar)
    # No borramos uploads real entero, solo aseguramos directorio
    UPLOADS_REAL.mkdir(parents=True, exist_ok=True)
    # limpiar imágenes previas de productos CSV viejos en uploads real para evitar mezcla (opcional)
    for p in UPLOADS_REAL.glob("producto-*.png"):
        # si son del rango anterior (ids >20) dejarlos? limpiamos para clean
        try:
            p.unlink()
        except: pass
    for p in UPLOADS_REAL.glob("producto-*.jpg"):
        try: p.unlink()
        except: pass

    insertados = 0
    con_imagen = 0
    lotes_existentes = set()
    for code, row in rows_by_code.items():
        nombre = row["nombre_producto"].strip()
        if not nombre:
            continue
        precio_raw = row["precio"].strip()
        precio_venta = float(precio_raw) if precio_raw else 10000.0
        costo = round(precio_venta / 1.5, 2)
        if costo < 100:
            costo = round(precio_venta * 0.6, 2)
        margen = round(((precio_venta - costo) / costo * 100), 2) if costo else 0.0

        numero_lote = f"LOT-{code}"[:40]
        base = numero_lote
        suf = 1
        while numero_lote in lotes_existentes:
            numero_lote = f"{base}-{suf}"
            suf += 1
        lotes_existentes.add(numero_lote)

        dst_cur.execute("""
            INSERT INTO producto
            (fecha_creacion, usuario_creacion, codigo_barras, costo, estado, margen_ganancia, nombre,
             porcentaje_iva, precio_venta, requiere_prescripcion, stock_actual, stock_minimo,
             id_categoria, imagen_url)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """, (now, "SISTEMA", code, costo, "ACTIVO", margen, nombre, 0.0, precio_venta, 0, STOCK_INICIAL, STOCK_MINIMO, default_cat, None))
        new_id = dst_cur.lastrowid

        dst_cur.execute("""
            INSERT INTO lote (fecha_creacion, usuario_creacion, cantidad, fecha_vencimiento, numero_lote, id_producto)
            VALUES (?, ?, ?, ?, ?, ?)
        """, (now, "SISTEMA", STOCK_INICIAL, FECHA_VENC_MILLIS, numero_lote, new_id))
        lote_id = dst_cur.lastrowid

        dst_cur.execute("""
            INSERT INTO movimiento_inventario
            (cantidad_anterior, cantidad_nueva, diferencia, fecha_movimiento, motivo, nombre_producto, tipo_movimiento, usuario_responsable, id_lote, id_producto)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """, (0, STOCK_INICIAL, STOCK_INICIAL, now, "Carga inicial CSV larebaja", nombre, "COMPRA", "SISTEMA", lote_id, new_id))

        # imagen
        src_img = None
        img_local = row["imagen_local"].strip()
        if img_local and Path(img_local).exists():
            src_img = Path(img_local)
        else:
            cand = IMG_DIR / f"{code}.png"
            if cand.exists():
                src_img = cand
            else:
                for ext in [".png",".jpg",".jpeg",".webp",".gif"]:
                    c = IMG_DIR / f"{code}{ext}"
                    if c.exists():
                        src_img = c
                        break
        if src_img and src_img.exists():
            ext = src_img.suffix.lower() or ".png"
            nombre_archivo = f"producto-{new_id}{ext}"
            dest = UPLOADS_POBLADA / nombre_archivo
            dest_real = UPLOADS_REAL / nombre_archivo
            shutil.copy2(src_img, dest)
            shutil.copy2(src_img, dest_real)
            dst_cur.execute("UPDATE producto SET imagen_url=? WHERE uniqueid=?", (nombre_archivo, new_id))
            con_imagen += 1

        insertados += 1

    dst.commit()
    print(f"  Insertados: {insertados} | con imagen: {con_imagen}")

    # stats
    for q in ["SELECT count(*) FROM producto","SELECT count(*) FROM producto WHERE imagen_url IS NOT NULL","SELECT count(*) FROM lote","SELECT count(*) FROM movimiento_inventario","SELECT count(*) FROM usuario","SELECT count(*) FROM categoria","SELECT count(*) FROM venta"]:
        dst_cur.execute(q)
        print(f"  {q} -> {dst_cur.fetchone()[0]}")

    # integrity
    dst_cur.execute("PRAGMA foreign_key_check")
    print("  FK check:", dst_cur.fetchall())
    dst_cur.execute("PRAGMA integrity_check")
    print("  integrity:", dst_cur.fetchone()[0])
    src.close()
    dst.close()

    print(f"""
=== LISTO CLEAN ===
DB limpia: {DST_DB} ({insertados} productos CSV, sin ventas/compras)
Imagenes: {UPLOADS_POBLADA} ({con_imagen} archivos, nombres producto-{{id}}.png)

DEV:  Copy-Item {DST_DB} {SRC_DB} -Force  + xcopy {UPLOADS_POBLADA} {UPLOADS_REAL}  -> ./mvnw spring-boot:run
PROD: Copy-Item {DST_DB} $env:USERPROFILE\\.farmarosplus\\farmarosplus.db -Force
      xcopy {UPLOADS_POBLADA.parent} target-jpackage-output\\FarmarosPlus\\uploads\\
""")

if __name__ == "__main__":
    main()
