package co.edu.unbosque.backend.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Comando de entrada para crear o actualizar un proveedor.
 *
 * @param nombre    nombre del proveedor (obligatorio, único)
 * @param nit       NIT o RUT del proveedor
 * @param contacto  nombre de la persona de contacto
 * @param telefono  número de teléfono
 * @param email     correo electrónico
 * @param direccion dirección física
 * @author juanjo2748
 */
public record ProveedorRequest(
        @NotBlank(message = "El nombre del proveedor es obligatorio")
        @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
        String nombre,

        @Size(max = 20, message = "El NIT no puede superar 20 caracteres")
        String nit,

        @Size(max = 100, message = "El contacto no puede superar 100 caracteres")
        String contacto,

        @Size(max = 20, message = "El teléfono no puede superar 20 caracteres")
        String telefono,

        @Size(max = 100, message = "El email no puede superar 100 caracteres")
        String email,

        @Size(max = 200, message = "La dirección no puede superar 200 caracteres")
        String direccion
) {
}
