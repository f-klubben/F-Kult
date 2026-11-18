package dk.fklub.fkult.config.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextRepository;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Web security for F-Kult.
 *
 * Members log in through F-Klub SSO (OIDC), the break-glass admin account can log
 * in with a password instead. Both produce an HTTP session, and the session - not
 * a username sent by the client - decides who the caller is.
 */
@Configuration
@EnableWebSecurity
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class SecurityConfig {

    /** Endpoints of the login machinery itself, plus the session endpoint the SPA reads. */
    private static final String[] LOGIN_PATHS = {"/oauth2/**", "/login/**", "/error", "/api/auth/**"};

    @Value("${fkult.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   SecurityContextRepository securityContextRepository,
                                                   FkultOidcUserService oidcUserService) throws Exception {
        http
                .securityContext(context -> context.securityContextRepository(securityContextRepository))
                // No CSRF token: the session cookie is SameSite=Lax and CORS only allows the
                // configured frontend origins, so a third party cannot post with the cookie.
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(LOGIN_PATHS).permitAll()
                        // Was a check on a username sent by the client, now the session decides
                        .requestMatchers("/api/user", "/api/user/admin/**").hasRole("ADMIN")
                        // Nothing else changes access, so browsing and the showcase page keep working
                        .anyRequest().permitAll())
                .oauth2Login(login -> login
                        .userInfoEndpoint(userInfo -> userInfo.oidcUserService(oidcUserService))
                        .successHandler(this::backToFrontend)
                        .failureHandler(this::loginFailed))
                // F-Kult has its own logout endpoint, POST /api/auth/logout
                .logout(AbstractHttpConfigurer::disable)
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint((request, response, exception) ->
                                writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, "Login required"))
                        .accessDeniedHandler((request, response, exception) ->
                                writeJson(response, HttpServletResponse.SC_FORBIDDEN, "Admin access required")));

        return http.build();
    }

    /** F-Klub SSO is done, hand the browser back to the login page which reads the session. */
    private void backToFrontend(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {
        response.sendRedirect(frontendUrl + "/login?auth=success");
    }

    /** F-Klub SSO failed (cancelled, banned, unknown user, ...): tell the login page why. */
    private void loginFailed(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException {
        String reason = (exception instanceof OAuth2AuthenticationException oauth2Exception)
                ? oauth2Exception.getError().getErrorCode()
                : "login_failed";
        response.sendRedirect(frontendUrl + "/login?auth=error&reason="
                + URLEncoder.encode(reason, StandardCharsets.UTF_8));
    }

    private static void writeJson(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}
