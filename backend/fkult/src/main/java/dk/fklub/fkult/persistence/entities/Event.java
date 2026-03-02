package dk.fklub.fkult.persistence.entities;

import java.time.LocalDateTime;

public class Event {
    private Long id;
    private LocalDateTime eventDate;
    private Long themeId;

    public Event() {}

    public Event(Long id, LocalDateTime eventDate, Long themeId) {
        this.id = id;
        this.eventDate = eventDate;
        this.themeId = themeId;
    }

    // Get functions
    public Long getId() {
        return this.id;
    }
    public LocalDateTime getEventDate() {
        return this.eventDate;
    }
    public Long getThemeId() {
        return this.themeId;
    }

    // Set functions
    public void setId(Long id) {
        this.id = id;
    }
    public void setEventDate(LocalDateTime eventDate) {
        this.eventDate = eventDate;
    }
    public void setThemeId(Long themeId) {
        this.themeId = themeId;
    }
}
