package dk.fklub.fkult.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.fklub.fkult.persistence.entities.Movie;
import dk.fklub.fkult.persistence.repository.MovieRepository;

import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.jsoup.nodes.Document;

//set up test enviornment
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class MovieControllerIT {

    //autowire to simplify functions
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @Autowired MovieRepository movieRepo;

    // helper to serialize objects to JSON
    private String json(Object o) throws Exception { return om.writeValueAsString(o); }

    //find movies by id and succed
    @Test
    void byIdSuccess() throws Exception {
        // Arrange: get two known movies from test DB
        Movie m1 = movieRepo.findByTconst("tt0133093");
        Movie m2 = movieRepo.findByTconst("tt0083658"); 

        List<Long> ids = List.of(m1.getId(), m2.getId());

        // Act + Assert
        mvc.perform(post("/api/movies/batchById")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(ids)))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].title", notNullValue()))
            .andExpect(jsonPath("$[1].title", notNullValue()));
    }

    //find movie by id and fail
    @Test
    void byIdFail() throws Exception {
        List<Long> ids = List.of(999999L); // id that does not exist

        mvc.perform(post("/api/movies/batchById")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(ids)))
            .andExpect(status().isNotFound())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.error", containsString("Movie not found with id")));
    }

    //find movie by tconst and succed
    @Test
    void byTconstSuccess() throws Exception {
        List<String> tconsts = List.of("tt0133093", "tt0083658");

        mvc.perform(post("/api/movies/batchByTconst")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(tconsts)))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].title", notNullValue()))
            .andExpect(jsonPath("$[1].title", notNullValue()));
    }

    //find movie by tconst and fail
    @Test
    void byTconstFail() throws Exception {
        List<String> tconsts = List.of("tt9999999"); // fake tconst

        mvc.perform(post("/api/movies/batchByTconst")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(tconsts)))
            .andExpect(status().isNotFound())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.error", containsString("Movie not found with tconst")));
    }

    //search for movies and succed
    @Test
    void searchMoviesSuccess() throws Exception {
        mvc.perform(get("/api/movies/search")
            .param("q", "Matrix").param("page", "1").param("limit", "10"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].tConst", 
            containsString("tt0133093")));
    }

    //search for movies and fail
    @Test
    void searchMoviesFail() throws Exception {
        mvc.perform(get("/api/movies/search")
                .param("q", "NoSuchMovieTitle")
                .param("page", "1")
                .param("limit", "10"))
            .andExpect(status().isNotFound());
    }

    //search for movies with limit
    @Test
    void searchMoviesLimit() throws Exception {
        mvc.perform(get("/api/movies/search")
                .param("q", "Matrix")
                .param("page", "1")
                .param("limit", "999"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    //count movies with given name input
    @Test
    void countMovies() throws Exception {
        mvc.perform(get("/api/movies/search/count")
                .param("q", "Matrix"))
            .andExpect(status().isOk())
            .andExpect(content().string(notNullValue()))
            .andExpect(content().string(containsString("1"))); // only "The Matrix" matches
    }

    //get poster by tconst
    @Test
    void posterByTconst() throws Exception {
        // testdata.sql must have a poster_url for this tconst
        String expectedPoster = movieRepo.findPosterById(
                movieRepo.findByTconst("tt0133093").getId()
        );

        mvc.perform(get("/api/movies/poster/{tconst}", "tt0133093"))
            .andExpect(status().isOk())
            .andExpect(content().string(expectedPoster));
    }

    //get poster by id
    @Test
    void posterById() throws Exception {
        Movie movie = movieRepo.findByTconst("tt0133093");
        String expectedPoster = movieRepo.findPosterById(movie.getId());

        mvc.perform(get("/api/movies/poster/id/{id}", movie.getId()))
            .andExpect(status().isOk())
            .andExpect(content().string(expectedPoster));
    }

//preview succeds test
@Test
void previewSuccess() throws Exception {

    String fakeHtml = """
        <html><head>
           <meta property="og:title" content="Fake Movie Title">
           <meta property="og:description" content="A fake movie.">
           <meta property="og:image" content="https://fake.com/poster.jpg">
           <meta property="og:url" content="https://fake.com/fake">
        </head></html>
    """;

    Document fakeDoc = Jsoup.parse(fakeHtml);

    try (MockedStatic<Jsoup> jsoup = mockStatic(Jsoup.class)) {

        // Mock the Connection class
        org.jsoup.Connection mockConn = org.mockito.Mockito.mock(org.jsoup.Connection.class);

        // 1️⃣ When Jsoup.connect("something") → return mock Connection
        jsoup.when(() -> Jsoup.connect(org.mockito.ArgumentMatchers.anyString()))
             .thenReturn(mockConn);

        // 2️⃣ Mock connection.userAgent("...") → return itself
        org.mockito.Mockito.when(mockConn.userAgent(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(mockConn);

        // 3️⃣ Mock connection.get() → return fake HTML document
        org.mockito.Mockito.when(mockConn.get())
                .thenReturn(fakeDoc);

        mvc.perform(get("/api/movies/preview/{t}", "tt1234567"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Fake Movie Title"))
            .andExpect(jsonPath("$.image").value("https://fake.com/poster.jpg"));
    }
}

//preview fails test
@Test
void preview_badTconst_returnsError() throws Exception {

    try (MockedStatic<Jsoup> jsoup = mockStatic(Jsoup.class)) {

        org.jsoup.Connection mockConn = org.mockito.Mockito.mock(org.jsoup.Connection.class);

        // connect() returns mock connection
        jsoup.when(() -> Jsoup.connect(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(mockConn);

        // userAgent returns itself
        org.mockito.Mockito.when(mockConn.userAgent(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(mockConn);

        // get() throws exception
        org.mockito.Mockito.when(mockConn.get())
                .thenThrow(new RuntimeException("404 Not Found"));

        mvc.perform(get("/api/movies/preview/{t}", "badId"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").exists());
    }
}

}
