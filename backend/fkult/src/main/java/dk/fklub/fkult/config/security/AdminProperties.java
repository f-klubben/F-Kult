package dk.fklub.fkult.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The break-glass admin account. It can log in with a username and password,
 * without going through F-Klub SSO, and is configured in application.properties.
 */
@ConfigurationProperties(prefix = "fkult.admin")
public class AdminProperties {

    private String username = "root";

    /** Spring Security {id} prefixed hash, for example {bcrypt}$2a$10$... */
    private String passwordHash = "{noop}admin";

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
