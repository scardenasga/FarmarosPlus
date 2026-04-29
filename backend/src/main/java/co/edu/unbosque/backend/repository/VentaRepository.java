package co.edu.unbosque.backend.repository;

import co.edu.unbosque.backend.model.entity.Venta;

import org.springframework.cglib.core.Local;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio de acceso a datos para {@link Venta}.
 * Expone consultas operativas y de agregación para el módulo de ventas.
 *
 * @author Sebastian Cardenas Garcia
 */
@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    /**
     * Lista ventas por estado.
     *
     * @param estado estado de la venta
     * @return ventas en el estado indicado
     */
    List<Venta> findByEstadoOrderByFechaDesc(String estado);

    /**
     * Lista ventas realizadas por un usuario.
     *
     * @param idUsuario identificador del usuario
     * @return ventas del usuario ordenadas por fecha descendente
     */
    List<Venta> findByUsuario_IdUsuarioOrderByFechaDesc(Long idUsuario);

    /**
     * Lista ventas dentro de un rango temporal.
     *
     * @param fechaInicio inicio inclusivo
     * @param fechaFin fin inclusivo
     * @return ventas del rango indicado
     */
    List<Venta> findByFechaBetweenOrderByFechaDesc(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * Carga una venta con sus relaciones principales para consulta o reversión.
     *
     * @param idVenta identificador de la venta
     * @return venta con detalles, pagos, usuario, productos y lotes
     */
    @EntityGraph(attributePaths = {"detalles", "detalles.producto", "detalles.lote", "pagos", "usuario"})
    Optional<Venta> findWithDetallesAndPagosByIdVenta(Long idVenta);

    /**
     * Calcula el total monetario de ventas completadas en un rango dado.
     *
     * @param fechaInicio inicio inclusivo
     * @param fechaFin fin inclusivo
     * @return suma de totales completados
     */
    @Query("""
            SELECT COALESCE(SUM(v.total), 0)
            FROM Venta v
            WHERE v.estado = 'COMPLETADA'
              AND v.fecha BETWEEN :fechaInicio AND :fechaFin
            """)
    Double calcularTotalVentasCompletadas(
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );

    /**
     * Histórico filtrable: todos los parámetros son opcionales.
     * Si un parámetro llega null se ignora en el WHERE.
     */

    @Query ("""
        SELECT v From Venta v
        JOIN FETCH v.usuario u
        WHERE (:fechaInicio IS NULL OR v.fecha >= :fechaInicio)
          AND (:fechaFin IS NULL OR v.fecha <= :fechaFin) 
          AND (:idVendedor IS NULL OR u.idUsuario = :idVendedor)
          AND (:estado IS NULL OR v.estado = :estado)
        ORDER BY v.fecha DESC

    """)

    @EntityGraph(attributePaths = {"detalles", "detalles.producto", "detalles.lote", "pagos", "usuario"})
    List<Venta> findHistorico (
         @Param("fechaInicio") LocalDateTime fechaInicio,
         @Param("fechaFin") LocalDateTime fechaFin,
         @Param("idVendedor") Long idVendedor,
         @Param("estado") String estado
    );
}
