package dk.fklub.fkult;

import dk.fklub.fkult.presentation.DTOs.SoundSampleRequest;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SoundSampleRequestTest {

    // test constructor and getters return correct values
    @Test
    void testConstructorAndGetters() {
        // Arrange
        String sample = "sample.mp3";
        String username = "johnDoe";
        String fullName = "John Doe";
        long id = 42L;

        // Act
        SoundSampleRequest request = new SoundSampleRequest(sample, username, fullName, id);

        // Assert
        assertEquals(sample, request.getSoundSample());
        assertEquals(username, request.getUsername());
        assertEquals(fullName, request.getUsersFullName());
        assertEquals(id, request.getId());
    }

    // test setters modify values correctly
    @Test
    void testSetters() {
        // Arrange
        SoundSampleRequest request = new SoundSampleRequest(
                "old.mp3",
                "oldUser",
                "Old Name",
                1L
        );

        // Act
        request.setSoundSample("new.mp3");
        request.setUsername("newUser");
        request.setId(99L);

        // Assert
        assertEquals("new.mp3", request.getSoundSample());
        assertEquals("newUser", request.getUsername());
        assertEquals(99L, request.getId());
    }
}
