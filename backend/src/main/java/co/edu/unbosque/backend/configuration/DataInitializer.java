package co.edu.unbosque.backend.configuration;

import co.edu.unbosque.backend.model.entity.Permiso;
import co.edu.unbosque.backend.model.entity.RolPermiso;
import co.edu.unbosque.backend.model.entity.Usuario;
import co.edu.unbosque.backend.repository.PermisoRepository;
import co.edu.unbosque.backend.repository.RolPermisoRepository;
import co.edu.unbosque.backend.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

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
    private final PermisoRepository permisoRepository;
    private final RolPermisoRepository rolPermisoRepository;

    public DataInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                           PermisoRepository permisoRepository, RolPermisoRepository rolPermisoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.permisoRepository = permisoRepository;
        this.rolPermisoRepository = rolPermisoRepository;
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

        seedPermisos();
    }

    private void seedPermisos() {
        List<Permiso> catalogo = List.of(
                crearPermiso("DASHBOARD_VER", "Ver dashboard", "DASHBOARD"),
                crearPermiso("VENTAS_VER", "Ver historial ventas", "VENTAS"),
                crearPermiso("VENTAS_CREAR", "Crear ventas (POS)", "VENTAS"),
                crearPermiso("VENTAS_REPORTES_VER", "Ver reportes ventas", "VENTAS"),
                crearPermiso("VENTAS_ANULAR", "Anular ventas", "VENTAS"),
                crearPermiso("INVENTARIO_VER", "Ver inventario", "INVENTARIO"),
                crearPermiso("INVENTARIO_GESTIONAR", "Crear/editar inventario", "INVENTARIO"),
                crearPermiso("PROVEEDORES_VER", "Ver proveedores", "PROVEEDORES"),
                crearPermiso("PROVEEDORES_GESTIONAR", "Crear/editar proveedores y productos", "PROVEEDORES"),
                crearPermiso("COMPRAS_VER", "Ver compras", "COMPRAS"),
                crearPermiso("COMPRAS_GESTIONAR", "Crear/ordenar compras y recepciones", "COMPRAS"),
                crearPermiso("DEVOLUCIONES_VER", "Ver devoluciones", "COMPRAS"),
                crearPermiso("DEVOLUCIONES_GESTIONAR", "Crear devoluciones", "COMPRAS"),
                crearPermiso("ANALITICA_VER", "Ver analítica", "ANALITICA"),
                crearPermiso("USUARIOS_GESTIONAR", "Gestionar usuarios", "CONFIG"),
                crearPermiso("CONFIG_VER", "Ver configuración", "CONFIG")
        );
        for (Permiso p : catalogo) {
            if (!permisoRepository.existsByClave(p.getClave())) {
                permisoRepository.save(p);
            }
        }
        // Rol ADMIN = todo
        if (rolPermisoRepository.findByRol("ADMIN").isEmpty()) {
            for (Permiso p : permisoRepository.findAll()) {
                RolPermiso rp = new RolPermiso();
                rp.setRol("ADMIN");
                rp.setPermiso(p);
                rolPermisoRepository.save(rp);
            }
        }
        // Rol VENDEDOR = base restringida
        if (rolPermisoRepository.findByRol("VENDEDOR").isEmpty()) {
            List<String> vendedorClaves = List.of("DASHBOARD_VER", "VENTAS_VER", "VENTAS_CREAR", "INVENTARIO_VER", "PROVEEDORES_VER", "CONFIG_VER");
            for (String clave : vendedorClaves) {
                permisoRepository.findByClave(clave).ifPresent(perm -> {
                    RolPermiso rp = new RolPermiso();
                    rp.setRol("VENDEDOR");
                    rp.setPermiso(perm);
                    rolPermisoRepository.save(rp);
                });
            }
        }
    }

    private Permiso crearPermiso(String clave, String desc, String modulo) {
        Permiso p = new Permiso();
        p.setClave(clave);
        p.setDescripcion(desc);
        p.setModulo(modulo);
        return p;
    }
}
