package co.edu.unbosque.backend.model.response;

import java.util.List;

/**
 * DTO para enviar el estado de seguimiento de compras y las alertas detalladas.
 * CUMPLE CON EL CRITERIO DE ACEPTACIÓN #3.
 */
public record ResumenAlertasComprasResponse(
    long totalOrdenes,
    long pendientesRecibirOPagar,
    long vencidasAtrasadas,
    List<AlertaOrdenDetalle> alertasDetalladas
) {
    /**
     * DTO interno para cada alerta individual.
     */
    public record AlertaOrdenDetalle(
        Long idOrden,
        String codigoGenerado, // Ej: OC-2026-001
        String proveedorNombre,
        String estadoActual, // PENDIENTE | NO_RECIBIDA
        String fechaCreacionFormateada, // dd/MM/yyyy
        long diasTranscurridos
    ) {}
}