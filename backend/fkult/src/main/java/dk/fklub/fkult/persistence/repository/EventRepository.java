package dk.fklub.fkult.persistence.repository;

import dk.fklub.fkult.persistence.entities.Event;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.List;

@Repository
public class EventRepository {

    // Setup template for database operations
    private final JdbcTemplate jdbcTemplate;
    public EventRepository(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    // Setup RowMapper to map database rows to Event objects
    private final RowMapper<Event> rowMapper = (rs, rowNum) -> {
        Long id = rs.getObject("id") != null ? rs.getLong("id") : null;
        Long themeId = rs.getObject("theme_id") != null ? rs.getLong("theme_id") : null;
        Timestamp timestamp = rs.getTimestamp("event_date");
        LocalDateTime eventDate = toLocalDateTime(timestamp);
        return new Event(id, eventDate, themeId);
    };

    // Get all Events from the database
    public List<Event> getAll(){
        String sql = "SELECT * FROM event";
        return jdbcTemplate.query(sql, rowMapper);
    }
    //event_date with bigger values than ? are newer and therefor selected
    public List<Event> getFutureEventsFromTimeStamp(LocalDateTime localDateTime){
        String sql = "SELECT * FROM event WHERE event_date > ? ORDER BY event_date ASC";
        return jdbcTemplate.query(sql,rowMapper, localDateTime);
    }
    //event_date with smaller values than ? are older and therefor selected
    public List<Event> getPastEventsFromTimeStamp(LocalDateTime localDateTime){
        String sql = "SELECT * FROM event WHERE event_date < ? ORDER BY event_date ASC";
        return jdbcTemplate.query(sql,rowMapper, localDateTime);
    }

    // Save an Event to the database
    public void save(String eventDate, Long themeId){
        String sql = "INSERT INTO event (event_date, theme_id) VALUES (?,?)";
        jdbcTemplate.update(sql, eventDate, themeId);
    }

    // Delete one Event from the database by id
    public void delete(long id){
        String sql = "DELETE FROM event WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public Event getLastStartupEvent(){
        String sql = "SELECT * FROM event WHERE theme_id IS NULL AND event_date <= ? ORDER BY event_date DESC LIMIT 1";
        LocalDateTime today = LocalDateTime.now();
        List<Event> startups = jdbcTemplate.query(sql, rowMapper, today.toString());
        if(startups.isEmpty()){
            return null;
        }
        return startups.get(0);
    }

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return (timestamp != null) ? timestamp.toLocalDateTime() : null;
    }

    // Find the id of the event happening today (within 24 hours of now)
    public Long findIdOfStartupDayToday() {
        String sql = "SELECT * FROM event WHERE theme_id IS NULL ORDER BY event_date DESC LIMIT 1";
        List<Event> events = jdbcTemplate.query(sql, rowMapper);
        if (events.isEmpty()){
            return null;
        }
        Event event = events.get(0);
        
        LocalDateTime eventDate = event.getEventDate();
        LocalDateTime now = LocalDateTime.now();
        
        long hoursBetween = Duration.between(now, eventDate).abs().toHours();

        if (hoursBetween > 24) {
            return null;
        }
        return event.getId();
    }

    // Update the theme_id of an event
    public void updateThemeId(Long eventId, Long themeId) {
        jdbcTemplate.update("UPDATE event SET theme_id = ? WHERE id = ?", themeId, eventId);
    }

    public void updateEventDate(long id, String date){
        jdbcTemplate.update("UPDATE event SET event_date = ? WHERE id = ? ", date, id);
    }
}
