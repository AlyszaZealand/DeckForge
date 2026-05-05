package feedback.deckforge.Model;

import feedback.deckforge.Model.Enum.EventStatus;

import java.time.LocalDateTime;

public class Event {

    private int eventId;
    private String eventFormat;
    private EventStatus eventStatus;
    private int eventSize;
    private LocalDateTime eventDate;
    private String eventDescription;

    public Event() {}

    public Event(String eventFormat, EventStatus eventStatus, int eventSize, LocalDateTime eventDate, String eventDescription) {
        this.eventFormat = eventFormat;
        this.eventStatus = eventStatus;
        this.eventSize = eventSize;
        this.eventDate = eventDate;
        this.eventDescription = eventDescription;
    }

    // Getters og Setters
    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }

    public String getEventFormat() { return eventFormat; }
    public void setEventFormat(String eventFormat) { this.eventFormat = eventFormat; }

    public EventStatus getEventStatus() { return eventStatus; }
    public void setEventStatus(EventStatus eventStatus) { this.eventStatus = eventStatus; }

    public int getEventSize() { return eventSize; }
    public void setEventSize(int eventSize) { this.eventSize = eventSize; }

    public LocalDateTime getEventDate() { return eventDate; }
    public void setEventDate(LocalDateTime eventDate) { this.eventDate = eventDate; }

    public String getEventDescription() { return eventDescription; }
    public void setEventDescription(String eventDescription) { this.eventDescription = eventDescription; }
}

