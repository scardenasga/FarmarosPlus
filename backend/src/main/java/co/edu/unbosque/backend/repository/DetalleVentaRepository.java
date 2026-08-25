package co.edu.unbosque.backend.repository;

import co.edu.unbosque.backend.model.entity.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio del detalle histórico de ventas.
 * Permite consultar líneas vendidas por producto, venta o periodo.
 *
 * @author Sebastian Cardenas Garcia
 * @author Angie Tatiana Ortiz
 */
@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {

    /**
     * Recupera los detalles de una venta.
     *
     * @param idVenta identificador de la venta
     * @return líneas de la venta
     */
    List<DetalleVenta> findByVenta_IdVentaOrderByIdDetalleAsc(Long idVenta);

    /**
     * Recupera detalles donde intervino un producto.
     *
     * @param idProducto identificador del producto
     * @return líneas históricas del producto
     */
    List<DetalleVenta> findByProducto_UniqueIDOrderByIdDetalleDesc(Long idProducto);

    /**
     * Recupera detalles de venta dentro de un periodo.
     *
     * @param fechaInicio inicio inclusivo
     * @param fechaFin fin inclusivo
     * @return detalles vendidos en el rango
     */
    @Query("""
            SELECT dv
            FROM DetalleVenta dv
            WHERE dv.venta.fecha BETWEEN :fechaInicio AND :fechaFin
            ORDER BY dv.venta.fecha DESC, dv.idDetalle DESC
            """)
    List<DetalleVenta> findByFechaVentaBetween(
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );

    /**
     * Obtiene los identificadores de producto ordenados por cantidad vendida.
     *
     * @param fechaInicio inicio inclusivo
     * @param fechaFin fin inclusivo
     * @return ids de productos más vendidos en orden descendente
     */
    @Query("""
            SELECT dv.producto.uniqueID
            FROM DetalleVenta dv
            WHERE dv.venta.estado = 'COMPLETADA'
              AND dv.venta.fecha BETWEEN :fechaInicio AND :fechaFin
            GROUP BY dv.producto.uniqueID
            ORDER BY SUM(dv.cantidad) DESC
            """)
    List<Long> findProductosMasVendidosIds(
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );

    /**
     * Suma las unidades vendidas por producto dentro de un periodo,
     * considerando solo ventas completadas.
     *
     * @param fechaInicio inicio inclusivo
     * @param fechaFin fin inclusivo
     * @return pares [idProducto, unidadesVendidas]
     */
    @Query("""
            SELECT dv.producto.uniqueID, SUM(dv.cantidad)
            FROM DetalleVenta dv
            WHERE dv.venta.estado = 'COMPLETADA'
              AND dv.venta.fecha BETWEEN :fechaInicio AND :fechaFin
            GROUP BY dv.producto.uniqueID
            """)
    List<Object[]> sumarUnidadesPorProducto(
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );
}
