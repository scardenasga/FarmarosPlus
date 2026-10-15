package co.edu.unbosque.backend.repository;

import co.edu.unbosque.backend.model.entity.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermisoRepository extends JpaRepository<Permiso, Long> {
    Optional<Permiso> findByClave(String clave);
    boolean existsByClave(String clave);
}
