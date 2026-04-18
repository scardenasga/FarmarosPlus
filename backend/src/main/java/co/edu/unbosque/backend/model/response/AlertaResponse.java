package co.edu.unbosque.backend.model.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de salida para alertas de inventario.
 *
 * @param idAlerta          identificador de la alerta
 * @param tipo              STOCK_MINIMO | PROXIMO_VENCIMIENTO
 * @param idProducto        id del producto afectado
 * @param nombreProducto    nombre del producto afectado
 * @param idLote            id del lote (solo para PROXIMO_VENCIMIENTO)
 * @param numeroLote        número del lote (solo para PROXIMO_VENCIMIENTO)
 * @param cantidadActual    stock actual (solo para STOCK_MINIMO)
 * @param stockMinimo       stock mínimo configurado (solo para STOCK_MINIMO)
 * @param fechaVencimiento  fecha de vencimiento del lote (solo para PROXIMO_VENCIMIENTO)
 * @param leida             si el admin ya leyó la alerta
 * @param fechaGeneracion   fecha y hora en que se generó
 * @author juanjo2748
 */
public record AlertaResponse(
        Long idAlerta,
        String tipo,
        Long idProducto,
        String nombreProducto,
        Long idLote,
        String numeroLote,
        Integer cantidadActual,
        Integer stockMinimo,
        LocalDate fechaVencimiento,
        boolean leida,
        LocalDateTime fechaGeneracion
) {
}
