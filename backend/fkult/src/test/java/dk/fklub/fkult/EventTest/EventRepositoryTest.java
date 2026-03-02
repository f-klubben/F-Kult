package dk.fklub.fkult.EventTest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import com.p3.fkult.persistence.entities.Event;
import com.p3.fkult.persistence.repository.EventRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class EventRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private EventRepository eventRepository;

    @Captor
    private ArgumentCaptor<String> sqlCaptor;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }


    // Tests getAll() executes correct SQL and returns mapped events
    @Test
    void testGetAll() {
        // Arrange
        Event e1 = new Event(1L, LocalDateTime.now(), 5L);
        when(jdbcTemplate.query(anyString(), any(RowMapper.class))).thenReturn(List.of(e1));

        // Act
        List<Event> result = eventRepository.getAll();

        // Assert
        verify(jdbcTemplate).query(sqlCaptor.capture(), any(RowMapper.class));
        assertEquals("SELECT * FROM event", sqlCaptor.getValue());
        assertEquals(1, result.size());
        assertEquals(e1, result.get(0));
    }


    // Tests getFutureEventsFromTimestamp()
    @Test
    void testGetFutureEvents() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        Event event = new Event(1L, now.plusDays(1), 5L);

        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(now))).thenReturn(List.of(event));

        // Act
        List<Event> result = eventRepository.getFutureEventsFromTimeStamp(now);

        // Assert
        assertEquals(1, result.size());
        assertEquals(event, result.get(0));
    }


    // Tests getPastEventsFromTimestamp()
    @Test
    void testGetPastEvents() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        Event event = new Event(1L, now.minusDays(1), 5L);

        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(now))).thenReturn(List.of(event));

        // Act
        List<Event> result = eventRepository.getPastEventsFromTimeStamp(now);

        // Assert
        assertEquals(1, result.size());
        assertEquals(event, result.get(0));
    }


    // Tests save() calls update() with correct values
    @Test
    void testSave() {
        // Arrange
        String date = "2024-06-01 16:30:00";
        long themeId = 10L;

        // Act
        eventRepository.save(date, themeId);

        // Assert
        verify(jdbcTemplate).update(eq("INSERT INTO event (event_date, theme_id) VALUES (?,?)"), eq(date), eq(themeId));
    }


    // Tests delete() calls update() with correct id
    @Test
    void testDelete() {
        // Arrange
        long id = 123L;

        // Act
        eventRepository.delete(id);

        // Assert
        verify(jdbcTemplate).update(eq("DELETE FROM event WHERE id = ?"), eq(id));
    }


    // Tests getLastStartupEvent() returns null when no events
    @Test
    void testGetLastStartupEventReturnsNull() {
        // Arrange
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any())).thenReturn(List.of());

        // Act
        Event result = eventRepository.getLastStartupEvent();

        // Assert
        assertNull(result);
    }


    // Tests getLastStartupEvent() returns the first event when available
    @Test
    void testGetLastStartupEventReturnsEvent() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        Event event = new Event(99L, now.minusDays(1), null);

        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any())).thenReturn(List.of(event));

        // Act
        Event result = eventRepository.getLastStartupEvent();

        // Assert
        assertNotNull(result);
        assertEquals(event, result);
    }


    // Tests that the RowMapper maps all non-null ResultSet values correctly
    @Test
    void testRowMapperMapsValuesCorrectly() throws Exception {
        // Arrange
        ResultSet rs = mock(ResultSet.class);
        LocalDateTime time = LocalDateTime.of(2024, 1, 1, 12, 0);
        Timestamp ts = Timestamp.valueOf(time);

        when(rs.getObject("id")).thenReturn(42L);
        when(rs.getLong("id")).thenReturn(42L);
        when(rs.getObject("theme_id")).thenReturn(100L);
        when(rs.getLong("theme_id")).thenReturn(100L);
        when(rs.getTimestamp("event_date")).thenReturn(ts);

        // Act
        var field = EventRepository.class.getDeclaredField("rowMapper");
        field.setAccessible(true);
        RowMapper<Event> mapper = (RowMapper<Event>) field.get(eventRepository);
        Event event = mapper.mapRow(rs, 0);

        // Assert
        assertEquals(42L, event.getId());
        assertEquals(100L, event.getThemeId());
        assertEquals(time, event.getEventDate());
    }


    // Tests that the RowMapper correctly handles NULL values
    @Test
    void testRowMapperHandlesNullValues() throws Exception {
        // Arrange
        ResultSet rs = mock(ResultSet.class);

        when(rs.getObject("id")).thenReturn(null);
        when(rs.getObject("theme_id")).thenReturn(null);
        when(rs.getTimestamp("event_date")).thenReturn(null);

        // Act
        var field = EventRepository.class.getDeclaredField("rowMapper");
        field.setAccessible(true);
        RowMapper<Event> mapper = (RowMapper<Event>) field.get(eventRepository);
        Event event = mapper.mapRow(rs, 0);

        // Assert
        assertNull(event.getId());
        assertNull(event.getThemeId());
        assertNull(event.getEventDate());
    }


    // Tests that getLastStartupEvent() uses the correct SQL query
    @Test
    void testGetLastStartupEventUsesCorrectSQL() {
        // Arrange
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any())).thenReturn(List.of());

        // Act
        eventRepository.getLastStartupEvent();

        // Assert
        verify(jdbcTemplate).query(sqlCaptor.capture(), any(RowMapper.class), any());
        assertEquals("SELECT * FROM event WHERE theme_id IS NULL AND event_date <= ? ORDER BY event_date DESC LIMIT 1", sqlCaptor.getValue());
    }


// Tests findIdOfStartupDayToday() returns the newest event's ID
@Test
void testFindIdOfStartupDayTodayReturnsId() {
    // Arrange
    Event e = new Event(70L, LocalDateTime.now(), null);
    when(jdbcTemplate.query(anyString(), any(RowMapper.class))).thenReturn(List.of(e));

    // Act
    Long result = eventRepository.findIdOfStartupDayToday();

    // Assert
    assertEquals(70L, result);
}


// Tests that findIdOfStartupDayToday() uses the correct SQL query
@Test
void testFindIdOfStartupDayTodayUsesCorrectSQL() {
    // Arrange
    when(jdbcTemplate.query(anyString(), any(RowMapper.class))).thenReturn(List.of());

    // Act
    eventRepository.findIdOfStartupDayToday();

    // Assert
    verify(jdbcTemplate).query(sqlCaptor.capture(), any(RowMapper.class));
    assertEquals(
        "SELECT * FROM event WHERE theme_id IS NULL ORDER BY event_date DESC LIMIT 1",
        sqlCaptor.getValue()
    );
}


    // Tests findIdOfStartupDayToday() returns null when there are no events
@Test
void testFindIdOfStartupDayTodayReturnsNull() {
    // Arrange
    when(jdbcTemplate.query(anyString(), any(RowMapper.class))).thenReturn(List.of());

    // Act
    Long result = eventRepository.findIdOfStartupDayToday();

    // Assert
    assertNull(result);
}


// Tests updateThemeId() executes the correct SQL and parameters
@Test
void testUpdateThemeId() {
    // Act
    eventRepository.updateThemeId(1L, 50L);

    // Assert
    verify(jdbcTemplate).update(
        eq("UPDATE event SET theme_id = ? WHERE id = ?"),
        eq(50L), eq(1L)
    );
}


// Tests updateEventDate() executes the correct SQL and parameters
@Test
void testUpdateEventDate() {
    // Act
    eventRepository.updateEventDate(123L, "2025-01-01 12:00:00");

    // Assert
    verify(jdbcTemplate).update(
        eq("UPDATE event SET event_date = ? WHERE id = ? "),
        eq("2025-01-01 12:00:00"), eq(123L)
    );
}


}
