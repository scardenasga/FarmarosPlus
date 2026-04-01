package co.edu.unbosque.backend.model.response;

/**
 * DTO de salida resumido para categorías de producto.
 *
 * @param id id de la categoría
 * @param nombre nombre de la categoría
 * @param descripcion descripción funcional
 * @author Sebastian Cardenas Garcia
 */
public record CategoriaResponse(
        Long id,
        String nombre,
        String descripcion
) {
}
