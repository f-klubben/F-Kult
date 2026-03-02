package dk.fklub.fkult.persistence.repository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Repository
public class AuthRepository {

    private final JdbcTemplate jdbc;
    private final RestTemplate http = new RestTemplate();

    //connect to "stegsystem" API
    @Value("${stregsystem.base-url:https://stregsystem.fklub.dk}")
    private String stregBaseUrl;

    public AuthRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Test if username exist locally in DB
    public boolean userExistsLocally(String username) {
        try {
            Integer usernameCheck = jdbc.queryForObject(
                "SELECT COUNT(*) FROM user WHERE username = ?",
                Integer.class,
                username
            );
            return usernameCheck != null && usernameCheck > 0;
        } catch (Exception e) {
            System.out.println("Local DB check failed: " + e.getMessage());
            return false;
        }
    }

    //Check if username exists in stregsystem API using /api/member/get_id?username=<u> which retunrs { "member_id": 321 } or 400
    public Integer fetchMemberId(String username) {

        //api call for id
        String url = stregBaseUrl + "/api/member/get_id?username={u}";

        //call api to get id
        try {
            ResponseEntity<MemberIdResponse> res =
                http.getForEntity(url, MemberIdResponse.class, username);
            if (res.getStatusCode().is2xxSuccessful() && res.getBody() != null) { //check if api call is succesful
                Integer id = res.getBody().member_id;
                System.out.println("Remote member_id: " + id);
                return id;
            }
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 400) {
                System.out.println("Remote get_id: 400 → not found or missing param");
                return null;
            }
            System.out.println("Remote get_id error: " + e.getStatusCode()
                + " body=" + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.out.println("Remote get_id error: " + e.getMessage());
        }
        return null;
    }

    //If ID is found, collect user info from API using /api/member?member_id=<id>, which gives { balance, username, active, name }
    public MemberInfo fetchMemberInfo(Integer memberId) {

        //api call for member info
        String url = stregBaseUrl + "/api/member?member_id={id}";
        try {
            ResponseEntity<MemberInfo> res =
                http.getForEntity(url, MemberInfo.class, memberId);
            if (res.getStatusCode().is2xxSuccessful() && res.getBody() != null) { //check if api call is succesful
                MemberInfo memberInfo = res.getBody();
                System.out.println("Remote member info: username=" + memberInfo.username + ", name=" + memberInfo.name);
                return memberInfo;
            }
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 400) {
                System.out.println("Remote member info: 400 → invalid/missing member_id");
                return null;
            }
            System.out.println("Remote member info error: " + e.getStatusCode()
                + " body=" + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.out.println("Remote member info error: " + e.getMessage());
        }
        return null;
    }

    //Insertion of users in the DB based on data gotten from the API
    public int insertUser(String name, String username) {
        return jdbc.update("INSERT INTO user (name, username) VALUES (?, ?)", name, username);
    }

    public int upsertUser(String name, String username) {
    return jdbc.update(
      """
      INSERT INTO user (name, username) VALUES (?, ?)
      ON CONFLICT(username) DO UPDATE SET name = excluded.name
      """,
      name, username
    );
  }

    // Data transfer objects defined
    public static class MemberIdResponse {
        public Integer member_id;
        public Integer getMember_id() { return member_id; }
        public void setMember_id(Integer member_id) { this.member_id = member_id; }
    }
    public static class MemberInfo {
        public Integer balance;
        public String  username;
        public Boolean active;
        public String  name;
    }
}

