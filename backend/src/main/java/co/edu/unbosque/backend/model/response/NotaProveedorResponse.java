package co.edu.unbosque.backend.model.response;

import java.time.LocalDateTime;

/**
 * DTO de salida para reclamos/observaciones de proveedor.
 */
public record NotaProveedorResponse(
        Long idNota,
        String tipoNota,
        String titulo,
        String descripcion,
        LocalDateTime fechaCreacion,
        String usuarioCreacion,
        Long idOrdenRelacionada
) {
}
