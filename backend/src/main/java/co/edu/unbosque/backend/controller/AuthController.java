package co.edu.unbosque.backend.controller;

import co.edu.unbosque.backend.exception.BusinessException;
import co.edu.unbosque.backend.exception.ResourceNotFoundException;
import co.edu.unbosque.backend.model.request.LoginRequest;
import co.edu.unbosque.backend.model.response.ApiErrorResponse;
import co.edu.unbosque.backend.model.response.LoginResponse;
import co.edu.unbosque.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Controlador de autenticación - login con username/password.
 *
 * @author Sebastian Cardenas Garcia
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Autenticación de usuarios (ADMIN / VENDEDOR)")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Autentica usuario por username y password. Retorna datos de sesión para guardar en localStorage. Roles válidos: ADMIN, VENDEDOR.")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request.username(), request.password());
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException ex) {
            return buildError(HttpStatus.UNAUTHORIZED, ex.getMessage(), "/api/auth/login");
        } catch (BusinessException ex) {
            // Credenciales inválidas o usuario inactivo
            String msg = ex.getMessage();
            boolean credenciales = msg != null && msg.toLowerCase().contains("credenciales");
            boolean inactivo = msg != null && msg.toLowerCase().contains("inactivo");
            HttpStatus status = credenciales ? HttpStatus.UNAUTHORIZED : (inactivo ? HttpStatus.FORBIDDEN : HttpStatus.BAD_REQUEST);
            return buildError(status, msg, "/api/auth/login");
        }
    }

    private ResponseEntity<ApiErrorResponse> buildError(HttpStatus status, String message, String path) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path
        ));
    }
}
