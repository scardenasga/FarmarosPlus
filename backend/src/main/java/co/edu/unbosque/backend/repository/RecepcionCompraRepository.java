package co.edu.unbosque.backend.repository;

import co.edu.unbosque.backend.model.entity.RecepcionCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para la entidad RecepcionCompra.
 */
@Repository
public interface RecepcionCompraRepository extends JpaRepository<RecepcionCompra, Long> {
    List<RecepcionCompra> findByEstadoPagoOrderByFechaRecepcionDesc(String estadoPago);
    List<RecepcionCompra> findByOrden_IdOrdenOrderByFechaRecepcionDesc(Long ordenId);
}
