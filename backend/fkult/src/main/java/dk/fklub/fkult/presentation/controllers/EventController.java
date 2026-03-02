package dk.fklub.fkult.presentation.controllers;

import java.time.LocalDateTime;
import java.util.List;

import dk.fklub.fkult.presentation.DTOs.EventRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import dk.fklub.fkult.business.services.EventService;
import dk.fklub.fkult.persistence.entities.Event;


@RestController
@RequestMapping("/api/event")
public class EventController {
    
    // Download copy of EventService to use its functions
    private final EventService service;
    public EventController(EventService service) {
        this.service = service;
    }

    @GetMapping("/all")
    public List<Event> getAllEvents() {
    return service.getAllEvents();
    }

    @GetMapping("/future")
    public List<EventRequest> getFutureEvents(){
        return service.getFutureEventsFromNow();
    }

    // Delete event from id
    @DeleteMapping("/delete/{id}")
    public String deleteEvent(@PathVariable long id) {
        return service.DeleteEvent(id);
    }

    // Upload event to the database
    @PutMapping("/upload/{LocalDate}/{themeId}")
    public String UploadEvent(@PathVariable("LocalDate") LocalDateTime eventDate, @PathVariable(value = "themeId") long themeId) {
        return service.UploadEvent(eventDate, themeId);
    }

    // Upload startup day to the database
    @PutMapping("/upload/{LocalDate}")
    public String UploadEvent(@PathVariable("LocalDate") LocalDateTime eventDate) {
        return service.UploadEvent(eventDate, null);
    }

    //gotta set this up
    @GetMapping("/next")
    public ResponseEntity<EventRequest> getNextEvent() {
        EventRequest next = service.getNextEvent();
        if (next == null) {
            return ResponseEntity.noContent().build(); // 204
        }
        return ResponseEntity.ok(next);
    }

    // Change date of event
    @PutMapping("/changeDate/{id}")
    public ResponseEntity<?> putNewDate(@PathVariable long id,  @RequestBody LocalDateTime date) {
        if (date == null) {
            return ResponseEntity.status(400).body("No body received");
        }
        return service.updateEventDate(id, date);
    }
}