package feedback.deckforge.Service;

import feedback.deckforge.Exceptions.EventFullException;
import feedback.deckforge.Exceptions.InvalidEventSizeException;
import feedback.deckforge.Model.Enum.EventStatus;
import feedback.deckforge.Model.Event;
import feedback.deckforge.Model.User;
import feedback.deckforge.Service.RepoInterfaces.IEventRepository;
import feedback.deckforge.Service.RepoInterfaces.IUserRepository;
import feedback.deckforge.Service.Validation.EventValidation;
import feedback.deckforge.Service.Validation.ValidationResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    private final IEventRepository eventRepository;
    private final EventValidation eventValidation;
    private IUserRepository userRepository;

    public EventService(IEventRepository eventRepository, EventValidation eventValidation, IUserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.eventValidation = eventValidation;
        this.userRepository = userRepository;
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAllEvents();
    }

    public Optional<Event> getEventByID(int eventID){
        return eventRepository.findEventByID(eventID);
    }

    public ValidationResult createEvent(Event event){
        ValidationResult result = eventValidation.validateEvent(event);

        if (result.hasErrors()){
            return result;
        }

        eventRepository.createEvent(event);
        return result;
    }

    public void updateEventStatuses() {
        List<Event> allEvents = eventRepository.findAllEvents();
        LocalDateTime now = LocalDateTime.now();

        for (Event event : allEvents) {
            if (event.getEventStatus() == EventStatus.CANCELLED) {
                continue;
            }

            EventStatus currentStatus = event.getEventStatus();
            EventStatus newStatus = currentStatus;

            if (now.isBefore(event.getEventDate())) {
                newStatus = EventStatus.PLANNED;
            } else if ((now.isEqual(event.getEventDate()) || now.isAfter(event.getEventDate()))
                    && now.isBefore(event.getEventEndDate())) {
                newStatus = EventStatus.ACTIVE;
            } else if (now.isEqual(event.getEventEndDate()) || now.isAfter(event.getEventEndDate())) {
                newStatus = EventStatus.COMPLETED;
            }

            if (newStatus != currentStatus) {
                event.setEventStatus(newStatus);
                eventRepository.updateEvent(event);
            }
        }
    }

    public void deleteEvent(int eventID){
        eventRepository.deleteEvent(eventID);
    }

    public ValidationResult updateEvent(Event event){
        int currentAttendees = getSignedUpUsersByEventID(event.getEventID()).size();

        if (event.getEventSize() < currentAttendees) {
            // Kaster fejlen og stopper koden prompte!
            throw new InvalidEventSizeException("Fejl: Du kan ikke sætte max deltagere til " + event.getEventSize() + ", da der allerede er " + currentAttendees + " spillere tilmeldt.");
        }
        ValidationResult result = eventValidation.validateEvent(event);
        if(result.hasErrors()){
            return result;
        }
        eventRepository.updateEvent(event);
        return result;
    }


    public List<User> getSignedUpUsersByEventID(int eventID){
         List<Integer> userIDs = eventRepository.findSignedUpUsersByEventID(eventID);

         List<User> signedUpUsers = new ArrayList<>();

         for (Integer userID : userIDs){
             Optional<User> userOptional = userRepository.findUserByID(userID);

             if(userOptional.isPresent()){
                 signedUpUsers.add(userOptional.get());
             }
         }
         return signedUpUsers;
    }

    public void signUpForEvent(int eventID, int userID) {
        if (isEventFull(eventID)) {
            // Hvis eventet er fuldt, stopper Java koden her og kaster en fejl!
            throw new EventFullException("Der er ikke flere pladser på dette event.");
        }

        // Hvis koden når herned, er der plads, og vi gemmer det i databasen.
        eventRepository.signUp(eventID, userID);
    }
    public void removeSignUpForEvent(int eventID, int userID){
        eventRepository.removeSignUp(eventID, userID);
    }

    private boolean isEventFull(int eventID) {
        Optional<Event> eventOptional = eventRepository.findEventByID(eventID);

        if (eventOptional.isPresent()) {
            Event event = eventOptional.get();
            int currentAttendees = getSignedUpUsersByEventID(eventID).size();
            return currentAttendees >= event.getEventSize();
        }
        return true;
    }

}
