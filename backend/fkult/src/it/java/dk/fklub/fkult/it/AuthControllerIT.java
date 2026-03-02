package dk.fklub.fkult.it;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthControllerIT {

  static WireMockServer wm;


  //connects to a mock hosts (this also includes a mock stregsystem api)
  @BeforeAll
  static void startWireMock() {
    wm = new WireMockServer(9099);
    wm.start();
    configureFor("localhost", 9099);
  }

  //after each tests stops the mock host
  @AfterAll
  static void stopWireMock() {
    if (wm != null) wm.stop();
  }

  @LocalServerPort int port;
  @Autowired TestRestTemplate rest;
  @Autowired JdbcTemplate jdbc;

  private String url() { return "http://localhost:" + port + "/api/auth/username"; }

  private HttpEntity<String> jsonBody(String username) {
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return new HttpEntity<>("{\"username\":\"" + username + "\"}", headers);
  }

  //tests if username exists and are not banned
  @Test
  void usernameExistsAndNotBanned() {
    ResponseEntity<String> resp = rest.postForEntity(url(), jsonBody("allowed_user"), String.class); //inserts user name allowed_user which should log in

    //test user got logged in
    assertThat(resp.getStatusCode().value()).isEqualTo(200);
    assertThat(resp.getBody()).isEqualTo("Login successful");
  }

  //test if user is banned
  @Test
  void usernameExistsAndBanned() {
    ResponseEntity<String> resp = rest.postForEntity(url(), jsonBody("banned_user"), String.class); //insert banned username to check response

    //asserts based on response that user is banned
    assertThat(resp.getStatusCode().value()).isEqualTo(404);
    assertThat(resp.getBody()).isEqualTo("Your Banned Bozo!");
  }

  //test if user does not exist in database but in stregsystem api
  @Test
  void usernameNotLocalButExistsRemoteIsInserted() {
    Integer before = jdbc.queryForObject("select count(*) from user where username = ?", Integer.class, "remote_user");
    assertThat(before).isZero();

    //promt mock stregsystem to return member id 321 when given value remote_user
    wm.stubFor(get(urlPathEqualTo("/api/member/get_id"))
        .withQueryParam("username", equalTo("remote_user"))
        .willReturn(okJson("{\"member_id\":321}")));

    // Define all values stregsystem returns when given user id 321
    wm.stubFor(get(urlPathEqualTo("/api/member"))
        .withQueryParam("member_id", equalTo("321"))
        .willReturn(okJson("{\"balance\":0,\"username\":\"remote_user\",\"active\":true,\"name\":\"Remote User\"}")));

    ResponseEntity<String> resp = rest.postForEntity(url(), jsonBody("remote_user"), String.class);

    //test that the process is a succes and user is logged in
    assertThat(resp.getStatusCode().value()).isEqualTo(200);
    assertThat(resp.getBody()).isEqualTo("Login successful");

    // verify user was upserted locally
    Integer after = jdbc.queryForObject("select count(*) from user where username = ?", Integer.class, "remote_user");
    assertThat(after).isEqualTo(1);
  }

  //test user does not exist locally or on stregsystem api
  @Test
  void usernameNotLocalAndRemote() {
    wm.stubFor(get(urlPathEqualTo("/api/member/get_id"))
        .withQueryParam("username", equalTo("unknown_user"))
        .willReturn(aResponse().withStatus(400).withBody("{\"error\":\"bad request\"}"))); //promts stregsystem api to return bad request

    ResponseEntity<String> resp = rest.postForEntity(url(), jsonBody("unknown_user"), String.class);

    //assert user does not exists
    assertThat(resp.getStatusCode().value()).isEqualTo(404);
    assertThat(resp.getBody()).isEqualTo("Username does not exist");
  }
}

