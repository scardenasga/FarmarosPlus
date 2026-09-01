package co.edu.unbosque.backend.repository;

import co.edu.unbosque.backend.model.entity.UsuarioPermiso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioPermisoRepository extends JpaRepository<UsuarioPermiso, Long> {
    List<UsuarioPermiso> findByUsuario_IdUsuario(Long idUsuario);
    Optional<UsuarioPermiso> findByUsuario_IdUsuarioAndPermiso_Clave(Long idUsuario, String clave);
    void deleteByUsuario_IdUsuario(Long idUsuario);
}
