package co.edu.unbosque.backend.controller;

import co.edu.unbosque.backend.model.entity.Usuario;
import co.edu.unbosque.backend.model.request.ActualizarEstadoUsuarioRequest;
import co.edu.unbosque.backend.model.request.CrearUsuarioRequest;
import co.edu.unbosque.backend.model.response.UsuarioResponse;
import co.edu.unbosque.backend.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de usuarios.
 *
 * @author Sebastian Cardenas Garcia
 */
@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Operaciones para gestionar usuarios internos del sistema")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * Crea un nuevo usuario.
     */
    @PostMapping
    @Operation(summary = "Crear usuario", description = "Registra un nuevo usuario interno disponible para operar ventas y otros modulos")
    public ResponseEntity<UsuarioResponse> crearUsuario(@Valid @RequestBody CrearUsuarioRequest request) {
        Usuario usuarioCreado = usuarioService.crearUsuario(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toUsuarioResponse(usuarioCreado));
    }

    /**
     * Consulta un usuario por id.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Consultar usuario por id")
    public ResponseEntity<UsuarioResponse> obtenerUsuario(@PathVariable Long id) {
        return ResponseEntity.ok(toUsuarioResponse(usuarioService.obtenerUsuarioPorId(id)));
    }

    /**
     * Lista usuarios con filtros opcionales por estado y rol.
     */
    @GetMapping
    @Operation(summary = "Listar usuarios", description = "Permite listar todos los usuarios o filtrar por estado y rol")
    public ResponseEntity<List<UsuarioResponse>> listarUsuarios(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String rol
    ) {
        return ResponseEntity.ok(usuarioService.listarUsuarios(q, estado, rol)
                .stream()
                .map(this::toUsuarioResponse)
                .toList());
    }

    /**
     * Cambia el estado funcional de un usuario.
     */
    @PatchMapping("/{id}/estado")
    @Operation(summary = "Actualizar estado de usuario")
    public ResponseEntity<UsuarioResponse> actualizarEstado(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarEstadoUsuarioRequest request
    ) {
        return ResponseEntity.ok(toUsuarioResponse(usuarioService.actualizarEstado(id, request.estado())));
    }

    private UsuarioResponse toUsuarioResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getIdUsuario(),
                usuario.getUsername(),
                usuario.getNombreCompleto(),
                usuario.getRol(),
                usuario.getEstado(),
                usuario.getUltimoAcceso()
        );
    }
}
