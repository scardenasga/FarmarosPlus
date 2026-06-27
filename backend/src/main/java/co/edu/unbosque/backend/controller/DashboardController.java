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
 * Controlador REST para el dashboard administrativo.
 * Expone indicadores agregados de ventas, inventario y productos destacados.
 *
 * @author Angie Tatiana Ortiz
 */
@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Indicadores agregados del negocio para el dashboard administrativo")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Retorna los indicadores del dashboard para un período dado.
     * Si no se envían fechas, se usan los últimos 30 días por defecto.
     *
     * @param fechaInicio inicio del período en formato dd-MM-yyyy
     * @param fechaFin    fin del período en formato dd-MM-yyyy 
     * @return respuesta con KPIs, ventas por día, inventario y productos destacados
     */
    @GetMapping
    @Operation(summary = "Obtener indicadores del dashboard administrativo")
    public ResponseEntity<DashboardResponse> obtenerDashboard(
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fechaInicio,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fechaFin
    ) {
        LocalDate fin = fechaFin != null ? fechaFin : LocalDate.now();
        LocalDate inicio = fechaInicio != null ? fechaInicio : fin.minusDays(30);

        return ResponseEntity.ok(
                dashboardService.obtenerDashboard(
                        inicio.atStartOfDay(),
                        fin.atTime(23, 59, 59)
                )
        );
    }
}