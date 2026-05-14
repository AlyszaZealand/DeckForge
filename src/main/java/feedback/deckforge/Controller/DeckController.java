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

        // Henter alle decks og alle tilgængelige formater (til dropdown-menuen)
        model.addAttribute("deckCollection", deckService.findAllDecksByUserId(loggedInUser.getUserID()));
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
                                  @RequestParam(required = false, defaultValue = "CATALOG") CollectionType collectionType, // Ny parameter!
                                  HttpSession session,
                                  Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/login";

        // 1. Hent decket og tjek ejerskab
        Deck deck = deckService.getDeckForBuilder(deckID, loggedInUser.getUserID());
        model.addAttribute("deck", deck);

        // 2. Bestem om vi skal søge i samling eller katalog
        // Hvis brugeren vælger "COLLECTION", skal vi sende deres bruger-ID med til CardService
        Integer searchUserId = (collectionType == CollectionType.COLLECTION) ? loggedInUser.getUserID() : null;

        // 3. Udfør søgningen
        if (search != null || rarity != null || color != null || collectionType != null) {
            List<Card> searchResults = cardService.searchCards(search, rarity, color, collectionType, searchUserId);
            model.addAttribute("searchResults", searchResults);
        }

        // Gemmer værdierne så formularen "husker" dem (inklusive toggle-knappen)
        model.addAttribute("currentSearch", search);
        model.addAttribute("currentRarity", rarity);
        model.addAttribute("currentColor", color);
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
        // Kald en metode i DeckService, der øger mængden med 1
        deckService.addOneToDeck(deckID, cardID);
        return "redirect:/deckBuilder/" + deckID;
    }

    @PostMapping("/deckBuilder/{deckID}/removeOne")
    public String removeOne(@PathVariable int deckID, @RequestParam int cardID) {
        // Kald en metode i DeckService, der fjerner 1 eller sletter rækken hvis mængden er 1
        deckService.removeOneFromDeck(deckID, cardID);
        return "redirect:/deckBuilder/" + deckID;
    }

    @PostMapping("/deckBuilder/{deckID}/setCommander")
    public String setCommander(@PathVariable int deckID, @RequestParam int cardID) {
        // Kaster jeres Exception hvis kortet ikke er en Creature/Planeswalker
        deckService.setCommander(deckID, cardID);
        return "redirect:/deckBuilder/" + deckID;
    }

    @PostMapping("/deleteDeck/{deckID}")
    public String deleteDeck(@PathVariable int deckID, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        deckService.deleteDeck(deckID);

        // Efter sletning sender vi brugeren tilbage til den opdaterede liste
        return "redirect:/myDecks";
    }


}