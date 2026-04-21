package co.edu.unbosque.backend.repository;

import co.edu.unbosque.backend.model.entity.DetalleDevolucionProveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de acceso a datos para {@link DetalleDevolucionProveedor}.
 *
 * @author juanjo2748
 */
@Repository
public interface DetalleDevolucionProveedorRepository extends JpaRepository<DetalleDevolucionProveedor, Long> {

    List<DetalleDevolucionProveedor> findByDevolucion_IdDevolucion(Long idDevolucion);

    List<DetalleDevolucionProveedor> findByProducto_UniqueID(Long idProducto);
}
