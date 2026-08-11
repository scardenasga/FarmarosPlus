package co.edu.unbosque.backend.model.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de salida para una devolución a cliente.
 */
public record DevolucionClienteResponse(
        Long id,
        Long idVenta,
        String nombreCliente,
        String documentoCliente,
        String usuarioResponsable,
        String motivo,
        LocalDateTime fecha,
        List<DetalleDevolucionClienteResponse> detalles
) {
}
