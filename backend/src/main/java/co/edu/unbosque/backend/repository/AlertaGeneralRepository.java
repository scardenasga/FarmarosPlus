package co.edu.unbosque.backend.repository;

import co.edu.unbosque.backend.model.entity.Alerta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de acceso a datos para {@link Alerta}.
 *
 * @author juanjo2748
 */
@Repository
public interface AlertaGeneralRepository extends JpaRepository<Alerta, Long> {

    List<Alerta> findByLeidaFalseOrderByFechaGeneracionDesc();

    List<Alerta> findAllByOrderByFechaGeneracionDesc();

    boolean existsByReferenciaAndLeidaFalse(String referencia);

    @Modifying
    @Query("UPDATE Alerta a SET a.leida = true WHERE a.leida = false")
    void marcarTodasComoLeidas();
}
