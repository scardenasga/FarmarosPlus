package co.edu.unbosque.backend.model.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Cumplimiento de entregas a tiempo de un proveedor.
 */
@Schema(description = "Cumplimiento de entregas a tiempo por proveedor")
public record CumplimientoProveedorResponse(
        String proveedor,
        long recepcionesTotales,
        long entregasATiempo,
        Double porcentajeCumplimiento
) {
}
