package dk.fklub.fkult.ThemeTests;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import dk.fklub.fkult.presentation.DTOs.ThemeRequest;


public class ThemeRequestTest {
    

    //Tests the constructor: (themeId, name, userId, movieIds, drinkingRules)
    @Test
    void testPrimaryConstructor() {
         // Arrange
        Long themeId = 1L;
        String name = "Test Theme";
        Long userId = 10L;
        List<Long> movieIds = List.of(101L, 102L);
        List<String> rules = List.of("Rule 1", "Rule 2");

        // Act
        ThemeRequest req = new ThemeRequest(themeId, name, userId, movieIds, rules);

        // Assert
        assertAll(
            () -> assertEquals(themeId, req.getThemeId()),
            () -> assertEquals(name, req.getName()),
            () -> assertEquals(userId, req.getUserId()),
            () -> assertEquals(movieIds, req.getMovieIds()),
            () -> assertEquals(rules, req.getDrinkingRules())
        );

    }

    // Tests the constructor: (tConsts, themeId, name, username, drinkingRules)
    @Test
    void testTConstConstructor() {
        // Arrange
        Long themeId = 2L;
        String name = "Theme";
        String username = "User123";
        List<String> tConsts = List.of("tt0001", "tt0002");
        List<String> rules = List.of("Drink A", "Drink B");

        // Act
        ThemeRequest req = new ThemeRequest(tConsts, themeId, name, username, rules);

        // Assert
        assertAll(
            () -> assertEquals(themeId, req.getThemeId()),
            () -> assertEquals(name, req.getName()),
            () -> assertEquals(username, req.getUsername()),
            () -> assertEquals(tConsts, req.gettConsts()),
            () -> assertEquals(rules, req.getDrinkingRules())
        );
    }



    // Tests the constructor: (name, username, tConsts, drinkingRules)
    @Test
    void testMinimalConstructor() {
        // Arrange
        String name = "Mini";
        String username = "Alpha";
        List<String> tConsts = List.of("tc1", "tc2");
        List<String> rules = List.of("Rule X");

        // Act
        ThemeRequest req = new ThemeRequest(name, username, tConsts, rules);

        // Assert
        assertAll(
            () -> assertEquals(name, req.getName()),
            () -> assertEquals(username, req.getUsername()),
            () -> assertEquals(tConsts, req.gettConsts()),
            () -> assertEquals(rules, req.getDrinkingRules())
        );
    }
    

    // Tests the constructor including timestamp
    @Test
    void testTimestampConstructor() {
        // Arrange
        Long themeId = 3L;
        LocalDateTime now = LocalDateTime.now();

        // Act
        ThemeRequest req = new ThemeRequest(themeId, "Theme", 5L, List.of(9L, 10L), List.of("A", "B"), now);

        // Assert
        assertEquals(now, req.getTimestamp());
    }


    // Tests the full constructor with username + timestamp
    @Test
    void testFullConstructor() {
        // Arrange
        Long themeId = 4L;
        String username = "Zed";
        LocalDateTime now = LocalDateTime.now();

        // Act
        ThemeRequest req = new ThemeRequest(themeId, "Full Theme", username, 12L, List.of(1L, 2L), List.of("R1"),now);

        // Assert
        assertAll(
            () -> assertEquals(themeId, req.getThemeId()),
            () -> assertEquals(username, req.getUsername()),
            () -> assertEquals(now, req.getTimestamp())
        );
    }


    // Tests that all setters correctly update their fields
    @Test
    void testSetters() {
        // Arrange
        ThemeRequest req = new ThemeRequest();
        LocalDateTime time = LocalDateTime.now();

        // Act
        req.setName("New Name");
        req.setUserId(99L);
        req.setMovieIds(List.of(7L, 8L));
        req.setDrinkingRules(List.of("Drink 1"));
        req.settConsts(List.of("t1"));
        req.setUsername("Tester");
        req.setTimestamp(time);

        // Assert
        assertAll(
            () -> assertEquals("New Name", req.getName()),
            () -> assertEquals(99L, req.getUserId()),
            () -> assertEquals(List.of(7L, 8L), req.getMovieIds()),
            () -> assertEquals(List.of("Drink 1"), req.getDrinkingRules()),
            () -> assertEquals(List.of("t1"), req.gettConsts()),
            () -> assertEquals("Tester", req.getUsername()),
            () -> assertEquals(time, req.getTimestamp())
        );
    }

    // Tests the toString output contains all major fields
    @Test
    void testToString() {
        // Arrange
        ThemeRequest req = new ThemeRequest(7L, "My Theme", 10L, List.of(1L, 2L), List.of("X", "Y"), LocalDateTime.of(2024, 1, 1, 12, 0));

        // Act
        String s = req.toString();

        // Assert
        assertAll(
            () -> assertTrue(s.contains("themeId=7")),
            () -> assertTrue(s.contains("name='My Theme'")),
            () -> assertTrue(s.contains("userId=10")),
            () -> assertTrue(s.contains("1, 2")),
            () -> assertTrue(s.contains("X, Y")),
            () -> assertTrue(s.contains("2024-01-01"))
        );
    }

    // Tests that the default constructor initializes all fields to null
    @Test
    void testDefaultConstructor() {
        // Arrange
        ThemeRequest req = new ThemeRequest();

        // Act & Assert
        assertAll(
            () -> assertNull(req.getThemeId()),
            () -> assertNull(req.getName()),
            () -> assertNull(req.getUserId()),
            () -> assertNull(req.getMovieIds()),
            () -> assertNull(req.getDrinkingRules()),
            () -> assertNull(req.gettConsts()),
            () -> assertNull(req.getUsername()),
            () -> assertNull(req.getTimestamp())
        );
    }

    // Tests behavior when movieIds and drinkingRules are null
    @Test
    void testNullListsHandling() {
        // Arrange
        ThemeRequest req = new ThemeRequest(1L, "Theme", 2L, null, null);

        // Act & Assert
        assertNull(req.getMovieIds());
        assertNull(req.getDrinkingRules());
        assertTrue(req.toString().contains("movieIds=null"));
        assertTrue(req.toString().contains("drinkingRules=null"));
    }

    // Tests that username is null when not supplied in constructor
    @Test
    void testUsernameIsNullWhenNotProvided() {
        // Arrange
        ThemeRequest req = new ThemeRequest(5L, "No Username", 3L, List.of(1L), List.of("R1"));

        // Act & Assert
        assertNull(req.getUsername());
    }

    // Tests toString formatting for tConsts
    @Test
    void testToStringForTConsts() {
        // Arrange
        ThemeRequest req = new ThemeRequest(
                List.of("a", "b", "c"), 1L, "Test", "User", List.of("R1")
        );

        // Act
        String s = req.toString();

        // Assert
        assertTrue(s.contains("tConsts=a, b, c"));
    }

}
