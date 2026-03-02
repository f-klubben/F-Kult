package dk.fklub.fkult.MovieTest;

import com.p3.fkult.persistence.repository.MovieRepository;
import com.p3.fkult.persistence.entities.Movie;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


public class MovieRepositoryTest {

    private JdbcTemplate jdbcTemplate;
    private MovieRepository movieRepository;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(JdbcTemplate.class);
        movieRepository = new MovieRepository(jdbcTemplate);
    }


    // TEST: findAll()
    @Test
    void findAll_returnsListOfMovies() {
        // Arrange
        Movie m = new Movie(1L, "tt001", "Test", "Test", 2020, 120, true, false, false, "poster", "7.5");
        when(jdbcTemplate.query(anyString(), any(RowMapper.class)))
                .thenReturn(List.of(m));

        // Act
        List<Movie> result = movieRepository.findAll();

        // Assert
        assertEquals(1, result.size());
        verify(jdbcTemplate).query(eq("SELECT * FROM movie"), any(RowMapper.class));
    }


    // TEST: findById() returns movie
    @Test
    void findById_returnsMovie() {
        // Arrange
        Movie m = new Movie(1L, "tt001", "Test", "Test", 2020, 120, true, false, false, "poster", "7.5");

        when(jdbcTemplate.queryForObject(
                anyString(),
                any(RowMapper.class),
                eq(1L)
        )).thenReturn(m);

        // Act
        Movie result = movieRepository.findById(1);

        // Assert
        assertNotNull(result);
        assertEquals("tt001", result.getTconst());
    }


    // TEST: findById() returns null on empty result
    @Test
    void findById_noResult_returnsNull() {
        // Arrange
        when(jdbcTemplate.queryForObject(
                anyString(),
                any(RowMapper.class),
                eq(1L)
        )).thenThrow(new EmptyResultDataAccessException(1));

        // Act
        Movie result = movieRepository.findById(1);

        // Assert
        assertNull(result);
    }


    // TEST: findByTconst()
    @Test
    void findByTconst_returnsMovie() {
        // Arrange
        Movie m = new Movie(1L, "tt001", "Test", "Test", 2020, 120, true, false, false, "poster", "7.5");

        when(jdbcTemplate.queryForObject(
                anyString(),
                any(RowMapper.class),
                eq("tt001")
        )).thenReturn(m);

        // Act
        Movie result = movieRepository.findByTconst("tt001");

        // Assert
        assertNotNull(result);
        assertEquals("tt001", result.getTconst());
    }


    // TEST: findNameById()
    @Test
    void findNameById_returnsName() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), eq(String.class), eq(5L)))
                .thenReturn("Avatar");

        // Act
        String name = movieRepository.findNameById(5L);

        // Assert
        assertEquals("Avatar", name);
    }


    // TEST: updatePosterURL()
    @Test
    void updatePosterURL_updatesCorrectly() {
        // Arrange
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        movieRepository.updatePosterURL(10L, "newPoster.jpg");

        // Assert
        verify(jdbcTemplate).update(sqlCaptor.capture(), eq("newPoster.jpg"), eq(10L));
        assertTrue(sqlCaptor.getValue().contains("UPDATE movie SET poster_url"));
    }


    // TEST: searchMovies()
    @Test
    void searchMovies_executesSearchQuery() {
        // Arrange
        when(jdbcTemplate.query(
                anyString(),
                any(RowMapper.class),
                any()
        )).thenReturn(List.of());

        // Act
        movieRepository.searchMovies("star", 1, 10, null, null, null, null, null, null);

        // Assert
        verify(jdbcTemplate).query(
                anyString(),
                any(RowMapper.class),
                eq("star*"),
                eq(10),
                eq(0)
        );
    }


    // TEST: countMovies()
    @Test
    void countMovies_executesCountQuery() {
        // Arrange
        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                eq("star*")
        )).thenReturn(5);

        // Act
        int count = movieRepository.countMovies("star");

        // Assert
        assertEquals(5, count);
    }


    @Test
    void findPosterById_returnsPoster() {
        // Arrange
        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(String.class),
                eq(7L)
        )).thenReturn("poster.jpg");

        // Act
        String result = movieRepository.findPosterById(7L);

        // Assert
        assertEquals("poster.jpg", result);
    }


    @Test
    void findRatingById_returnsRating() {
        // Arrange
        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Double.class),
                eq(3L)
        )).thenReturn(9.0);

        // Act
        Double result = movieRepository.findRatingById(3L);

        // Assert
        assertEquals(9.0, result);
    }


    @Test
    void findRunTimeById_returnsRuntime() {
        // Arrange
        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Long.class),
                eq(12L)
        )).thenReturn(142L);

        // Act
        Long result = movieRepository.findRunTimeById(12L);

        // Assert
        assertEquals(142L, result);
    }


    //trying big mocking stuff
    // test upsertfromimdbfile processes rows and marks inactive
    @Test
    void upsertFromImdbFile_processesRowsAndMarksInactive() throws Exception {
        // ARRANGE
        File mockFile = mock(File.class);

        // Mock BufferedReader manually
        BufferedReader mockReader = mock(BufferedReader.class);
        when(mockReader.readLine())
                .thenReturn("header")
                .thenReturn("tt123\tmovie\tTitle\tOriginal\t\\N\t2020\t\\N\t120\t\\N")
                .thenReturn(null);

        // Mock GZIPInputStream constructor
        try (MockedConstruction<FileInputStream> fisMock = mockConstruction(FileInputStream.class);
        MockedConstruction<GZIPInputStream> gzipMock = mockConstruction(GZIPInputStream.class);
        MockedConstruction<InputStreamReader> isrMock = mockConstruction(InputStreamReader.class,
                (isr, context) -> when(isr.read()).thenReturn(-1));
        MockedConstruction<BufferedReader> brMock = mockConstruction(BufferedReader.class,
                (br, context) -> when(br.readLine())
                        .thenAnswer(inv -> mockReader.readLine()))) {

            doNothing().when(jdbcTemplate).execute(anyString());
            when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);
            when(jdbcTemplate.batchUpdate(anyString(), any(List.class)))
                    .thenReturn(new int[]{1});
            // ACT
            int processed = movieRepository.upsertFromImdbFile(mockFile, true, true);

            // ASSERT
            assertEquals(1, processed);

            verify(jdbcTemplate).execute("PRAGMA foreign_keys = ON");
            verify(jdbcTemplate).execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_movie_tconst ON movie(tconst)");
            verify(jdbcTemplate).batchUpdate(contains("INSERT INTO movie"), any(List.class));
            verify(jdbcTemplate).update(eq(
                    "UPDATE movie SET is_active = 0 WHERE tconst NOT IN (SELECT tconst FROM tmp_seen)"
            ));
        }
    }


    // mergeRatingsFromImdbFile test
    @Test
    void mergeRatingsFromImdbFile_updatesRatings() throws Exception {
        // ARRANGE
        File mockFile = mock(File.class);


        // Fake reader lines
        BufferedReader mockReader = mock(BufferedReader.class);
        when(mockReader.readLine())
                .thenReturn("tconst\trating\tnumVotes")
                .thenReturn("tt123\t8.4\t10000")
                .thenReturn(null);

        try (
                MockedConstruction<FileInputStream> fisMock =
                        mockConstruction(FileInputStream.class, (mock, context) -> {
                            when(mock.read(any(byte[].class))).thenReturn(-1);
                            when(mock.read()).thenReturn(-1);
                        });

                MockedConstruction<GZIPInputStream> gzipMock = mockConstruction(GZIPInputStream.class);

                MockedConstruction<InputStreamReader> isrMock = mockConstruction(InputStreamReader.class,
                        (isr, context) -> when(isr.read()).thenReturn(-1));

                MockedConstruction<BufferedReader> brMock = mockConstruction(BufferedReader.class,
                        (br, context) -> when(br.readLine())
                                .thenAnswer(inv -> mockReader.readLine()))
        ) {
        // DB mocks
            doNothing().when(jdbcTemplate).execute(anyString());
            when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);
            when(jdbcTemplate.batchUpdate(anyString(), any(List.class)))
                    .thenReturn(new int[]{1});
            // ACT
            movieRepository.mergeRatingsFromImdbFile(mockFile);

            // ASSERT
            verify(jdbcTemplate).execute("PRAGMA foreign_keys = ON");
            verify(jdbcTemplate).execute(contains("CREATE TEMP TABLE IF NOT EXISTS tmp_ratings"));
            verify(jdbcTemplate).batchUpdate(
                    eq("INSERT OR REPLACE INTO tmp_ratings(tconst, rating, votes) VALUES (?,?,?)"),
                    any(List.class)
            );
            verify(jdbcTemplate).update(contains("UPDATE movie"));
            verify(jdbcTemplate).execute("DROP TABLE IF EXISTS tmp_ratings");
        }
    }


    //  normalize 
    @Test
    void normalize_test() throws Exception {
        // Arrange
        var method = MovieRepository.class.getDeclaredMethod("normalize", String.class);
        method.setAccessible(true);

        // Act + Assert
        assertNull(method.invoke(null, (Object) null));
        assertNull(method.invoke(null, ""));
        assertNull(method.invoke(null, "\\N"));
        assertEquals("Hello", method.invoke(null, "Hello"));
    }

    //  parseIntOrNull: 
    @Test
    void parseIntOrNull_testt() throws Exception {
        // Arrange
        var method = MovieRepository.class.getDeclaredMethod("parseIntOrNull", String.class);
        method.setAccessible(true);

        // Act + Assert
        assertNull(method.invoke(null, (Object) null));
        assertNull(method.invoke(null, ""));
        assertNull(method.invoke(null, "\\N"));
        assertNull(method.invoke(null, "abc")); // invalid integer
        assertEquals(123, method.invoke(null, "123"));
    }

    //  upsert: header == null 
    @Test
    void upsert_headerNull_returnsZeroWithoutInactiveMarking() throws Exception {
        // Arrange
        File mockFile = mock(File.class);
        BufferedReader mockReader = mock(BufferedReader.class);
        when(mockReader.readLine()).thenReturn(null);

        try (
            MockedConstruction<FileInputStream> fis = mockConstruction(FileInputStream.class);
            MockedConstruction<GZIPInputStream> gzip = mockConstruction(GZIPInputStream.class);
            MockedConstruction<InputStreamReader> isr =
                    mockConstruction(InputStreamReader.class, (o,c)-> when(o.read()).thenReturn(-1));
            MockedConstruction<BufferedReader> br =
                    mockConstruction(BufferedReader.class, (o,c)-> when(o.readLine()).thenAnswer(x-> mockReader.readLine()))
        ) {
            doNothing().when(jdbcTemplate).execute(anyString());

            // Act
            int result = movieRepository.upsertFromImdbFile(mockFile, true, true);

            // Assert
            assertEquals(0, result);
            verify(jdbcTemplate).execute("PRAGMA foreign_keys = ON");
            verify(jdbcTemplate).execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_movie_tconst ON movie(tconst)");
            verify(jdbcTemplate, never()).batchUpdate(anyString(), any(List.class));
            verify(jdbcTemplate, never()).update(contains("UPDATE movie SET is_active"));
            verify(jdbcTemplate, never()).execute("DROP TABLE IF EXISTS tmp_seen");
        }
    }

    //  upsert: skip line because too short 
    @Test
    void upsert_skipsLineTooShort() throws Exception {
        // Arrange
        File mockFile = mock(File.class);
        BufferedReader mockReader = mock(BufferedReader.class);
        when(mockReader.readLine())
                .thenReturn("header")
                .thenReturn("1\t2\t3")
                .thenReturn(null);

        try (
            MockedConstruction<FileInputStream> fis = mockConstruction(FileInputStream.class);
            MockedConstruction<GZIPInputStream> gzip = mockConstruction(GZIPInputStream.class);
            MockedConstruction<InputStreamReader> isr =
                    mockConstruction(InputStreamReader.class, (o,c)-> when(o.read()).thenReturn(-1));
            MockedConstruction<BufferedReader> br =
                    mockConstruction(BufferedReader.class, (o,c)-> when(o.readLine()).thenAnswer(x-> mockReader.readLine()))
        ) {
            doNothing().when(jdbcTemplate).execute(anyString());

            // Act
            int result = movieRepository.upsertFromImdbFile(mockFile, true, false);

            // Assert
            assertEquals(0, result);
            verify(jdbcTemplate, never()).batchUpdate(anyString(), any(List.class));
        }
    }


    //  upsert: onlyMovies = false accepts anything 
    @Test
    void upsert_onlyMoviesFalse_acceptsNonMovieType() throws Exception {
        // Arrange
        File mockFile = mock(File.class);
        BufferedReader mockReader = mock(BufferedReader.class);
        when(mockReader.readLine())
                .thenReturn("header")
                .thenReturn("tt1\tshort\tTitle\tOrig\t\\N\t2020\t\\N\t120\t\\N")
                .thenReturn(null);

        try (
            MockedConstruction<FileInputStream> fis = mockConstruction(FileInputStream.class);
            MockedConstruction<GZIPInputStream> gzip = mockConstruction(GZIPInputStream.class);
            MockedConstruction<InputStreamReader> isr =
                    mockConstruction(InputStreamReader.class, (o,c)-> when(o.read()).thenReturn(-1));
            MockedConstruction<BufferedReader> br =
                    mockConstruction(BufferedReader.class, (o,c)-> when(o.readLine()).thenAnswer(x-> mockReader.readLine()))
        ) {
            doNothing().when(jdbcTemplate).execute(anyString());
            when(jdbcTemplate.batchUpdate(anyString(), any(List.class))).thenReturn(new int[]{1});

            // Act
            int result = movieRepository.upsertFromImdbFile(mockFile, false, false);

            // Assert
            assertEquals(1, result);
            verify(jdbcTemplate).batchUpdate(contains("INSERT INTO movie"), any(List.class));
        }
    }

    //  upsert: tvSeries sets is_series = 1 
    @Test
    void upsert_tvSeries_setsSeriesFlag() throws Exception {
        // Arrange
        File mockFile = mock(File.class);
        BufferedReader mockReader = mock(BufferedReader.class);
        when(mockReader.readLine())
                .thenReturn("header")
                .thenReturn("tt1\ttvSeries\tT\tO\t\\N\t2020\t\\N\t120\t\\N")
                .thenReturn(null);

        ArgumentCaptor<List> cap = ArgumentCaptor.forClass(List.class);

        try (
            MockedConstruction<FileInputStream> fis = mockConstruction(FileInputStream.class);
            MockedConstruction<GZIPInputStream> gzip = mockConstruction(GZIPInputStream.class);
            MockedConstruction<InputStreamReader> isr =
                    mockConstruction(InputStreamReader.class, (o,c)-> when(o.read()).thenReturn(-1));
            MockedConstruction<BufferedReader> br =
                    mockConstruction(BufferedReader.class, (o,c)-> when(o.readLine()).thenAnswer(x-> mockReader.readLine()))
        ) {
            doNothing().when(jdbcTemplate).execute(anyString());
            when(jdbcTemplate.batchUpdate(anyString(), any(List.class))).thenReturn(new int[]{1});

            // Act
            movieRepository.upsertFromImdbFile(mockFile, true, false);

            // Assert
            verify(jdbcTemplate).batchUpdate(contains("INSERT INTO movie"), cap.capture());
            Object[] params = (Object[]) cap.getValue().get(0);
            assertEquals(1, params[5]); // is_series flag
        }
    }

    //  upsert: invalid early year 
    @Test
    void upsert_skipsInvalidEarlyYear() throws Exception {
        // Arrange
        File mockFile = mock(File.class);
        BufferedReader mockReader = mock(BufferedReader.class);
        when(mockReader.readLine())
                .thenReturn("header")
                .thenReturn("tt1\tmovie\tT\tO\t\\N\t1500\t\\N\t120\t\\N") // Invalid year
                .thenReturn(null);

        try (
            MockedConstruction<FileInputStream> fis = mockConstruction(FileInputStream.class);
            MockedConstruction<GZIPInputStream> gzip = mockConstruction(GZIPInputStream.class);
            MockedConstruction<InputStreamReader> isr =
                    mockConstruction(InputStreamReader.class, (o,c)-> when(o.read()).thenReturn(-1));
            MockedConstruction<BufferedReader> br =
                    mockConstruction(BufferedReader.class, (o,c)-> when(o.readLine()).thenAnswer(x-> mockReader.readLine()))
        ) {
            doNothing().when(jdbcTemplate).execute(anyString());

            // Act
            int result = movieRepository.upsertFromImdbFile(mockFile, true, false);

            // Assert
            assertEquals(0, result);
        }
    }

    //  upsert: future year 
    @Test
    void upsert_skipsFutureYear() throws Exception {
        // Arrange
        int futureYear = java.time.Year.now().getValue() + 10;
        File mockFile = mock(File.class);

        BufferedReader mockReader = mock(BufferedReader.class);
        when(mockReader.readLine())
                .thenReturn("header")
                .thenReturn("tt1\tmovie\tT\tO\t\\N\t" + futureYear + "\t\\N\t120\t\\N")
                .thenReturn(null);

        try (
            MockedConstruction<FileInputStream> fis = mockConstruction(FileInputStream.class);
            MockedConstruction<GZIPInputStream> gzip = mockConstruction(GZIPInputStream.class);
            MockedConstruction<InputStreamReader> isr =
                    mockConstruction(InputStreamReader.class, (o,c)-> when(o.read()).thenReturn(-1));
            MockedConstruction<BufferedReader> br =
                    mockConstruction(BufferedReader.class, (o,c)-> when(o.readLine()).thenAnswer(x-> mockReader.readLine()))
        ) {
            doNothing().when(jdbcTemplate).execute(anyString());

            // Act
            int result = movieRepository.upsertFromImdbFile(mockFile, true, false);

            // Assert
            assertEquals(0, result);
        }
    }


    //  mergeRatings: header == null 
    @Test
    void mergeRatings_headerNull_exitsEarly() throws Exception {
        // Arrange
        File mockFile = mock(File.class);
        BufferedReader mockReader = mock(BufferedReader.class);
        when(mockReader.readLine()).thenReturn(null);

        try (
            MockedConstruction<FileInputStream> fis = mockConstruction(FileInputStream.class);
            MockedConstruction<GZIPInputStream> gzip = mockConstruction(GZIPInputStream.class);
            MockedConstruction<InputStreamReader> isr =
                mockConstruction(InputStreamReader.class, (o,c)-> when(o.read()).thenReturn(-1));
            MockedConstruction<BufferedReader> br =
                mockConstruction(BufferedReader.class, (o,c)-> when(o.readLine()).thenAnswer(x-> mockReader.readLine()))
        ) {
            doNothing().when(jdbcTemplate).execute(anyString());

            // Act
            movieRepository.mergeRatingsFromImdbFile(mockFile);

            // Assert
            verify(jdbcTemplate).execute("PRAGMA foreign_keys = ON");
            verify(jdbcTemplate).execute(contains("CREATE TEMP TABLE"));
            verify(jdbcTemplate).update("DELETE FROM tmp_ratings");
            verify(jdbcTemplate, never()).batchUpdate(anyString(), any(List.class));
            verify(jdbcTemplate, never()).update(contains("UPDATE movie"));
            verify(jdbcTemplate, never()).execute("DROP TABLE IF EXISTS tmp_ratings");
        }
    }

    //  mergeRatings: skip line too short 
    @Test
    void mergeRatings_skipsLineTooShort() throws Exception {
        // Arrange
        File mockFile = mock(File.class);
        BufferedReader mockReader = mock(BufferedReader.class);

        when(mockReader.readLine())
                .thenReturn("header")
                .thenReturn("tt1\t8.5") // too short
                .thenReturn(null);

        try (
            MockedConstruction<FileInputStream> fis = mockConstruction(FileInputStream.class);
            MockedConstruction<GZIPInputStream> gzip = mockConstruction(GZIPInputStream.class);
            MockedConstruction<InputStreamReader> isr =
                mockConstruction(InputStreamReader.class, (o,c)-> when(o.read()).thenReturn(-1));
            MockedConstruction<BufferedReader> br =
                mockConstruction(BufferedReader.class, (o,c)-> when(o.readLine()).thenAnswer(x-> mockReader.readLine()))
        ) {
            doNothing().when(jdbcTemplate).execute(anyString());
            when(jdbcTemplate.batchUpdate(anyString(), any(List.class))).thenReturn(new int[]{1});

            // Act
            movieRepository.mergeRatingsFromImdbFile(mockFile);

            // Assert
            verify(jdbcTemplate, never()).batchUpdate(
                    eq("INSERT OR REPLACE INTO tmp_ratings(tconst, rating, votes) VALUES (?,?,?)"),
                    any(List.class)
            );
        }
    }

    //  mergeRatings: votes null 
    @Test
    void mergeRatings_votesNull_accepted() throws Exception {
        // Arrange
        File mockFile = mock(File.class);
        BufferedReader mockReader = mock(BufferedReader.class);

        when(mockReader.readLine())
                .thenReturn("header")
                .thenReturn("tt1\t8.4\t\\N")
                .thenReturn(null);

        try (
            MockedConstruction<FileInputStream> fis = mockConstruction(FileInputStream.class);
            MockedConstruction<GZIPInputStream> gzip = mockConstruction(GZIPInputStream.class);
            MockedConstruction<InputStreamReader> isr =
                mockConstruction(InputStreamReader.class, (o,c)-> when(o.read()).thenReturn(-1));
            MockedConstruction<BufferedReader> br =
                mockConstruction(BufferedReader.class, (o,c)-> when(o.readLine()).thenAnswer(x-> mockReader.readLine()))
        ) {
            doNothing().when(jdbcTemplate).execute(anyString());
            when(jdbcTemplate.batchUpdate(anyString(), any(List.class))).thenReturn(new int[]{1});

            // Act
            movieRepository.mergeRatingsFromImdbFile(mockFile);

            // Assert
            verify(jdbcTemplate).batchUpdate(
                eq("INSERT OR REPLACE INTO tmp_ratings(tconst, rating, votes) VALUES (?,?,?)"),
                any(List.class)
            );
        }
    }

    //  mergeRatings: invalid vote number 
    @Test
    void mergeRatings_invalidVoteNumber_convertsToNull() throws Exception {
        // Arrange
        File mockFile = mock(File.class);
        BufferedReader mockReader = mock(BufferedReader.class);

        when(mockReader.readLine())
                .thenReturn("header")
                .thenReturn("tt1\t8.4\tinvalid")
                .thenReturn(null);

        try (
            MockedConstruction<FileInputStream> fis = mockConstruction(FileInputStream.class);
            MockedConstruction<GZIPInputStream> gzip = mockConstruction(GZIPInputStream.class);
            MockedConstruction<InputStreamReader> isr =
                mockConstruction(InputStreamReader.class, (o,c)-> when(o.read()).thenReturn(-1));
            MockedConstruction<BufferedReader> br =
                mockConstruction(BufferedReader.class, (o,c)-> when(o.readLine()).thenAnswer(x-> mockReader.readLine()))
        ) {
            doNothing().when(jdbcTemplate).execute(anyString());
            when(jdbcTemplate.batchUpdate(anyString(), any(List.class))).thenReturn(new int[]{1});

            // Act
            movieRepository.mergeRatingsFromImdbFile(mockFile);

            // Assert
            verify(jdbcTemplate).batchUpdate(
                eq("INSERT OR REPLACE INTO tmp_ratings(tconst, rating, votes) VALUES (?,?,?)"),
                any(List.class)
            );
        }
    }

    // sanitizeFTS: null input returns empty string
    @Test
    void sanitizeFTS_null_returnsEmpty() throws Exception {
        // Arrange
        var method = MovieRepository.class.getDeclaredMethod("sanitizeFTS", String.class);
        method.setAccessible(true);

        // Act
        String result = (String) method.invoke(movieRepository, (Object) null);

        // Assert
        assertEquals("", result);
    }

    // sanitizeFTS: removes only characters included in the regex
    @Test
    void sanitizeFTS_removesIllegalSymbols() throws Exception {
        var method = MovieRepository.class.getDeclaredMethod("sanitizeFTS", String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(movieRepository, "he!!o@@@");

        assertEquals("he o", result);
    }


    // sanitizeFTS: collapses multiple spaces into a single one
    @Test
    void sanitizeFTS_collapsesSpaces() throws Exception {
        // Arrange
        var method = MovieRepository.class.getDeclaredMethod("sanitizeFTS", String.class);
        method.setAccessible(true);

        // Act
        String result = (String) method.invoke(movieRepository, "hello   world");

        // Assert
        assertEquals("hello world", result);
    }

    @Test
    void sanitizeFTS_onlySymbols_returnsEmpty() throws Exception {
        var method = MovieRepository.class.getDeclaredMethod("sanitizeFTS", String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(movieRepository, "!!!%%%$$$");

        assertEquals("", result); // all removed
    }

    // countMovies: sanitized empty keyword returns 0 and avoids DB access
    @Test
    void countMovies_sanitizedEmpty_returnsZero() {
        // Arrange / Act
        int result = movieRepository.countMovies("!!!%%%");

        // Assert
        assertEquals(0, result);
        verifyNoInteractions(jdbcTemplate);
    }

    // countMovies: IMDb tconst pattern returns 1 when movie found
    @Test
    void countMovies_exactTconst_returnsOne() {
        // Arrange
        MovieRepository spyRepo = spy(new MovieRepository(jdbcTemplate));
        Movie m = new Movie(1L, "tt1234567", "Test", "Test", 2020, 120, true, false, false, null, null);
        doReturn(m).when(spyRepo).findByTconst("tt1234567");

        // Act
        int result = spyRepo.countMovies("tt1234567");

        // Assert
        assertEquals(1, result);
        verifyNoInteractions(jdbcTemplate);
    }

    // countMovies: IMDb tconst pattern returns 0 when movie not found
    @Test
    void countMovies_exactTconst_returnsZeroWhenNotFound() {
        // Arrange
        MovieRepository spyRepo = spy(new MovieRepository(jdbcTemplate));
        doReturn(null).when(spyRepo).findByTconst("tt7654321");

        // Act
        int result = spyRepo.countMovies("tt7654321");

        // Assert
        assertEquals(0, result);
        verifyNoInteractions(jdbcTemplate);
    }

    // searchMovies: IMDb tconst returns direct match and bypasses FTS query
    @Test
    void searchMovies_exactTconst_returnsDirectMovie() {
        // Arrange
        MovieRepository spyRepo = spy(new MovieRepository(jdbcTemplate));
        Movie m = new Movie(1L, "tt1234567", "Test", "Test", 2020, 120, true, false, false, null, null);
        doReturn(m).when(spyRepo).findByTconst("tt1234567");

        // Act
        var result = spyRepo.searchMovies("tt1234567", 1, 10, null, null,
                                        null, null, null, null);

        // Assert
        assertEquals(1, result.size());
        assertEquals("tt1234567", result.get(0).getTconst());
        verifyNoInteractions(jdbcTemplate);
    }

    // searchMovies: sanitized keyword empty and returns empty list without hitting DB
    @Test
    void searchMovies_keywordSanitizesToEmpty_returnsEmptyList() {
        // Act
        var result = movieRepository.searchMovies("!!!###", 1, 10,
                null, null, null, null, null, null);

        // Assert
        assertTrue(result.isEmpty());

        // JdbcTemplate must not be hit
        verifyNoInteractions(jdbcTemplate);
    }




    // searchMovies: sort by year ASC and filter movies + rated=true
    @Test
    void searchMovies_sortYearAsc_withMovieAndRatedFilter_buildsCorrectSql() {
        // Arrange
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any(Object[].class)))
                .thenReturn(List.of());

        // Act
        movieRepository.searchMovies("star!", 2, 5,
                "year", "asc",
                true, false, false, true);

        // Assert
        ArgumentCaptor<String> sqlCap = ArgumentCaptor.forClass(String.class);

        verify(jdbcTemplate).query(sqlCap.capture(),
                any(RowMapper.class),
                any(Object[].class));

        String sql = sqlCap.getValue();
        assertTrue(sql.contains("AND ("));                   // type filter group present
        assertTrue(sql.contains("m.rating IS NOT NULL"));    // rated = true works
        assertTrue(sql.contains("ORDER BY m.year ASC"));     // sorting correct
    }

    // findByTconst: returns null when no row is found
    @Test
    void findByTconst_noResult_returnsNull() {
        // Arrange
        when(jdbcTemplate.queryForObject(
                anyString(),
                any(RowMapper.class),
                eq("tt404")
        )).thenThrow(new EmptyResultDataAccessException(1));

        // Act
        Movie result = movieRepository.findByTconst("tt404");

        // Assert
        assertNull(result);
    }

    // findIdByName: returns corresponding ID
    @Test
    void findIdByName_returnsId() {
        // Arrange
        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Long.class),
                eq("Avatar")
        )).thenReturn(42L);

        // Act
        Long id = movieRepository.findIdByName("Avatar");

        // Assert
        assertEquals(42L, id);
    }

    // upsertFromImdbFile: skips rows where runtime is null/invalid
    @Test
    void upsert_skipsWhenRuntimeInvalid() throws Exception {
        // Arrange
        File mockFile = mock(File.class);
        BufferedReader mockReader = mock(BufferedReader.class);

        when(mockReader.readLine())
                .thenReturn("header")
                .thenReturn("tt1\tmovie\tTitle\tOrig\t\\N\t2020\t\\N\t\\N\t\\N")
                .thenReturn(null);

        try (MockedConstruction<FileInputStream> fis = mockConstruction(FileInputStream.class);
            MockedConstruction<GZIPInputStream> gzip = mockConstruction(GZIPInputStream.class);
            MockedConstruction<InputStreamReader> isr =
                    mockConstruction(InputStreamReader.class, (o,c) -> when(o.read()).thenReturn(-1));
            MockedConstruction<BufferedReader> br =
                    mockConstruction(BufferedReader.class, (o,c) -> when(o.readLine()).thenAnswer(x -> mockReader.readLine()))
        ) {
            doNothing().when(jdbcTemplate).execute(anyString());

            // Act
            int result = movieRepository.upsertFromImdbFile(mockFile, true, false);

            // Assert
            assertEquals(0, result);
            verify(jdbcTemplate, never()).batchUpdate(anyString(), any(List.class));
        }
    }

    // upsertFromImdbFile: when both titles are null, movie_name falls back to tconst
    @Test
    void upsert_titleNull_fallsBackToTconst() throws Exception {
        // Arrange
        File mockFile = mock(File.class);
        BufferedReader mockReader = mock(BufferedReader.class);

        when(mockReader.readLine())
                .thenReturn("header")
                .thenReturn("tt1\tmovie\t\\N\t\\N\t\\N\t2020\t\\N\t120\t\\N")
                .thenReturn(null);

        ArgumentCaptor<List> batchCaptor = ArgumentCaptor.forClass(List.class);

        try (MockedConstruction<FileInputStream> fis = mockConstruction(FileInputStream.class);
            MockedConstruction<GZIPInputStream> gzip = mockConstruction(GZIPInputStream.class);
            MockedConstruction<InputStreamReader> isr =
                    mockConstruction(InputStreamReader.class, (o,c) -> when(o.read()).thenReturn(-1));
            MockedConstruction<BufferedReader> br =
                    mockConstruction(BufferedReader.class, (o,c) -> when(o.readLine()).thenAnswer(x -> mockReader.readLine()))
        ) {
            doNothing().when(jdbcTemplate).execute(anyString());
            when(jdbcTemplate.batchUpdate(anyString(), any(List.class))).thenReturn(new int[]{1});

            // Act
            movieRepository.upsertFromImdbFile(mockFile, true, false);

            // Assert
            verify(jdbcTemplate).batchUpdate(contains("INSERT INTO movie"), batchCaptor.capture());
            Object[] params = (Object[]) batchCaptor.getValue().get(0);
            // upsert params array: [ tconst, movie_name, original_title, startYear, runtime, is_series, is_shorts ]
            assertEquals("tt1", params[1]);
        }
    }

    // upsertFromImdbFile: when 1000 valid rows, triggers batch flush inside loop
    @Test
    void upsert_reachesBatchThreshold_flushesAt1000() throws Exception {
        // Arrange
        File mockFile = mock(File.class);
        BufferedReader mockReader = mock(BufferedReader.class);
        AtomicInteger counter = new AtomicInteger(0);

        when(mockReader.readLine()).thenAnswer(inv -> {
            int c = counter.getAndIncrement();
            if (c == 0) {
                return "header";
            }
            if (c <= 1000) {
                return "tt" + c + "\tmovie\tTitle\tOrig\t\\N\t2020\t\\N\t120\t\\N";
            }
            return null;
        });

        try (MockedConstruction<FileInputStream> fis = mockConstruction(FileInputStream.class);
            MockedConstruction<GZIPInputStream> gzip = mockConstruction(GZIPInputStream.class);
            MockedConstruction<InputStreamReader> isr =
                    mockConstruction(InputStreamReader.class, (o,c) -> when(o.read()).thenReturn(-1));
            MockedConstruction<BufferedReader> br =
                    mockConstruction(BufferedReader.class, (o,c) -> when(o.readLine()).thenAnswer(x -> mockReader.readLine()))
        ) {
            doNothing().when(jdbcTemplate).execute(anyString());
            when(jdbcTemplate.batchUpdate(anyString(), any(List.class)))
                    .thenReturn(new int[]{1000});

            // Act
            int processed = movieRepository.upsertFromImdbFile(mockFile, true, false);

            // Assert
            assertEquals(1000, processed);
            verify(jdbcTemplate, atLeastOnce()).batchUpdate(contains("INSERT INTO movie"), any(List.class));
        }
    }

    // mergeRatingsFromImdbFile: reaches batch threshold at 10_000 rows and flushes inside loop
    @Test
    void mergeRatings_reachesBatchThreshold_flushesAt10000() throws Exception {
        // Arrange
        File mockFile = mock(File.class);
        BufferedReader mockReader = mock(BufferedReader.class);
        AtomicInteger counter = new AtomicInteger(0);

        when(mockReader.readLine()).thenAnswer(inv -> {
            int c = counter.getAndIncrement();
            if (c == 0) {
                return "tconst\taverageRating\tnumVotes";
            }
            if (c <= 10_000) {
                return "tt" + c + "\t8.4\t100";
            }
            return null;
        });

        try (MockedConstruction<FileInputStream> fis = mockConstruction(FileInputStream.class);
            MockedConstruction<GZIPInputStream> gzip = mockConstruction(GZIPInputStream.class);
            MockedConstruction<InputStreamReader> isr =
                    mockConstruction(InputStreamReader.class, (o,c) -> when(o.read()).thenReturn(-1));
            MockedConstruction<BufferedReader> br =
                    mockConstruction(BufferedReader.class, (o,c) -> when(o.readLine()).thenAnswer(x -> mockReader.readLine()))
        ) {
            doNothing().when(jdbcTemplate).execute(anyString());
            when(jdbcTemplate.batchUpdate(anyString(), any(List.class)))
                    .thenReturn(new int[]{10_000});

            // Act
            movieRepository.mergeRatingsFromImdbFile(mockFile);

            // Assert
            verify(jdbcTemplate, atLeastOnce()).batchUpdate(
                    eq("INSERT OR REPLACE INTO tmp_ratings(tconst, rating, votes) VALUES (?,?,?)"),
                    any(List.class)
            );
            verify(jdbcTemplate).update(contains("UPDATE movie"));
            verify(jdbcTemplate).execute("DROP TABLE IF EXISTS tmp_ratings");
        }
    }

}