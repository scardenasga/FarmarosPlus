package co.edu.unbosque.backend.repository;

import co.edu.unbosque.backend.model.entity.ConfiguracionAlerta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de acceso a datos para {@link ConfiguracionAlerta}.
 *
 * @author juanjo2748
 */
@Repository
public interface ConfiguracionAlertaRepository extends JpaRepository<ConfiguracionAlerta, Long> {
}
