package co.edu.unbosque.backend.repository;

import co.edu.unbosque.backend.model.entity.OrdenCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para la entidad OrdenCompra.
 */
@Repository
public interface OrdenCompraRepository extends JpaRepository<OrdenCompra, Long> {
    List<OrdenCompra> findByEstadoOrderByFechaPedidoDesc(String estado);
    List<OrdenCompra> findByProveedor_IdProveedorOrderByFechaPedidoDesc(Long proveedorId);
}
