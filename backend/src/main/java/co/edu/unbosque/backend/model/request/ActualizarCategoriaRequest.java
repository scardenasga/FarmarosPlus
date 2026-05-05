package co.edu.unbosque.backend.model.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Comando de entrada para actualizar una categoria de producto.
 *
 * @param nombre      nombre único de la categoria
 * @param descripcion descripción opcional
 * @author juanjo2748
 */
public record ActualizarCategoriaRequest(
        @NotBlank(message = "El nombre de la categoria es obligatorio")
        String nombre,
        String descripcion
) {
}
