package feedback.deckforge.Controller;

import feedback.deckforge.Model.Enum.UserRole;
import feedback.deckforge.Model.Event;
import feedback.deckforge.Model.User;
import feedback.deckforge.Service.EventService;
import feedback.deckforge.Service.FormatService;
import feedback.deckforge.Service.UserService;
import feedback.deckforge.Service.Validation.EventValidation;
import feedback.deckforge.Service.Validation.ValidationResult;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
public class EventController {

    private EventService eventService;
    private EventValidation eventValidation;
    private UserService userService;
    private FormatService formatService;

    public EventController(EventService eventService, EventValidation eventValidation, FormatService formatService) {
        this.eventService = eventService;
        this.eventValidation = eventValidation;
        this.formatService = formatService;
    }

    @GetMapping("/registerEvent")
    public String showEventForm(Model model, HttpSession session){
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null){
            return "redirect:/login";
        }
        if (!loggedInUser.getUserRole().equals(UserRole.ORGANIZER)){
            return "redirect:/";
        }


        model.addAttribute("event", new Event());

        model.addAttribute("formats", formatService.getAllFormats());

        return "EventController/create-event";
    }

    @PostMapping("/registerEvent")
    public String handleEventForm(@ModelAttribute Event newEvent, Model model, HttpSession httpSession){
        if (httpSession.getAttribute("loggedInUser") == null){
            return "redirect:/login";
        }

        ValidationResult result = eventService.createEvent(newEvent);

        if(result.hasErrors()){
            model.addAttribute("errorMessage", result.getErrors());
            model.addAttribute("event", newEvent);
            return "EventController/create-event";
        }

        return "redirect:/";
    }

    @GetMapping("/events/{id}")
    public String showEventDetails(@PathVariable int id, HttpSession httpSession, Model model){
        if (httpSession.getAttribute("loggedInUser") == null){
            return "redirect:/login";
        }

        Optional<Event> eventOptional = eventService.getEventByID(id);
        if (eventOptional.isEmpty()){
            return "redirect:/";
        }

        Event event = eventOptional.get();

        //Get User and Attendees
        User loggedInUser = (User) httpSession.getAttribute("loggedInUser");
        List<User> attendingUsers = eventService.getSignedUpUsersByEventID(id);

        //Check if the logged-in user is already signed up
        boolean isAttending = false;
        for (User user : attendingUsers) {
            if (user.getUserID() == loggedInUser.getUserID()) {
                isAttending = true;
            }
        }

        model.addAttribute("event", event);
        model.addAttribute("attendingUsers", attendingUsers);
        model.addAttribute("isAttending", isAttending);

        return "EventController/event-details";
    }

    @PostMapping("/events/{id}/signup")
    public String signUpUser(@PathVariable("id") int eventId, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        // Securely use the ID from the session, not from the HTML form
        eventService.signUpForEvent(eventId, loggedInUser.getUserID());

        // Redirect back to the event details page so it refreshes the attendee list
        return "redirect:/events/" + eventId;
    }

    @PostMapping("/events/{id}/leave")
    public String removeUserFromSignedUp(@PathVariable("id") int eventId, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        // Securely use the ID from the session
        eventService.removeSignUpForEvent(eventId, loggedInUser.getUserID());

        // Redirect back to the event details page
        return "redirect:/events/" + eventId;
    }






}
