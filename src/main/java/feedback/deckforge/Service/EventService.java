package feedback.deckforge.Service;

import feedback.deckforge.Model.Event;
import feedback.deckforge.Model.User;
import feedback.deckforge.Service.RepoInterfaces.IEventRepository;
import feedback.deckforge.Service.RepoInterfaces.IUserRepository;
import feedback.deckforge.Service.Validation.EventValidation;
import feedback.deckforge.Service.Validation.ValidationResult;
import org.springframework.stereotype.Service;

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

    public void deleteEvent(int eventID){
        eventRepository.deleteEvent(eventID);
    }

    public ValidationResult updateEvent(Event event){
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

    public void signUpForEvent(int eventID, int userID){
        eventRepository.singUp(eventID, userID);
    }
    public void removeSignUpForEvent(int eventID, int userID){
        eventRepository.removeSignUp(eventID, userID);
    }


}
