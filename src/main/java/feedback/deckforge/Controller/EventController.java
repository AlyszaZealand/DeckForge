package feedback.deckforge.Controller;

import feedback.deckforge.Exceptions.CollectionNotFoundException;
import feedback.deckforge.Exceptions.EventNotFoundException;
import feedback.deckforge.Model.Event;
import feedback.deckforge.Model.User;
import feedback.deckforge.Service.EventService;
import feedback.deckforge.Service.Validation.ValidationResult;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    // --- VISNING AF ALLE EVENTS ---

    @GetMapping("/events")
    public String showAllEvents(HttpSession session, Model model) {
        if (session.getAttribute("loggedInUser") == null) return "redirect:/login";

        List<Event> events = eventService.getAllEvents();
        model.addAttribute("events", events);
        return "EventController/events";
    }

    // --- OPRETTELSE AF EVENT ---

    @GetMapping("/registerEvent")
    public String showEventForm(Model model, HttpSession session) {
        if (session.getAttribute("loggedInUser") == null) return "redirect:/login";

        model.addAttribute("event", new Event());
        return "EventController/create-event";
    }

    @PostMapping("/registerEvent")
    public String handleEventForm(@ModelAttribute Event newEvent, Model model, HttpSession session) {
        if (session.getAttribute("loggedInUser") == null) return "redirect:/login";

        ValidationResult result = eventService.createEvent(newEvent);

        if (result.hasErrors()) {
            model.addAttribute("errors", result.getErrors());
            model.addAttribute("event", newEvent);
            return "EventController/create-event";
        }

        return "redirect:/events";
    }

    // --- EVENT DETALJER OG DELTAGELSE ---

    @GetMapping("/events/{id}")
    public String showEventDetails(@PathVariable int id, HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        // Brug orElseThrow for at aktivere GlobalExceptionHandler hvis eventet ikke findes
        Event event = eventService.getEventByID(id)
                .orElseThrow(() -> new EventNotFoundException("Eventet med ID " + id + " blev ikke fundet."));

        List<User> attendingUsers = eventService.getSignedUpUsersByEventID(id);

        // Tjek om den loggede bruger er på deltagerlisten med et for-loop
        boolean isAttending = false;
        for (User user : attendingUsers) {
            if (user.getUserID() == loggedInUser.getUserID()) {
                isAttending = true;
                break; // Vi har fundet brugeren, så vi behøver ikke lede videre i listen!
            }
        }

        model.addAttribute("event", event);
        model.addAttribute("attendingUsers", attendingUsers);
        model.addAttribute("isAttending", isAttending);

        return "EventController/event-details";
    }

    @PostMapping("/events/{id}/join")
    public String joinEvent(@PathVariable int id, HttpSession session, RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        // Tilmeld brugeren via servicen
        eventService.signUpForEvent(id, loggedInUser.getUserID());

        return "redirect:/events/" + id;
    }

    @PostMapping("/events/{id}/leave")
    public String leaveEvent(@PathVariable int id, HttpSession session, RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        // Afmeld brugeren via servicen
        eventService.removeSignUpForEvent(id, loggedInUser.getUserID());

        redirectAttributes.addFlashAttribute("successMessage", "Du er nu afmeldt eventet.");
        return "redirect:/events/" + id;
    }
}
