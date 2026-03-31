package co.edu.unbosque.backend.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracion base de OpenAPI para la documentacion interactiva del backend.
 *
 * @author Sebastian Cardenas Garcia
 */
@Configuration
public class OpenApiConfig {

    /**
     * Define los metadatos principales de la API expuesta en Swagger UI.
     *
     * @return configuracion base de OpenAPI
     */
    @Bean
    public OpenAPI farmarosOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FarmarosPlus API")
                        .description("API REST para el backend POS-ERP de farmacia, enfocada en usuarios, productos, inventario y ventas.")
                        .version("v1")
                        .contact(new Contact()
                                .name("Sebastian Cardenas Garcia")
                                .email("sebastian.cardenas.garcia@unbosque.edu.co"))
                        .license(new License()
                                .name("Uso academico / interno")));
    }
}
