package co.edu.unbosque.backend.model.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Solicitud para crear un proveedor.
 *
 * @param nombre   nombre único del proveedor
 * @param nit      NIT (opcional)
 * @param contacto persona de contacto (opcional)
 * @param telefono teléfono (opcional)
 * @param email    correo electrónico (opcional)
 * @author juanjo2748
 */
public record CrearProveedorRequest(
        @NotBlank(message = "El nombre del proveedor es obligatorio")
        String nombre,
        String nit,
        String contacto,
        String telefono,
        String email
) {
}
