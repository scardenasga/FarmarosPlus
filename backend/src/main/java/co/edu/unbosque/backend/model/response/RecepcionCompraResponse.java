package co.edu.unbosque.backend.model.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de salida para recepción de compra.
 */
public record RecepcionCompraResponse(
        Long idRecepcion,
        OrdenCompraResumenResponse orden,
        UsuarioResumenResponse usuario,
        LocalDateTime fechaRecepcion,
        String observaciones,
        String estado,
        Double totalRecepcion,
        String estadoPago,
        Double montoPagado,
        LocalDateTime fechaLimitePago,
        List<DetalleRecepcionCompraResponse> detalles,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {
}
