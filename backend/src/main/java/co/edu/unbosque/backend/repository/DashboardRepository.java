package co.edu.unbosque.backend.repository;

import co.edu.unbosque.backend.model.entity.Venta;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DashboardRepository extends JpaRepository<Venta, Long> {

    /**
     * Calcula el total monetario y la cantidad de ventas
     * completadas en un rango de fechas.
     */
    @Query("""
        SELECT COALESCE(SUM(v.total), 0), COUNT(v)
        FROM Venta v
        WHERE v.estado = 'COMPLETADA'
        AND v.fecha BETWEEN :inicio AND :fin
        """)
    List<Object[]> resumenVentas(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );


    /**
     * Obtiene las ventas completadas dentro de un periodo.
     */
    @Query("""
        SELECT v.idVenta, v.fecha, v.total, v.estado
        FROM Venta v
        WHERE v.estado = 'COMPLETADA'
        AND v.fecha BETWEEN :inicio AND :fin
        ORDER BY v.fecha DESC
        """)
    List<Object[]> ventasCompletadasPorPeriodo(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );


    /**
     * Agrupa las ventas completadas por día.
     *
     * SQLite trabaja directamente con el valor almacenado
     * de fecha y permite obtener la parte correspondiente al día.
     */
    @Query(value = """
        SELECT
            DATE(v.fecha) AS fecha,
            COALESCE(SUM(v.total), 0) AS total,
            COUNT(v.id_venta) AS cantidad
        FROM venta v
        WHERE v.estado = 'COMPLETADA'
        AND datetime(v.fecha) BETWEEN datetime(:inicio) AND datetime(:fin)
        GROUP BY DATE(v.fecha)
        ORDER BY DATE(v.fecha) ASC
        """, nativeQuery = true)
    List<Object[]> ventasAgrupadasPorDia(
            @Param("inicio") String inicio,
            @Param("fin") String fin
    );


    /**
     * Obtiene los productos más vendidos durante el periodo.
     */
    @Query("""
        SELECT
            p.uniqueID,
            p.nombre,
            COALESCE(SUM(dv.cantidad), 0),
            COALESCE(SUM(dv.subtotalLinea), 0)
        FROM DetalleVenta dv
        JOIN dv.producto p
        WHERE dv.venta.estado = 'COMPLETADA'
        AND dv.venta.fecha BETWEEN :inicio AND :fin
        GROUP BY p.uniqueID, p.nombre
        ORDER BY SUM(dv.cantidad) DESC
        """)
    List<Object[]> productosMasVendidos(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            Pageable pageable
    );


    /**
     * Agrupa el stock actual por categoría.
     */
    @Query("""
        SELECT
            COALESCE(c.nombre, 'Sin categoria'),
            COALESCE(SUM(p.stockActual), 0),
            COUNT(p)
        FROM Producto p
        LEFT JOIN p.categoria c
        WHERE p.estado = 'ACTIVO'
        GROUP BY c.idCategoria, c.nombre
        ORDER BY SUM(p.stockActual) DESC
        """)
    List<Object[]> inventarioPorCategoria();


    /**
     * Agrupa las ventas por categoría en un rango de fechas.
     */
    @Query("""
        SELECT
            COALESCE(c.nombre, 'Sin categoria'),
            COALESCE(SUM(dv.subtotalLinea), 0),
            COALESCE(SUM(dv.cantidad), 0)
        FROM DetalleVenta dv
        JOIN dv.producto p
        LEFT JOIN p.categoria c
        WHERE dv.venta.estado = 'COMPLETADA'
        AND dv.venta.fecha BETWEEN :inicio AND :fin
        GROUP BY c.idCategoria, c.nombre
        ORDER BY SUM(dv.subtotalLinea) DESC
        """)
    List<Object[]> ventasPorCategoriaPeriodo(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );


    /**
     * Obtiene el desglose de ventas y stock por producto en un período.
     */
    @Query("""
        SELECT
            p.uniqueID,
            p.nombre,
            COALESCE(c.nombre, 'Sin categoria'),
            COALESCE(SUM(dv.cantidad), 0),
            COALESCE(SUM(dv.subtotalLinea), 0),
            p.costo,
            p.precioVenta,
            p.stockActual,
            p.stockMinimo,
            c.idCategoria
        FROM Producto p
        LEFT JOIN p.categoria c
        LEFT JOIN DetalleVenta dv ON dv.producto.uniqueID = p.uniqueID
            AND dv.venta.estado = 'COMPLETADA'
            AND dv.venta.fecha BETWEEN :inicio AND :fin
        WHERE p.estado = 'ACTIVO'
        GROUP BY p.uniqueID, p.nombre, c.nombre, p.costo, p.precioVenta, p.stockActual, p.stockMinimo, c.idCategoria
        ORDER BY SUM(dv.subtotalLinea) DESC, p.nombre ASC
        """)
    List<Object[]> productosAnaliticaPeriodo(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );


    /**
     * Cuenta los productos activos.
     */
    @Query("""
        SELECT COUNT(p)
        FROM Producto p
        WHERE p.estado = 'ACTIVO'
        """)
    Long contarProductosActivos();


    /**
     * Cuenta los productos con stock bajo.
     */
    @Query("""
        SELECT COUNT(p)
        FROM Producto p
        WHERE p.estado = 'ACTIVO'
        AND p.stockActual <= p.stockMinimo
        """)
    Long contarProductosStockBajo();


    /**
     * Cuenta los productos diferentes que tienen
     * lotes disponibles próximos a vencer.
     */
    @Query("""
        SELECT COUNT(DISTINCT l.producto.uniqueID)
        FROM Lote l
        WHERE l.cantidad > 0
        AND l.fechaVencimiento BETWEEN :hoy AND :fechaLimite
        """)
    Long contarProductosPorVencer(
            @Param("hoy") LocalDate hoy,
            @Param("fechaLimite") LocalDate fechaLimite
    );


    /**
     * Calcula el valor estimado de inventario en riesgo de vencimiento.
     */
    @Query("""
        SELECT COALESCE(SUM(l.cantidad * p.costo), 0)
        FROM Lote l
        JOIN l.producto p
        WHERE l.cantidad > 0
        AND l.fechaVencimiento BETWEEN :hoy AND :fechaLimite
        """)
    Double calcularValorLotesPorVencer(
            @Param("hoy") LocalDate hoy,
            @Param("fechaLimite") LocalDate fechaLimite
    );


    /**
     * Obtiene los productos cuyo stock actual
     * es menor o igual al stock mínimo.
     */
    @Query("""
        SELECT
            p.uniqueID,
            p.nombre,
            p.stockActual,
            p.stockMinimo
        FROM Producto p
        WHERE p.estado = 'ACTIVO'
        AND p.stockActual <= p.stockMinimo
        ORDER BY p.stockActual ASC
        """)
    List<Object[]> productosConStockBajo();


    /**
     * Calcula el valor total del inventario activo.
     */
    @Query("""
        SELECT COALESCE(SUM(p.stockActual * p.costo), 0)
        FROM Producto p
        WHERE p.estado = 'ACTIVO'
        """)
    Double calcularValorInventario();


    /**
     * Total monetario de compras a proveedores en un período.
     */
    @Query("""
        SELECT COALESCE(SUM(cp.total), 0)
        FROM CompraProveedor cp
        WHERE cp.fechaRecepcion BETWEEN :inicio AND :fin
        """)
    Double totalComprasProveedor(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );


    /**
     * Total monetario de recepciones de compra en un período.
     */
    @Query("""
        SELECT COALESCE(SUM(rc.totalRecepcion), 0)
        FROM RecepcionCompra rc
        WHERE rc.fechaRecepcion BETWEEN :inicio AND :fin
        """)
    Double totalRecepcionesCompra(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

}