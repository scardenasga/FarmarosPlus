package co.edu.unbosque.backend.model.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de salida para una compra a proveedor.
 *
 * @author juanjo2748
 */
public record CompraResponse(
        Long id,
        Long idProveedor,
        String nombreProveedor,
        String usuarioResponsable,
        String numeroFactura,
        String notas,
        LocalDateTime fechaRecepcion,
        Double total,
        List<DetalleCompraResponse> detalles
) {
}
