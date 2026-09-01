package co.edu.unbosque.backend.controller;

import co.edu.unbosque.backend.model.entity.Permiso;
import co.edu.unbosque.backend.model.entity.UsuarioPermiso;
import co.edu.unbosque.backend.service.PermisoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api")
@Tag(name = "Permisos", description = "Gestión de permisos por usuario")
public class PermisoController {

    private final PermisoService permisoService;

    public PermisoController(PermisoService permisoService) {
        this.permisoService = permisoService;
    }

    @GetMapping("/permisos")
    @Operation(summary = "Listar todos los permisos del catálogo")
    public ResponseEntity<List<PermisoResponse>> listar() {
        return ResponseEntity.ok(permisoService.listarTodos().stream().map(this::toResp).toList());
    }

    @GetMapping("/usuarios/{id}/permisos")
    @Operation(summary = "Permisos efectivos de un usuario (rol + overrides)")
    public ResponseEntity<Set<String>> efectivos(@PathVariable Long id) {
        return ResponseEntity.ok(permisoService.permisosEfectivos(id));
    }

    @GetMapping("/usuarios/{id}/permisos/overrides")
    public ResponseEntity<List<UsuarioPermisoResponse>> overrides(@PathVariable Long id) {
        return ResponseEntity.ok(permisoService.listarOverrides(id).stream().map(this::toOverrideResp).toList());
    }

    @PutMapping("/usuarios/{id}/permisos")
    @Operation(summary = "Guardar overrides de permisos por usuario. Body: {clave: boolean} true=GRANT false=DENY. Solo guarda diff vs rol.")
    public ResponseEntity<Set<String>> guardar(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        return ResponseEntity.ok(permisoService.guardarOverrides(id, body));
    }

    private PermisoResponse toResp(Permiso p) {
        return new PermisoResponse(p.getIdPermiso(), p.getClave(), p.getDescripcion(), p.getModulo());
    }

    private UsuarioPermisoResponse toOverrideResp(UsuarioPermiso up) {
        return new UsuarioPermisoResponse(up.getId(), up.getUsuario().getIdUsuario(), up.getPermiso().getClave(), up.getConcedido());
    }

    public record PermisoResponse(Long id, String clave, String descripcion, String modulo) {}
    public record UsuarioPermisoResponse(Long id, Long idUsuario, String clave, Boolean concedido) {}
}
