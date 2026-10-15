package co.edu.unbosque.backend.repository;

import co.edu.unbosque.backend.model.entity.Permiso;
import co.edu.unbosque.backend.model.entity.RolPermiso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolPermisoRepository extends JpaRepository<RolPermiso, Long> {
    List<RolPermiso> findByRol(String rol);
    List<RolPermiso> findByRolIn(List<String> roles);
}
