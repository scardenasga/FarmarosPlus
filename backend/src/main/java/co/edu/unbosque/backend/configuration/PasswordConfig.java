package co.edu.unbosque.backend.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuracion de componentes de seguridad basicos usados por la aplicacion.
 *
 * @author Sebastian Cardenas Garcia
 */
@Configuration
public class PasswordConfig {

    /**
     * Encoder usado para persistir contrasenas con hash BCrypt.
     *
     * @return implementacion de PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
