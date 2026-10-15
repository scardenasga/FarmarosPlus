package co.edu.unbosque.backend.repository;

import co.edu.unbosque.backend.model.entity.DevolucionCliente;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de acceso a datos para devoluciones de cliente.
 */
@Repository
public interface DevolucionClienteRepository extends JpaRepository<DevolucionCliente, Long> {

    List<DevolucionCliente> findAllByOrderByFechaDesc();

    List<DevolucionCliente> findByVenta_IdVentaOrderByFechaDesc(Long idVenta);

    @EntityGraph(attributePaths = {"detalles", "detalles.detalleVenta", "detalles.producto", "detalles.lote", "venta", "usuario"})
    Optional<DevolucionCliente> findWithDetallesByIdDevolucionCliente(Long idDevolucionCliente);

    boolean existsByUsuario_IdUsuario(Long idUsuario);
}
