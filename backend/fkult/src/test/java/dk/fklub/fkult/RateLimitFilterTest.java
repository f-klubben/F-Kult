package dk.fklub.fkult;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import dk.fklub.fkult.presentation.controllers.RateLimitingFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

public class RateLimitFilterTest {

    private RateLimitingFilter filter;
    
    @BeforeEach
    void setUp() {
        filter = new RateLimitingFilter();
    }

    private void runRequests(String route, int limit) throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        FilterChain chain = (req, res) -> {};
        request.setRequestURI(route);
        request.setRemoteAddr("192.0.69.420");
        
        // Kør antal requests lig med limit (skal alle være 200)
        for (int i = 0; i < limit; i++) {
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilter(request, response, chain);
            assertEquals(200, response.getStatus());
        }
        
        // Kør én request over limit (skal være 429)
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, chain);
        assertEquals(429, response.getStatus());
    }

    // Default endpoint (limit 1000)
    @Test
    void defaultTestLimit() throws IOException, ServletException {
        runRequests("/not-a-endpoint", 1000);
    }

    // "/api/themes"
    @Test
    void themesTestLimit() throws IOException, ServletException {
        // "/api/themes" (limit 100)
        runRequests("/api/themes", 100);

        // "/api/themes/New" (limit 100)
        runRequests("/api/themes/New", 100);

        // "/api/themes/Old" (limit 100)
        runRequests("/api/themes/Old", 100);

        // "/api/themes/User" (limit 100)
        runRequests("/api/themes/User", 100);
    }

    // "/api/sound-sample"
    @Test
    void soundSampleTestLimit() throws IOException, ServletException {
        // "/api/sound-sample" (limit 50)
        runRequests("/api/sound-sample", 50);

        // "/api/sound-sample/upload" (limit 50)
        runRequests("/api/sound-sample/upload", 50);

        // "/api/sound-sample/delete" (limit 50)
        runRequests("/api/sound-sample/delete", 50);

        // "/api/sound-sample/get-all" (limit 50)
        runRequests("/api/sound-sample/get-all", 50);

        // "/api/sound-sample/download" (limit 50)
        runRequests("/api/sound-sample/download", 50);
    }

    // "/api/auth/username" (limit 10)
    @Test
    void authTestLimit() throws IOException, ServletException {
        runRequests("/api/auth/username", 10);
    }
    
    // "/api/movies"
    @Test
    void moviesTestLimit() throws IOException, ServletException {
        // "/api/movies/batchById" (limit 100)
        runRequests("/api/movies/batchById", 100);

        // "/api/movies/batchByTconst" (limit 100)
        runRequests("/api/movies/batchByTconst", 100);

        // "/api/movies/search" (limit 1000)
        runRequests("/api/movies/search", 1000);

        // "/api/movies/count" (limit 1000)
        runRequests("/api/movies/count", 1000);

        // "/api/movies/poster" (limit 1000)
        runRequests("/api/movies/poster", 1000);

        // "/api/movies/preview" (limit 1000)
        runRequests("/api/movies/preview", 1000);

        // "/api/movies/poster/id" (limit 1000)
        runRequests("/api/movies/poster/id", 1000);
    }

    // "/api/user"
    @Test
    void userTestLimit() throws IOException, ServletException {
        // "/api/user/admin" (limit 100)
        runRequests("/api/user/admin", 100);

        // "/api/user/admin/ban_user" (limit 100)
        runRequests("/api/user/admin/ban_user", 100);

        // "/api/user/admin/unban_user" (limit 100)
        runRequests("/api/user/admin/unban_user", 100);

        // "/api/user/id" (limit 100)
        runRequests("/api/user/id", 100);

        // "/api/user/full_name" (limit 100)
        runRequests("/api/user/full_name", 100);
    }
}
