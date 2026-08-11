package co.edu.unbosque.backend.model.request;

/**
 * Solicitud para actualizar una devolución a cliente.
 * Solo permite modificar campos informativos; no altera cantidades ni inventario.
 *
 * @param nombreCliente    nombre del cliente, si se conoce
 * @param documentoCliente documento o identificador del cliente, si se conoce
 * @param motivo           razón de la devolución
 */
public record ActualizarDevolucionClienteRequest(
        String nombreCliente,
        String documentoCliente,
        String motivo
) {
}
