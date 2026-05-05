package co.edu.unbosque.backend.model.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de salida para una devolución a proveedor.
 *
 * @author juanjo2748
 */
public record DevolucionResponse(
        Long id,
        Long idProveedor,
        String nombreProveedor,
        String usuarioResponsable,
        String motivo,
        LocalDateTime fecha,
        List<DetalleDevolucionResponse> detalles
) {
}
