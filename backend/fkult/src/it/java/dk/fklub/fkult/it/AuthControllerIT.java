package dk.fklub.fkult.it;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The login endpoints: reading the session, the admin password login (which works
 * without F-Klub SSO) and logging out.
 *
 * The F-Klub SSO login itself needs a real OIDC provider, so it is not covered
 * here; see FkultOidcUserServiceTest and LoginServiceTest for that logic.
 */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "fkult.admin.username=testadmin",
        "fkult.admin.password-hash={noop}supersecret"
})
class AuthControllerIT {

    record SessionResponse(boolean authenticated, String username, String name, long userId, boolean admin) {}

    @LocalServerPort int port;
    @Autowired TestRestTemplate rest;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private ResponseEntity<SessionResponse> adminLogin(String username, String password) {
        return rest.postForEntity(url("/api/auth/admin-login"),
                Map.of("username", username, "password", password), SessionResponse.class);
    }

    private String sessionCookie(ResponseEntity<?> response) {
        String setCookie = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat(setCookie).as("logging in must set a session cookie").isNotNull();
        return setCookie.split(";")[0];
    }

    private ResponseEntity<SessionResponse> getSession(String sessionCookie) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, sessionCookie);
        return rest.exchange(url("/api/auth/session"), HttpMethod.GET, new HttpEntity<>(headers), SessionResponse.class);
    }

    @Test
    void sessionIsAnonymousBeforeLoggingIn() {
        ResponseEntity<SessionResponse> response = rest.getForEntity(url("/api/auth/session"), SessionResponse.class);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().authenticated()).isFalse();
    }

    @Test
    void adminLoginWithAWrongPasswordIsRejected() {
        ResponseEntity<String> response = rest.postForEntity(url("/api/auth/admin-login"),
                Map.of("username", "testadmin", "password", "wrong"), String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(401);
        assertThat(response.getBody()).isEqualTo("Wrong username or password");
    }

    @Test
    void adminLoginStartsASession() {
        ResponseEntity<SessionResponse> login = adminLogin("testadmin", "supersecret");

        assertThat(login.getStatusCode().value()).isEqualTo(200);
        assertThat(login.getBody().authenticated()).isTrue();
        assertThat(login.getBody().username()).isEqualTo("testadmin");
        assertThat(login.getBody().admin()).isTrue();

        ResponseEntity<SessionResponse> session = getSession(sessionCookie(login));
        assertThat(session.getBody().authenticated()).isTrue();
        assertThat(session.getBody().admin()).isTrue();
    }

    @Test
    void browsingDoesNotRequireALogin() {
        assertThat(rest.getForEntity(url("/api/themes"), String.class).getStatusCode().value()).isEqualTo(200);
        assertThat(rest.getForEntity(url("/api/event/next"), String.class).getStatusCode().value()).isLessThan(500);
    }

    @Test
    void adminEndpointsRequireAnAdminSession() {
        assertThat(rest.getForEntity(url("/api/user"), String.class).getStatusCode().value()).isEqualTo(401);
    }

    @Test
    void adminEndpointsAreOpenWithAnAdminSession() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, sessionCookie(adminLogin("testadmin", "supersecret")));

        ResponseEntity<String> response = rest.exchange(url("/api/user"), HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).contains("test1");
    }

    @Test
    void logoutEndsTheSession() {
        String sessionCookie = sessionCookie(adminLogin("testadmin", "supersecret"));
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, sessionCookie);

        rest.exchange(url("/api/auth/logout"), HttpMethod.POST, new HttpEntity<>(headers), String.class);

        assertThat(getSession(sessionCookie).getBody().authenticated()).isFalse();
        assertThat(rest.exchange(url("/api/user"), HttpMethod.GET, new HttpEntity<>(headers), String.class)
                .getStatusCode().value()).isEqualTo(401);
    }
}
