package dk.fklub.fkult;

import com.p3.fkult.business.services.MovieService;
import com.p3.fkult.persistence.entities.Movie;
import com.p3.fkult.persistence.repository.MovieRepository;

import com.p3.fkult.presentation.DTOs.MovieRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class MovieSearchTest {

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private MovieService movieService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSearchMovies() {
        // arrange
        Movie testMovie = new Movie(
        1,
        "tt1234567",
        "Test Movie",
        "Original",
        2020,
        120,
        true,
        false,
        false,
        null,
        null
        );
        when(movieRepository.searchMovies("test", 1, 6, null, null, null, null, null, null)).thenReturn(List.of(testMovie));

        // act
        List<MovieRequest> result = movieService.searchMovies("test", 1, 6,null, null, null, null, null, null);

        // assert
        assertEquals(1, result.size());
        assertEquals("Test Movie", result.get(0).getTitle());
        verify(movieRepository, times(1)).searchMovies("test", 1, 6, null, null, null, null, null, null);
    }
}
