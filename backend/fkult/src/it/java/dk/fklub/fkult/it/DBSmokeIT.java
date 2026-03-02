// src/it/java/com/p3/fkult/it/DbSmokeIT.java
package dk.fklub.fkult.it;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DbSmokeIT {

  @Autowired JdbcTemplate jdbc;

  @Test
  void schemaAndDataLoaded() {
    Integer users = jdbc.queryForObject("select count(*) from user", Integer.class);
    Integer movies = jdbc.queryForObject("select count(*) from movie", Integer.class);
    Integer themes = jdbc.queryForObject("select count(*) from theme", Integer.class);

    assertThat(users).isGreaterThanOrEqualTo(2);
    assertThat(movies).isGreaterThanOrEqualTo(2);
    assertThat(themes).isGreaterThanOrEqualTo(1);
  }
}
