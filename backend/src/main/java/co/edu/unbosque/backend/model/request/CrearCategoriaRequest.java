package co.edu.unbosque.backend.model.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Comando de entrada para crear una categoria de producto.
 *
 * @param nombre nombre unico de la categoria
 * @param descripcion descripcion opcional
 * @author Sebastian Cardenas Garcia
 */
public record CrearCategoriaRequest(
        @NotBlank(message = "El nombre de la categoria es obligatorio")
        String nombre,
        String descripcion
) {
}
