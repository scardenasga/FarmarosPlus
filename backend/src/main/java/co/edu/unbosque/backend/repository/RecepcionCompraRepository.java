package co.edu.unbosque.backend.repository;

import co.edu.unbosque.backend.model.entity.RecepcionCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para la entidad RecepcionCompra.
 */
@Repository
public interface RecepcionCompraRepository extends JpaRepository<RecepcionCompra, Long> {
    List<RecepcionCompra> findByEstadoPagoOrderByFechaRecepcionDesc(String estadoPago);
    List<RecepcionCompra> findByOrden_IdOrdenOrderByFechaRecepcionDesc(Long ordenId);
    boolean existsByUsuario_IdUsuario(Long idUsuario);

    /**
     * Cumplimiento de entregas a tiempo por proveedor: cuenta recepciones y
     * cuantas llegaron en o antes de la fecha esperada de su orden.
     *
     * @return pares [nombreProveedor, totalRecepciones, recibidasATiempo]
     */
    @Query("""
            SELECT r.orden.proveedor.nombre,
                   COUNT(r),
                   SUM(CASE WHEN r.orden.fechaEsperada IS NOT NULL
                             AND r.fechaRecepcion <= r.orden.fechaEsperada
                            THEN 1 ELSE 0 END)
            FROM RecepcionCompra r
            GROUP BY r.orden.proveedor.nombre
            ORDER BY 2 DESC
            """)
    List<Object[]> cumplimientoPorProveedor();
}
