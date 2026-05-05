package co.edu.unbosque.backend.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * DTO para crear un nuevo proveedor.
 */
public record CrearProveedorRequest(
        @NotBlank(message = "El nombre del proveedor es obligatorio")
        String nombre,

        String nit,

        String telefono,

        String email,

        String contacto,

        String condicionPago
) {
}
