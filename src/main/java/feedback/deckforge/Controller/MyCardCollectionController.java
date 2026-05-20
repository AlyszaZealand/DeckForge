package feedback.deckforge.Controller;

import feedback.deckforge.Model.Card;
import feedback.deckforge.Model.Collection;
import feedback.deckforge.Model.Enum.CollectionType;
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
    public String showMyCards(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String rarity,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String type, // NY PARAMETER TILFØJET HER
            @RequestParam(required = false, defaultValue = "CATALOG") String searchTarget,
            HttpSession session, Model model) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if(loggedInUser == null) return "redirect:/login";

        Optional<Collection> collectionOptional = collectionService.findCollectionByUserId(loggedInUser.getUserID());

        List<Card> catalogCards;

        if ("COLLECTION".equals(searchTarget)) {
            // HUSK AT OPDATERE CardService og Repository til at modtage 'type' parameteren
            List<Card> filteredOwnedCards = cardService.searchCards(name, rarity, color, type, CollectionType.COLLECTION, loggedInUser.getUserID());

            collectionOptional.ifPresent(coll -> {
                coll.getCollectionItems().removeIf(item ->
                        filteredOwnedCards.stream().noneMatch(c -> c.getCardID() == item.getCard().getCardID())
                );
                model.addAttribute("collection", coll);
            });

            // Kataloget vises blankt/ufiltreret når der søges i samling
            catalogCards = cardService.searchCards("", "", "", "", CollectionType.CATALOG, null);

        } else {
            // Filtrér kataloget inklusiv 'type'
            catalogCards = cardService.searchCards(name, rarity, color, type, CollectionType.CATALOG, null);

            collectionOptional.ifPresent(coll -> model.addAttribute("collection", coll));
        }

        model.addAttribute("cardList", catalogCards);
        return "CollectionController/my-cards";
    }

    @PostMapping("/addExtraCardToCollection")
    public String handleAddExtraCardToCollection(@RequestParam int cardID, HttpSession session){
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        Optional<Collection> collectionOpt = collectionService.findCollectionByUserId(loggedInUser.getUserID());
        int collectionID = collectionOpt.get().getCollectionId();
        collectionService.addCards(collectionID,cardID,1);
        return "redirect:/myCards";
    }

    @PostMapping("/removeCardFromCollection")
    public String handleRemoveCardFromCollection(@RequestParam int cardID, HttpSession session){
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if(loggedInUser == null) return "redirect:/login";

        Optional<Collection> collectionOpt = collectionService.findCollectionByUserId(loggedInUser.getUserID());
        int collectionID = collectionOpt.get().getCollectionId();
        collectionService.removeCards(loggedInUser.getUserID(),cardID,1);
        return "redirect:/myCards";
    }

    @PostMapping("/addNewCard")
    public String handleAddNewCardToCollection(@RequestParam int cardID, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        Optional<Collection> collectionOpt = collectionService.findCollectionByUserId(loggedInUser.getUserID());
        int collectionID = collectionOpt.get().getCollectionId();
        collectionService.addCards(loggedInUser.getUserID(),cardID,1);
        return "redirect:/myCards";
    }
}