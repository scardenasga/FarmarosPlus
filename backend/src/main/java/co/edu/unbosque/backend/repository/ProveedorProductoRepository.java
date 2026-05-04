package co.edu.unbosque.backend.repository;

import co.edu.unbosque.backend.model.entity.ProveedorProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la entidad ProveedorProducto.
 */
@Repository
public interface ProveedorProductoRepository extends JpaRepository<ProveedorProducto, Long> {
}
