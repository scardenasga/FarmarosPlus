package co.edu.unbosque.backend.model.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

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

        @JsonAlias({"condicionDePago", "condicion_pago"})
        String condicionPago
) {
}
