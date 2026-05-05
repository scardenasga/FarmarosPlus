package co.edu.unbosque.backend.repository;

import co.edu.unbosque.backend.model.entity.CompraProveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CompraProveedorRepository extends JpaRepository<CompraProveedor, Long> {

    List<CompraProveedor> findAllByOrderByFechaRecepcionDesc();

    List<CompraProveedor> findByProveedor_IdProveedorOrderByFechaRecepcionDesc(Long idProveedor);

    @Query("SELECT c FROM CompraProveedor c LEFT JOIN FETCH c.detalles WHERE c.idCompra = :id")
    Optional<CompraProveedor> findWithDetallesById(@Param("id") Long id);
}
