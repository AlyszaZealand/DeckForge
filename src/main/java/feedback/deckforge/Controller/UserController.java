package feedback.deckforge.Controller;

import feedback.deckforge.Model.Enum.EventStatus;
import feedback.deckforge.Model.Event;
import feedback.deckforge.Model.User;
import feedback.deckforge.Service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class UserController {

    private UserService userService;
    private CollectionService collectionService;
    private TradeCollectionService tradeCollectionService;
    private WishCollectionService wishCollectionService;
    private EventService eventService;

    public UserController(UserService userService, CollectionService collectionService, TradeCollectionService tradeCollectionService, WishCollectionService wishCollectionService, EventService eventService) {
        this.userService = userService;
        this.collectionService = collectionService;
        this.tradeCollectionService = tradeCollectionService;
        this.wishCollectionService = wishCollectionService;
        this.eventService = eventService;
    }

    @GetMapping("/")
    public String showHomePage(Model model){
        // Sørg for at status på events er opdateret inden vi sorterer
        eventService.updateEventStatuses();

        // Hent alle events, og brug stream til at filtrere, sortere og tage top 3
        List<Event> top3UpcomingEvents = eventService.getAllEvents().stream()
                .filter(e -> e.getEventStatus() == EventStatus.PLANNED || e.getEventStatus() == EventStatus.ACTIVE)
                .sorted(Comparator.comparing(Event::getEventDate))
                .limit(3)
                .collect(Collectors.toList());

        model.addAttribute("events", top3UpcomingEvents);

        return "UserController/home";
    }


    @GetMapping("/colletion")
    public String showCollectionPage(HttpSession session, Model model){
        if(session.getAttribute("loggedInUser") == null){
            return "redirect:/login";
        }
        return "CollectionController/colletion-hub";
    }

    @GetMapping("/members")
    public String showMembersPage(@RequestParam(required = false) String searchCard, Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        List<User> membersToDisplay;


        if (searchCard != null && !searchCard.trim().isEmpty()) {
            membersToDisplay = userService.getMembersByTradelistCard(searchCard);
        } else {
            membersToDisplay = userService.getAllMembers();
        }

        membersToDisplay = membersToDisplay.stream()
                .filter(user -> user.getUserID() != loggedInUser.getUserID())
                .collect(Collectors.toList());

        model.addAttribute("members", membersToDisplay);
        model.addAttribute("searchCard", searchCard);

        return "MemberController/members";
    }

    @GetMapping("/profile")
    public String showProfilePage(HttpSession session, Model model){
        User sessionUser = (User) session.getAttribute("loggedInUser");
        if (sessionUser == null){
            return "redirect:/login";
        }

        // opdaterer altid brugeren fra databasen, så vi har den nyeste rolle/beskrivelse
        User loggedInUser = userService.getUserByID(sessionUser.getUserID());
        session.setAttribute("loggedInUser", loggedInUser);
        model.addAttribute("loggedInUser", loggedInUser);

        // 2. Henter specifik data afhængigt af rollen
        if (loggedInUser.getUserRole() == feedback.deckforge.Model.Enum.UserRole.MEMBER) {
            // Henter events som brugeren er tilmeldt
            List<Event> myEvents = eventService.getEventsByAttendeeId(loggedInUser.getUserID());

            model.addAttribute("upcomingEvents", myEvents.stream()
                    .filter(e -> e.getEventStatus().name().equals("PLANNED") || e.getEventStatus().name().equals("ACTIVE"))
                    .collect(Collectors.toList()));
            model.addAttribute("pastEvents", myEvents.stream()
                    .filter(e -> e.getEventStatus().name().equals("COMPLETED") || e.getEventStatus().name().equals("CANCELLED"))
                    .collect(Collectors.toList()));

        } else if (loggedInUser.getUserRole() == feedback.deckforge.Model.Enum.UserRole.ORGANIZER) {
            // Henter events som organizer har oprettet
            List<Event> organizedEvents = eventService.getEventsByOrganizerId(loggedInUser.getUserID());

            model.addAttribute("upcomingEvents", organizedEvents.stream()
                    .filter(e -> e.getEventStatus().name().equals("PLANNED") || e.getEventStatus().name().equals("ACTIVE"))
                    .collect(Collectors.toList()));
            model.addAttribute("pastEvents", organizedEvents.stream()
                    .filter(e -> e.getEventStatus().name().equals("COMPLETED") || e.getEventStatus().name().equals("CANCELLED"))
                    .collect(Collectors.toList()));

        } else if (loggedInUser.getUserRole() == feedback.deckforge.Model.Enum.UserRole.ADMIN) {
            // Henter alle andre brugere til Admin
            List<User> allUsers = userService.getAllUsers().stream()
                    .filter(u -> u.getUserID() != loggedInUser.getUserID())
                    .collect(Collectors.toList());
            model.addAttribute("allUsers", allUsers);
        }

        return "UserController/profile";
    }


    // --- ADMIN ACTIONS ---
    @PostMapping("/admin/promote/{id}")
    public String promoteToOrganizer(@PathVariable int id, HttpSession session) {
        User admin = (User) session.getAttribute("loggedInUser");
        if (admin != null && admin.getUserRole() == feedback.deckforge.Model.Enum.UserRole.ADMIN) {
            userService.changeUserRole(id, feedback.deckforge.Model.Enum.UserRole.ORGANIZER);
        }
        return "redirect:/profile";
    }

    @PostMapping("/admin/demote/{id}")
    public String demoteToMember(@PathVariable int id, HttpSession session) {
        User admin = (User) session.getAttribute("loggedInUser");
        if (admin != null && admin.getUserRole() == feedback.deckforge.Model.Enum.UserRole.ADMIN) {
            userService.changeUserRole(id, feedback.deckforge.Model.Enum.UserRole.MEMBER);
        }
        return "redirect:/profile";
    }

    @PostMapping("/admin/deleteUser/{id}")
    public String deleteUser(@PathVariable int id, HttpSession session) {
        User admin = (User) session.getAttribute("loggedInUser");
        if (admin != null && admin.getUserRole() == feedback.deckforge.Model.Enum.UserRole.ADMIN) {
            // Henter og sletter brugeren
            User userToDelete = userService.getUserByID(id);
            userService.deleteUser(userToDelete);
        }
        return "redirect:/profile";
    }
}