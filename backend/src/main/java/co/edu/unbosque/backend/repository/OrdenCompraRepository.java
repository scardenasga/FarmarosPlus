package co.edu.unbosque.backend.repository;

import co.edu.unbosque.backend.model.entity.OrdenCompra;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para la entidad OrdenCompra.
 * @author Angie Tatiana Ortiz
 */
@Repository
public interface OrdenCompraRepository extends JpaRepository<OrdenCompra, Long> {
    @EntityGraph(attributePaths = {"proveedor", "usuario"})
    List<OrdenCompra> findByEstadoOrderByFechaPedidoDesc(String estado);
    @EntityGraph(attributePaths = {"proveedor", "usuario"})
    List<OrdenCompra> findByProveedor_IdProveedorOrderByFechaPedidoDesc(Long proveedorId);
    boolean existsByUsuario_IdUsuario(Long idUsuario);

    @Override
    @EntityGraph(attributePaths = {"proveedor", "usuario"})
    List<OrdenCompra> findAll();
}
