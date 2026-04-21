package co.edu.unbosque.backend.repository;

import co.edu.unbosque.backend.model.entity.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de acceso a datos para {@link Proveedor}.
 *
 * @author juanjo2748
 */
@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    Optional<Proveedor> findByNombre(String nombre);

    boolean existsByNit(String nit);

    boolean existsByNombre(String nombre);

    List<Proveedor> findByEstadoOrderByNombreAsc(String estado);

    List<Proveedor> findAllByOrderByNombreAsc();
}
