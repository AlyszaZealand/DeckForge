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

        if (hasSearch) {
            // Vi kalder searchCards med TRADE-typen, så den kun kigger i brugerens egne byttekort
            List<Card> searchResult = cardService.searchCards(cardName, rarity, color, CollectionType.TRADE, loggedInUser.getUserID());

            // Lav en liste over de ID'er der matchede søgningen
            List<Integer> matchedIds = searchResult.stream().map(Card::getCardID).toList();

            // Filtrér byttelistens items, så vi kun beholder dem, hvis kort-ID'et findes i søgeresultatet
            List<TradeCollectionItem> filteredItems = tradeCollection.getTradeCollectionItems().stream()
                    .filter(item -> matchedIds.contains(item.getCard().getCardID()))
                    .toList();

            // Opdater objektet med den filtrerede liste inden det sendes til HTML
            tradeCollection.setTradeCollectionItems(filteredItems);
        }

        // 4. Send data til HTML-siden
        model.addAttribute("tradeCollection", tradeCollection);
        return "CollectionController/myTradeList";
    }

    @PostMapping("/addCardToTradeList")
    public String handleAddCardToTradeCollection(@RequestParam int cardID, HttpSession session, RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        int tradeColID = tradeCollectionService.getTradeCollectionByUserID(loggedInUser.getUserID())
                .orElseThrow(() -> new CollectionNotFoundException("Bytteliste ikke fundet"))
                .getTradeCollectionId();

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