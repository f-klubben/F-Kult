package dk.fklub.fkult;

import dk.fklub.fkult.business.services.SoundSampleService;
import dk.fklub.fkult.business.services.UserService;
import dk.fklub.fkult.persistence.entities.SoundSample;
import dk.fklub.fkult.persistence.entities.User;
import dk.fklub.fkult.persistence.repository.SoundSampleRepository;
import dk.fklub.fkult.presentation.DTOs.SoundSampleRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import java.io.File;
import java.nio.file.Files;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.springframework.core.io.Resource;

public class SoundSampleTest {

    @Mock
    private SoundSampleRepository repository;
    private SoundSampleService service;
    
    @Mock
    private UserService userService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        service = new SoundSampleService(repository, userService);
    }

    @Test
    @DisplayName("Upload soundsample with a link")
    void testUploadSoundSampleWithLink() throws Exception {

        // Arrange
        String link = "http://example.com/sample";
        Long userId = 1l;
        String json = String.format("{\"link\":\"%s\",\"filePath\":null,\"userId\":\"%s\"}", link, userId);

        // Act
        String response = service.upload(null, json);

        // Assert
        assertEquals("Upload complete!", response);
        verify(repository, times(1)).save(any(SoundSample.class));
    }

    @Test
    @DisplayName("Upload soundsample with a file")
    void testUploadSoundSampleWithFile() throws Exception {

        // Arrange
        Long userId = 1l;
        String json = String.format("{\"link\":\"null\",\"filePath\":null,\"userId\":\"%s\"}", userId);
        MockMultipartFile file = new MockMultipartFile(
            "file", // Data type
            "sample.mp3", // Original file name
            "audio/mpeg", // Content type
            "dummy content".getBytes() // File content
        );
        File uploadedFile = new File("soundSampleUploads/sample.mp3");

        try {
            // Act
            String response = service.upload(file, json);

            // Assert
            assertEquals("Upload complete!", response);
            verify(repository, times(1)).save(any(SoundSample.class));
            assertTrue(uploadedFile.exists(), "Uploaded file should exist");

        } finally {
            // Cleanup
            if (uploadedFile.exists()) {
                assertTrue(uploadedFile.delete(), "Cleanup: uploaded file deleted");
            }
        }
    }

    @Test
    @DisplayName("Upload a soundsample with no data")
    void testUploadSoundSampleWithNoData() throws Exception {

        // Arrange
        Long userId = 1l;
        String json = String.format("{\"link\":null,\"filePath\":null,\"userId\":\"%s\"}", userId);

        // Act
        String response = service.upload(null, json);

        // Assert
        assertEquals("Upload failed: either link or file must be provided for upload.", response);
        verify(repository, times(0)).save(any(SoundSample.class));
    }

    @Test
    @DisplayName("Delete a valid soundsample from file name")
    void testDeleteValidSoundSampleFromFileName() throws Exception {
        
        // Arrange
        String fileName = "testfile.wav";
        File uploadDir = new File("soundSampleUploads");
        if (!uploadDir.exists()) uploadDir.mkdirs();
        File testFile = new File(uploadDir, fileName);
        Files.writeString(testFile.toPath(), "dummy data");

        // Act
        String response = service.delete(null, fileName, "1");

        // Assert
        assertNull(response, "Service should return null since repository mock returns null");
        assertFalse(testFile.exists(), "File should have been deleted");
        verify(repository, times(1)).delete(isNull(), contains(fileName), eq("1"));
    }

    @Test
    @DisplayName("Try to delete a sound sample from link")
    void testDeleteValidSoundSampleFromLink() throws Exception {

        // Arrange
        String link = "http://example.com/test-sample.mp3";
        String id ="1";
        when(repository.delete(eq(link), isNull(), eq(id))).thenReturn("Reached database succesfully");

        // Act
        String response = service.delete(link, null, id);

        // Assert
        assertEquals("Reached database succesfully", response);
        verify(repository, times(1)).delete(eq(link), isNull(), eq(id));
    }

    @Test
    @DisplayName("Delete an invalid sound sample from file name")
    void testDeleteInvalidSoundSampleFromFileName() throws Exception {

        // Arrange
        String invalidFileName = "nonexistent.wav";

        // Act
        String response = service.delete(null, invalidFileName, "1");

        // Assert
        assertTrue(response.contains(invalidFileName + ". Aborting database deletion.")); // Checks last part of string
        verify(repository, times(0)).delete(isNull(), eq(invalidFileName), eq("2"));
    }

    private List<SoundSample> createTargetSamples() {
        SoundSample sample1 = new SoundSample(null, "/intro.mp3", 1L);
        SoundSample sample2 = new SoundSample("https://cdn.example.com/sound/wind.mp3", null, 1l);
        SoundSample sample3 = new SoundSample("https://www.example.com/audio/intro.mp3", null, 2l);
        return Arrays.asList(sample1, sample2, sample3);
    }

    private List<User> createTargetUsers() {
        User user1 = new User(1l, "k1m", "Kim Nielsen", 0, 0);
        User user2 = new User(2l, "Bob", "Bob Marlie", 0, 0);
        return Arrays.asList(user1, user2);
    }

    @Test
    // Get all sound samples - deafault
    void getAllSoundSamplesDefault() throws Exception {
        when(userService.getAllUsers()).thenReturn(createTargetUsers());
        when(repository.getAll()).thenReturn(createTargetSamples());

        List<SoundSampleRequest> soundSamples = service.getAllSoundSamples(false,false);

        assertNotEquals(0, soundSamples.size());
        assertEquals(3, soundSamples.size());
    }

    @Test
    // Get all sound samples - quick shuffle
    void getAllSoundSamplesQuick() throws Exception {
        when(userService.getAllUsers()).thenReturn(createTargetUsers());
        when(repository.getAll()).thenReturn(createTargetSamples());

        List<SoundSampleRequest> soundSamples = service.getAllSoundSamples(false,false);
        List<SoundSampleRequest> quickSoundSamples = service.getAllSoundSamples(true,false);

        assertNotEquals(0, soundSamples.size());
        assertEquals(soundSamples.size(), quickSoundSamples.size());
        assertTrue(
            quickSoundSamples
                .stream()
                .map(SoundSampleRequest::getSoundSample)
                .toList()
                .containsAll(
                    soundSamples.stream()
                        .map(SoundSampleRequest::getSoundSample)
                        .toList()
                )
        );
        
        boolean sameOrder = true;
        for (int i = 0; i < soundSamples.size(); i++) {
            if (soundSamples.get(i) != quickSoundSamples.get(i)) {
                sameOrder = false;
                break;
            }
        }
        assertFalse(sameOrder);
    }

    @Test
    // Get all sound samples - weighted shuffle
    void getAllSoundSamplesWeighted() throws Exception {
        when(userService.getAllUsers()).thenReturn(createTargetUsers());
        when(repository.getAll()).thenReturn(createTargetSamples());

        List<SoundSampleRequest> soundSamples = service.getAllSoundSamples(false,false);
        List<SoundSampleRequest> weightedSoundSamples = service.getAllSoundSamples(false,true);
        
        assertNotEquals(0, soundSamples.size());
        assertEquals(soundSamples.size(), weightedSoundSamples.size());
        assertTrue(
            weightedSoundSamples
                .stream()
                .map(SoundSampleRequest::getSoundSample)
                .toList()
                .containsAll(
                    soundSamples.stream()
                        .map(SoundSampleRequest::getSoundSample)
                        .toList()
                )
        );
        
        boolean sameOrder = true;
        for (int i = 0; i < soundSamples.size(); i++) {
            if (soundSamples.get(i) != weightedSoundSamples.get(i)) {
                sameOrder = false;
                break;
            }
        }
        assertFalse(sameOrder);
    }

    @Test
    // Get all sound samples - both quick and weighted shuffle, should return default
    void getAllSoundSamplesBothFilter() throws Exception {
        when(userService.getAllUsers()).thenReturn(createTargetUsers());
        when(repository.getAll()).thenReturn(createTargetSamples());

        List<SoundSampleRequest> BothSoundSamples = service.getAllSoundSamples(true, true);

        assertEquals(0, BothSoundSamples.size());
    }

    @Test
    // Get a spesafc existing sound sample file
    void getExistingSoundSampleFile() throws Exception {

        // Arrange
        Long userId = 1l;
        String json = String.format("{\"link\":\"null\",\"filePath\":null,\"userId\":\"%s\"}", userId);
        MockMultipartFile file = new MockMultipartFile(
            "file", // Data type
            "sample.mp3", // Original file name
            "audio/mpeg", // Content type
            "dummy content".getBytes() // File content
        );
        File uploadedFile = new File("soundSampleUploads/sample.mp3");

        try {
            // Act
            String response = service.upload(file, json);

            // Assert the sound sample
            assertEquals("Upload complete!", response);
            verify(repository, times(1)).save(any(SoundSample.class));
            assertTrue(uploadedFile.exists(), "Uploaded file should exist");

            // Assert getting the sound sample file
            String fileName = "sample.mp3";
            Resource resFile = service.getSoundSampleFile(fileName);
            // Assert the resource
            assertNotNull(resFile, "Resource should not be null");
            assertTrue(resFile.exists(), "Resource should point to an existing file");
            assertEquals(fileName, resFile.getFilename(), "Resource filename should match requested filename");
        } finally {
            // Cleanup
            if (uploadedFile.exists()) {
                assertTrue(uploadedFile.delete(), "Cleanup: uploaded file deleted");
            }
        }
    }

    @Test
    // Try to get an non-existing sound sample file
    void getNonExistingSoundSampleFile() throws Exception {
        assertThrows(RuntimeException.class, () -> {
            service.getSoundSampleFile("notAFile.mp3");
        });
    }
}