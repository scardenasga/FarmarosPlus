package co.edu.unbosque.backend.repository;

import co.edu.unbosque.backend.model.entity.DetalleDevolucionProveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la entidad DetalleDevolucionProveedor.
 */
@Repository
public interface DetalleDevolucionProveedorRepository extends JpaRepository<DetalleDevolucionProveedor, Long> {
}
