package feedback.deckforge.Controller;

import feedback.deckforge.Model.Card;
import feedback.deckforge.Model.Deck;
import feedback.deckforge.Model.User;
import feedback.deckforge.Model.Enum.CollectionType;
import feedback.deckforge.Service.CardService;
import feedback.deckforge.Service.DeckService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class DeckController {

    private final DeckService deckService;
    private final CardService cardService;

    public DeckController(DeckService deckService, CardService cardService) {
        this.deckService = deckService;
        this.cardService = cardService;
    }

    // --- SIDE 1: OVERSIGT OVER MINE DECKS ---
    @GetMapping("/myDecks")
    public String showMyDecks(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        // Service.findAllDecksByUserId sørger nu for at hente items,
        // så tælleren (45/60) virker på oversigten.
        List<Deck> deckCollection = deckService.findAllDecksByUserId(loggedInUser.getUserID());
        model.addAttribute("deckCollection", deckCollection);
        model.addAttribute("formats", deckService.getAllFormats());

        return "DeckController/my-decks";
    }

    // Post-metode til at oprette et nyt, tomt deck
    @PostMapping("/createDeck")
    public String handleCreateDeck(@RequestParam String name,
                                   @RequestParam int formatID,
                                   HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        deckService.createNewDeck(name, formatID, loggedInUser.getUserID());
        return "redirect:/myDecks";
    }

    // --- SIDE 2: SELVE DECK BUILDEREN ---
    @GetMapping("/deckBuilder/{deckID}")
    public String showDeckBuilder(@PathVariable int deckID,
                                  @RequestParam(required = false) String search,
                                  @RequestParam(required = false) String rarity,
                                  @RequestParam(required = false) String color,
                                  @RequestParam(required = false) String cardType,
                                  @RequestParam(required = false, defaultValue = "CATALOG") CollectionType collectionType,
                                  HttpSession session,
                                  Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        // 1. Hent decket (Service sørger for Commander, Items og Ejerskabstjek)
        Deck deck = deckService.getDeckForBuilder(deckID, loggedInUser.getUserID());
        model.addAttribute("deck", deck);

        // 2. Beregn total mængde kort til tælleren (X / 100)
        // Dette sikrer at din HTML altid har et korrekt tal uanset Thymeleaf-version
        int totalCardsInDeck = deck.getDeckItems().stream()
                .mapToInt(item -> item.getQuantity())
                .sum();
        model.addAttribute("totalCardsInDeck", totalCardsInDeck);

        // 3. Bestem søgemål (Katalog eller egen samling)
        Integer searchUserId = (collectionType == CollectionType.COLLECTION) ? loggedInUser.getUserID() : null;

        // 4. Udfør søgningen med alle parametre
        // CardType sendes som String - Service/Repo håndterer Enum-konverteringen
        List<Card> searchResults = cardService.searchCards(search, rarity, color, cardType, collectionType, searchUserId);
        model.addAttribute("searchResults", searchResults);

        // 5. Gem værdier til "Persistent UI" (Formularen husker hvad du valgte)
        model.addAttribute("currentSearch", search);
        model.addAttribute("currentRarity", rarity);
        model.addAttribute("currentColor", color);
        model.addAttribute("currentCardType", cardType);
        model.addAttribute("currentCollectionType", collectionType);

        return "DeckController/deck-builder";
    }

    // Tilføjer et valgt kort til decket
    @PostMapping("/deckBuilder/{deckID}/add")
    public String addCardToDeck(@PathVariable int deckID,
                                @RequestParam int cardID,
                                @RequestParam int quantity) {

        deckService.addCardToDeck(deckID, cardID, quantity);
        return "redirect:/deckBuilder/" + deckID;
    }

    @PostMapping("/deckBuilder/{deckID}/addOne")
    public String addOne(@PathVariable int deckID, @RequestParam int cardID) {
        deckService.addOneToDeck(deckID, cardID);
        return "redirect:/deckBuilder/" + deckID;
    }

    @PostMapping("/deckBuilder/{deckID}/removeOne")
    public String removeOne(@PathVariable int deckID, @RequestParam int cardID) {
        deckService.removeOneFromDeck(deckID, cardID);
        return "redirect:/deckBuilder/" + deckID;
    }

    @PostMapping("/deckBuilder/{deckID}/setCommander")
    public String setCommander(@PathVariable int deckID, @RequestParam int cardID) {
        deckService.setCommander(deckID, cardID);
        return "redirect:/deckBuilder/" + deckID;
    }

    @PostMapping("/deleteDeck/{deckID}")
    public String deleteDeck(@PathVariable int deckID, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        deckService.deleteDeck(deckID);
        return "redirect:/myDecks";
    }
}