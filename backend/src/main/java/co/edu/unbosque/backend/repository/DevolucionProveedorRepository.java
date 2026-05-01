package co.edu.unbosque.backend.repository;

import co.edu.unbosque.backend.model.entity.DevolucionProveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la entidad DevolucionProveedor.
 */
@Repository
public interface DevolucionProveedorRepository extends JpaRepository<DevolucionProveedor, Long> {
}
