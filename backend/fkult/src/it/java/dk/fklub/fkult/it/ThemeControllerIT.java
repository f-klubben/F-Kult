package dk.fklub.fkult.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ThemeControllerIT {
    
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    //Get all themes test
    @Test
    @Order(1)
    void getAllThemes() throws Exception {
        mvc.perform(get("/api/themes")).andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content().string(containsString("Cyber Night")))
        .andExpect(content().string(containsString("Horror Night")));
    }

    //Get all themes from a specific user test
    @Test
    @Order(2)
    void getUserThemes() throws Exception {
        mvc.perform(get("/api/themes/User").param("username", "test1"))
        .andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content().string(containsString("Cyber Night")));
    }

    //create theme test
    @Test
    @Order(3)
    void createTheme() throws Exception {

        //setup costume theme
        String newTheme = """
                {
                "name": "test night",
                "username": "allowed_user",
                "tConsts": ["tt0133093"],
                "drinkingRules": ["test"]
                }
                """;

        mvc.perform(post("/api/themes").contentType(MediaType.APPLICATION_JSON).content(newTheme))
        .andExpect(status().isOk()).andExpect(content().string(containsString("Theme created successfully")));
    }

    //test to create theme with missing values
    @Test
    @Order(4)
    void createThemeWithMissingValues() throws Exception {
        //set up mock theme
        String newTheme = """
            {
            "name": "",
            "username": "allowed_user",
            "tConsts": [],
            "drinkingRules": []
            }
            """;

        mvc.perform(post("/api/themes").contentType(MediaType.APPLICATION_JSON).content(newTheme))
        .andExpect(status().isBadRequest()).andExpect(content().string(containsString("Theme data not accepted please ensure there is a title")));
    }

    //Test theme creation on banned user where it should return bad request
    @Test
    @Order(5)
    void createThemeAsBannedUser() throws Exception {
        //setup mock theme
        String newTheme = """
            {
            "name": "test night",
            "username": "banned_user",
            "tConsts": ["tt0133093"],
            "drinkingRules": ["test"]
            }
            """;

        mvc.perform(post("/api/themes").contentType(MediaType.APPLICATION_JSON).content(newTheme))
        .andExpect(status().isBadRequest()).andExpect(content().string(containsString("Theme data not accepted as user is banned")));
    }

    //test theme updating function
    @Test
    @Order(6)
    void updateTheme() throws Exception {
        //setup update for theme
        String updatedTheme = """
            {
            "name": "Cyber Night Updated",
            "username": "test1",
            "tConsts": ["tt0133093"],
            "drinkingRules": ["Updated rule"]
            }
            """;

        mvc.perform(put("/api/themes/{id}", 1L).contentType(MediaType.APPLICATION_JSON).content(updatedTheme))
        .andExpect(status().isOk()).andExpect(content().string(containsString("Theme updated successfully")));
    }
}