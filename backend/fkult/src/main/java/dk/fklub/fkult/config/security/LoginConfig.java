package dk.fklub.fkult.config.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

/**
 * Beans needed to log in, kept separate from the web security configuration so
 * the command line tools (which run without a web server) still start.
 */
@Configuration
@EnableConfigurationProperties(AdminProperties.class)
public class LoginConfig {

    /** Supports the {bcrypt}, {noop}, ... prefixed hashes used in application.properties. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /** Where the logged-in user is kept between requests: the HTTP session. */
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }
}
