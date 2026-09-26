package co.edu.unbosque.backend.model.response;

import java.time.LocalDateTime;

/**
 * Resumen en vivo sin persistir, usado para previsualizar el cierre
 * antes de guardarlo. Misma estructura que CierreCajaResponse pero sin id.
 */
public record CierreResumenResponse(
        LocalDateTime fechaApertura,
        LocalDateTime fechaCierre,
        Double montoInicial,
        Double totalEfectivo,
        Double totalTarjeta,
        Double totalTransferencia,
        Double totalVentas,
        Double totalDescuentos,
        Double totalIva,
        Integer cantidadVentas,
        Integer cantidadAnuladas,
        Double gananciaEstimada,
        Double efectivoEsperado
) {}
