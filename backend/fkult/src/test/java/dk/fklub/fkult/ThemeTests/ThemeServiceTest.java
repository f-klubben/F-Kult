package dk.fklub.fkult.ThemeTests;

import dk.fklub.fkult.business.services.EventService;
import dk.fklub.fkult.business.services.ThemeService;
import dk.fklub.fkult.persistence.entities.DrinkingRule;
import dk.fklub.fkult.persistence.entities.Movie;
import dk.fklub.fkult.persistence.entities.Theme;
import dk.fklub.fkult.persistence.entities.ThemeMovie;
import dk.fklub.fkult.persistence.repository.*;
import dk.fklub.fkult.presentation.DTOs.ThemeRequest;
import dk.fklub.fkult.presentation.controllers.UserController;
import dk.fklub.fkult.persistence.entities.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ThemeServiceTest {

    private ThemeRepository themeRepository;
    private MovieRepository movieRepository;
    private DrinkingRuleRepository drinkingRuleRepository;
    private ThemeMovieRepository themeMovieRepository;
    private UserRepository userRepository;
    private EventService eventService;

    private ThemeService service;

    @BeforeEach
    void setUp() {
        themeRepository = mock(ThemeRepository.class);
        movieRepository = mock(MovieRepository.class);
        drinkingRuleRepository = mock(DrinkingRuleRepository.class);
        themeMovieRepository = mock(ThemeMovieRepository.class);
        userRepository = mock(UserRepository.class);
        eventService = mock(EventService.class);

        service = new ThemeService(
                themeRepository,
                movieRepository,
                drinkingRuleRepository,
                themeMovieRepository,
                userRepository,
                eventService
        );
    }


    // getAllThemes()
    @Test
    void getAllThemes_ReturnsMappedThemeRequests() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();

        Theme t1 = new Theme(1L, "Theme A", 10L, now, 0);
        Theme t2 = new Theme(2L, "Theme B", 20L, now, 0);

        when(themeRepository.findAll()).thenReturn(List.of(t1, t2));

        when(userRepository.findUserNameById(10L)).thenReturn("Alice");
        when(userRepository.findUserNameById(20L)).thenReturn("Jens");

        when(themeMovieRepository.findByThemeId(1L)).thenReturn(List.of());
        when(themeMovieRepository.findByThemeId(2L)).thenReturn(List.of());

        when(drinkingRuleRepository.findByThemeId(1L)).thenReturn(List.of());
        when(drinkingRuleRepository.findByThemeId(2L)).thenReturn(List.of());

        // Act
        List<ThemeRequest> result = service.getAllThemes();

        // Assert
        assertEquals(2, result.size());
        assertEquals("Theme A", result.get(0).getName());
        assertEquals("Alice", result.get(0).getUsername());
        assertEquals("Theme B", result.get(1).getName());
        assertEquals("Jens", result.get(1).getUsername());

        verify(themeRepository).findAll();
    }


    // getNewThemes()
    @Test
    void getNewThemes_ReturnsFilteredNewThemes() {
        // Arrange
        LocalDateTime startup = LocalDateTime.now().minusDays(1);
        when(eventService.getLastStartupDate()).thenReturn(startup);

        Theme t = new Theme(5L, "New Theme", 10L, LocalDateTime.now(), 0);

        when(themeRepository.findAfter(startup)).thenReturn(List.of(t));
        when(userRepository.findUserNameById(10L)).thenReturn("Alice");
        when(themeMovieRepository.findByThemeId(5L)).thenReturn(List.of());
        when(drinkingRuleRepository.findByThemeId(5L)).thenReturn(List.of());

        // Act
        List<ThemeRequest> result = service.getNewThemes();

        // Assert
        assertEquals(1, result.size());
        assertEquals("New Theme", result.get(0).getName());
    }

    @Test
    void getNewThemes_WhenNoStartupEvent_ReturnsEmptyList() {
        when(eventService.getLastStartupDate()).thenReturn(null);

        List<ThemeRequest> result = service.getNewThemes();

        assertTrue(result.isEmpty());
    }


    // getUserThemes()
    @Test
    void getUserThemes_ReturnsMappedThemes() {
        // Arrange
        when(userRepository.findIdByUsername("alice")).thenReturn(10L);

        Theme t = new Theme(1L, "Alice Theme", 10L, LocalDateTime.now(), 0);

        when(themeRepository.findFromUser(10L)).thenReturn(List.of(t));

        when(userRepository.findUserNameById(10L)).thenReturn("alice");
        when(themeMovieRepository.findByThemeId(1L)).thenReturn(List.of());
        when(drinkingRuleRepository.findByThemeId(1L)).thenReturn(List.of());

        // Act
        List<ThemeRequest> result = service.getUserThemes("alice");

        // Assert
        assertEquals(1, result.size());
        assertEquals("Alice Theme", result.get(0).getName());
    }


    // createTheme()
    @Test
    void createTheme_SavesThemeMoviesAndRules() {
        // Arrange
        List<Long> movieIds = List.of(100L, 200L);
        List<String> rules = List.of("Rule 1", "Rule 2");

        ThemeRequest req =
                new ThemeRequest(null, "Party", "test", 50L, movieIds, rules, null);

        Theme saved = new Theme("Party", 50L);
        saved.setId(99L);

        when(themeRepository.save(any())).thenReturn(saved);

        // Act
        service.createTheme(req);

        // Assert
        verify(themeRepository).save(any(Theme.class));
        verify(themeMovieRepository, times(2)).save(any(ThemeMovie.class));
        verify(drinkingRuleRepository, times(2)).save(any(DrinkingRule.class));
    }


    // getOldThemes()
    @Test
    void getOldThemes_ReturnsFilteredOldThemes() {
        LocalDateTime startup = LocalDateTime.now().minusDays(5);
        when(eventService.getLastStartupDate()).thenReturn(startup);

        Theme t = new Theme(3L, "Old Theme", 40L, LocalDateTime.now().minusDays(10), 0);

        when(themeRepository.findBefore(startup)).thenReturn(List.of(t));
        when(userRepository.findUserNameById(40L)).thenReturn("Charlie");
        when(themeMovieRepository.findByThemeId(3L)).thenReturn(List.of());
        when(drinkingRuleRepository.findByThemeId(3L)).thenReturn(List.of());

        List<ThemeRequest> result = service.getOldThemes();

        assertEquals(1, result.size());
        assertEquals("Old Theme", result.get(0).getName());
    }

    @Test
    void getOldThemes_WhenNoStartupEvent_ReturnsEmptyList() {
        when(eventService.getLastStartupDate()).thenReturn(null);

        List<ThemeRequest> result = service.getOldThemes();

        assertTrue(result.isEmpty());
    }


    // createThemeWithTConsts()
    @Test
    void createThemeWithTConsts_SavesThemeMoviesAndRules() {
        // Arrange
        ThemeRequest req =
                new ThemeRequest(1L, "Horror Night", "alice", 10L,
                        List.of(), List.of("Sip every scream"), LocalDateTime.now());
        req.settConsts(List.of("tt001", "tt002"));

        User fakeUser = mock(User.class);
        when(fakeUser.getId()).thenReturn(10L);
        when(userRepository.findUser("alice")).thenReturn(fakeUser);

        Theme saved = new Theme("Horror Night", 10L);
        saved.setId(55L);
        when(themeRepository.save(any())).thenReturn(saved);

        Movie m1 = mock(Movie.class);
        when(m1.getId()).thenReturn(100L);
        when(m1.getTconst()).thenReturn("tt001");

        Movie m2 = mock(Movie.class);
        when(m2.getId()).thenReturn(200L);
        when(m2.getTconst()).thenReturn("tt002");

        when(movieRepository.findByTconst("tt001")).thenReturn(m1);
        when(movieRepository.findByTconst("tt002")).thenReturn(m2);

        // Act
        service.createThemeWithTConsts(req);

        // Assert
        verify(themeRepository).save(any(Theme.class));
        verify(themeMovieRepository, times(2)).save(any(ThemeMovie.class));
        verify(drinkingRuleRepository, times(1)).save(any(DrinkingRule.class));
    }


    // updateThemeWithTConsts()
    @Test
    void updateThemeWithTConsts_UpdatesCorrectly() {
        ThemeRequest req =
                new ThemeRequest(5L, "Updated Theme", "alice", 10L,
                        List.of(), List.of("Updated Rule"), LocalDateTime.now());
        req.settConsts(List.of("tt100"));

        Movie movie = mock(Movie.class);
        when(movie.getId()).thenReturn(77L);
        when(movie.getTconst()).thenReturn("tt100");

        when(movieRepository.findByTconst("tt100")).thenReturn(movie);

        service.updateThemeWithTConsts(req);

        verify(themeRepository).updateName(5L, "Updated Theme");
        verify(themeMovieRepository).deleteByThemeId(5L);
        verify(themeMovieRepository).save(any(ThemeMovie.class));

        verify(drinkingRuleRepository).deleteByThemeId(5L);
        verify(drinkingRuleRepository).save(any(DrinkingRule.class));
    }


    // updateThemeWithTConsts(): missing movie throws
    @Test
    void updateThemeWithTConsts_WhenMovieMissing_ThrowsException() {
        ThemeRequest req =
                new ThemeRequest(9L, "ThemeX", "Jens", 20L,
                        List.of(), List.of("Rule X"), LocalDateTime.now());
        req.settConsts(List.of("ttMissing"));

        when(movieRepository.findByTconst("ttMissing")).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> service.updateThemeWithTConsts(req));
    }
}
