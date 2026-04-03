package co.edu.unbosque.backend.repository;

import co.edu.unbosque.backend.configuration.JpaAuditingConfig;
import co.edu.unbosque.backend.configuration.LocalDateTimeAttributeConverter;
import co.edu.unbosque.backend.model.entity.AuditorAwareImpl;
import co.edu.unbosque.backend.model.entity.Categoria;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaAuditingConfig.class, AuditorAwareImpl.class, LocalDateTimeAttributeConverter.class})
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:sqlite:target/categoria-repository-test.db",
        "spring.datasource.driver-class-name=org.sqlite.JDBC",
        "spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class CategoriaRepositoryTest {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Test
    void findByNombreIgnoreCase_debeEncontrarCategoria() {
        Categoria categoria = new Categoria();
        categoria.setNombre("Analgesicos");
        categoria.setDescripcion("Dolor");
        categoriaRepository.save(categoria);

        var resultado = categoriaRepository.findByNombreIgnoreCase("analgesicos");

        assertTrue(resultado.isPresent());
        assertEquals("Analgesicos", resultado.get().getNombre());
    }

    @Test
    void findByNombreContainingIgnoreCaseOrderByNombreAsc_debeFiltrarPorFragmento() {
        Categoria categoria = new Categoria();
        categoria.setNombre("Antibioticos");
        categoriaRepository.save(categoria);

        var resultado = categoriaRepository.findByNombreContainingIgnoreCaseOrderByNombreAsc("bio");

        assertEquals(1, resultado.size());
        assertEquals("Antibioticos", resultado.getFirst().getNombre());
    }
}
