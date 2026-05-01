package co.edu.unbosque.backend.model.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de salida para orden de compra (detallada).
 */
public record OrdenCompraResponse(
        Long idOrden,
        ProveedorResponse proveedor,
        UsuarioResumenResponse usuario,
        LocalDateTime fechaPedido,
        LocalDateTime fechaEsperada,
        String estado,
        Double totalEsperado,
        String observaciones,
        List<DetalleOrdenCompraResponse> detalles,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {
}
