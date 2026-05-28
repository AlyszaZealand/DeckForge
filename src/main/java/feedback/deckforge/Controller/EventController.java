package feedback.deckforge.Controller;

import feedback.deckforge.Exceptions.EventFullException;
import feedback.deckforge.Exceptions.InvalidEventSizeException;
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

import java.util.ArrayList;
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
        User loggedInUser = (User) httpSession.getAttribute("loggedInUser");
        if (loggedInUser == null){
            return "redirect:/login";
        }

        newEvent.setOrganizerId(loggedInUser.getUserID());

        // Tvinger altid nye events til at være PLANNED fra start!
        newEvent.setEventStatus(feedback.deckforge.Model.Enum.EventStatus.PLANNED);

        try {
            ValidationResult result = eventService.createEvent(newEvent);

            if(result.hasErrors()){
                model.addAttribute("errorMessage", result.getErrors());
                model.addAttribute("event", newEvent);
                model.addAttribute("formats", formatService.getAllFormats());
                return "EventController/create-event";
            }
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("event", newEvent);
            model.addAttribute("formats", formatService.getAllFormats());
            return "EventController/create-event";
        }

        return "redirect:/events";
    }

    @GetMapping("/events/{id}")
    public String showEventDetails(@PathVariable int id, HttpSession httpSession, Model model){
        if (httpSession.getAttribute("loggedInUser") == null){
            return "redirect:/login";
        }

        eventService.updateEventStatuses();

        Optional<Event> eventOptional = eventService.getEventByID(id);
        if (eventOptional.isEmpty()){
            return "redirect:/";
        }

        Event event = eventOptional.get();

        //hent user og deltagere
        User loggedInUser = (User) httpSession.getAttribute("loggedInUser");
        List<User> attendingUsers = eventService.getSignedUpUsersByEventID(id);

        // tjek om bruger allerede er oprettet
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
    public String signUpUser(@PathVariable("id") int eventID, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        // Sikrer at kun MEMBERs kan tilmelde sig som spillere
        if (!loggedInUser.getUserRole().equals(UserRole.MEMBER)) {
            return "redirect:/events/" + eventID + "?error=only_members";
        }

        try {
            eventService.signUpForEvent(eventID, loggedInUser.getUserID());
        } catch (EventFullException e) {
            // Hvis Exception kastes, sendes brugeren tilbage med en fejl-besked i URL'en
            return "redirect:/events/" + eventID + "?error=event_full";
        }

        return "redirect:/events/" + eventID;
    }

    @PostMapping("/events/{id}/leave")
    public String removeUserFromSignedUp(@PathVariable("id") int eventId, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        eventService.removeSignUpForEvent(eventId, loggedInUser.getUserID());

        return "redirect:/events/" + eventId;
    }

    @GetMapping("/events/{id}/edit")
    public String showEditEventForm(@PathVariable int id, HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        // Tjek at brugeren er logget ind OG er en ORGANIZER
        if (loggedInUser == null || !loggedInUser.getUserRole().equals(UserRole.ORGANIZER)) {
            return "redirect:/login";
        }

        Optional<Event> eventOptional = eventService.getEventByID(id);
        if (eventOptional.isEmpty()) {
            return "redirect:/";
        }

        model.addAttribute("event", eventOptional.get());
        model.addAttribute("formats", formatService.getAllFormats());

        // Vi sender deltagerlisten med, så Organizeren kan vælge en vinder blandt de tilmeldte
        model.addAttribute("attendingUsers", eventService.getSignedUpUsersByEventID(id));

        return "EventController/edit-event";
    }


    @PostMapping("/events/{id}/edit")
    public String handleEditEvent(@PathVariable int id, @ModelAttribute Event updatedEvent, HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.getUserRole().equals(UserRole.ORGANIZER)) {
            return "redirect:/login";
        }

        updatedEvent.setEventID(id);

        try {
            ValidationResult result = eventService.updateEvent(updatedEvent);

            if(result.hasErrors()){
                model.addAttribute("errorMessage", result.getErrors());
                model.addAttribute("event", updatedEvent);
                model.addAttribute("formats", formatService.getAllFormats());
                model.addAttribute("attendingUsers", eventService.getSignedUpUsersByEventID(id));
                return "EventController/edit-event";
            }

        } catch (InvalidEventSizeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("event", updatedEvent);
            model.addAttribute("formats", formatService.getAllFormats());
            model.addAttribute("attendingUsers", eventService.getSignedUpUsersByEventID(id));
            return "EventController/edit-event";
        }

        return "redirect:/events/" + id;
    }

    @GetMapping("/events")
    public String showAllEvents(@RequestParam(defaultValue = "future") String filter, Model model, HttpSession session) {
        // 1. Opdater alle statusser efter uret
        eventService.updateEventStatuses();

        // 2. Hent den loggede bruger, så vi kan tjekke ID
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        List<Event> allEvents = eventService.getAllEvents();
        List<Event> displayEvents = new ArrayList<>();

        for (Event event : allEvents) {
            boolean isPast = (event.getEventStatus() == feedback.deckforge.Model.Enum.EventStatus.COMPLETED ||
                    event.getEventStatus() == feedback.deckforge.Model.Enum.EventStatus.CANCELLED);

            // Filter: Gamle events
            if (filter.equals("past") && isPast) {
                displayEvents.add(event);
            }
            // Filter: Kommende/aktive events
            else if (filter.equals("future") && !isPast) {
                displayEvents.add(event);
            }
            // NYT: Filter til arrangørens egne events (viser både nye og gamle de har oprettet)
            else if (filter.equals("mine") && loggedInUser != null && event.getOrganizerId() == loggedInUser.getUserID()) {
                displayEvents.add(event);
            }
        }

        model.addAttribute("events", displayEvents);
        return "EventController/events";
    }






}
