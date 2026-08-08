package co.edu.unbosque.backend.controller;

import co.edu.unbosque.backend.model.response.DashboardResponse;
import co.edu.unbosque.backend.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Controlador REST para el dashboard.
 *
 * Expone los indicadores principales del negocio:
 * ventas del día, ventas del mes, stock bajo,
 * productos próximos a vencer y estadísticas de ventas.
 *
 * @author Angie Tatiana Ortiz
 */
@RestController
@RequestMapping("/api/dashboard")
@Tag(
        name = "Dashboard",
        description = "Indicadores y estadísticas del negocio"
)
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Retorna los indicadores del dashboard para un período determinado.
     *
     * Si no se envían fechas, se utilizan los últimos 30 días.
     *
     * @param fechaInicio fecha inicial del período
     * @param fechaFin fecha final del período
     * @return información completa del dashboard
     */
    @GetMapping
    @Operation(
            summary = "Obtener información del dashboard",
            description = "Retorna ventas, stock bajo, próximos vencimientos y productos más vendidos"
    )
    public ResponseEntity<DashboardResponse> obtenerDashboard(

            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate fechaInicio,

            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate fechaFin

    ) {

        LocalDate fin = fechaFin != null
                ? fechaFin
                : LocalDate.now();

        LocalDate inicio = fechaInicio != null
                ? fechaInicio
                : fin.minusDays(30);

        return ResponseEntity.ok(
                dashboardService.obtenerDashboard(
                        inicio.atStartOfDay(),
                        fin.atTime(23, 59, 59)
                )
        );
    }
}