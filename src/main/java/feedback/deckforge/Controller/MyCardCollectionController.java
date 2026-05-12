package feedback.deckforge.Controller;

import feedback.deckforge.Model.Card;
import feedback.deckforge.Model.Collection;
import feedback.deckforge.Model.User;
import feedback.deckforge.Service.CardService;
import feedback.deckforge.Service.CollectionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
public class MyCardCollectionController {

    private CardService cardService;
    private CollectionService collectionService;

    public MyCardCollectionController(CardService cardService, CollectionService collectionService) {
        this.cardService = cardService;
        this.collectionService = collectionService;
    }


    @GetMapping("/myCards")
    public String showMyCards(HttpSession session, Model model){
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if(loggedInUser == null){
            return "redirect:/login";
        }

        Optional<Collection> collectionOptional = collectionService.findCollectionByUserId(loggedInUser.getUserID());
        collectionOptional.ifPresent(collection -> model.addAttribute("collection", collection));


        List<Card> cardList = cardService.getAllCards();
        model.addAttribute("cardList", cardList);


        return "CollectionController/my-cards";
    }


    @PostMapping("/addExtraCardToCollection")
    public String handleAddExtraCardToCollection(@RequestParam int cardID, HttpSession session){
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if(loggedInUser == null){
            return "redirect:/login";
        }

        Optional<Collection> collectionOpt = collectionService.findCollectionByUserId(loggedInUser.getUserID());
        int collectionID = collectionOpt.get().getCollectionId();
        collectionService.addCardToCollection(collectionID,cardID,1);

        return "redirect:/myCards";
    }

    @PostMapping("/removeCardFromCollection")
    public String handleRemoveCardFromCollection(@RequestParam int cardID, HttpSession session){
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if(loggedInUser == null){
            return "redirect:/login";
        }

        Optional<Collection> collectionOpt = collectionService.findCollectionByUserId(loggedInUser.getUserID());
        int collectionID = collectionOpt.get().getCollectionId();
        collectionService.removeOne(collectionID, cardID);

        return "redirect:/myCards";
    }

    @PostMapping("/addNewCard")
    public String handleAddNewCardToCollection(@RequestParam int cardID, HttpSession session) {

        // 1. Tjek om brugeren er logget ind
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        Optional<Collection> collectionOpt = collectionService.findCollectionByUserId(loggedInUser.getUserID());


        int collectionID = collectionOpt.get().getCollectionId();
        collectionService.addCardToCollection(collectionID, cardID, 1);

        return "redirect:/myCards";
    }

}
