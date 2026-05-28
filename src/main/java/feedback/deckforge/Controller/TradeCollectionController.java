package feedback.deckforge.Controller;

import feedback.deckforge.Model.Card;
import feedback.deckforge.Model.Enum.CollectionType;
import feedback.deckforge.Model.TradeCollection;
import feedback.deckforge.Model.User;
import feedback.deckforge.Service.CardService;
import feedback.deckforge.Service.TradeCollectionService;
import feedback.deckforge.Service.Validation.ValidationResult;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
public class TradeCollectionController {

    private TradeCollectionService tradeCollectionService;
    private CardService cardService;

    public TradeCollectionController(TradeCollectionService tradeCollectionService, CardService cardService) {
        this.tradeCollectionService = tradeCollectionService;
        this.cardService = cardService;
    }

    @GetMapping("/myTradeList")
    public String showTradeCollectionPage(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String rarity,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String type, //
            @RequestParam(required = false, defaultValue = "COLLECTION") String searchTarget,
            HttpSession session, Model model){

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null){
            return "redirect:/login";
        }

        // Hent brugerens Tradelist
        Optional<TradeCollection> tradeCollectionOptional = tradeCollectionService.getTradeCollectionByUserID(loggedInUser.getUserID());

        if ("TRADELIST".equals(searchTarget)) {
            List<Card> filteredTradeCards = cardService.searchCards(name, rarity, color, type, CollectionType.TRADE, loggedInUser.getUserID());

            tradeCollectionOptional.ifPresent(tc -> {
                tc.getTradeCollectionItems().removeIf(item ->
                        filteredTradeCards.stream().noneMatch(c -> c.getCardID() == item.getCard().getCardID())
                );
                model.addAttribute("tradeCollection", tc);
            });

            // Vis hele den private samling til højre
            List<Card> allOwnedCards = cardService.searchCards("", "", "", "", CollectionType.COLLECTION, loggedInUser.getUserID());
            model.addAttribute("myOwnedCards", allOwnedCards);

        } else {
            // --- SØG I EGEN SAMLING
            tradeCollectionOptional.ifPresent(tc -> model.addAttribute("tradeCollection", tc));

            // Filtrer den private samling og send den til HTML
            List<Card> myOwnedCards = cardService.searchCards(name, rarity, color, type, CollectionType.COLLECTION, loggedInUser.getUserID());
            model.addAttribute("myOwnedCards", myOwnedCards);
        }

        return "CollectionController/my-tradelist";
    }

    @PostMapping("/addCardToTradeList")
    public String handleAddCardToTradeCollection(@RequestParam int cardID, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        Optional<TradeCollection> tradeCollectionOptional = tradeCollectionService.getTradeCollectionByUserID(loggedInUser.getUserID());
        int tradeCollectionID = tradeCollectionOptional.get().getTradeCollectionId();

        tradeCollectionService.addCardsToTradeCollection(loggedInUser.getUserID(), cardID,1);
        return "redirect:/myTradeList";
    }

    @PostMapping("/removeCardFromTradeList")
    public String handleRemoveCardFromTradeCollection(@RequestParam int cardID, HttpSession session){
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        Optional<TradeCollection> tradeCollectionOptional = tradeCollectionService.getTradeCollectionByUserID(loggedInUser.getUserID());
        int tradeCollectionID = tradeCollectionOptional.get().getTradeCollectionId();

        tradeCollectionService.removeCardsFromTradeCollection(loggedInUser.getUserID(),cardID,1);

        return "redirect:/myTradeList";
    }

    @PostMapping("/removeOneCardFromTradeList")
    public String handleRemoveOneCard(@RequestParam int cardID, HttpSession session){
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        tradeCollectionService.removeCardsFromTradeCollection(loggedInUser.getUserID(),cardID,1);

        return "redirect:/myTradeList";
    }
}