package co.edu.unbosque.backend.model.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de salida para ventas con su detalle completo.
 *
 * @param id id de la venta
 * @param usuario usuario que registró la venta
 * @param fecha fecha de registro
 * @param estado estado de la venta
 * @param motivoAnulacion motivo de anulación, si aplica
 * @param subtotal subtotal calculado
 * @param descuento descuento aplicado
 * @param total total final
 * @param detalles líneas de la venta
 * @param pagos pagos asociados
 * @author Sebastian Cardenas Garcia
 */
public record VentaResponse(
        Long id,
        UsuarioResumenResponse usuario,
        LocalDateTime fecha,
        String estado,
        String motivoAnulacion,
        Double subtotal,
        Double iva,
        Double descuento,
        Double total,
        Double cambio,
        List<DetalleVentaResponse> detalles,
        List<PagoVentaResponse> pagos
) {
}
