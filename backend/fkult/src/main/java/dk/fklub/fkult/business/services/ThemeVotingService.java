package dk.fklub.fkult.business.services;

import java.util.*;

import dk.fklub.fkult.presentation.DTOs.ThemeRequest;
import org.springframework.stereotype.Service;
import dk.fklub.fkult.presentation.DTOs.ThemeVotingRequest;
import dk.fklub.fkult.persistence.repository.ThemeRepository;
import dk.fklub.fkult.persistence.repository.UserRepository;
import dk.fklub.fkult.persistence.repository.MovieRepository;
import dk.fklub.fkult.persistence.repository.EventRepository;
import dk.fklub.fkult.persistence.repository.ThemeMovieRepository;
import dk.fklub.fkult.persistence.entities.ThemeMovie;

@Service
public class ThemeVotingService {

    // Download copy of different files to utilize their functions
    private final ThemeRepository themeRepository;
    private final ThemeService themeService;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final EventRepository eventRepository;
    private final ThemeMovieRepository themeMovieRepository;

    public ThemeVotingService(ThemeService themeService, ThemeRepository themeRepository, UserRepository userRepository, MovieRepository movieRepository, EventRepository eventRepository, ThemeMovieRepository themeMovieRepository) {
        this.themeService = themeService;
        this.themeRepository = themeRepository;
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
        this.eventRepository = eventRepository;
        this.themeMovieRepository = themeMovieRepository;
    }

    // Get shuffled list of themes that avoid repeated user ids
    public List<ThemeVotingRequest> getShuffledThemes() {

        // Get default theme info and setup theme data array
        List<ThemeRequest> themeInfo = themeService.getAllThemes();
        List<ThemeVotingRequest> themeData = new ArrayList<>();

        // Run a for-loop to input all data themeData needs
        for (int i = 0; i < themeInfo.size(); i++){

            // Setup new ThemeVotingRequest object
            ThemeVotingRequest singularTheme = new ThemeVotingRequest();

            // Set the data we already have from themeInfo
            singularTheme.setThemeId(themeInfo.get(i).getThemeId());
            singularTheme.setThemeName(themeInfo.get(i).getName());
            singularTheme.setDrinkingRules(themeInfo.get(i).getDrinkingRules());

            // Set the votes for the theme
            singularTheme.setVotes(themeRepository.findVotesById(themeInfo.get(i).getThemeId()));

            // Set the name of the submitter
            singularTheme.setSubmitterName(userRepository.findUserNameById(themeInfo.get(i).getUserId()));

            // Set movie names, posters, ratings, and runtimes
            List<String> movieNames = new ArrayList<>();
            List<String> moviePosters = new ArrayList<>();
            List<Double> ratings = new ArrayList<>();
            List<Long> runTime = new ArrayList<>();
            List<Long> movieIds = new ArrayList<>();

            for (int j = 0; j < themeInfo.get(i).getMovieIds().size(); j++){

                Long movieId = themeInfo.get(i).getMovieIds().get(j);

                movieIds.add(movieId);
                movieNames.add(movieRepository.findNameById(movieId));
                moviePosters.add(movieRepository.findPosterById(movieId));
                ratings.add(movieRepository.findRatingById(movieId));
                runTime.add(movieRepository.findRunTimeById(movieId));
            }
            singularTheme.setMovieNames(movieNames);
            singularTheme.setMoviePosters(moviePosters);
            singularTheme.setRatings(ratings);
            singularTheme.setRunTimes(runTime);
            singularTheme.setMovieIds(movieIds);

            // Add the singularTheme to the themeData list
            themeData.add(singularTheme);
        }

        // Remove themes from themeData where votes != null
        themeData.removeIf(theme -> theme.getVotes() != null);

        // Shuffle the list of theme data
        Collections.shuffle(themeData);

        // Ensure there arent 2 sequantial themes from same user
        for (int i = 0; i < themeData.size() - 1; i++) {
            ThemeVotingRequest current = themeData.get(i);
            ThemeVotingRequest next = themeData.get(i + 1);

            // If the next one has the same userId, try to swap with another further ahead
            if (current.getSubmitterName().equals(next.getSubmitterName())) {
                for (int j = i + 2; j < themeData.size(); j++) {
                    if (!themeData.get(j).getSubmitterName().equals(current.getSubmitterName())) {
                        // Swap next with j
                        Collections.swap(themeData, i + 1, j);
                        break;
                    }
                }
            }
        }

        // Return the now shuffled list of theme data
        return themeData;
    }
    
    // Updates the votes of a theme based on the id
    public String UpdateVote(long id, long votes) {
        try{
            themeRepository.setVote(id, votes);
            return "Set votes for theme " + id + " to: " + votes;
        } catch (Exception error) {
            return "failed to update votes for id " + id + " due to error: " + error;
        }
    }

    // Deletes a theme based on the id
    public String DeleteTheme(long id) {
        try{
            themeRepository.delete(id);
            return "Deleted theme with id: " + id;
        } catch (Exception error) {
            return "failed to delete theme for id " + id + " due to error: " + error;
        }
    }

    // Add the wheel of fortune winner to the current startup event
    public String AddWheelWinner(Long winningThemeId, Long movieId) {
        try{
            Long eventId = eventRepository.findIdOfStartupDayToday();
            if (eventId == null) {
                return "Could not find an event happening today to add the wheel winner to.";
            }

            // Edit the theme to only have the winning movie
            themeMovieRepository.deleteByThemeId(winningThemeId);
            themeMovieRepository.save(new ThemeMovie(winningThemeId, movieId));

            // Update the event to have the winning theme
            eventRepository.updateThemeId(eventId, winningThemeId);

            return "Added wheel winner with movie ID: " + movieId + " to todays startup day!";
        } catch (Exception error) {
            return "failed to add wheel winner with movie ID: " + movieId + " due to error: " + error;
        }
    }
}