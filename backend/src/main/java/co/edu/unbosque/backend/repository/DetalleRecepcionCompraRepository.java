package co.edu.unbosque.backend.repository;

import co.edu.unbosque.backend.model.entity.DetalleRecepcionCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la entidad DetalleRecepcionCompra.
 */
@Repository
public interface DetalleRecepcionCompraRepository extends JpaRepository<DetalleRecepcionCompra, Long> {
}
