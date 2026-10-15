package co.edu.unbosque.backend.model.request;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para registrar un reclamo u observación sobre un proveedor.
 */
public record CrearNotaProveedorRequest(
        @NotBlank(message = "El tipo de nota es obligatorio")
        String tipoNota,

        @NotBlank(message = "El título de la nota es obligatorio")
        String titulo,

        @NotBlank(message = "La descripción de la nota es obligatoria")
        String descripcion,

        Long idOrdenRelacionada
) {
}
