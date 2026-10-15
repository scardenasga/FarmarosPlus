package co.edu.unbosque.backend.repository;

import co.edu.unbosque.backend.model.entity.PagoVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para los pagos asociados a una venta.
 * Facilita la consulta de recaudo por tipo y por transacción.
 *
 * @author Sebastian Cardenas Garcia
 */
@Repository
public interface PagoVentaRepository extends JpaRepository<PagoVenta, Long> {

    /**
     * Recupera los pagos registrados para una venta.
     *
     * @param idVenta identificador de la venta
     * @return pagos asociados
     */
    List<PagoVenta> findByVenta_IdVentaOrderByIdPagoAsc(Long idVenta);

    /**
     * Lista pagos por tipo.
     *
     * @param tipo tipo de pago
     * @return pagos filtrados
     */
    List<PagoVenta> findByTipoOrderByIdPagoDesc(String tipo);

    /**
     * Calcula el recaudo por un tipo de pago dentro de un rango.
     *
     * @param tipo tipo de pago
     * @param fechaInicio inicio inclusivo
     * @param fechaFin fin inclusivo
     * @return total recaudado
     */
    @Query("""
            SELECT COALESCE(SUM(p.monto), 0)
            FROM PagoVenta p
            WHERE p.tipo = :tipo
              AND p.venta.estado = 'COMPLETADA'
              AND p.venta.fecha BETWEEN :fechaInicio AND :fechaFin
            """)
    Double totalRecaudadoPorTipoEnRango(
            @Param("tipo") String tipo,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );
}
