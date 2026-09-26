package co.edu.unbosque.backend.configuration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Para el perfil dev resuelve la ruta de la DB versionada (database/farmarosplus.db)
 * probando multiples workingDirectory (IDE vs mvn).
 * Evita que el IDE intente crear una DB vacia en user.home.
 */
public class DevDatabasePathProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        boolean isDev = false;
        for (String p : environment.getActiveProfiles()) {
            if ("dev".equalsIgnoreCase(p)) { isDev = true; break; }
        }
        // Tambien si spring.profiles.active viene por jvmArguments o property
        String active = environment.getProperty("spring.profiles.active");
        if (active != null && active.toLowerCase().contains("dev")) isDev = true;

        if (!isDev) return;

        String[] candidates = {
                "../database/farmarosplus.db",
                "database/farmarosplus.db",
                "./database/farmarosplus.db",
                "backend/../database/farmarosplus.db"
        };
        String userDir = System.getProperty("user.dir", ".");
        for (String rel : candidates) {
            Path p = Path.of(userDir).resolve(rel).normalize();
            if (Files.exists(p)) {
                String url = "jdbc:sqlite:" + p.toAbsolutePath().toString().replace("\\", "/");
                Map<String, Object> map = new HashMap<>();
                map.put("spring.datasource.url", url);
                environment.getPropertySources().addFirst(new MapPropertySource("dev-db-path", map));
                System.out.println("[FarmarosPlus] DEV DB resuelta: " + url + " (user.dir=" + userDir + ", rel=" + rel + ")");
                return;
            }
        }
        // Si no existe archivo, crea el directorio padre para evitar SQLITE_CANTOPEN
        try {
            for (String rel : candidates) {
                Path p = Path.of(userDir).resolve(rel).normalize();
                Path parent = p.getParent();
                if (parent != null && !Files.exists(parent)) {
                    // intenta crear el directorio del primer candidato mas probable
                    if (rel.equals("../database/farmarosplus.db") || rel.equals("database/farmarosplus.db")) {
                        Files.createDirectories(parent);
                        String url = "jdbc:sqlite:" + p.toAbsolutePath().toString().replace("\\", "/");
                        Map<String, Object> map = new HashMap<>();
                        map.put("spring.datasource.url", url);
                        environment.getPropertySources().addFirst(new MapPropertySource("dev-db-path-created", map));
                        System.out.println("[FarmarosPlus] DEV DB directorio creado: " + parent.toAbsolutePath() + " url=" + url);
                        return;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[FarmarosPlus] No se pudo crear directorio DEV DB: " + e.getMessage());
        }
        // Fallback: deja la url de application-dev.properties
    }
}
