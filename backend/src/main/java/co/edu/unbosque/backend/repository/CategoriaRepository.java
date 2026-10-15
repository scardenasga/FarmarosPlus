package co.edu.unbosque.backend.repository;

import co.edu.unbosque.backend.model.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de acceso a datos para {@link Categoria}.
 *
 * @author Sebastian Cardenas Garcia
 */
@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    /**
     * Busca una categoría por nombre sin distinguir mayúsculas.
     *
     * @param nombre nombre a consultar
     * @return categoría coincidente si existe
     */
    Optional<Categoria> findByNombreIgnoreCase(String nombre);

    /**
     * Verifica si ya existe una categoría con un nombre dado.
     *
     * @param nombre nombre a validar
     * @return {@code true} si el nombre ya existe
     */
    boolean existsByNombreIgnoreCase(String nombre);

    /**
     * Busca categorías por coincidencia parcial del nombre.
     *
     * @param nombre fragmento del nombre
     * @return categorías encontradas
     */
    List<Categoria> findByNombreContainingIgnoreCaseOrderByNombreAsc(String nombre);
}
