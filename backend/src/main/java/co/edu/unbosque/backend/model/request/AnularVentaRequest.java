package co.edu.unbosque.backend.model.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Comando de entrada para anular una venta existente.
 *
 * @param motivoAnulacion motivo funcional de la anulación
 * @param usuarioResponsable usuario o actor responsable de la anulación
 * @author Sebastian Cardenas Garcia
 * @author Angie Tatiana Ortiz
 */
public record AnularVentaRequest(
        @NotBlank(message = "El motivoAnulacion es obligatorio")
        String motivoAnulacion,
        @NotBlank(message = "El usuarioResponsable es obligatorio")
        String usuarioResponsable,
        Long usuarioId,
        Boolean confirmacion

) {
}
