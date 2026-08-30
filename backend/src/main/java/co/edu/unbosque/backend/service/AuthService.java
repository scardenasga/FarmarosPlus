package co.edu.unbosque.backend.service;

import co.edu.unbosque.backend.exception.BusinessException;
import co.edu.unbosque.backend.exception.ResourceNotFoundException;
import co.edu.unbosque.backend.model.entity.Usuario;
import co.edu.unbosque.backend.model.response.LoginResponse;
import co.edu.unbosque.backend.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Servicio de autenticación simple para login por username/password.
 * Limitado a los dos roles del sistema: ADMIN y VENDEDOR/EMPLEADO.
 *
 * @author Sebastian Cardenas Garcia
 */
@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Autentica un usuario por username y password.
     *
     * @param username username (case-insensitive, trim)
     * @param password contraseña en texto plano
     * @return datos de sesión sin exponer hash
     */
    @Transactional
    public LoginResponse login(String username, String password) {
        String usernameNorm = username == null ? "" : username.trim();
        String passwordNorm = password == null ? "" : password.trim();

        if (usernameNorm.isBlank() || passwordNorm.isBlank()) {
            throw new BusinessException("Usuario y contraseña son obligatorios");
        }

        Usuario usuario = usuarioRepository.findByUsernameIgnoreCase(usernameNorm)
                .orElseThrow(() -> new ResourceNotFoundException("Credenciales inválidas"));

        if (!"ACTIVO".equalsIgnoreCase(usuario.getEstado())) {
            throw new BusinessException("Usuario inactivo. Contacte al administrador");
        }

        if (!passwordEncoder.matches(passwordNorm, usuario.getPasswordHash())) {
            throw new BusinessException("Credenciales inválidas");
        }

        usuario.setUltimoAcceso(LocalDateTime.now());
        usuarioRepository.save(usuario);

        return new LoginResponse(
                usuario.getIdUsuario(),
                usuario.getUsername(),
                usuario.getNombreCompleto(),
                usuario.getRol(),
                usuario.getEstado(),
                usuario.getUltimoAcceso()
        );
    }
}
