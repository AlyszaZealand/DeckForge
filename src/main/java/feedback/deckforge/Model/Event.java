package feedback.deckforge.Model;

import feedback.deckforge.Model.Enum.EventStatus;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;


public class Event {

    private int eventID;
    private String eventName;
    private String eventFormat;
    private EventStatus eventStatus;
    private int eventSize;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime eventDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime eventEndDate;
    private Integer winnerID;// integer så winner kan være null
    private int organizerID;
    private String eventDescription;


    public Event() {}

    public Event(String eventName, String eventFormat, EventStatus eventStatus, int eventSize, LocalDateTime eventDate, LocalDateTime eventEndDate, Integer winnerID, int organizerID, String eventDescription) {
        this.eventName = eventName;
        this.eventFormat = eventFormat;
        this.eventStatus = eventStatus;
        this.eventSize = eventSize;
        this.eventDate = eventDate;
        this.eventEndDate = eventEndDate;
        this.winnerID = winnerID;
        this.organizerID = organizerID;
        this.eventDescription = eventDescription;
    }

    // Getters og Setters
    public int getEventID() { return eventID; }
    public void setEventID(int eventID) { this.eventID = eventID; }

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

    public String getEventName(){
        return this.eventName;
    }
    public void setEventName(String eventName){
        this.eventName = eventName;
    }

    public LocalDateTime getEventEndDate() {return eventEndDate;}
    public void setEventEndDate(LocalDateTime eventEndDate) {this.eventEndDate = eventEndDate;}

    public Integer getWinnerId() {return winnerID;}
    public void setWinnerId(Integer winnerId) {this.winnerID = winnerId;}

    public int getOrganizerId() {return organizerID;}

    public void setOrganizerId(int organizerId) {this.organizerID = organizerId;}
}

