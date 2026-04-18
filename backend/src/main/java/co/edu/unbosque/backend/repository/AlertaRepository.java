package co.edu.unbosque.backend.repository;

import co.edu.unbosque.backend.model.entity.AlertaInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de acceso a datos para {@link AlertaInventario}.
 *
 * @author juanjo2748
 */
@Repository
public interface AlertaRepository extends JpaRepository<AlertaInventario, Long> {

    List<AlertaInventario> findAllByOrderByFechaGeneracionDesc();

    List<AlertaInventario> findByLeidaFalseOrderByFechaGeneracionDesc();

    boolean existsByIdProductoAndTipoAndLeidaFalse(Long idProducto, String tipo);

    boolean existsByIdLoteAndTipoAndLeidaFalse(Long idLote, String tipo);

    @Modifying
    @Query("UPDATE AlertaInventario a SET a.leida = true WHERE a.leida = false")
    void marcarTodasComoLeidas();
}
