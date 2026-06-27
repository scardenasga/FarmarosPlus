package co.edu.unbosque.backend.repository;

import co.edu.unbosque.backend.model.entity.Venta;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio de consultas agregadas para el dashboard administrativo.
 * Concentra el resumen de ventas, inventario y productos destacados.
 *
 * @author Angie Tatiana Ortiz
 */
@Repository
public interface DashboardRepository extends JpaRepository<Venta, Long> {

    /**
     * Calcula el total monetario y la cantidad de ventas completadas en un rango.
     *
     * @param inicio inicio  del rango
     * @param fin    fin  del rango
     * @return lista con un arreglo [totalVentas, cantidadVentas]
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
     * Agrupa el total y la cantidad de ventas completadas por dia en un rango.
     *
     * @param inicio inicio 
     * @param fin    fin
     * @return lista de arreglos con [fecha, total, cantidad]
     */
    @Query(value = """
            SELECT DATE(v.fecha) AS fecha,
                   COALESCE(SUM(v.total), 0) AS total,
                   COUNT(v.id_venta) AS cantidad
            FROM venta v
            WHERE v.estado = 'COMPLETADA'
              AND v.fecha BETWEEN :inicio AND :fin
            GROUP BY DATE(v.fecha)
            ORDER BY DATE(v.fecha) ASC
            """, nativeQuery = true)
    List<Object[]> ventasAgrupadasPorDia(
            @Param("inicio") String inicio,
            @Param("fin") String fin
    );

    /**
     * Lista los productos mas vendidos en un periodo, limitados por paginacion.
     *
     * @param inicio   inicio  del rango
     * @param fin      fin del rango
     * @param pageable paginacion para limitar resultados
     * @return lista de arreglos con [idProducto, nombre, cantidadVendida, totalVendido]
     */
    @Query("""
            SELECT p.uniqueID, p.nombre,
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
     * Agrupa el stock total y cantidad de productos activos por categoria.
     *
     * @return lista de arreglos con [categoria, stockTotal, cantidadProductos]
     */
    @Query("""
            SELECT COALESCE(c.nombre, 'Sin categoria'),
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
     * Cuenta los productos en estado ACTIVO.
     *
     * @return cantidad de productos activos
     */
    @Query("""
            SELECT COUNT(p) FROM Producto p WHERE p.estado = 'ACTIVO'
            """)
    Long contarProductosActivos();

    /**
     * Cuenta los productos activos cuyo stock actual no supera el minimo.
     *
     * @return cantidad de productos con stock bajo
     */
    @Query("""
            SELECT COUNT(p) FROM Producto p
            WHERE p.estado = 'ACTIVO' AND p.stockActual <= p.stockMinimo
            """)
    Long contarProductosStockBajo();

    /**
     * Calcula el valor total del inventario activo (stock * costo por producto).
     *
     * @return valor monetario total del inventario
     */
    @Query("""
            SELECT COALESCE(SUM(p.stockActual * p.costo), 0)
            FROM Producto p WHERE p.estado = 'ACTIVO'
            """)
    Double calcularValorInventario();
}