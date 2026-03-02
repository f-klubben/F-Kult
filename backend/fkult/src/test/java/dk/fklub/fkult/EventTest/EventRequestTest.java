package dk.fklub.fkult.EventTest;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import dk.fklub.fkult.presentation.DTOs.EventRequest;

public class EventRequestTest {
    // Tests the main constructor: (id, name, username, timestamp, drinkingRules, tConsts)
    @Test
    void testMainConstructor() {
        // Arrange
        Long id = 10L;
        String name = "Event Name";
        String username = "TestUser";
        LocalDateTime timestamp = LocalDateTime.now();
        List<String> rules = List.of("Rule 1", "Rule 2");
        List<String> tConsts = List.of("tt1", "tt2");

        // Act
        EventRequest req = new EventRequest(id, name, username, timestamp, rules, tConsts);

        // Assert
        assertAll(
            () -> assertEquals(id, req.getId()),
            () -> assertEquals(name, req.getName()),
            () -> assertEquals(username, req.getUsername()),
            () -> assertEquals(timestamp, req.getTimestamp()),
            () -> assertEquals(rules, req.getDrinkingRules()),
            () -> assertEquals(tConsts, req.gettConsts())
        );
    }

    // Tests the constructor: (id, timestamp)
    @Test
    void testPartialConstructor() {
        // Arrange
        Long id = 5L;
        LocalDateTime timestamp = LocalDateTime.now();

        // Act
        EventRequest req = new EventRequest(id, timestamp);

        // Assert
        assertAll(
            () -> assertEquals(timestamp, req.getTimestamp()),
            () -> assertNull(req.getName()),
            () -> assertNull(req.getUsername()),
            () -> assertNull(req.getDrinkingRules()),
            () -> assertNull(req.gettConsts())
        );
    }

    // Tests that the default constructor initializes all fields to null
    @Test
    void testDefaultConstructor() {
        // Arrange
        EventRequest req = new EventRequest();

        // Act & Assert
        assertAll(
            () -> assertNull(req.getId()),
            () -> assertNull(req.getName()),
            () -> assertNull(req.getUsername()),
            () -> assertNull(req.getTimestamp()),
            () -> assertNull(req.getDrinkingRules()),
            () -> assertNull(req.gettConsts())
        );
    }

    // Tests that setters correctly update all fields
    @Test
    void testSetters() {
        // Arrange
        EventRequest req = new EventRequest();
        LocalDateTime time = LocalDateTime.now();
        List<String> rules = List.of("A", "B");
        List<String> tConsts = List.of("x", "y");

        // Act
        req.setId(100L);
        req.setName("Updated Name");
        req.setUsername("UpdatedUser");
        req.setTimestamp(time);
        req.setDrinkingRules(rules);
        req.settConsts(tConsts);

        // Assert
        assertAll(
            () -> assertEquals(100L, req.getId()),
            () -> assertEquals("Updated Name", req.getName()),
            () -> assertEquals("UpdatedUser", req.getUsername()),
            () -> assertEquals(time, req.getTimestamp()),
            () -> assertEquals(rules, req.getDrinkingRules()),
            () -> assertEquals(tConsts, req.gettConsts())
        );
    }

    // Tests that getters return null when not initialized
    @Test
    void testNullState() {
        // Arrange
        EventRequest req = new EventRequest();

        // Act & Assert
        assertNull(req.getName());
        assertNull(req.getUsername());
        assertNull(req.getTimestamp());
        assertNull(req.getDrinkingRules());
        assertNull(req.gettConsts());
    }
}
