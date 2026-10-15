package co.edu.unbosque.backend.model.response;

import java.time.LocalDateTime;

/**
 * DTO de salida para orden de compra (resumen).
 */
public record OrdenCompraResumenResponse(
        Long idOrden,
        Long proveedorId,
        String proveedorNombre,
        LocalDateTime fechaPedido,
        LocalDateTime fechaEsperada,
        String estado,
        Double totalEsperado
) {
}
