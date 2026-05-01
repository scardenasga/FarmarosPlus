package co.edu.unbosque.backend.repository;

import co.edu.unbosque.backend.configuration.JpaAuditingConfig;
import co.edu.unbosque.backend.configuration.LocalDateTimeAttributeConverter;
import co.edu.unbosque.backend.model.entity.AuditorAwareImpl;
import co.edu.unbosque.backend.model.entity.OrdenCompra;
import co.edu.unbosque.backend.model.entity.Proveedor;
import co.edu.unbosque.backend.model.entity.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaAuditingConfig.class, AuditorAwareImpl.class, LocalDateTimeAttributeConverter.class})
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:sqlite:target/orden-compra-repository-test.db",
        "spring.datasource.driver-class-name=org.sqlite.JDBC",
        "spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class OrdenCompraRepositoryTest {

    @Autowired
    private OrdenCompraRepository ordenCompraRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void findByEstadoOrderByFechaPedidoDesc_debeFiltrarPorEstado() {
        Proveedor proveedor = new Proveedor();
        proveedor.setNombre("Farmaceutica Test");
        proveedor.setEstado("ACTIVO");
        proveedorRepository.save(proveedor);

        Usuario usuario = new Usuario();
        usuario.setUsername("admin");
        usuario.setPasswordHash("hash");
        usuario.setNombreCompleto("Admin");
        usuario.setRol("ADMIN");
        usuario.setEstado("ACTIVO");
        usuarioRepository.save(usuario);

        OrdenCompra orden = new OrdenCompra();
        orden.setProveedor(proveedor);
        orden.setUsuario(usuario);
        orden.setFechaPedido(LocalDateTime.now());
        orden.setEstado("PENDIENTE");
        orden.setTotalEsperado(50000.0);
        ordenCompraRepository.save(orden);

        List<OrdenCompra> resultado = ordenCompraRepository.findByEstadoOrderByFechaPedidoDesc("PENDIENTE");

        assertTrue(resultado.size() >= 1);
        assertEquals("PENDIENTE", resultado.getFirst().getEstado());
    }

    @Test
    void findByProveedor_IdProveedorOrderByFechaPedidoDesc_debeFiltrarPorProveedor() {
        Proveedor proveedor = new Proveedor();
        proveedor.setNombre("Farmaceutica ABC");
        proveedor.setEstado("ACTIVO");
        proveedorRepository.save(proveedor);

        Usuario usuario = new Usuario();
        usuario.setUsername("vendedor1");
        usuario.setPasswordHash("hash");
        usuario.setNombreCompleto("Vendedor");
        usuario.setRol("VENDEDOR");
        usuario.setEstado("ACTIVO");
        usuarioRepository.save(usuario);

        OrdenCompra orden = new OrdenCompra();
        orden.setProveedor(proveedor);
        orden.setUsuario(usuario);
        orden.setFechaPedido(LocalDateTime.now());
        orden.setEstado("PENDIENTE");
        orden.setTotalEsperado(30000.0);
        ordenCompraRepository.save(orden);

        List<OrdenCompra> resultado = ordenCompraRepository
                .findByProveedor_IdProveedorOrderByFechaPedidoDesc(proveedor.getIdProveedor());

        assertTrue(resultado.size() >= 1);
    }
}