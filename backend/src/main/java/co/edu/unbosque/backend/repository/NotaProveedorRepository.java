package co.edu.unbosque.backend.repository;

import co.edu.unbosque.backend.model.entity.NotaProveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para la entidad NotaProveedor.
 */
@Repository
public interface NotaProveedorRepository extends JpaRepository<NotaProveedor, Long> {

    List<NotaProveedor> findByProveedor_IdProveedorOrderByFechaCreacionDesc(Long idProveedor);
}