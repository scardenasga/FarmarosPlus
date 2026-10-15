package co.edu.unbosque.backend.repository;

import co.edu.unbosque.backend.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
     * Busca un usuario por username ignorando mayúsculas.
     *
     * @param username nombre de usuario
     * @return usuario encontrado si existe
     */
    Optional<Usuario> findByUsernameIgnoreCase(String username);

    /**
     * Verifica existencia de un nombre de usuario.
     *
     * @param username nombre de usuario
     * @return {@code true} si ya existe
     */
    boolean existsByUsername(String username);

    /**
     * Verifica existencia de un nombre de usuario ignorando mayúsculas.
     *
     * @param username nombre de usuario
     * @return {@code true} si ya existe
     */
    boolean existsByUsernameIgnoreCase(String username);

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

    /**
     * Busca usuarios por coincidencia parcial en username.
     *
     * @param username fragmento del username
     * @return usuarios coincidentes
     */
    List<Usuario> findByUsernameContainingIgnoreCaseOrderByNombreCompletoAsc(String username);

    /**
     * Busca usuarios por coincidencia parcial en nombre completo.
     *
     * @param nombre fragmento del nombre
     * @return usuarios coincidentes
     */
    List<Usuario> findByNombreCompletoContainingIgnoreCaseOrderByNombreCompletoAsc(String nombre);

    /**
     * Busqueda libre por username o nombre completo.
     *
     * @param termino texto de busqueda
     * @return usuarios coincidentes
     */
    @Query("""
            SELECT u
            FROM Usuario u
            WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :termino, '%'))
               OR LOWER(u.nombreCompleto) LIKE LOWER(CONCAT('%', :termino, '%'))
            ORDER BY u.nombreCompleto ASC
            """)
    List<Usuario> buscarPorUsernameONombre(@Param("termino") String termino);
}
