package co.edu.unbosque.backend.repository;

import co.edu.unbosque.backend.configuration.JpaAuditingConfig;
import co.edu.unbosque.backend.configuration.LocalDateTimeAttributeConverter;
import co.edu.unbosque.backend.model.entity.AuditorAwareImpl;
import co.edu.unbosque.backend.model.entity.CompraProveedor;
import co.edu.unbosque.backend.model.entity.Proveedor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaAuditingConfig.class, AuditorAwareImpl.class, LocalDateTimeAttributeConverter.class})
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:sqlite:target/compra-repository-test.db",
        "spring.datasource.driver-class-name=org.sqlite.JDBC",
        "spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class CompraRepositoryTest {

    @Autowired
    private CompraProveedorRepository compraRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    private Proveedor buildProveedor(String nombre) {
        Proveedor p = new Proveedor();
        p.setNombre(nombre);
        p.setEstado("ACTIVO");
        return proveedorRepository.save(p);
    }

    private CompraProveedor buildCompra(Proveedor proveedor, LocalDateTime fecha) {
        CompraProveedor c = new CompraProveedor();
        c.setProveedor(proveedor);
        c.setFechaRecepcion(fecha);
        c.setUsuarioResponsable("admin");
        c.setTotal(10000.0);
        return compraRepository.save(c);
    }

    @Test
    void findByProveedor_debeRetornarSoloComprasDelProveedor() {
        Proveedor p1 = buildProveedor("Laboratorio ABC");
        Proveedor p2 = buildProveedor("Drogas S.A.");
        buildCompra(p1, LocalDateTime.now());
        buildCompra(p2, LocalDateTime.now());

        List<CompraProveedor> resultado =
                compraRepository.findByProveedor_IdProveedorOrderByFechaRecepcionDesc(p1.getIdProveedor());

        assertEquals(1, resultado.size());
        assertEquals("Laboratorio ABC", resultado.getFirst().getProveedor().getNombre());
    }

    @Test
    void findAllByOrderByFechaRecepcionDesc_debeOrdenarDescendente() {
        Proveedor proveedor = buildProveedor("Proveedor Test");
        buildCompra(proveedor, LocalDateTime.now().minusDays(2));
        buildCompra(proveedor, LocalDateTime.now());

        List<CompraProveedor> resultado = compraRepository.findAllByOrderByFechaRecepcionDesc();

        assertEquals(2, resultado.size());
        assertTrue(resultado.get(0).getFechaRecepcion()
                .isAfter(resultado.get(1).getFechaRecepcion()));
    }
}
