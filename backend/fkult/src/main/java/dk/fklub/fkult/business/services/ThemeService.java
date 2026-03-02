package dk.fklub.fkult.business.services;

import dk.fklub.fkult.persistence.entities.DrinkingRule;
import dk.fklub.fkult.persistence.entities.Movie;
import dk.fklub.fkult.persistence.entities.Theme;
import dk.fklub.fkult.persistence.entities.ThemeMovie;
import dk.fklub.fkult.persistence.repository.*;
import dk.fklub.fkult.presentation.DTOs.ThemeRequest;
import dk.fklub.fkult.presentation.controllers.UserController;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ThemeService {
    private final ThemeRepository themeRepository;
    private final MovieRepository movieRepository;
    private final DrinkingRuleRepository drinkingRuleRepository;
    private final ThemeMovieRepository themeMovieRepository;
    private final UserRepository userRepository;
    private final EventService eventService;

    public ThemeService(ThemeRepository themeRepository, MovieRepository movieRepository, DrinkingRuleRepository drinkingRuleRepository, ThemeMovieRepository themeMovieRepository, UserRepository userRepository, EventService eventService) {
        this.themeRepository = themeRepository;
        this.movieRepository = movieRepository;
        this.drinkingRuleRepository = drinkingRuleRepository;
        this.themeMovieRepository = themeMovieRepository;
        this.userRepository = userRepository;
        this.eventService = eventService;
    }
    public List<ThemeRequest> getAllThemes() {
        List<Theme> themes =  themeRepository.findAll();
        System.out.println("Found themes: " + themes.size());
        //converts model object to DTO object, function defined below.
        return convertThemesToThemeRequests(themes);
    }

    public List<ThemeRequest> getNewThemes() {
        LocalDateTime lastStartUpDate = eventService.getLastStartupDate();
        if (lastStartUpDate == null) {
            return convertThemesToThemeRequests(new ArrayList<>());
        }
        List<Theme> newThemes = themeRepository.findAfter(lastStartUpDate);
        return convertThemesToThemeRequests(newThemes);
    }

    public List<ThemeRequest> getOldThemes() {
        LocalDateTime lastStartUpDate = eventService.getLastStartupDate();
        if (lastStartUpDate == null) {
            return convertThemesToThemeRequests(new ArrayList<>());
        }
        List<Theme> oldThemes = themeRepository.findBefore(lastStartUpDate);
        return convertThemesToThemeRequests(oldThemes);
    }

    public List<ThemeRequest> getUserThemes(String username) {
        List<Theme> userThemes = themeRepository.findFromUser(userRepository.findIdByUsername(username));
        return convertThemesToThemeRequests(userThemes);
    }

    @Transactional
    public void createTheme(ThemeRequest themeRequest){
        //two birds one stone ahh line. assigns themeid AND saves theme to database
        long themeId = themeRepository.save(new Theme(themeRequest.getName(), themeRequest.getUserId())).getId();

        List<ThemeMovie> themeMovies = themeRequest.getMovieIds().stream()
                        .map(movieId -> new ThemeMovie(themeId, movieId)).toList();
        themeMovies.forEach(themeMovie -> themeMovieRepository.save(themeMovie));

        themeRequest.getDrinkingRules().forEach(ruleText ->
                drinkingRuleRepository.save(new DrinkingRule(themeId, ruleText)));
    }
    //Transactional ensures if insertions fail halfway through it is undone
    @Transactional
    public void createThemeWithTConsts(ThemeRequest themeRequest){
        Long userId = userRepository.findUser(themeRequest.getUsername()).getId();
        //double effect. Save a new theme, and store the themeId from the newly saved theme
        long themeId = themeRepository.save(new Theme(themeRequest.getName(), userId)).getId();
        //map list of tconsts to list of movieIds given in the themeRequest
        List<Long> movieIds = themeRequest.gettConsts().stream().map(
                tConst -> movieRepository.findByTconst(tConst).getId()
        ).toList();
        //Create ThemeMovies that link the newly created Theme to given movies
        List<ThemeMovie> themeMovies = movieIds
                .stream()
                .map(movieId -> new ThemeMovie(themeId,movieId))
                .toList();
        //save each thememovie to the repository
        themeMovies.forEach(themeMovie -> themeMovieRepository.save(themeMovie));
        if (!(themeRequest.getDrinkingRules() == null)){
            themeRequest.getDrinkingRules().forEach(ruleText ->
                    drinkingRuleRepository.save(new DrinkingRule(themeId,ruleText)));
        }
    }
    
    private List<ThemeRequest> convertThemesToThemeRequests(List<Theme> themes) {
        List<ThemeRequest> themeRequests =  new ArrayList<>();
        //for-each loops over all themes inputted
        for(Theme theme : themes) {
            //constructing a themeRequest one by one
            String name = theme.getName();
            Long userId = theme.getUserid();
            String username = userRepository.findUserNameById(userId);
            //repository returns a list of thememovies, for each of those we save their movieid to a list.
            List<Long> movieIds = themeMovieRepository.findByThemeId(theme.getId())
                    .stream()
                    .map(ThemeMovie::getMovieid)
                    .toList();
            //We map each movie id to a tConst
            List<String> tconsts = movieIds.stream()
                    .map(id -> {
                        Movie movie = movieRepository.findById(id);
                        //Ternary operator: if movie exists, return its tconst, else return null.
                        return movie != null ? movie.getTconst() : null;
                    })
                    .filter(tconst -> tconst != null) //remove null values where no movie was found
                    .toList();
            //find all drinking rules attached to themeId and convert them to list of strings
            List<String> drinkingRules = drinkingRuleRepository.findByThemeId(theme.getId())
                    .stream()
                    .map(DrinkingRule::getRuleText)
                    .toList();
            //construct DTO from all the values we've just collected
            ThemeRequest themeRequest = new ThemeRequest(theme.getId(),  name, username, userId, movieIds, drinkingRules, theme.getTimestamp());
            themeRequest.settConsts(tconsts);
            //add to list defined at the top.
            themeRequests.add(themeRequest);
        };
        return themeRequests;
    }


    @Transactional
    public void updateThemeWithTConsts(ThemeRequest themeRequest){
        Long themeId = themeRequest.getThemeId();
        System.out.println("Updating themeId = " + themeId + " with request = " + themeRequest);

        // 1) Update core theme
        themeRepository.updateName(themeId, themeRequest.getName());

        // 2) Replace movies
        themeMovieRepository.deleteByThemeId(themeId);

        List<Long> movieIds = new ArrayList<>();
        for (String tConst : themeRequest.gettConsts()) {
            Movie movie = movieRepository.findByTconst(tConst);
            if (movie == null) {
                // Log clearly and fail in a controlled way
                throw new IllegalArgumentException("No movie found in DB for tConst: " + tConst);
            }
            movieIds.add(movie.getId());
        }

        for (Long movieId : movieIds) {
            themeMovieRepository.save(new ThemeMovie(themeId, movieId));
        }

        // 3) Replace drinking rules
        drinkingRuleRepository.deleteByThemeId(themeId);

        if (themeRequest.getDrinkingRules() != null) {
            for (String ruleText : themeRequest.getDrinkingRules()) {
                drinkingRuleRepository.save(new DrinkingRule(themeId, ruleText));
            }
        }
    }


}
