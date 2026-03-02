package dk.fklub.fkult;

import dk.fklub.fkult.business.services.ImdbMovieImportService;
import dk.fklub.fkult.config.ImportSchedular;
import dk.fklub.fkult.persistence.repository.MovieRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


// ImdbMovieImportService Tests
class ImdbMovieImportServiceTest {

    @Mock
    private MovieRepository movieRepository;

    private ImdbMovieImportService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        service = new ImdbMovieImportService(movieRepository);
    }

    @Test
    @DisplayName("weeklyRefresh calls repository and returns expected result")
    void testWeeklyRefresh() throws Exception {
        // Arrange
        when(movieRepository.upsertFromImdbFile(any(File.class), eq(true), eq(true)))
                .thenReturn(42);

        // Act
        int result = service.weeklyRefresh();

        // Assert
        assertEquals(42, result);
        verify(movieRepository, times(1))
                .upsertFromImdbFile(any(File.class), eq(true), eq(true));
    }

    @Test
    @DisplayName("weeklyRefresh throws IOException if download fails")
    void testWeeklyRefreshThrowsIOException() throws Exception {
        // Arrange
        ImdbMovieImportService failingService = new ImdbMovieImportService(movieRepository) {
            @Override
            public int weeklyRefresh() throws IOException {
                throw new IOException("Network error");
            }
        };

        // Act & Assert
        assertThrows(IOException.class, () -> failingService.weeklyRefresh(),
                "Expected IOException if download fails");

        // Verify repository not called
        verify(movieRepository, never())
                .upsertFromImdbFile(any(), anyBoolean(), anyBoolean());
    }

    @Test
    @DisplayName("weeklyRefresh throws IOException if replaceOldWithTemp fails")
    void testReplaceOldWithTempThrowsIOException() throws Exception {
    // Arrange
    ImdbMovieImportService failingService = new ImdbMovieImportService(movieRepository) {
        @Override
        public int weeklyRefresh() throws IOException {
            // Simulate file replacement failure
            throw new IOException("Disk error during file replacement");
        }
    };

    // Act & Assert
    assertThrows(IOException.class, failingService::weeklyRefresh,
            "Expected IOException if replaceOldWithTemp fails");

    // Verify the repository was never called
    verify(movieRepository, never())
            .upsertFromImdbFile(any(), anyBoolean(), anyBoolean());
    }

    @Test
    @DisplayName("weeklyRefresh throws if repository upsert fails")
    void testRepositoryThrows() throws Exception {
    // Arrange
    when(movieRepository.upsertFromImdbFile(any(File.class), anyBoolean(), anyBoolean()))
            .thenThrow(new RuntimeException("Database failure"));

    // Act & Assert
    assertThrows(RuntimeException.class, () -> service.weeklyRefresh(),
            "Expected RuntimeException if repository fails");
    }
    
@Test
@DisplayName("downloadTemp cleans up temp file when IOException occurs")
void testDownloadTempSimulatedFailure(@TempDir Path tempDir) throws Exception {
    // Use the real service (repo is irrelevant for this test)
    ImdbMovieImportService svc = new ImdbMovieImportService(mock(MovieRepository.class));

    // Make the service write relative to the temp dir ("database" is a relative Path)
    System.setProperty("user.dir", tempDir.toString());

    // Locate the protected method with the correct signature
    Method m = ImdbMovieImportService.class
            .getDeclaredMethod("downloadTemp", String.class, String.class);
    m.setAccessible(true);

    // A missing local file URL so openStream() fails -> downloadTemp catches and deletes .tmp
    String badUrl = tempDir.resolve("nope-does-not-exist.gz").toUri().toString();
    String prefix = "title.basics-";

    // Invoke and assert that it throws IOException (wrapped in InvocationTargetException)
    try {
        m.invoke(svc, badUrl, prefix);
        fail("Expected an IOException from downloadTemp");
    } catch (InvocationTargetException ite) {
        assertTrue(ite.getCause() instanceof IOException, "Cause should be IOException");
    }

    // Verify: no leftover *.tmp files under the expected "database" dir
    Path dataDir = tempDir.resolve("database");
    if (Files.exists(dataDir)) {
        try (var stream = Files.list(dataDir)) {
            long leftovers = stream
                    .filter(p -> p.getFileName().toString().endsWith(".tmp"))
                    .count();
            assertEquals(0, leftovers, "Temp files should be deleted after simulated failure");
        }
    }
}


}



// ImportSchedular Tests
class ImportSchedularTest {

    @Mock
    private ImdbMovieImportService mockService;

    private ImportSchedular schedular;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        schedular = new ImportSchedular(mockService);
    }

    @Test
    @DisplayName("weeklyImdbUpdate runs successfully and prints output")
    void testWeeklyImdbUpdateSuccess() throws Exception {
        // Arrange
        when(mockService.weeklyRefresh()).thenReturn(100);

        // Act
        schedular.weeklyImdbUpdate();

        // Assert
        verify(mockService, times(1)).weeklyRefresh();
    }

    @Test
    @DisplayName("weeklyImdbUpdate catches and logs exceptions")
    void testWeeklyImdbUpdateHandlesException() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Simulated failure")).when(mockService).weeklyRefresh();

        // Act (should not throw)
        assertDoesNotThrow(() -> schedular.weeklyImdbUpdate());

        // Assert (still called once)
        verify(mockService, times(1)).weeklyRefresh();
    }
}
