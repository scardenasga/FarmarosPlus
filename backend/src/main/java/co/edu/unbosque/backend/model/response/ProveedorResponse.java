package co.edu.unbosque.backend.model.response;

import java.time.LocalDateTime;

/**
 * DTO de salida para proveedores.
 *
 * @param idProveedor  identificador del proveedor
 * @param nombre       nombre del proveedor
 * @param nit          NIT o RUT
 * @param contacto     nombre de la persona de contacto
 * @param telefono     número de teléfono
 * @param email        correo electrónico
 * @param direccion    dirección física
 * @param estado       ACTIVO | INACTIVO
 * @param fechaCreacion fecha de registro
 * @author juanjo2748
 */
public record ProveedorResponse(
        Long idProveedor,
        String nombre,
        String nit,
        String contacto,
        String telefono,
        String email,
        String direccion,
        String estado,
        LocalDateTime fechaCreacion
) {
}
