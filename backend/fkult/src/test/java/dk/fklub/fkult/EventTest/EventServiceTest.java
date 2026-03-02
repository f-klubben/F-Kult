package dk.fklub.fkult.EventTest;

import dk.fklub.fkult.business.services.EventService;
import dk.fklub.fkult.persistence.entities.Event;
import dk.fklub.fkult.persistence.entities.Theme;
import dk.fklub.fkult.persistence.repository.*;
import dk.fklub.fkult.presentation.DTOs.EventRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class EventServiceTest {

    private EventRepository eventRepository;
    private ThemeRepository themeRepository;
    private UserRepository userRepository;
    private DrinkingRuleRepository drinkingRuleRepository;
    private MovieRepository movieRepository;
    private ThemeMovieRepository themeMovieRepository;

    private EventService service;

    @BeforeEach
    void setUp() {
        eventRepository = mock(EventRepository.class);
        themeRepository = mock(ThemeRepository.class);
        userRepository = mock(UserRepository.class);
        drinkingRuleRepository = mock(DrinkingRuleRepository.class);
        movieRepository = mock(MovieRepository.class);
        themeMovieRepository = mock(ThemeMovieRepository.class);

        service = new EventService(
                eventRepository,
                themeRepository,
                userRepository,
                drinkingRuleRepository,
                movieRepository,
                themeMovieRepository
        );
    }


    // UploadEvent()
    @Test
    void uploadEvent_ReturnsSuccessMessage() {
        // Arrange
        LocalDateTime date = LocalDateTime.now();

        // Act
        String result = service.UploadEvent(date, 5L);

        // Assert
        assertEquals("Event upload complete!", result);
        verify(eventRepository).save(any(String.class), eq(5L));
    }

    @Test
    void uploadEvent_WhenException_ReturnsFailureMessage() {
        // Arrange
        doThrow(new RuntimeException("DB error")).when(eventRepository).save(any(), anyLong());

        // Act
        String result = service.UploadEvent(LocalDateTime.now(), 1L);

        // Assert
        assertTrue(result.contains("Event upload failed"));
    }


    // getAllEvents()
    @Test
    void getAllEvents_ReturnsList() {
        // Arrange
        Event e1 = new Event();
        Event e2 = new Event();
        when(eventRepository.getAll()).thenReturn(List.of(e1, e2));

        // Act
        List<Event> result = service.getAllEvents();

        // Assert
        assertEquals(2, result.size());
        verify(eventRepository).getAll();
    }


    // DeleteEvent()
    @Test
    void deleteEvent_ReturnsSuccessMessage() {
        // Arrange
        doNothing().when(eventRepository).delete(10L);

        // Act
        String result = service.DeleteEvent(10L);

        // Assert
        assertEquals("Event deletion complete!", result);
        verify(eventRepository).delete(10L);
    }

    @Test
    void deleteEvent_WhenException_ReturnsFailureMessage() {
        // Arrange
        doThrow(new RuntimeException("Delete error")).when(eventRepository).delete(50L);

        // Act
        String result = service.DeleteEvent(50L);

        // Assert
        assertTrue(result.contains("Event deletion failed"));
    }


    // getFutureEventsFromNow()
    @Test
    void getFutureEventsFromNow_ReturnsMappedEventRequests() {
        // Arrange: mock event
        LocalDateTime now = LocalDateTime.now();
        Event event = new Event();
        event.setId(1L);
        event.setThemeId(100L);
        event.setEventDate(now.plusDays(3));

        when(eventRepository.getFutureEventsFromTimeStamp(any())).thenReturn(List.of(event));

        // Theme
        Theme theme = new Theme("Halloween", 5L);
        theme.setId(100L);

        when(themeRepository.findById(100L))
                .thenReturn(theme);

        when(userRepository.findUserNameById(5L))
                .thenReturn("Alice");

        when(drinkingRuleRepository.findByThemeId(100L))
                .thenReturn(List.of());

        when(themeMovieRepository.findByThemeId(100L))
                .thenReturn(List.of());

        // Act
        List<EventRequest> result = service.getFutureEventsFromNow();

        // Assert
        assertEquals(1, result.size());
        assertEquals("Halloween", result.get(0).getName());
    }

@Test
void getFutureEventsFromNow_HandlesNullTheme() {
    // Arrange: event with a themeId for which themeRepository returns null
    Event event = new Event();
    event.setId(1L);
    event.setThemeId(999L);
    LocalDateTime time = LocalDateTime.now().plusDays(1);
    event.setEventDate(time);

    when(eventRepository.getFutureEventsFromTimeStamp(any())).thenReturn(List.of(event));

    when(themeRepository.findById(999L)).thenReturn(null);

    // Act
    List<EventRequest> result = service.getFutureEventsFromNow();

    // Assert
    assertEquals(1, result.size());
    assertEquals(1L, result.get(0).getId());
    assertEquals(time, result.get(0).getTimestamp());
}



    // getLastStartupDate()
    @Test
    void getLastStartupDate_ReturnsEventDate() {
        // Arrange
        Event e = new Event();
        LocalDateTime d = LocalDateTime.now();
        e.setEventDate(d);

        when(eventRepository.getLastStartupEvent()).thenReturn(e);

        // Act
        LocalDateTime result = service.getLastStartupDate();

        // Assert
        assertEquals(d, result);
    }


    // getNextEvent()
    @Test
    void getNextEvent_ReturnsFirstFutureEvent() {
        // Arrange
        EventRequest req = new EventRequest(1L, LocalDateTime.now().plusDays(1));

        EventService spyService = Mockito.spy(service);
        doReturn(List.of(req)).when(spyService).getFutureEventsFromNow();

        // Act
        EventRequest result = spyService.getNextEvent();

        // Assert
        assertEquals(req, result);
    }

    @Test
    void getNextEvent_ReturnsNullWhenNoEvents() {
        // Arrange
        EventService spyService = Mockito.spy(service);
        doReturn(List.of()).when(spyService).getFutureEventsFromNow();

        // Act
        EventRequest result = spyService.getNextEvent();

        // Assert
        assertNull(result);
    }

    @Test
    void updateEventDate_UploadComplete(){
        // Arrange
        ResponseEntity<?> expected = ResponseEntity.ok("Event upload complete!");
        LocalDateTime date = LocalDateTime.of(2026, 2, 6, 16, 45);

        // Act
        ResponseEntity<?> result = service.updateEventDate(1, date);

        // Assert
        assertEquals(result, expected);

    }

    @Test
    void updateEventDate_UploadFailed(){
        // Arrange
        LocalDateTime date = LocalDateTime.of(2026, 2, 6, 16, 45);
        doThrow(new RuntimeException("Error changing date")).when(eventRepository).updateEventDate(1, service.formatDate(date));

        // Act
        ResponseEntity<?> result = service.updateEventDate(1, date);
        ResponseEntity<?> expected = ResponseEntity.status(500).body("Event upload failed: Error changing date");

        // Assert
        assertEquals(result, expected);

    }
}
