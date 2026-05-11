package feedback.deckforge.Controller;

import feedback.deckforge.Exceptions.CollectionNotFoundException;
import feedback.deckforge.Model.*;
import feedback.deckforge.Model.Enum.CollectionType;
import feedback.deckforge.Service.CardService;
import feedback.deckforge.Service.CollectionService;
import feedback.deckforge.Service.TradeCollectionService;
import feedback.deckforge.Service.WishCollectionService;
import feedback.deckforge.Service.Validation.ValidationResult;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class CollectionController {

    private final CollectionService collectionService;
    private final TradeCollectionService tradeCollectionService;
    private final WishCollectionService wishCollectionService;
    private final CardService cardService;

    public CollectionController(CollectionService collectionService,
                                TradeCollectionService tradeCollectionService,
                                WishCollectionService wishCollectionService,
                                CardService cardService) { // HUSK CONSTRUCTOR
        this.collectionService = collectionService;
        this.tradeCollectionService = tradeCollectionService;
        this.wishCollectionService = wishCollectionService;
        this.cardService = cardService;
    }

    // --- HUB OG GENEREL VISNING ---

    @GetMapping("/collectionPage")
    public String showCollection(HttpSession session) {
        if (session.getAttribute("loggedInUser") == null) return "redirect:/login";
        return "CollectionController/collection-hub";
    }

    // --- PRIVAT SAMLING (MY CARDS) ---

    @GetMapping("/myCards")
    public String showMyCards(
            @RequestParam(required = false) String cardName,
            @RequestParam(required = false) String rarity,
            @RequestParam(required = false) String color,
            HttpSession session, Model model) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        Collection collection = collectionService.findCollectionByUserId(loggedInUser.getUserID())
                .orElseThrow(() -> new CollectionNotFoundException("Privat samling ikke fundet"));

        // Tjek om brugeren har søgt på noget
        boolean hasSearch = (cardName != null && !cardName.isEmpty()) ||
                (rarity != null && !rarity.isEmpty()) ||
                (color != null && !color.isEmpty());

        if (hasSearch) {
            // 1. Brug jeres CardService metode
            List<Card> searchResult = cardService.searchCards(cardName, rarity, color, CollectionType.COLLECTION, loggedInUser.getUserID());

            // 2. Udtræk ID'er for at gøre filtrering nemt
            List<Integer> matchedIds = searchResult.stream().map(Card::getCardID).toList();

            // 3. Behold KUN de items i samlingen, der findes i søgeresultatet (Så vi bevarer 'quantity'!)
            List<CollectionItem> filteredItems = collection.getCollectionItems().stream()
                    .filter(item -> matchedIds.contains(item.getCard().getCardID()))
                    .toList();

            collection.setCollectionItems(filteredItems);
        }

        model.addAttribute("collection", collection);
        return "CollectionController/my-cards";
    }

    @PostMapping("/addCardToCollection")
    public String handleAddCardToCollection(@RequestParam int cardID, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        int collectionID = collectionService.findCollectionByUserId(loggedInUser.getUserID())
                .orElseThrow(() -> new CollectionNotFoundException("Samling ikke fundet"))
                .getCollectionId();

        collectionService.addCardToCollection(collectionID, cardID, 1);
        return "redirect:/myCards";
    }

    @PostMapping("/removeCardFromCollection")
    public String handleRemoveCardFromCollection(@RequestParam int cardID, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        int collectionID = collectionService.findCollectionByUserId(loggedInUser.getUserID())
                .orElseThrow(() -> new CollectionNotFoundException("Samling ikke fundet"))
                .getCollectionId();

        collectionService.removeCardFromCollection(collectionID, cardID);
        return "redirect:/myCards";
    }

    // --- ØNSKELISTE (WISH LIST) ---

    @GetMapping("/myWishCollection")
    public String showWishCollectionPage(
            @RequestParam(required = false) String cardName,
            @RequestParam(required = false) String rarity,
            @RequestParam(required = false) String color,
            HttpSession session, Model model) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        WishCollection wishCollection = wishCollectionService.getWishCollectionByUserID(loggedInUser.getUserID())
                .orElseThrow(() -> new CollectionNotFoundException("Ønskeliste ikke fundet"));

        boolean hasSearch = (cardName != null && !cardName.isEmpty()) ||
                (rarity != null && !rarity.isEmpty()) ||
                (color != null && !color.isEmpty());

        if (hasSearch) {
            List<Card> searchResult = cardService.searchCards(cardName, rarity, color, CollectionType.WISH, loggedInUser.getUserID());
            List<Integer> matchedIds = searchResult.stream().map(Card::getCardID).toList();

            List<WishCollectionItem> filteredItems = wishCollection.getWishCollectionItems().stream()
                    .filter(item -> matchedIds.contains(item.getCard().getCardID()))
                    .toList();
            wishCollection.setWishCollectionItems(filteredItems);
        }

        model.addAttribute("collection", wishCollection);
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

    // --- BYTTELISTE (TRADE LIST) ---

    @GetMapping("/myTradeList")
    public String showTradeCollectionPage(
            @RequestParam(required = false) String cardName,
            @RequestParam(required = false) String rarity,
            @RequestParam(required = false) String color,
            HttpSession session, Model model) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        TradeCollection tradeCollection = tradeCollectionService.getTradeCollectionByUserID(loggedInUser.getUserID())
                .orElseThrow(() -> new CollectionNotFoundException("Bytteliste ikke fundet"));

        boolean hasSearch = (cardName != null && !cardName.isEmpty()) ||
                (rarity != null && !rarity.isEmpty()) ||
                (color != null && !color.isEmpty());

        if (hasSearch) {
            List<Card> searchResult = cardService.searchCards(cardName, rarity, color, CollectionType.TRADE, loggedInUser.getUserID());
            List<Integer> matchedIds = searchResult.stream().map(Card::getCardID).toList();

            List<TradeCollectionItem> filteredItems = tradeCollection.getTradeCollectionItems().stream()
                    .filter(item -> matchedIds.contains(item.getCard().getCardID()))
                    .toList();
            tradeCollection.setTradeCollectionItems(filteredItems);
        }

        model.addAttribute("tradeCollection", tradeCollection);
        return "CollectionController/my-tradelist";
    }

    @PostMapping("/addCardToTradeList")
    public String handleAddCardToTradeCollection(@RequestParam int cardID, HttpSession session, RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        int tradeColID = tradeCollectionService.getTradeCollectionByUserID(loggedInUser.getUserID())
                .orElseThrow(() -> new CollectionNotFoundException("Bytteliste ikke fundet"))
                .getTradeCollectionId();

        // Her bruger vi valideringen fra servicen (f.eks. ejer du overhovedet kortet?)
        ValidationResult result = tradeCollectionService.addCardToTradeCollection(loggedInUser.getUserID(), tradeColID, cardID, 1);

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", result.getErrors().get(0));
        }

        return "redirect:/myTradeList";
    }

    @PostMapping("/removeCardFromTradeList")
    public String handleRemoveCardFromTradeCollection(@RequestParam int cardID, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        int tradeColID = tradeCollectionService.getTradeCollectionByUserID(loggedInUser.getUserID())
                .orElseThrow(() -> new CollectionNotFoundException("Bytteliste ikke fundet"))
                .getTradeCollectionId();

        tradeCollectionService.removeCardFromTradeCollection(tradeColID, cardID);
        return "redirect:/myTradeList";
    }
}







