package co.edu.unbosque.backend.repository;

import co.edu.unbosque.backend.model.entity.DetalleDevolucionCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para detalles de devolución de cliente.
 */
@Repository
public interface DetalleDevolucionClienteRepository extends JpaRepository<DetalleDevolucionCliente, Long> {

    @Query("""
            SELECT COALESCE(SUM(d.cantidad), 0)
            FROM DetalleDevolucionCliente d
            WHERE d.detalleVenta.idDetalle = :idDetalleVenta
            """)
    Integer sumarCantidadDevueltaPorDetalleVenta(@Param("idDetalleVenta") Long idDetalleVenta);
}
