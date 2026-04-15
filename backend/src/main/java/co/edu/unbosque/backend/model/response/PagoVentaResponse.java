package co.edu.unbosque.backend.model.response;

/**
 * DTO de salida para pagos de una venta.
 *
 * @param id id del pago
 * @param tipo tipo de pago
 * @param monto monto pagado
 * @author Sebastian Cardenas Garcia
 */
public record PagoVentaResponse(
        Long id,
        String tipo,
        Double monto
) {
}
