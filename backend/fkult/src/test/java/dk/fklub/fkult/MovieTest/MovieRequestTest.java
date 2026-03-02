package dk.fklub.fkult.MovieTest;

import dk.fklub.fkult.presentation.DTOs.MovieRequest;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class MovieRequestTest {
    
 @Test
    public void testFirstConstructorAndGetters() {
        // Arrange
        long movieId = 101;
        String title = "Interstellar";
        String poster = "url.jpg";
        String rating = "PG-13";
        int runtime = 169;
        int year = 2014;

        // Act
        MovieRequest request = new MovieRequest(movieId, title, poster, rating, runtime, year);

        // Assert
        assertEquals(movieId, request.getMovieId());
        assertEquals(title, request.getTitle());
        assertEquals(poster, request.getMoviePosterURL());
        assertEquals(rating, request.getRating());
        assertEquals(runtime, request.getRuntimeMinutes());
        assertEquals(year, request.getYear());
    }

    // test creating MovieRequest using the second constructor and verifying getters
    @Test
    public void testSecondConstructorAndGetters() {
        // Arrange
        String tConst = "tt1234567";
        String title = "Breaking Bad";
        int runtime = 47;
        int year = 2008;
        String rating = "TV-MA";
        String poster = "poster.jpg";
        boolean isSeries = true;
        boolean isShorts = false;

        // Act
        MovieRequest request = new MovieRequest(
                tConst, title, runtime, year, rating, poster, isSeries, isShorts
        );

        // Assert
        assertEquals(tConst, request.gettConst());
        assertEquals(title, request.getTitle());
        assertEquals(runtime, request.getRuntimeMinutes());
        assertEquals(year, request.getYear());
        assertEquals(rating, request.getRating());
        assertEquals(poster, request.getMoviePosterURL());
        assertTrue(request.getIsSeries());
        assertFalse(request.getIsShorts());
    }

    // test setting values using setters and verifying they changed
    @Test
    public void testSetters() {
        // Arrange
        MovieRequest request = new MovieRequest(1, "Old Title", "old.jpg", "PG", 90, 2000);

        // Act
        request.setMovieId(999);
        request.setTitle("New Title");
        request.setMoviePosterURL("new.jpg");
        request.setRating("R");
        request.setRuntimeMinutes(120);
        request.setYear(2024);
        request.settConst("tt9876543");
        request.setIsSeries(true);
        request.setIsShorts(true);

        // Assert
        assertEquals(999, request.getMovieId());
        assertEquals("New Title", request.getTitle());
        assertEquals("new.jpg", request.getMoviePosterURL());
        assertEquals("R", request.getRating());
        assertEquals(120, request.getRuntimeMinutes());
        assertEquals(2024, request.getYear());
        assertEquals("tt9876543", request.gettConst());
        assertTrue(request.getIsSeries());
        assertTrue(request.getIsShorts());
    }
}
