package dk.fklub.fkult;

import dk.fklub.fkult.business.services.LoginRejectedException;
import dk.fklub.fkult.business.services.LoginService;
import dk.fklub.fkult.config.security.AdminProperties;
import dk.fklub.fkult.persistence.entities.User;
import dk.fklub.fkult.persistence.repository.AuthRepository;
import dk.fklub.fkult.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

class LoginServiceTest {

    private static final String ADMIN_USERNAME = "root";
    private static final String ADMIN_PASSWORD = "correct horse battery staple";

    private UserRepository userRepository;
    private AuthRepository authRepository;
    private AdminProperties adminProperties;
    private LoginService loginService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        authRepository = mock(AuthRepository.class);
        adminProperties = new AdminProperties();
        adminProperties.setUsername(ADMIN_USERNAME);
        adminProperties.setPasswordHash("{bcrypt}" + new BCryptPasswordEncoder().encode(ADMIN_PASSWORD));

        // The real encoder from LoginConfig, so the {id} prefix handling is covered too
        PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        loginService = new LoginService(userRepository, authRepository, adminProperties, passwordEncoder);
    }

    private static List<String> names(List<GrantedAuthority> authorities) {
        return authorities.stream().map(GrantedAuthority::getAuthority).toList();
    }

    @Test
    void ssoLoginInsertsTheMemberAndGrantsMemberAccess() {
        when(authRepository.upsertUser("Kresten Laust", "root")).thenReturn(1);
        when(userRepository.findUser("root")).thenReturn(new User(7, "root", "Kresten Laust", 0, 0));

        List<GrantedAuthority> authorities = loginService.loginWithSso("root", "Kresten Laust");

        assertThat(names(authorities)).containsExactly("ROLE_USER");
        verify(authRepository).upsertUser("Kresten Laust", "root");
    }

    @Test
    void ssoLoginGrantsAdminAccessToAnAdminMember() {
        when(authRepository.upsertUser("Jakob Topholt Jensen", "Topholt")).thenReturn(1);
        when(userRepository.findUser("Topholt")).thenReturn(new User(6, "Topholt", "Jakob Topholt Jensen", 0, 1));

        List<GrantedAuthority> authorities = loginService.loginWithSso("Topholt", "Jakob Topholt Jensen");

        assertThat(names(authorities)).containsExactly("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    void ssoLoginFallsBackToTheUsernameWhenTheProviderSendsNoName() {
        when(authRepository.upsertUser("root", "root")).thenReturn(1);
        when(userRepository.findUser("root")).thenReturn(new User(7, "root", "root", 0, 0));

        loginService.loginWithSso("root", null);

        verify(authRepository).upsertUser("root", "root");
    }

    @Test
    void ssoLoginRejectsABannedMember() {
        when(authRepository.upsertUser("Banned User", "banned_user")).thenReturn(1);
        when(userRepository.findUser("banned_user")).thenReturn(new User(11, "banned_user", "Banned User", 1, 0));

        assertThatExceptionOfType(LoginRejectedException.class)
                .isThrownBy(() -> loginService.loginWithSso("banned_user", "Banned User"))
                .satisfies(exception -> assertThat(exception.getReason()).isEqualTo("user_banned"));
    }

    @Test
    void ssoLoginRejectsAMemberThatCouldNotBeCreated() {
        when(authRepository.upsertUser("Ghost", "ghost")).thenReturn(0);
        when(userRepository.findUser("ghost")).thenReturn(new User(-1, "error", "Error Error", 0, 0));

        assertThatExceptionOfType(LoginRejectedException.class)
                .isThrownBy(() -> loginService.loginWithSso("ghost", "Ghost"))
                .satisfies(exception -> assertThat(exception.getReason()).isEqualTo("unknown_user"));
    }

    @Test
    void adminLoginReturnsAnAdminAuthentication() {
        UsernamePasswordAuthenticationToken authentication =
                loginService.loginAsAdmin(ADMIN_USERNAME, ADMIN_PASSWORD);

        assertThat(authentication.getName()).isEqualTo(ADMIN_USERNAME);
        assertThat(names(List.copyOf(authentication.getAuthorities()))).containsExactly("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    void adminLoginAcceptsTheUsernameInAnyCase() {
        assertThat(loginService.loginAsAdmin("RoOt", ADMIN_PASSWORD).getName()).isEqualTo(ADMIN_USERNAME);
    }

    @Test
    void adminLoginRejectsAWrongPassword() {
        assertThatExceptionOfType(LoginRejectedException.class)
                .isThrownBy(() -> loginService.loginAsAdmin(ADMIN_USERNAME, "wrong"))
                .satisfies(exception -> assertThat(exception.getReason()).isEqualTo("invalid_credentials"));
    }

    @Test
    void adminLoginRejectsAnotherUsername() {
        assertThatExceptionOfType(LoginRejectedException.class)
                .isThrownBy(() -> loginService.loginAsAdmin("someone-else", ADMIN_PASSWORD))
                .satisfies(exception -> assertThat(exception.getReason()).isEqualTo("invalid_credentials"));
    }

    @Test
    void adminLoginSurvivesAHashWithoutAnIdPrefix() {
        adminProperties.setPasswordHash("plain-text-hash-without-prefix");

        assertThatExceptionOfType(LoginRejectedException.class)
                .isThrownBy(() -> loginService.loginAsAdmin(ADMIN_USERNAME, ADMIN_PASSWORD));
    }
}
