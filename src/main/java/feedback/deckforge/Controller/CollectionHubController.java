package feedback.deckforge.Controller;

import feedback.deckforge.Model.*;
import feedback.deckforge.Service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
public class CollectionHubController {

    private CollectionService collectionService;
    private CardService cardService;

    public CollectionHubController(CollectionService collectionService, CardService cardService) {
        this.collectionService = collectionService;
        this.cardService = cardService;
    }


    @GetMapping("/collectionPage")
    public String showCollection(HttpSession session, Model model){
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        if(loggedInUser == null){
            return "redirect:/login";
        }

        collectionService.findCollectionByUserId(loggedInUser.getUserID());

        return"CollectionController/collection-hub";
    }

















}
