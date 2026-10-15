package co.edu.unbosque.backend.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Request para crear un cierre de caja.
 * Si fechaApertura/fechaCierre no se envían, se usa el día actual (00:00 a 23:59:59).
 */
public record CierreCajaRequest(
        @Schema(description = "Fecha inicio del periodo a cerrar (inclusive)", example = "2026-09-05T00:00:00")
        LocalDateTime fechaApertura,

        @Schema(description = "Fecha fin del periodo a cerrar (inclusive)", example = "2026-09-05T23:59:59")
        LocalDateTime fechaCierre,

        @Schema(description = "Monto inicial en caja al abrir turno", example = "50000.0")
        Double montoInicial,

        @Schema(description = "Monto en efectivo contado al cerrar (para calcular diferencia)", example = "850000.0")
        Double montoDeclarado,

        @Schema(description = "Id del usuario/vendedor a filtrar. Si no se envía, incluye todas las ventas del periodo.")
        Long idUsuario,

        @Schema(description = "Observaciones del cierre", example = "Turno mañana sin novedades")
        String observaciones
) {}
