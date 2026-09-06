package co.edu.unbosque.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Punto de entrada principal de la aplicación backend.
 *
 * @author Sebastian Cardenas Garcia
 * @author Angie Tatiana Ortiz
 */
@SpringBootApplication
public class BackendApplication {

     public static void main(String[] args) {
        // Crear carpeta de la DB antes de que Spring intente conectarse
        try {
            java.nio.file.Path dir = java.nio.file.Path.of(System.getProperty("user.home"), ".farmarosplus");
            if (!java.nio.file.Files.exists(dir)) {
                java.nio.file.Files.createDirectories(dir);
            }
        } catch (Exception ignored) {}

        // Si ya hay una instancia corriendo en 8080, solo abre el navegador y sale.
        // Asi no da "Failed to launch JVM" por puerto ocupado si hace doble click 2 veces.
        if (yaEstaCorriendo()) {
            abrirNavegadorEstatico();
            System.exit(0);
        }

        SpringApplication.run(BackendApplication.class, args);
    }

    private static boolean yaEstaCorriendo() {
        try (java.net.Socket s = new java.net.Socket()) {
            s.connect(new java.net.InetSocketAddress("127.0.0.1", 8080), 500);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static void abrirNavegadorEstatico() {
        String url = "http://localhost:8080";
        try {
            if (java.awt.Desktop.isDesktopSupported() && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
                return;
            }
        } catch (Exception ignored) {}
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) new ProcessBuilder("cmd", "/c", "start", url).start();
            else if (os.contains("mac")) new ProcessBuilder("open", url).start();
            else new ProcessBuilder("xdg-open", url).start();
        } catch (Exception ignored) {}
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("http://localhost:4200")
                        .allowedMethods("GET", "POST", "PATCH", "PUT", "DELETE")
                        .allowedHeaders("*");
            }
        };
    }

    // Al arrancar, abre el navegador solo. Asi el usuario hace doble click y ya ve la app.
    @EventListener(ApplicationReadyEvent.class)
    public void abrirNavegador() {
        String url = "http://localhost:8080";
        try {
            // Solo si no estamos en modo test
            if (System.getProperty("java.awt.headless") != null) return;

            // Intento 1: Desktop API (funciona con JRE bundlado)
            try {
                if (java.awt.Desktop.isDesktopSupported() && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
                    java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
                    return;
                }
            } catch (Exception ignored) {}

            // Intento 2: comando del sistema
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                new ProcessBuilder("cmd", "/c", "start", url).start();
            } else if (os.contains("mac")) {
                new ProcessBuilder("open", url).start();
            } else {
                new ProcessBuilder("xdg-open", url).start();
            }
        } catch (Exception e) {
            System.out.println("Abre manualmente: " + url);
        }
    }
}

