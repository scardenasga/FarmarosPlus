package co.edu.unbosque.backend.repository;

import co.edu.unbosque.backend.model.entity.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para la entidad Proveedor.
 */
@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
    boolean existsByNombreIgnoreCase(String nombre);
    List<Proveedor> findByEstadoOrderByNombreAsc(String estado);
}
