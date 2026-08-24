package co.edu.unbosque.backend.repository;

import co.edu.unbosque.backend.model.entity.ProveedorProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad ProveedorProducto.
 */
@Repository
public interface ProveedorProductoRepository extends JpaRepository<ProveedorProducto, Long> {
    @Query("""
            SELECT pp
            FROM ProveedorProducto pp
            JOIN FETCH pp.producto p
            WHERE pp.proveedor.idProveedor = :proveedorId
            ORDER BY p.nombre ASC
            """)
    List<ProveedorProducto> findByProveedorIdWithProductoOrderByProductoNombreAsc(@Param("proveedorId") Long proveedorId);

    @Query("""
            SELECT pp
            FROM ProveedorProducto pp
            JOIN FETCH pp.producto p
            JOIN FETCH pp.proveedor pr
            WHERE pr.idProveedor = :proveedorId
              AND p.uniqueID = :productoId
            """)
    Optional<ProveedorProducto> findByProveedorIdAndProductoId(
            @Param("proveedorId") Long proveedorId,
            @Param("productoId") Long productoId
    );

    List<ProveedorProducto> findByProducto_UniqueID(Long idProducto);
}
