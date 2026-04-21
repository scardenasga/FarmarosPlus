package co.edu.unbosque.backend.repository;

import co.edu.unbosque.backend.model.entity.DevolucionProveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio de acceso a datos para {@link DevolucionProveedor}.
 *
 * @author juanjo2748
 */
@Repository
public interface DevolucionProveedorRepository extends JpaRepository<DevolucionProveedor, Long> {

    List<DevolucionProveedor> findByProveedor_IdProveedorOrderByFechaDesc(Long idProveedor);

    List<DevolucionProveedor> findByFechaBetweenOrderByFechaDesc(LocalDateTime desde, LocalDateTime hasta);

    List<DevolucionProveedor> findAllByOrderByFechaDesc();

    @Query("""
            SELECT d FROM DevolucionProveedor d
            JOIN FETCH d.proveedor
            JOIN FETCH d.usuario
            LEFT JOIN FETCH d.detalles det
            LEFT JOIN FETCH det.producto
            LEFT JOIN FETCH det.lote
            WHERE d.idDevolucion = :id
            """)
    Optional<DevolucionProveedor> findWithDetallesById(@Param("id") Long id);
}
