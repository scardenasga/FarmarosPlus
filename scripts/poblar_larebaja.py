#!/usr/bin/env python3
"""
poblar_larebaja.py — genera una DB temporal ya poblada con datos de
../datos/productos_final.csv + imagenes en ../datos/imagenes_larebaja

- NO modifica scripts/empaquetar.ps1
- Clona database/farmarosplus.db -> database/farmarosplus_poblada.db
- Inserta productos faltantes (deduplicando codigo_barras)
- Crea lote + movimiento_inventario por cada producto (como hace ProductoService.crearProducto)
- Copia imagenes a uploads_poblada/productos/producto-{id}.png y actualiza producto.imagen_url

Uso:
  python scripts/poblar_larebaja.py
  python scripts/poblar_larebaja.py --csv ../datos/productos_final.csv --db database/farmarosplus_poblada.db
"""
import argparse
import csv
import os
import shutil
import sqlite3
import sys
from pathlib import Path
from datetime import datetime

ROOT = Path(__file__).resolve().parent.parent  # FarmarosPlus/
DEFAULT_CSV = (ROOT.parent / "datos" / "productos_final.csv").resolve()
DEFAULT_IMG_DIR = (ROOT.parent / "datos" / "imagenes_larebaja").resolve()
DEFAULT_SRC_DB = ROOT / "database" / "farmarosplus.db"
DEFAULT_DST_DB = ROOT / "database" / "farmarosplus_poblada.db"
# uploads intercambiable: el usuario copiará esta carpeta a mano al destino final
# Para DEV:  FarmarosPlus/uploads/productos/
# Para PROD: ~/.farmarosplus/  o  FarmarosPlus/uploads/productos/ (según user.dir del JAR)
DEFAULT_UPLOADS_POBLADA = ROOT / "uploads_poblada" / "productos"
DEFAULT_UPLOADS_REAL = ROOT / "uploads" / "productos"  # donde el servicio guarda en DEV

STOCK_INICIAL = 20
STOCK_MINIMO = 5
FECHA_VENC_STR = "2027-12-31"
# epoch millis usado en DB actual para lote.fecha_vencimiento (ej 1830229200000 = 2028-01-01 aprox)
# Usamos millis para 2027-12-31 00:00 UTC
FECHA_VENC_MILLIS = int(datetime(2027, 12, 31).timestamp() * 1000)

def parse_args():
    p = argparse.ArgumentParser()
    p.add_argument("--csv", default=str(DEFAULT_CSV))
    p.add_argument("--img-dir", default=str(DEFAULT_IMG_DIR))
    p.add_argument("--src-db", default=str(DEFAULT_SRC_DB))
    p.add_argument("--dst-db", default=str(DEFAULT_DST_DB))
    p.add_argument("--uploads-poblada", default=str(DEFAULT_UPLOADS_POBLADA))
    return p.parse_args()

def ensure_dirs(p: Path):
    p.mkdir(parents=True, exist_ok=True)

def main():
    args = parse_args()
    csv_path = Path(args.csv)
    img_dir = Path(args.img_dir)
    src_db = Path(args.src_db)
    dst_db = Path(args.dst_db)
    uploads_poblada = Path(args.uploads_poblada)

    if not csv_path.exists():
        print(f"[ERROR] CSV no existe: {csv_path}", file=sys.stderr)
        sys.exit(1)
    if not src_db.exists():
        print(f"[ERROR] DB origen no existe: {src_db}", file=sys.stderr)
        sys.exit(1)
    if not img_dir.exists():
        print(f"[WARN] Directorio de imágenes no existe: {img_dir}")

    # 1. Clonar DB
    print(f"[1/4] Clonando {src_db} -> {dst_db}")
    shutil.copy2(src_db, dst_db)

    con = sqlite3.connect(str(dst_db))
    con.execute("PRAGMA foreign_keys=OFF;")
    cur = con.cursor()

    # categorías existentes (para asignar id_categoria)
    cur.execute("SELECT id_categoria, nombre FROM categoria")
    cats = cur.fetchall()
    cat_map = {nombre.lower(): cid for cid, nombre in cats}
    # fallback: usar primera categoria si no se puede mapear
    default_cat_id = cats[0][0] if cats else None
    print(f"  Categorías en DB: {cats}")

    # códigos existentes para evitar duplicados
    cur.execute("SELECT codigo_barras FROM producto WHERE codigo_barras IS NOT NULL")
    existentes = set(r[0].strip() for r in cur.fetchall() if r[0])
    print(f"  Productos existentes: {len(existentes)} códigos")

    # lotes existentes (numero_lote unico a nivel servicio)
    cur.execute("SELECT numero_lote FROM lote WHERE numero_lote IS NOT NULL")
    lotes_existentes = set(r[0] for r in cur.fetchall() if r[0])

    # 2. Leer CSV deduplicando por codigo_barras (quedarse con primera ocurrencia con precio si hay duplicado)
    print(f"[2/4] Leyendo CSV {csv_path}")
    rows_by_code = {}
    duplicados_csv = 0
    with open(csv_path, encoding="utf-8-sig") as f:
        reader = csv.DictReader(f)
        for row in reader:
            code = row["codigo_barras"].strip()
            if not code:
                continue
            if code in rows_by_code:
                duplicados_csv += 1
                # si el existente no tiene precio y el nuevo sí, reemplazar
                prev_precio = rows_by_code[code]["precio"].strip()
                cur_precio = row["precio"].strip()
                if not prev_precio and cur_precio:
                    rows_by_code[code] = row
                continue
            rows_by_code[code] = row
    print(f"  Filas únicas por código: {len(rows_by_code)} (duplicados CSV descartados: {duplicados_csv})")
    # filtrar los que ya están en DB
    a_insertar = {c: r for c, r in rows_by_code.items() if c not in existentes}
    print(f"  A insertar (no existen en DB): {len(a_insertar)}")
    omitidos_existentes = len(rows_by_code) - len(a_insertar)
    print(f"  Omitidos por ya existir en DB: {omitidos_existentes}")

    ensure_dirs(uploads_poblada)
    # también asegurar uploads real para que no falle si luego se corre en DEV
    ensure_dirs(DEFAULT_UPLOADS_REAL)

    insertados = 0
    con_imagen = 0
    sin_precio = 0
    errores = 0

    # para commit en lote
    now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

    for code, row in a_insertar.items():
        nombre = row["nombre_producto"].strip()
        if not nombre:
            continue
        # precio / costo
        precio_raw = row["precio"].strip()
        if precio_raw:
            try:
                precio_venta = float(precio_raw)
            except:
                precio_venta = 10000.0
        else:
            precio_venta = 10000.0
            sin_precio += 1

        # costo: derivar como precio/1.5 para asegurar margen > IVA 19%
        costo = round(precio_venta / 1.5, 2)
        if costo < 100:
            costo = round(precio_venta * 0.6, 2) if precio_venta else 5000.0
        # margen
        margen = round(((precio_venta - costo) / costo * 100.0), 2) if costo else 0.0

        # categoría: heurística simple por nombre, fallback a default
        nombre_low = nombre.lower()
        # keywords mínimas; si no mapean, usar default
        cat_id = default_cat_id
        # opcional: intentar mapear por palabras clave
        if any(k in nombre_low for k in ["acetaminofen","acetaminofén","ibuprofeno","dipirona","naprox"]):
            cat_id = cat_map.get("analgésicos", cat_id) or cat_map.get("analgesicos", cat_id)
        elif any(k in nombre_low for k in ["amoxicilina","azitrom","cefale"]):
            cat_id = cat_map.get("antibióticos", cat_id) or cat_map.get("antibioticos", cat_id)
        elif "loratadina" in nombre_low or "cetirizina" in nombre_low:
            cat_id = cat_map.get("antialérgicos", cat_id) or cat_map.get("antialergicos", cat_id)

        numero_lote = f"LOT-{code}"[:40]
        # asegurar unicidad lote
        base_lote = numero_lote
        suffix = 1
        while numero_lote in lotes_existentes:
            numero_lote = f"{base_lote}-{suffix}"
            suffix += 1
        lotes_existentes.add(numero_lote)

        # Insert producto
        try:
            cur.execute("""
                INSERT INTO producto
                (fecha_creacion, fecha_modificacion, usuario_creacion, usuario_modificacion,
                 codigo_barras, costo, descripcion, estado, margen_ganancia, nombre,
                 porcentaje_iva, precio_venta, requiere_prescripcion, stock_actual, stock_minimo,
                 id_categoria, imagen_url)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """, (
                now, None, "SISTEMA", None,
                code, costo, None, "ACTIVO", margen, nombre,
                0.0, precio_venta, 0, STOCK_INICIAL, STOCK_MINIMO,
                cat_id, None
            ))
            new_id = cur.lastrowid

            # Insert lote
            cur.execute("""
                INSERT INTO lote (fecha_creacion, fecha_modificacion, usuario_creacion, usuario_modificacion,
                                  cantidad, fecha_vencimiento, numero_lote, id_producto, id_proveedor)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """, (now, None, "SISTEMA", None, STOCK_INICIAL, FECHA_VENC_MILLIS, numero_lote, new_id, None))
            lote_id = cur.lastrowid

            # Insert movimiento_inventario COMPRA inicial (como ProductoService.registrarIngresoInicial)
            cur.execute("""
                INSERT INTO movimiento_inventario
                (cantidad_anterior, cantidad_nueva, diferencia, fecha_movimiento, motivo,
                 nombre_producto, referencia_documento, tipo_movimiento, usuario_responsable,
                 id_lote, id_producto)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """, (0, STOCK_INICIAL, STOCK_INICIAL, now, "Carga inicial CSV larebaja",
                  nombre, f"CSV-{code}", "COMPRA", "SISTEMA", lote_id, new_id))

            # Imagen: copiar si existe en datos/imagenes_larebaja
            imagen_local = row["imagen_local"].strip() if row["imagen_local"] else ""
            src_img = None
            if imagen_local and Path(imagen_local).exists():
                src_img = Path(imagen_local)
            else:
                # fallback: buscar por codigo_barras.png en img_dir
                cand = img_dir / f"{code}.png"
                if cand.exists():
                    src_img = cand
                else:
                    # probar cualquier extensión
                    for ext in [".png",".jpg",".jpeg",".webp"]:
                        cand = img_dir / f"{code}{ext}"
                        if cand.exists():
                            src_img = cand
                            break
            if src_img and src_img.exists():
                ext = src_img.suffix.lower() or ".png"
                # normalizar a .png/.jpg según original; servicio acepta jpg/jpeg/png/webp/gif
                # guardamos como producto-{id}.png/jpg manteniendo ext original
                nombre_archivo = f"producto-{new_id}{ext}"
                dest_poblada = uploads_poblada / nombre_archivo
                dest_real = DEFAULT_UPLOADS_REAL / nombre_archivo
                shutil.copy2(src_img, dest_poblada)
                # también copiar a uploads real para que DEV funcione inmediato
                try:
                    shutil.copy2(src_img, dest_real)
                except Exception:
                    pass
                cur.execute("UPDATE producto SET imagen_url=? WHERE uniqueid=?", (nombre_archivo, new_id))
                con_imagen += 1

            insertados += 1
            if insertados % 100 == 0:
                print(f"  ... {insertados} insertados")
        except Exception as e:
            print(f"[ERROR] code {code}: {e}")
            errores += 1
            con.rollback()
            # re-abrir cursor limpio
            cur = con.cursor()

    con.commit()
    print(f"[3/4] Insertados: {insertados} | con imagen copiada: {con_imagen} | sin precio (precio default 10000): {sin_precio} | errores: {errores}")

    # estadística final
    cur.execute("SELECT count(*) FROM producto")
    total = cur.fetchone()[0]
    print(f"[4/4] Total productos en {dst_db}: {total}")
    cur.execute("SELECT count(*) FROM producto WHERE imagen_url IS NOT NULL")
    print(f"  Con imagen_url: {cur.fetchone()[0]}")
    cur.execute("SELECT count(*) FROM lote")
    print(f"  Lotes: {cur.fetchone()[0]}")
    cur.execute("SELECT count(*) FROM movimiento_inventario")
    print(f"  Movimientos: {cur.fetchone()[0]}")

    con.close()

    # 4. Instrucciones de intercambio
    print("""
=== LISTO ===

DB poblada: """ + str(dst_db) + """
Imagenes pobladas: """ + str(uploads_poblada) + f""" ({con_imagen} archivos)

Como intercambiar (sin recompilar):

 DEV (./mvnw spring-boot:run o IntelliJ perfil dev):
   copy \"{dst_db}\" -> \"{src_db}\"  (o renombrar farmarosplus_poblada.db a farmarosplus.db)
   xcopy /E /I \"{uploads_poblada}\"  \"{DEFAULT_UPLOADS_REAL}\"
   # reinicia la app; ddl-auto=update no borra datos

 PROD (JAR / jpackage ZIP ya generado por empaquetar.ps1):
   Ubicacion PROD por defecto: %USERPROFILE%\\.farmarosplus\\farmarosplus.db
   -> copiar """ + str(dst_db) + """ a  C:\\Users\\TuUsuario\\.farmarosplus\\farmarosplus.db
   -> copiar carpeta """ + str(uploads_poblada.parent) + """ a  <carpeta_del_ZIP>/FarmarosPlus/uploads/
      (donde esta FarmarosPlus.exe / Iniciar FarmarosPlus.bat)
      El servicio ProductoImagenStorageService crea uploads/productos relativo a user.dir,
      por lo que debe quedar  FarmarosPlus/uploads/productos/producto-*.png

 Para regenerar el ZIP ya con todo incluido (opcional):
   powershell -ExecutionPolicy Bypass -File scripts/empaquetar.ps1 -Version 1.0.x
   # luego reemplaza manualmente dentro de target-jpackage-output/FarmarosPlus/ los dos paths de arriba
   # y vuelve a comprimir: Compress-Archive -Path target-jpackage-output/FarmarosPlus/* -DestinationPath FarmarosPlus-v1.0.x-poblada.zip -Force
""")

if __name__ == "__main__":
    main()
