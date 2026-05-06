package feedback.deckforge.Service.RepoInterfaces;

import feedback.deckforge.Model.Event;

import java.util.List;
import java.util.Optional;

public interface IEventRepository {

    void createEvent(Event event);
    void updateEvent(Event event);
    void deleteEvent(int eventID);
    Optional<Event> findEventByID(int eventID);
    List<Event> findAllEvents();
    List<Integer> findSignedUpUsersByEventID(int eventID);
    void singUp(int eventID, int userID);
    void removeSignUp(int eventID, int userID);

}
