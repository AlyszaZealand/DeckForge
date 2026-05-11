package feedback.deckforge.Controller;

import feedback.deckforge.Exceptions.CollectionNotFoundException;
import feedback.deckforge.Model.*;
import feedback.deckforge.Model.Enum.CollectionType;
import feedback.deckforge.Service.CardService;
import feedback.deckforge.Service.WishCollectionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class WishCollectionController {

    private final WishCollectionService wishCollectionService;
    private final CardService cardService;

    public WishCollectionController(WishCollectionService wishCollectionService, CardService cardService) {
        this.wishCollectionService = wishCollectionService;
        this.cardService = cardService;
    }

    @GetMapping("/myWishCollection")
    public String showWishCollectionPage(
            @RequestParam(required = false) String cardName,
            @RequestParam(required = false) String rarity,
            @RequestParam(required = false) String color,
            HttpSession session, Model model) {

        // 1. Sikkerhedstjek
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        // 2. Tjek om brugeren prøver at finde et nyt kort i kataloget
        boolean hasSearch = (cardName != null && !cardName.trim().isEmpty()) ||
                (rarity != null && !rarity.trim().isEmpty()) ||
                (color != null && !color.trim().isEmpty());

        if (hasSearch) {
            // Her bruger vi CATALOG-typen, så vi søger i alle eksisterende kort i systemet
            List<Card> catalogResults = cardService.searchCards(cardName, rarity, color, CollectionType.CATALOG, null);
            model.addAttribute("catalogResults", catalogResults);
        }

        // 3. Hent altid den nuværende ønskeliste, så brugeren kan se sine eksisterende ønsker
        WishCollection wishCollection = wishCollectionService.getWishCollectionByUserID(loggedInUser.getUserID())
                .orElseThrow(() -> new CollectionNotFoundException("Ønskeliste ikke fundet"));

        // 4. Send data til HTML
        model.addAttribute("wishCollection", wishCollection);
        return "CollectionController/my-wishlists";
    }

    @PostMapping("/addCardToWishList")
    public String handleAddCardToWishCollection(@RequestParam int cardID, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        int wishColID = wishCollectionService.getWishCollectionByUserID(loggedInUser.getUserID())
                .orElseThrow(() -> new CollectionNotFoundException("Ønskeliste ikke fundet"))
                .getWishCollectionId();

        wishCollectionService.addCardToWishCollection(wishColID, cardID);
        return "redirect:/myWishCollection";
    }

    @PostMapping("/removeCardFromWishList")
    public String handleRemoveCardFromWishCollection(@RequestParam int cardID, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        int wishColID = wishCollectionService.getWishCollectionByUserID(loggedInUser.getUserID())
                .orElseThrow(() -> new CollectionNotFoundException("Ønskeliste ikke fundet"))
                .getWishCollectionId();

        wishCollectionService.removeCardFromWishCollection(wishColID, cardID);
        return "redirect:/myWishCollection";
    }
}