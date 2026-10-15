package co.edu.unbosque.backend.repository;

import co.edu.unbosque.backend.model.entity.DetalleRecepcionCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio JPA para la entidad DetalleRecepcionCompra.
 */
@Repository
public interface DetalleRecepcionCompraRepository extends JpaRepository<DetalleRecepcionCompra, Long> {

    /**
     * Agrega los productos mas comprados en un periodo: suma unidades y monto
     * (cantidad x costo unitario real) agrupados por nombre de producto,
     * ordenados por mayor gasto.
     *
     * @param fechaInicio inicio inclusivo del periodo
     * @param fechaFin fin inclusivo del periodo
     * @return pares [nombreProducto, unidadesCompradas, montoTotal]
     */
    @Query("""
            SELECT dr.producto.nombre,
                   SUM(dr.cantidadRecibida),
                   SUM(dr.cantidadRecibida * COALESCE(dr.costoUnitarioReal, 0.0))
            FROM DetalleRecepcionCompra dr
            WHERE dr.recepcion.fechaRecepcion BETWEEN :fechaInicio AND :fechaFin
              AND dr.producto IS NOT NULL
            GROUP BY dr.producto.nombre
            ORDER BY 3 DESC
            """)
    List<Object[]> topProductosComprados(
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );
}
