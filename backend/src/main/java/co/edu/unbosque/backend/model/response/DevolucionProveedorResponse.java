package co.edu.unbosque.backend.model.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de salida para devoluciones a proveedor.
 *
 * @param idDevolucion    identificador de la devolución
 * @param idProveedor     identificador del proveedor
 * @param nombreProveedor nombre del proveedor
 * @param idUsuario       identificador del usuario que registró la devolución
 * @param nombreUsuario   nombre del usuario responsable
 * @param fecha           fecha y hora del registro
 * @param estado          PROCESADA | ANULADA
 * @param motivo          razón general
 * @param detalles        líneas de productos devueltos
 * @author juanjo2748
 */
public record DevolucionProveedorResponse(
        Long idDevolucion,
        Long idProveedor,
        String nombreProveedor,
        Long idUsuario,
        String nombreUsuario,
        LocalDateTime fecha,
        String estado,
        String motivo,
        List<DetalleDevolucionResponse> detalles
) {
    /**
     * Línea de detalle dentro de la respuesta de una devolución.
     *
     * @param idDetalle      identificador del detalle
     * @param idProducto     identificador del producto
     * @param nombreProducto nombre del producto (snapshot)
     * @param idLote         identificador del lote (puede ser null)
     * @param numeroLote     número del lote (snapshot, puede ser null)
     * @param cantidad       unidades devueltas
     * @param motivo         razón específica de esta línea
     */
    public record DetalleDevolucionResponse(
            Long idDetalle,
            Long idProducto,
            String nombreProducto,
            Long idLote,
            String numeroLote,
            Integer cantidad,
            String motivo
    ) {
    }
}
