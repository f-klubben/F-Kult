package dk.fklub.fkult;

import dk.fklub.fkult.business.services.ImdbMovieImportService;
import dk.fklub.fkult.config.ImdbChecker;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.ApplicationArguments;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;



// Imdbchecker test
public class ImdbCheckerTest {
    @Mock
    private ImdbMovieImportService mockService;
    private AutoCloseable mocks;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mocks != null) mocks.close();
    }

    @Test
    @DisplayName("checks no file exist so run data dumb downloader")
    void testFilesNotExist() throws Exception {
        //Arrange
        Path tempDir = Files.createTempDirectory("imdb_test_dir");
        Path local = tempDir.resolve("title.basics.tsv.gz");
        ImdbChecker checker = new ImdbChecker(mockService, local.toString());
        when(mockService.weeklyRefresh()).thenReturn(3);
        ApplicationArguments argument = mock(ApplicationArguments.class);

        //Act
        checker.run(argument);

        //Assert
        verify(mockService, times(1)).weeklyRefresh();
        verifyNoMoreInteractions(mockService);
    }
    
    @Test
    @DisplayName("checks files exist so don't run data dumb downloader")
    void testFilesExist() throws Exception {
        //Arrange
        Path tempDir = Files.createTempDirectory("imdb_test_dir");
        Path local = tempDir.resolve("title.basics.tsv.gz");
        Files.createFile(local);
        ImdbChecker checker = new ImdbChecker(mockService, local.toString());
        ApplicationArguments argument = mock(ApplicationArguments.class);

        //Act
        checker.run(argument);

        //Assert
        verify(mockService, never()).weeklyRefresh();
        verifyNoMoreInteractions(mockService);
    }

}