package co.edu.unbosque.backend.model.response;

import java.time.LocalDateTime;

public record CierreCajaResponse(
        Long id,
        LocalDateTime fechaApertura,
        LocalDateTime fechaCierre,
        Long idUsuario,
        String username,
        Double montoInicial,
        Double montoDeclarado,
        Double totalEfectivo,
        Double totalTarjeta,
        Double totalTransferencia,
        Double totalVentas,
        Double totalDescuentos,
        Double totalIva,
        Integer cantidadVentas,
        Integer cantidadAnuladas,
        Double gananciaEstimada,
        Double diferencia,
        String observaciones,
        String estado,
        LocalDateTime fechaCreacion
) {}
