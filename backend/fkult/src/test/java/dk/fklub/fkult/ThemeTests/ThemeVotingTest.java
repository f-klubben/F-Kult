package dk.fklub.fkult.ThemeTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dk.fklub.fkult.business.services.ThemeService;
import dk.fklub.fkult.business.services.ThemeVotingService;
import dk.fklub.fkult.persistence.repository.EventRepository;
import dk.fklub.fkult.persistence.repository.MovieRepository;
import dk.fklub.fkult.persistence.repository.ThemeMovieRepository;
import dk.fklub.fkult.persistence.repository.ThemeRepository;
import dk.fklub.fkult.persistence.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class ThemeVotingTest {

    @Mock private ThemeService themeService;
    @Mock private ThemeRepository themeRepository;
    @Mock private UserRepository userRepository;
    @Mock private MovieRepository movieRepository;
    @Mock private EventRepository eventRepository;
    @Mock private ThemeMovieRepository themeMovieRepository;

    @InjectMocks
    private ThemeVotingService themeVotingService;

    @Test
    @DisplayName("Test UpdateVote with valid input")
    void testUpdateVoteValidInput() {
        // Arrange
        long id = 1;
        long votes = 1;

        // Act
        String response = themeVotingService.UpdateVote(id, votes);

        // Assert
        assertEquals("Set votes for theme " + id + " to: " + votes, response);
    }
}