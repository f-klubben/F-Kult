package dk.fklub.fkult.business.services;

import dk.fklub.fkult.persistence.entities.Theme;
import dk.fklub.fkult.persistence.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import dk.fklub.fkult.persistence.entities.Event;
import dk.fklub.fkult.presentation.DTOs.EventRequest;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class EventService {

    // Download a copy of repository to run its code
    private final EventRepository eventRepository;
    private final ThemeRepository themeRepository;
    private final UserRepository userRepository;
    private final DrinkingRuleRepository drinkingRuleRepository;
    private final MovieRepository movieRepository;
    private final ThemeMovieRepository themeMovieRepository;

    public EventService(EventRepository eventRepository, ThemeRepository themeRepository, UserRepository userRepository, DrinkingRuleRepository drinkingRuleRepository, MovieRepository movieRepository, ThemeMovieRepository themeMovieRepository) {
        this.eventRepository = eventRepository;
        this.themeRepository = themeRepository;
        this.userRepository = userRepository;
        this.drinkingRuleRepository = drinkingRuleRepository;
        this.movieRepository = movieRepository;
        this.themeMovieRepository = themeMovieRepository;
    }

    public String formatDate(LocalDateTime date){
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(date);
    }

    // Handles event uploads
    public String UploadEvent(LocalDateTime eventDate, Long themeId) {
        try {
            
            ZoneId systemZone = ZoneId.systemDefault();
            ZonedDateTime utcTime = eventDate.atZone(ZoneOffset.UTC);
            LocalDateTime localDateTime = utcTime.withZoneSameInstant(systemZone).toLocalDateTime();
            String formattedDate = formatDate(localDateTime);

            eventRepository.save(formattedDate, themeId);
            return "Event upload complete!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Event upload failed: " + e.getMessage();
        }
    }

    public List<Event> getAllEvents() {
        return eventRepository.getAll();
    }
    // Handles event deletion
    public String DeleteEvent(long id) {
        try {
            eventRepository.delete(id);
            return "Event deletion complete!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Event deletion failed: " + e.getMessage();
        }
    }
    //constructing an EventRequest requires collecting a lot of information from different repositories
    public List<EventRequest> getFutureEventsFromNow(){
        LocalDateTime now = LocalDateTime.now();
        List<Event> futureEvents = eventRepository.getFutureEventsFromTimeStamp(now);
        List<Theme> eventThemes = futureEvents.stream().map(event -> themeRepository.findById(event.getThemeId())).toList();
        List<EventRequest> eventRequests = new ArrayList<>();
        for(int i = 0; i < futureEvents.size(); i++){//for loop, loops over all future events
            if (eventThemes.get(i)== null){ //if the theme for an event is null, its a startup day so create a startup day object
                eventRequests.add(new EventRequest(
                        futureEvents.get(i).getId(),
                        futureEvents.get(i).getEventDate()
                ));
            }else{//if the theme for an event isn't null, its a movie night so create a movie night event object
                eventRequests.add(new EventRequest(
                        futureEvents.get(i).getId(),
                        //theme name for event
                        eventThemes.get(i).getName(),
                        //username who created theme for the event
                        userRepository.findUserNameById(eventThemes.get(i).getUserid()),
                        //event date and start time
                        futureEvents.get(i).getEventDate(),
                        //Drinking Rule from theme
                        drinkingRuleRepository.findByThemeId(eventThemes.get(i).getId()).stream().map(drinkingRule -> drinkingRule.getRuleText()).toList(),
                        //movie tConsts for theme, the reason it is so long is We map ThemeMovies -> MovieIds -> Movies -> tConsts
                        themeMovieRepository.findByThemeId(eventThemes.get(i).getId()).stream().map(themeMovie -> themeMovie.getMovieid()).map(movieId -> movieRepository.findById(movieId)).map(movie -> movie.getTconst()).toList()
                ));
            }
        }
        return eventRequests;
    }

    public LocalDateTime getLastStartupDate(){
        Event event = eventRepository.getLastStartupEvent();
        return event.getEventDate();
        // NOT SURE HERE
    }

    //function to get the next event
    public EventRequest getNextEvent() {
        List<EventRequest> futureEvents = getFutureEventsFromNow(); //get list of future events
        if (futureEvents == null || futureEvents.isEmpty()) { //if future event are null, then no next event
            return null;  // no upcoming events
        }

        //get index 0 of future event which is next event
        return futureEvents.get(0);
    }

    public ResponseEntity<?> updateEventDate(long id, LocalDateTime date) {
        try {
            eventRepository.updateEventDate(id, formatDate(date));
            return ResponseEntity.ok("Event upload complete!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Event upload failed: " + e.getMessage());
        }
    }
}