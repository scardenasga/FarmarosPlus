package co.edu.unbosque.backend.repository;

import co.edu.unbosque.backend.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de acceso a datos para {@link Usuario}.
 *
 * @author Sebastian Cardenas Garcia
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por nombre de usuario.
     *
     * @param username credencial de acceso
     * @return usuario si existe
     */
    Optional<Usuario> findByUsername(String username);

    /**
     * Verifica existencia de un nombre de usuario.
     *
     * @param username nombre de usuario
     * @return {@code true} si ya existe
     */
    boolean existsByUsername(String username);

    /**
     * Lista usuarios por estado.
     *
     * @param estado estado del usuario
     * @return usuarios filtrados
     */
    List<Usuario> findByEstadoOrderByNombreCompletoAsc(String estado);

    /**
     * Lista usuarios por rol.
     *
     * @param rol rol del usuario
     * @return usuarios filtrados
     */
    List<Usuario> findByRolOrderByNombreCompletoAsc(String rol);
}
