package dk.fklub.fkult.MovieTest;

import dk.fklub.fkult.persistence.entities.Movie;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class MovieTest {

    // test constructor with wrapper types
    @Test
    void testConstructorWithWrappers() {
        // Arrange
        long id = 1L;
        String tconst = "tt123";
        String movieName = "Movie Name";
        String originalName = "Original Name";
        Integer year = 2020;
        Integer runtime = 120;
        Boolean isActive = true;
        Boolean isSeries = false;
        Boolean isShorts = false;
        String poster = "poster.jpg";
        String rating = "8.5";

        // Act
        Movie movie = new Movie(
                id,
                tconst,
                movieName,
                originalName,
                year,
                runtime,
                isActive,
                isSeries,
                isShorts,
                poster,
                rating
        );

        // Assert
        assertEquals(id, movie.getId());
        assertEquals(tconst, movie.getTconst());
        assertEquals(movieName, movie.getMovieName());
        assertEquals(originalName, movie.getOriginalMovieName());
        assertEquals(year, movie.getYear());
        assertEquals(runtime, movie.getRuntimeMinutes());
        assertEquals(isActive, movie.getIsActive());
        assertEquals(isSeries, movie.getIsSeries());
        assertEquals(poster, movie.getPosterURL());
        assertEquals(rating, movie.getRating());
    }

    // test constructor with primitives
    @Test
    void testConstructorWithPrimitives() {
        // Arrange
        int id = 2;
        String tconst = "tt999";
        String movieName = "Primitive Movie";
        String originalName = "Primitive Original";
        int year = 1999;
        int runtime = 90;
        boolean isActive = false;
        boolean isSeries = true;
        boolean isShorts = false;
        String poster = "img.png";
        String rating = "7.1";

        // Act
        Movie movie = new Movie(
                id,
                tconst,
                movieName,
                originalName,
                year,
                runtime,
                isActive,
                isSeries,
                isShorts,
                poster,
                rating
        );

        // Assert
        assertEquals((long) id, movie.getId());
        assertEquals(tconst, movie.getTconst());
        assertEquals(movieName, movie.getMovieName());
        assertEquals(originalName, movie.getOriginalMovieName());
        assertEquals(Integer.valueOf(year), movie.getYear());
        assertEquals(Integer.valueOf(runtime), movie.getRuntimeMinutes());
        assertEquals(isActive, movie.getIsActive());
        assertEquals(isSeries, movie.getIsSeries());
        assertEquals(poster, movie.getPosterURL());
        assertEquals(rating, movie.getRating());
    }

    // test getters
    @Test
    void testGetters() {
        // Arrange
        long id = 10L;
        String tconst = "tt555";
        String movieName = "Getter Test";
        String originalName = "Getter Original";
        Integer year = 2012;
        Integer runtime = 110;
        Boolean isActive = true;
        Boolean isSeries = true;
        Boolean isShorts = false;
        String poster = "poster.png";
        String rating = "9.0";

        Movie movie = new Movie(
                id,
                tconst,
                movieName,
                originalName,
                year,
                runtime,
                isActive,
                isSeries,
                isShorts,
                poster,
                rating
        );

        // Act & Assert
        assertEquals(id, movie.getId());
        assertEquals(tconst, movie.getTconst());
        assertEquals(movieName, movie.getMovieName());
        assertEquals(originalName, movie.getOriginalMovieName());
        assertEquals(year, movie.getYear());
        assertEquals(runtime, movie.getRuntimeMinutes());
        assertEquals(isActive, movie.getIsActive());
        assertEquals(isSeries, movie.getIsSeries());
        assertEquals(poster, movie.getPosterURL());
        assertEquals(rating, movie.getRating());
    }

    // test setters
    @Test
    void testSetters() {
        // Arrange
        Movie movie = new Movie(
                0L,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        long newId = 5L;
        String newTconst = "tt777";
        String newMovieName = "Setter Test";
        String newOriginalName = "Setter Original";
        Integer newYear = 2021;
        Integer newRuntime = 150;
        Boolean newIsActive = true;
        Boolean newIsSeries = false;
        String newPoster = "set.jpg";
        String newRating = "6.7";

        // Act
        movie.setId(newId);
        movie.setTconst(newTconst);
        movie.setMovieName(newMovieName);
        movie.setOriginalMovieName(newOriginalName);
        movie.setYear(newYear);
        movie.setRuntimeMinutes(newRuntime);
        movie.setIsActive(newIsActive);
        movie.setIsSeries(newIsSeries);
        movie.setPosterURL(newPoster);
        movie.setRating(newRating);

        // Assert
        assertEquals(newId, movie.getId());
        assertEquals(newTconst, movie.getTconst());
        assertEquals(newMovieName, movie.getMovieName());
        assertEquals(newOriginalName, movie.getOriginalMovieName());
        assertEquals(newYear, movie.getYear());
        assertEquals(newRuntime, movie.getRuntimeMinutes());
        assertEquals(newIsActive, movie.getIsActive());
        assertEquals(newIsSeries, movie.getIsSeries());
        assertEquals(newPoster, movie.getPosterURL());
        assertEquals(newRating, movie.getRating());
    }
}
