package co.edu.unbosque.backend.model.response;

import java.time.LocalDateTime;

/**
 * DTO de salida para movimientos históricos de inventario.
 *
 * @param id id del movimiento
 * @param productoId id del producto asociado
 * @param nombreProducto nombre snapshot del producto
 * @param loteId id del lote asociado
 * @param tipoMovimiento tipo de movimiento
 * @param cantidadAnterior stock anterior
 * @param cantidadNueva stock nuevo
 * @param diferencia diferencia aplicada
 * @param motivo motivo registrado
 * @param referenciaDocumento referencia documental
 * @param usuarioResponsable usuario responsable
 * @param fechaMovimiento fecha de creación del movimiento
 * @author Sebastian Cardenas Garcia
 */
public record MovimientoInventarioResponse(
        Long id,
        Long productoId,
        String nombreProducto,
        Long loteId,
        String tipoMovimiento,
        Integer cantidadAnterior,
        Integer cantidadNueva,
        Integer diferencia,
        String motivo,
        String referenciaDocumento,
        String usuarioResponsable,
        LocalDateTime fechaMovimiento
) {
}
