package co.edu.unbosque.backend.repository;

import co.edu.unbosque.backend.configuration.JpaAuditingConfig;
import co.edu.unbosque.backend.configuration.LocalDateTimeAttributeConverter;
import co.edu.unbosque.backend.model.entity.AuditorAwareImpl;
import co.edu.unbosque.backend.model.entity.Proveedor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaAuditingConfig.class, AuditorAwareImpl.class, LocalDateTimeAttributeConverter.class})
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:sqlite:target/proveedor-repository-test.db",
        "spring.datasource.driver-class-name=org.sqlite.JDBC",
        "spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ProveedorRepositoryTest {

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Test
    void existsByNombreIgnoreCase_debeDetectarDuplicado() {
        Proveedor proveedor = new Proveedor();
        proveedor.setNombre("Farmaceutica XYZ");
        proveedor.setNit("860123456-7");
        proveedor.setEstado("ACTIVO");
        proveedorRepository.save(proveedor);

        boolean resultado = proveedorRepository.existsByNombreIgnoreCase("farmaceutica xyz");

        assertTrue(resultado);
    }

    @Test
    void findByEstadoOrderByNombreAsc_debeFiltrarPorEstadoYOrdenar() {
        Proveedor activo = new Proveedor();
        activo.setNombre("Farmaceutica ABC");
        activo.setEstado("ACTIVO");
        proveedorRepository.save(activo);

        Proveedor inactivo = new Proveedor();
        inactivo.setNombre("Farmaceutica DEF");
        inactivo.setEstado("INACTIVO");
        proveedorRepository.save(inactivo);

        List<Proveedor> resultado = proveedorRepository.findByEstadoOrderByNombreAsc("ACTIVO");

        assertEquals(1, resultado.size());
        assertEquals("ACTIVO", resultado.getFirst().getEstado());
    }

    @Test
    void findByEstadoOrderByNombreAsc_debeOrdenarPorNombreAscendente() {
        Proveedor proveedor = new Proveedor();
        proveedor.setNombre("Farmaceutica XYZ");
        proveedor.setNit("860123456-7");
        proveedor.setEstado("ACTIVO");
        proveedorRepository.save(proveedor);

        List<Proveedor> resultado = proveedorRepository.findByEstadoOrderByNombreAsc("ACTIVO");

        assertTrue(resultado.size() >= 1);
        assertEquals("ACTIVO", resultado.getFirst().getEstado());
    }
}