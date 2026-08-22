package co.edu.unbosque.backend.controller;

import co.edu.unbosque.backend.model.response.AnaliticaDashboardResponse;
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
 * Controlador REST para el dashboard y analítica avanzada.
 *
 * Expone los indicadores principales del negocio:
 * ventas del día, ventas del mes, compras del mes, ganancia del mes,
 * margen de ganancia, stock bajo, productos próximos a vencer,
 * comparativa mensual y módulo interactivo de analítica avanzada.
 *
 * @author Angie Tatiana Ortiz
 * @author Sebastian Cardenas Garcia
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
            description = "Retorna ventas, compras, ganancia, margen, stock bajo, próximos vencimientos, gráficas y productos más vendidos"
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

    /**
     * Retorna las estadísticas detalladas y configurables para el módulo de analítica avanzada.
     *
     * @param fechaInicio fecha inicial del período
     * @param fechaFin fecha final del período
     * @param idCategoria filtro opcional por categoría
     * @param idProducto filtro opcional por producto
     * @param comparar si se debe calcular la serie de comparación con el período previo
     * @return información de analítica avanzada
     */
    @GetMapping("/analitica")
    @Operation(
            summary = "Obtener datos del módulo de analítica avanzada",
            description = "Retorna KPIs filtrados, tendencias, comparativas históricas, distribución por categoría, tabla detallada e insights automáticos"
    )
    public ResponseEntity<AnaliticaDashboardResponse> obtenerAnalitica(

            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate fechaInicio,

            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate fechaFin,

            @RequestParam(required = false)
            Long idCategoria,

            @RequestParam(required = false)
            Long idProducto,

            @RequestParam(required = false, defaultValue = "false")
            boolean comparar

    ) {

        LocalDate fin = fechaFin != null
                ? fechaFin
                : LocalDate.now();

        LocalDate inicio = fechaInicio != null
                ? fechaInicio
                : fin.minusDays(30);

        return ResponseEntity.ok(
                dashboardService.obtenerAnalitica(
                        inicio.atStartOfDay(),
                        fin.atTime(23, 59, 59),
                        idCategoria,
                        idProducto,
                        comparar
                )
        );
    }
}