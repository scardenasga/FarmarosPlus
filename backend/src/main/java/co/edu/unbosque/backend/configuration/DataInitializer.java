package co.edu.unbosque.backend.configuration;

import co.edu.unbosque.backend.model.entity.Usuario;
import co.edu.unbosque.backend.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Inicializador de datos para asegurar que el sistema tenga un usuario base.
 * Necesario para que la auditoría JPA (SISTEMA) funcione desde el primer arranque.
 *
 * @author Sebastian Cardenas Garcia
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!usuarioRepository.existsByUsernameIgnoreCase("SISTEMA")) {
            Usuario sistema = new Usuario();
            sistema.setUsername("SISTEMA");
            sistema.setPasswordHash(passwordEncoder.encode("sistema123"));
            sistema.setNombreCompleto("USUARIO DE SISTEMA");
            sistema.setRol("ADMIN");
            sistema.setEstado("ACTIVO");
            usuarioRepository.save(sistema);
        }
        if (!usuarioRepository.existsByUsernameIgnoreCase("admin")) {
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setNombreCompleto("Administrador Principal");
            admin.setRol("ADMIN");
            admin.setEstado("ACTIVO");
            usuarioRepository.save(admin);
        }
        if (!usuarioRepository.existsByUsernameIgnoreCase("vendedor")) {
            Usuario vendedor = new Usuario();
            vendedor.setUsername("vendedor");
            vendedor.setPasswordHash(passwordEncoder.encode("vendedor123"));
            vendedor.setNombreCompleto("Vendedor Farmaros");
            vendedor.setRol("VENDEDOR");
            vendedor.setEstado("ACTIVO");
            usuarioRepository.save(vendedor);
        }
    }
}
