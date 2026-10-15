package co.edu.unbosque.backend.model.request;

import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * DTO para actualización parcial de proveedor.
 */
public record ActualizarProveedorRequest(
        String nombre,
        String nit,
        String telefono,
        String email,
        String contacto,
        @JsonAlias({"condicionDePago", "condicion_pago"})
        String condicionPago
) {
}
