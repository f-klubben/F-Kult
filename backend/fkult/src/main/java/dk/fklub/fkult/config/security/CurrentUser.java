package dk.fklub.fkult.config.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Reads the logged-in user out of the Spring Security context.
 *
 * For a F-Klub SSO login the authentication name is the stregusername, because
 * the OIDC user is built with "username" as its name attribute; for an
 * admin login it is the configured admin username.
 */
public final class CurrentUser {

    private CurrentUser() {}

    public static Optional<String> username() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!isLoggedIn(authentication)) return Optional.empty();
        return Optional.ofNullable(authentication.getName());
    }

    public static boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!isLoggedIn(authentication)) return false;
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    private static boolean isLoggedIn(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
