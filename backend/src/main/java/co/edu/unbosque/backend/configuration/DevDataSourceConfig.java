package co.edu.unbosque.backend.configuration;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Para perfil dev, resuelve la ruta de database/farmarosplus.db
 * tanto si el Working Directory es FarmarosPlus/ (mvn) como backend/ (IntelliJ por defecto).
 * Evita SQLITE_CANTOPEN cuando la ruta relativa no coincide con el WD.
 */
@Configuration
@Profile("dev")
public class DevDataSourceConfig {

    @Value("${spring.datasource.url:}")
    private String originalUrl;

    @Value("${spring.datasource.driver-class-name:org.sqlite.JDBC}")
    private String driverClassName;

    @Bean
    @Primary
    public DataSource dataSource() {
        String userDir = System.getProperty("user.dir", ".");
        String[] candidates = {
                "database/farmarosplus.db",
                "../database/farmarosplus.db",
                "backend/../database/farmarosplus.db"
        };
        String resolvedUrl = originalUrl;
        for (String rel : candidates) {
            Path p = Path.of(userDir).resolve(rel).normalize();
            if (Files.exists(p)) {
                resolvedUrl = "jdbc:sqlite:" + p.toAbsolutePath().toString().replace("\\", "/");
                System.out.println("[FarmarosPlus] DEV DataSource resuelta: " + resolvedUrl + " (user.dir=" + userDir + ", rel=" + rel + ")");
                break;
            }
        }
        // Si no existe, crea el directorio padre para evitar CANTOPEN
        try {
            String pathPart = resolvedUrl.replace("jdbc:sqlite:", "");
            Path dbPath = Path.of(pathPart);
            if (dbPath.getParent() != null && !Files.exists(dbPath.getParent())) {
                Files.createDirectories(dbPath.getParent());
                System.out.println("[FarmarosPlus] Directorio DEV DB creado: " + dbPath.getParent());
            }
        } catch (Exception e) {
            System.out.println("[FarmarosPlus] No se pudo crear directorio DEV DB: " + e.getMessage());
        }
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(resolvedUrl);
        ds.setDriverClassName(driverClassName);
        ds.setMaximumPoolSize(1);
        return ds;
    }
}
