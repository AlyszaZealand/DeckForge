package feedback.deckforge.Controller;

import feedback.deckforge.Model.Event;
import feedback.deckforge.Model.User;
import feedback.deckforge.Service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

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
        // Hent alle events via din service og send dem til HTML'en
        List<Event> events = eventService.getAllEvents();
        model.addAttribute("events", events);

        return "UserController/home";
    }

    @GetMapping("/profile")
    public String showProfilePage(HttpSession session, Model model){

        User loggedInUser = (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null){
            return "redirect:/login";
        }

        model.addAttribute("loggedInUser", loggedInUser);
        return "UserController/profile";
    }

    @GetMapping("/colletion")
    public String showCollectionPage(HttpSession session, Model model){
        if(session.getAttribute("loggedInUser") == null){
            return "redirect:/login";
        }
        return "CollectionController/colletion-hub";
    }



}
