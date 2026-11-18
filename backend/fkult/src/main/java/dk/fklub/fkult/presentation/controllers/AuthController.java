package dk.fklub.fkult.presentation.controllers;

import dk.fklub.fkult.business.services.LoginRejectedException;
import dk.fklub.fkult.business.services.LoginService;
import dk.fklub.fkult.business.services.UserService;
import dk.fklub.fkult.config.security.CurrentUser;
import dk.fklub.fkult.persistence.entities.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

record AdminLoginDTO(String username, String password) {}

/** Who is logged in, so the frontend knows what to render. */
record SessionDTO(boolean authenticated, String username, String name, long userId, boolean admin) {

    static SessionDTO anonymous() {
        return new SessionDTO(false, null, null, 0, false);
    }
}

//controller for the session, using route "/api/auth"
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final LoginService loginService;
    private final UserService userService;
    private final SecurityContextRepository securityContextRepository;

    public AuthController(LoginService loginService, UserService userService,
                          SecurityContextRepository securityContextRepository) {
        this.loginService = loginService;
        this.userService = userService;
        this.securityContextRepository = securityContextRepository;
    }

    /**
     * Who is logged in right now. Also the endpoint the frontend reads after a
     * F-Klub SSO round trip, since the session cookie is the only proof of login.
     */
    @GetMapping("/session")
    public SessionDTO session() {
        Optional<String> username = CurrentUser.username();
        if (username.isEmpty()) return SessionDTO.anonymous();

        User user = userService.getUser(username.get());
        boolean known = user.getId() != -1;
        return new SessionDTO(true, username.get(), known ? user.getName() : username.get(),
                known ? user.getId() : 0, CurrentUser.isAdmin());
    }

    /**
     * Login with the admin account, without F-Klub SSO. The credentials come from
     * application.properties (fkult.admin.*). Members never use this.
     */
    @PostMapping("/admin-login")
    public ResponseEntity<?> adminLogin(@RequestBody AdminLoginDTO body,
                                        HttpServletRequest request,
                                        HttpServletResponse response) {
        Authentication authentication;
        try {
            authentication = loginService.loginAsAdmin(body.username(), body.password());
        } catch (LoginRejectedException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }

        // Start a fresh session id, so a session created before the login cannot be reused
        if (request.getSession(false) != null) request.changeSessionId();

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        return ResponseEntity.ok(session());
    }

    /** Ends the session, both for a SSO login and for an admin login. */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        new SecurityContextLogoutHandler().logout(request, response, authentication);
        return ResponseEntity.ok("Logged out");
    }
}
