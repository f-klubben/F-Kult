package dk.fklub.fkult.MovieTest;

import dk.fklub.fkult.business.services.MovieService;
import dk.fklub.fkult.persistence.entities.Movie;
import dk.fklub.fkult.persistence.repository.MovieRepository;
import dk.fklub.fkult.presentation.DTOs.MovieRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private MovieService movieService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // testing searchMovies()
    @Test
    void testSearchMovies() {
        // Arrange
        Movie m = new Movie(
                1L, "tt12345", "Test Movie", "Test Movie",
                2020, 120, true, false, false,
                "poster.jpg", "PG-13"
        );

        when(movieRepository.searchMovies(any(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(m));

        // Act
        List<MovieRequest> result = movieService.searchMovies(
                "test", 1, 10, "year", "asc", true, false, false, true
        );

        // Assert
        assertEquals(1, result.size());
        assertEquals("Test Movie", result.get(0).getTitle());
        assertEquals("tt12345", result.get(0).gettConst());
    }

    // getMovieSearchCount()
    @Test
    void testGetMovieSearchCount() {
        // Arrange
        when(movieRepository.countMovies("test")).thenReturn(7);

        // Act
        int result = movieService.getMovieSearchCount("test");

        // Assert
        assertEquals(7, result);
    }

    // getMoviesByIds()
    @Test
    void testGetMoviesByIds() {
        // Arrange
        Movie m = new Movie(
                1L, "tt001", "Test", "Test",
                2000, 90, true, false, false,
                "poster.jpg", "R"
        );

        when(movieRepository.findById(1L)).thenReturn(m);

        // Act
        List<MovieRequest> result = movieService.getMoviesByIds(List.of(1L));

        // Assert
        assertEquals(1, result.size());
        assertEquals("Test", result.get(0).getTitle());
    }

    @Test
    void testGetMoviesByIdsThrowsWhenMissing() {
        // Arrange
        when(movieRepository.findById(99L)).thenReturn(null);

        // Act + Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> movieService.getMoviesByIds(List.of(99L)));

        assertTrue(ex.getMessage().contains("Movie not found with id"));
    }

    // getMoviesByTconsts()
    @Test
    void testGetMoviesByTconsts() {
        // Arrange
        Movie m = new Movie(
                1L, "ttABC", "Test", "Test",
                2000, 90, true, false, false,
                "poster.jpg", "R"
        );

        when(movieRepository.findByTconst("ttABC")).thenReturn(m);

        // Act
        List<MovieRequest> result = movieService.getMoviesByTconsts(List.of("ttABC"));

        // Assert
        assertEquals(1, result.size());
        assertEquals("Test", result.get(0).getTitle());
    }

    @Test
    void testGetMoviesByTconstsThrowsWhenMissing() {
        // Arrange
        when(movieRepository.findByTconst("missing")).thenReturn(null);

        // Act + Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> movieService.getMoviesByTconsts(List.of("missing")));

        assertTrue(ex.getMessage().contains("Movie not found with tconst"));
    }

    // getPosterByTconst()
    @Test
    void testGetPosterByTconst() {
        // Arrange
        Movie m = new Movie(
                1L, "tt001", "X", "X",
                2000, 100, true, false, false,
                "poster123.jpg", "PG"
        );

        when(movieRepository.findByTconst("tt001")).thenReturn(m);

        // Act
        String result = movieService.getPosterByTconst("tt001");

        // Assert
        assertEquals("poster123.jpg", result);
    }

    // getPosterById()
    @Test
    void testGetPosterById() {
        // Arrange
        Movie m = new Movie(
                2L, "tt002", "Y", "Y",
                2000, 100, true, false, false,
                "poster999.jpg", "PG"
        );

        when(movieRepository.findById(2L)).thenReturn(m);

        // Act
        String result = movieService.getPosterById(2L);

        // Assert
        assertEquals("poster999.jpg", result);
    }

    // getTconstById()
    @Test
    void testGetTconstById() {
        // Arrange
        Movie m = new Movie(
                3L, "ttXYZ", "Z", "Z",
                2000, 100, true, false, false,
                "poster.jpg", "PG"
        );

        when(movieRepository.findById(3L)).thenReturn(m);

        // Act
        String result = movieService.getTconstById(3L);

        // Assert
        assertEquals("ttXYZ", result);
    }

    // test getPosterURL logic for cached value
    @Test
    void testGetPosterURL_ReturnsCachedPoster() {
        // Arrange
        Movie m = new Movie(
                1L, "tt999", "Test", "Test",
                2000, 100, true, false, false,
                "cached.jpg", "PG"
        );

        // Act
        String result = movieService.getPosterURL(m);

        // Assert
        assertEquals("cached.jpg", result);
    }

    //  getPosterURL() should return null if the movie has no tconst.
    @Test
    void testGetPosterURL_NoTconstReturnsNull() {
        // Arrange
        Movie m = new Movie(
                1L, null, "Test", "Test",
                2000, 100, true, false, false,
                null, "PG"
        );

        // Act
        String result = movieService.getPosterURL(m);

        // Assert
        assertNull(result);
    }

    // getPosterURL() should fetch the poster from IMDb when not cached, parse the og:image tag, return the URL, and update the database.
@Test
    void testGetPosterURL_FetchesFromImdb() {
        // Arrange
        Movie m = new Movie(
                1L, "tt777", "Test", "Test",
                2000, 90, true, false, false,
                null, "PG"
        );

        MovieService spyService = Mockito.spy(movieService);

        String fakeHtml =
                "<html><head>" +
                "<meta property=\"og:image\" content=\"http://img.fake/poster.jpg\">" +
                "</head></html>";

        ResponseEntity<String> fakeResponse = ResponseEntity.ok(fakeHtml);

        doReturn(fakeResponse).when(spyService)
                .exchangeImdb(anyString(), any(HttpEntity.class));

        // Act
        String result = spyService.getPosterURL(m);

        // Assert
        assertEquals("http://img.fake/poster.jpg", result);
        verify(movieRepository).updatePosterURL(1L, "http://img.fake/poster.jpg");
    }

    // getPosterURL() should fetch the poster from IMDb when not cached, parse the og:image tag, return the URL, and update the database.
    @Test
    void testGetPosterURL_NoOgImageReturnsNull() {
        // Arrange
        Movie m = new Movie(
                5L, "tt555", "Test", "Test",
                2000, 100, true, false, false,
                null, "PG"
        );

        MovieService spyService = Mockito.spy(movieService);

        String fakeHtml =
                "<html><head></head><body>No image here</body></html>";

        ResponseEntity<String> fakeResponse = ResponseEntity.ok(fakeHtml);

        doReturn(fakeResponse).when(spyService)
                .exchangeImdb(anyString(), any(HttpEntity.class));

        // Act
        String result = spyService.getPosterURL(m);

        // Assert
        assertNull(result);
        verify(movieRepository, never()).updatePosterURL(anyLong(), anyString());
    }
    // getPosterURL() should return null when IMDb page has no og:image tag.
    // test
    @Test
    void testGetPosterURL_ReturnsNullOnException() {
        // Arrange
        Movie m = new Movie(
                6L, "tt666", "Test", "Test",
                2000, 100, true, false, false,
                null, "PG"
        );

        MovieService spyService = Mockito.spy(movieService);

        doThrow(new RuntimeException("fail"))
                .when(spyService)
                .exchangeImdb(anyString(), any(HttpEntity.class));

        // Act
        String result = spyService.getPosterURL(m);

        // Assert
        assertNull(result);
        verify(movieRepository, never()).updatePosterURL(anyLong(), anyString());
    }

}
