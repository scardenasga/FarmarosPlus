package co.edu.unbosque.backend.repository;

import co.edu.unbosque.backend.model.entity.DevolucionProveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de acceso a datos para {@link DevolucionProveedor}.
 *
 * @author juanjo2748
 */
@Repository
public interface DevolucionProveedorRepository extends JpaRepository<DevolucionProveedor, Long> {

    List<DevolucionProveedor> findAllByOrderByFechaDesc();

    List<DevolucionProveedor> findByProveedor_IdProveedorOrderByFechaDesc(Long idProveedor);

    @Query("SELECT d FROM DevolucionProveedor d LEFT JOIN FETCH d.detalles WHERE d.idDevolucion = :id")
    Optional<DevolucionProveedor> findWithDetallesById(@Param("id") Long id);
}
