package feedback.deckforge.Controller;

import feedback.deckforge.Exceptions.CollectionNotFoundException;
import feedback.deckforge.Model.*;
import feedback.deckforge.Model.Enum.CollectionType;
import feedback.deckforge.Service.CardService;
import feedback.deckforge.Service.TradeCollectionService;
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
public class TradeCollectionController {

    private final TradeCollectionService tradeCollectionService;
    private final CardService cardService;

    public TradeCollectionController(TradeCollectionService tradeCollectionService, CardService cardService) {
        this.tradeCollectionService = tradeCollectionService;
        this.cardService = cardService;
    }

    @GetMapping("/myTradeList")
    public String showTradeCollectionPage(
            @RequestParam(required = false) String cardName,
            @RequestParam(required = false) String rarity,
            @RequestParam(required = false) String color,
            HttpSession session, Model model) {

        // 1. Sikkerhedstjek: Er brugeren logget ind?
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        // 2. Hent brugerens bytteliste fra servicen
        TradeCollection tradeCollection = tradeCollectionService.getTradeCollectionByUserID(loggedInUser.getUserID())
                .orElseThrow(() -> new CollectionNotFoundException("Bytteliste ikke fundet for denne bruger"));

        // 3. Tjek om der er indtastet noget i filteret
        boolean hasSearch = (cardName != null && !cardName.trim().isEmpty()) ||
                (rarity != null && !rarity.trim().isEmpty()) ||
                (color != null && !color.trim().isEmpty());

        if ((cardName != null && !cardName.isEmpty()) || rarity != null || color != null) {
            // Vi bruger CardService til at søge specifikt i denne brugers samling
            List<Card> ownedResults = cardService.searchCards(cardName, rarity, color, CollectionType.COLLECTION, loggedInUser.getUserID());
            model.addAttribute("ownedResults", ownedResults);
        }

        return "CollectionController/my-tradelist";
    }

    @PostMapping("/addCardToTradeList")
    public String handleAddCardToTradeCollection(
            @RequestParam int cardID,
            @RequestParam(required = false) String cardName,
            @RequestParam(required = false) String rarity,
            @RequestParam(required = false) String color,
            HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        int tradeColID = tradeCollectionService.getTradeCollectionByUserID(user.getUserID()).get().getTradeCollectionId();
        tradeCollectionService.addCardToTradeCollection(user.getUserID(), tradeColID, cardID, 1);

        // Sender filter-parametre med tilbage i URL'en
        return String.format("redirect:/myTradeList?cardName=%s&rarity=%s&color=%s",
                cardName != null ? cardName : "", rarity != null ? rarity : "", color != null ? color : "");
    }

    @PostMapping("/decreaseTradeCardQuantity")
    public String handleDecreaseTradeCardQuantity(
            @RequestParam int cardID,
            @RequestParam(required = false) String cardName,
            @RequestParam(required = false) String rarity,
            @RequestParam(required = false) String color,
            HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        int tradeColID = tradeCollectionService.getTradeCollectionByUserID(user.getUserID()).get().getTradeCollectionId();
        tradeCollectionService.decreaseCardQuantity(tradeColID, cardID);

        return String.format("redirect:/myTradeList?cardName=%s&rarity=%s&color=%s",
                cardName != null ? cardName : "", rarity != null ? rarity : "", color != null ? color : "");
    }

    @PostMapping("/removeCardFromTradeList")
    public String handleRemoveCardFromTradeCollection(
            @RequestParam int cardID,
            @RequestParam(required = false) String cardName,
            @RequestParam(required = false) String rarity,
            @RequestParam(required = false) String color,
            HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        int tradeColID = tradeCollectionService.getTradeCollectionByUserID(user.getUserID()).get().getTradeCollectionId();
        tradeCollectionService.removeCardFromTradeCollection(tradeColID, cardID);

        return String.format("redirect:/myTradeList?cardName=%s&rarity=%s&color=%s",
                cardName != null ? cardName : "", rarity != null ? rarity : "", color != null ? color : "");
    }
}