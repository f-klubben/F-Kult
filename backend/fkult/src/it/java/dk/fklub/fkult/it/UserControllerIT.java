package dk.fklub.fkult.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.p3.fkult.business.services.Authenticator;
import com.p3.fkult.persistence.entities.User;
import com.p3.fkult.persistence.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserControllerIT {
    @Autowired MockMvc mvc;
    @Autowired UserRepository userRepo;
    @Autowired ObjectMapper om;

    //avoids http calls for the authenticator
    @MockBean Authenticator auth;

    private String json(Object o) throws Exception { return om.writeValueAsString(o); }

    //get list of users test
    @Test
    void getUsers() throws Exception {
        mvc.perform(get("/api/user")).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    //test if user is admin
    @Test
    void checkAdmin() throws Exception {
        mvc.perform(get("/api/user/admin/{username}", "admin_user")).andExpect(status().isOk()).andExpect(content().string("1"));
    }

    //promote and demote user
    @Test
    void adminPromoteAndDemote() throws Exception {
        //confirms user exists
        when(auth.receiveUsername("normal_user")).thenReturn(true);

        //promote
        mvc.perform(post("/api/user/admin/{username}", "admin_user").param("newAdmin", "normal_user").param("status", "1")).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("User successfully admin")));

        User isPromoted = userRepo.findUser("normal_user");
        assertThat(isPromoted.getAdmin()).isEqualTo(1);

        //demote
        mvc.perform(post("/api/user/admin/{username}", "admin_user").param("newAdmin", "normal_user").param("status", "0")).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("User successfully unadmin")));

        User afterDemote = userRepo.findUser("normal_user");
        assertThat(afterDemote.getAdmin()).isEqualTo(0);
    }

    //ban and unban user while admin
    @Test
    void banAndUnbanUser() throws Exception {
        when(auth.receiveUsername("normal_user")).thenReturn(true);

        //ban
        mvc.perform(post("/api/user/admin/ban_user").contentType(MediaType.APPLICATION_JSON).content(json(List.of("admin_user", "normal_user")))).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("banned")));

        assertThat(userRepo.findIfUserBanned("normal_user")).isTrue();

        //unban
        mvc.perform(post("/api/user/admin/unban_user").contentType(MediaType.APPLICATION_JSON).content(json(List.of("admin_user", "normal_user")))).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("unbanned")));

        assertThat(userRepo.findIfUserBanned("normal_user")).isFalse();
    }

    //try banning person when not admin user
    @Test
    void banUserNotAdmin() throws Exception {
        List<String> body = List.of("not_admin_user", "normal_user");

        when(auth.receiveUsername("normal_user")).thenReturn(true);

        mvc.perform(post("/api/user/admin/ban_user").contentType(MediaType.APPLICATION_JSON).content(json(body))).andExpect(status().isForbidden()).andExpect(content().string(org.hamcrest.Matchers.containsString("User not admin")));
    }

    //find id and name using username
    @Test
    void idAndName() throws Exception {
        long id = userRepo.findIdByUsername("admin_user");
        mvc.perform(get("/api/user/id/{username}", "admin_user")).andExpect(status().isOk()).andExpect(content().string(String.valueOf(id)));

        mvc.perform(get("/api/user/full_name/{id}", id)).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("Admin Person")));
    }
}
