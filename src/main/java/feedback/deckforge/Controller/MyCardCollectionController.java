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
            @RequestParam(required = false, defaultValue = "CATALOG") String searchTarget,
            HttpSession session, Model model) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if(loggedInUser == null) return "redirect:/login";

        // 1. Hent altid brugerens fulde samling først
        Optional<Collection> collectionOptional = collectionService.findCollectionByUserId(loggedInUser.getUserID());

        List<Card> catalogCards;

        if ("COLLECTION".equals(searchTarget)) {
            // --- LOGIK FOR SØGNING I EGEN SAMLING ---

            // Vi henter listen over kort i samlingen der matcher filteret
            List<Card> filteredOwnedCards = cardService.searchCards(name, rarity, color, CollectionType.COLLECTION, loggedInUser.getUserID());

            // Vi filtrerer 'collection' objektet, så det kun indeholder de matchende kort
            collectionOptional.ifPresent(coll -> {
                coll.getCollectionItems().removeIf(item ->
                        filteredOwnedCards.stream().noneMatch(c -> c.getCardID() == item.getCard().getCardID())
                );
                model.addAttribute("collection", coll);
            });

            // Katalog-baren (højre side) skal bare vise alle kort (eller være tom/uændret)
            catalogCards = cardService.searchCards("", "", "", CollectionType.CATALOG, null);

        } else {
            // --- LOGIK FOR SØGNING I KATALOG (Standard) ---

            // Filtrér kataloget (højre side)
            catalogCards = cardService.searchCards(name, rarity, color, CollectionType.CATALOG, null);

            // Vis den fulde samling (venstre side) uden filtrering
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
