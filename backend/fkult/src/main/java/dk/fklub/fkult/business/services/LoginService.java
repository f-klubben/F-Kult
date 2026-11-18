package dk.fklub.fkult.business.services;

import dk.fklub.fkult.config.security.AdminProperties;
import dk.fklub.fkult.persistence.entities.User;
import dk.fklub.fkult.persistence.repository.AuthRepository;
import dk.fklub.fkult.persistence.repository.UserRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * The two ways of logging in to F-Kult: F-Klub SSO (OIDC) for members and the
 * break-glass admin account. Both produce the authorities of the session.
 */
@Service
public class LoginService {

    /** The placeholder from application.properties. Keeping it means the password is public. */
    static final String DEFAULT_ADMIN_HASH = "{noop}admin";

    private final UserRepository userRepository;
    private final AuthRepository authRepository;
    private final AdminProperties adminProperties;
    private final PasswordEncoder passwordEncoder;

    public LoginService(UserRepository userRepository,
                        AuthRepository authRepository,
                        AdminProperties adminProperties,
                        PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.authRepository = authRepository;
        this.adminProperties = adminProperties;
        this.passwordEncoder = passwordEncoder;

        if (DEFAULT_ADMIN_HASH.equals(adminProperties.getPasswordHash())) {
            System.out.println("[security] WARNING: the admin password is still the default ('admin'). "
                    + "Set fkult.admin.password-hash (or FKULT_ADMIN_PASSWORD_HASH) to a {bcrypt} hash.");
        }
    }

    /**
     * Login through F-Klub SSO. The member is inserted or refreshed in the local
     * user table, exactly like the old username login did, and the local row then
     * decides whether the member is an admin and whether they are banned.
     */
    @Transactional
    public List<GrantedAuthority> loginWithSso(String username, String name) {
        String stregUsername = username == null ? "" : username.trim();
        if (stregUsername.isEmpty()) {
            throw new LoginRejectedException("unknown_user", "F-Klub SSO returned an empty username");
        }

        String displayName = (name == null || name.isBlank()) ? stregUsername : name.trim();
        if (authRepository.upsertUser(displayName, stregUsername) != 1) {
            throw new LoginRejectedException("unknown_user", "Could not create the user " + stregUsername);
        }

        User user = userRepository.findUser(stregUsername);
        if (user.getId() == -1) {
            throw new LoginRejectedException("unknown_user", "Could not create the user " + stregUsername);
        }
        if (user.getBanned() == 1) {
            throw new LoginRejectedException("user_banned", "You are banned");
        }

        System.out.println("F-Klub SSO login: " + stregUsername);
        return authorities(user.getAdmin() == 1);
    }

    /**
     * Login as the configured admin with a password, without F-Klub SSO, so the
     * club can still get in when the stregsystem is unavailable.
     */
    public UsernamePasswordAuthenticationToken loginAsAdmin(String username, String password) {
        String givenUsername = username == null ? "" : username.trim();
        if (!givenUsername.equalsIgnoreCase(adminProperties.getUsername()) || !passwordMatches(password)) {
            throw new LoginRejectedException("invalid_credentials", "Wrong username or password");
        }

        System.out.println("Admin login: " + adminProperties.getUsername());
        return UsernamePasswordAuthenticationToken.authenticated(adminProperties.getUsername(), null, authorities(true));
    }

    private static List<GrantedAuthority> authorities(boolean admin) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        if (admin) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        return List.copyOf(authorities);
    }

    private boolean passwordMatches(String password) {
        String hash = adminProperties.getPasswordHash();
        if (password == null || hash == null || hash.isBlank()) return false;
        try {
            return passwordEncoder.matches(password, hash);
        } catch (IllegalArgumentException e) {
            // Unknown {id} prefix in the configured hash
            System.out.println("[security] WARNING: fkult.admin.password-hash has no usable {id} prefix");
            return false;
        }
    }
}
