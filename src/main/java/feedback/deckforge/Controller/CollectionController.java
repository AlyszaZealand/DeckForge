package feedback.deckforge.Controller;

import feedback.deckforge.Exceptions.CollectionNotFoundException;
import feedback.deckforge.Model.*;
import feedback.deckforge.Model.Enum.CollectionType;
import feedback.deckforge.Service.CardService;
import feedback.deckforge.Service.CollectionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class CollectionController {

    private final CollectionService collectionService;
    private final CardService cardService;

    public CollectionController(CollectionService collectionService, CardService cardService) {
        this.collectionService = collectionService;
        this.cardService = cardService;
    }

    @GetMapping("/collectionPage")
    public String showCollection(HttpSession session) {
        if (session.getAttribute("loggedInUser") == null) return "redirect:/login";
        return "CollectionController/collection-hub";
    }

    @GetMapping("/myCards")
    public String showMyCards(
            @RequestParam(required = false) String cardName,
            @RequestParam(required = false) String rarity,
            @RequestParam(required = false) String color,
            @RequestParam(defaultValue = "false") boolean searchCatalog, // Skifter mellem katalog/samling
            HttpSession session, Model model) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";
        model.addAttribute("searchCatalog", searchCatalog);

        boolean hasSearch = (cardName != null && !cardName.isEmpty()) || (rarity != null && !rarity.isEmpty()) || (color != null && !color.isEmpty());

        if (searchCatalog && hasSearch) {
            // Søg i det globale katalog
            List<Card> catalogResults = cardService.searchCards(cardName, rarity, color, CollectionType.CATALOG, null);
            model.addAttribute("catalogResults", catalogResults);
        } else {
            // Vis eller filtrér i brugerens egen samling
            Collection collection = collectionService.findCollectionByUserId(loggedInUser.getUserID())
                    .orElseThrow(() -> new CollectionNotFoundException("Samling ikke fundet"));
            if (hasSearch) {
                List<Card> searchResult = cardService.searchCards(cardName, rarity, color, CollectionType.COLLECTION, loggedInUser.getUserID());
                List<Integer> matchedIds = searchResult.stream().map(Card::getCardID).toList();
                collection.setCollectionItems(collection.getCollectionItems().stream()
                        .filter(item -> matchedIds.contains(item.getCard().getCardID())).toList());
            }
            model.addAttribute("collection", collection);
        }
        return "CollectionController/my-cards";
    }

    @PostMapping("/addCardToCollection")
    public String handleAddCardToCollection(@RequestParam int cardID, @RequestParam(defaultValue = "false") boolean searchCatalog, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        int collectionID = collectionService.findCollectionByUserId(loggedInUser.getUserID()).get().getCollectionId();
        collectionService.addCardToCollection(collectionID, cardID, 1);

        // Sender dig tilbage til den fane, du var på!
        return "redirect:/myCards?searchCatalog=" + searchCatalog;
    }

    @PostMapping("/removeCardFromCollection")
    public String handleRemoveCardFromCollection(@RequestParam int cardID, @RequestParam(defaultValue = "false") boolean searchCatalog, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        int collectionID = collectionService.findCollectionByUserId(loggedInUser.getUserID()).get().getCollectionId();
        collectionService.removeCardFromCollection(collectionID, cardID);

        // Sender dig tilbage til den fane, du var på!
        return "redirect:/myCards?searchCatalog=" + searchCatalog;
    }

    @PostMapping("/decreaseCardQuantity")
    public String handleDecreaseCardQuantity(@RequestParam int cardID, @RequestParam(defaultValue = "false") boolean searchCatalog, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        int collectionID = collectionService.findCollectionByUserId(loggedInUser.getUserID()).get().getCollectionId();
        collectionService.decreaseCardQuantity(collectionID, cardID);

        return "redirect:/myCards?searchCatalog=" + searchCatalog;
    }
}







