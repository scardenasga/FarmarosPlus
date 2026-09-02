package co.edu.unbosque.backend.recomendacion.api;

import co.edu.unbosque.backend.recomendacion.application.RecomendacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Endpoint POS para recomendaciones de canasta.
 */
@RestController
@RequestMapping("/api/recomendaciones")
@Tag(name = "Recomendaciones", description = "Recomendaciones de canasta basadas en FP-Growth para el POS")
public class RecomendacionController {

    private final RecomendacionService recomendacionService;

    public RecomendacionController(RecomendacionService recomendacionService) {
        this.recomendacionService = recomendacionService;
    }

    @GetMapping("/pos")
    @Operation(
            summary = "Recomendaciones para el carrito POS",
            description = "Devuelve hasta 5 productos que suelen llevarse junto a los del carrito. "
                    + "Usa reglas FP-Growth minadas sobre ventas COMPLETADA. "
                    + "Filtra por productos ACTIVO con stock >0 y excluye prescripcion por defecto. "
                    + "Si no hay reglas aplicables, hace fallback a productos populares. "
                    + "Nunca expone datos personales ni ventas individuales.",
            parameters = {
                    @Parameter(name = "productoIds", description = "IDs de productos en el carrito (coma o repetido)", required = true),
                    @Parameter(name = "limit", description = "Maximo de recomendaciones (1..5, default 5)")
            }
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Lista de recomendaciones",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RecomendacionResponse.class),
                    examples = @ExampleObject(value = """
                            {
                              "origen": "FPGROWTH",
                              "actualizadoEn": "2026-08-31T10:15:00",
                              "totalVentasAnalizadas": 16,
                              "recomendaciones": [
                                {
                                  "productoId": 9,
                                  "nombre": "Vitamina C 1000mg",
                                  "soporte": 0.125,
                                  "confianza": 1.0,
                                  "lift": 8.0,
                                  "frecuenciaConjunta": 2
                                }
                              ]
                            }
                            """)
            )
    )
    public ResponseEntity<RecomendacionResponse> recomendarPos(
            @RequestParam(value = "productoIds", required = false) java.util.List<String> productoIds,
            @RequestParam(defaultValue = "5") int limit
    ) {
        Set<Long> ids = parseProductoIds(productoIds);
        if (ids.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (ids.size() > 20) {
            return ResponseEntity.badRequest().build();
        }
        if (limit < 1 || limit > 5) {
            return ResponseEntity.badRequest().build();
        }
        RecomendacionResponse resp = recomendacionService.recomendar(ids, limit);
        return ResponseEntity.ok(resp);
    }

    /**
     * Soporta ?productoIds=1,9 y ?productoIds=1&productoIds=9
     */
    private Set<Long> parseProductoIds(java.util.List<String> productoIds) {
        Set<Long> result = new LinkedHashSet<>();
        if (productoIds != null) {
            for (String m : productoIds) {
                if (m == null) continue;
                String[] parts = m.split(",");
                for (String p : parts) {
                    p = p.trim();
                    if (!p.isEmpty()) {
                        try {
                            result.add(Long.valueOf(p));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }
        }
        return result;
    }
}
