package co.edu.unbosque.backend.repository;

import co.edu.unbosque.backend.model.entity.ConfiguracionSistema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfiguracionSistemaRepository extends JpaRepository<ConfiguracionSistema, Long> {

    Optional<ConfiguracionSistema> findByClave(String clave);

    boolean existsByClave(String clave);
}
