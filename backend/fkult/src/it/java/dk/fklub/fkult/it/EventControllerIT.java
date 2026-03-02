package dk.fklub.fkult.it;

import dk.fklub.fkult.business.services.EventService;
import dk.fklub.fkult.persistence.entities.Event;
import dk.fklub.fkult.presentation.DTOs.EventRequest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

//set up test enviornment
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EventControllerIT {

    @Autowired MockMvc mvc;

    // The EventService is mocked to isolate the controller layer.
    // This ensures the tests verify request handling and response mapping
    // without relying on business logic, database access, or external state.
    @MockBean EventService eventService;//mock event service


    @Test
    @Order(1)
    void getAllEvents() throws Exception {
        // Arrange fake events returned by the service
        List<Event> fakeEvents = List.of(
            new Event(1L, LocalDateTime.of(2025, 11, 15, 20, 0), 1L),
            new Event(2L, LocalDateTime.of(2025, 12, 31, 23, 59), 2L)
        );

        // Define service behavior for this test
        when(eventService.getAllEvents()).thenReturn(fakeEvents);

        // Act + Assert
        mvc.perform(get("/api/event/all"))
           .andExpect(status().isOk())
           .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
           .andExpect(jsonPath("$", hasSize(2)))
           .andExpect(jsonPath("$[0].id", is(1)))
           .andExpect(jsonPath("$[0].themeId", is(1)))
           .andExpect(jsonPath("$[1].id", is(2)));
    }

    @Test
    @Order(2)
    void getNextEvent() throws Exception {
        // Arrange: create a fake next event DTO returned by the service
        EventRequest next = new EventRequest(
            10L,
            "Cyber Night",
            "test1",
            LocalDateTime.of(2025, 11, 15, 20, 0),
            List.of("Drink when someone says matrix"),
            List.of("tt0133093")
        );

        // Mock service to return the next event
        when(eventService.getNextEvent()).thenReturn(next);

        // Act & Assert: verify successful response and returned JSON fields
        mvc.perform(get("/api/event/next"))
           .andExpect(status().isOk())
           .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
           .andExpect(jsonPath("$.id", is(10)))
           .andExpect(jsonPath("$.name", is("Cyber Night")))
           .andExpect(jsonPath("$.username", is("test1")));
    }

    @Test
    @Order(3)
    void getNextEventFail() throws Exception {
        // Arrange: simulate case where no upcoming event exists
        when(eventService.getNextEvent()).thenReturn(null);

        // Act & Assert: controller should respond with HTTP 204 No Content
        mvc.perform(get("/api/event/next"))
           .andExpect(status().isNoContent());
    }
}