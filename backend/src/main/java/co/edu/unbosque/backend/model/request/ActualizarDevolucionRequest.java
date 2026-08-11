package co.edu.unbosque.backend.model.request;

/**
 * Solicitud para actualizar una devolución a proveedor.
 * Solo permite modificar campos no transaccionales para no alterar el inventario.
 *
 * @param motivo        razón de la devolución (opcional)
 * @param observaciones notas adicionales (opcional)
 */
public record ActualizarDevolucionRequest(
        String motivo,
        String observaciones
) {
}
