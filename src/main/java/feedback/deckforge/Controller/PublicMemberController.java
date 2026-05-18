package feedback.deckforge.Controller;

import feedback.deckforge.Model.Deck;
import feedback.deckforge.Model.User;
import feedback.deckforge.Service.DeckService;
import feedback.deckforge.Service.TradeCollectionService;
import feedback.deckforge.Service.UserService;
import feedback.deckforge.Service.WishCollectionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PublicMemberController {

    private final UserService userService;
    private final TradeCollectionService tradeCollectionService;
    private final WishCollectionService wishCollectionService;
    private final DeckService deckService;

    public PublicMemberController(UserService userService,
                                  TradeCollectionService tradeCollectionService,
                                  WishCollectionService wishCollectionService,
                                  DeckService deckService) {
        this.userService = userService;
        this.tradeCollectionService = tradeCollectionService;
        this.wishCollectionService = wishCollectionService;
        this.deckService = deckService;
    }

    // 1. Vis en andens offentlige profil (Allerede oprettet tidligere)
    @GetMapping("/profile/{id}")
    public String showPublicProfile(@PathVariable int id, Model model, HttpSession session) {
        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/login";
        }
        try {
            User profileUser = userService.getUserByID(id);
            model.addAttribute("profileUser", profileUser);
            return "MemberController/public-profile";
        } catch (Exception e) {
            return "redirect:/members";
        }
    }

    // 2. Vis en andens Tradelist (Read-Only)
    @GetMapping("/tradelist/{id}")
    public String showPublicTradelist(@PathVariable int id, Model model, HttpSession session) {
        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/login";
        }
        try {
            User profileUser = userService.getUserByID(id);
            model.addAttribute("profileUser", profileUser);
            // Henter den andens trade collection og tilføjer den til modellen
            model.addAttribute("tradeCollection", tradeCollectionService.getTradeCollectionByUserID(id));
            return "MemberController/public-tradelist";
        } catch (Exception e) {
            return "redirect:/members";
        }
    }

    // 3. Vis en andens Wishlist (Read-Only)
    @GetMapping("/wishlist/{id}")
    public String showPublicWishlist(@PathVariable int id, Model model, HttpSession session) {
        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/login";
        }
        try {
            User profileUser = userService.getUserByID(id);
            model.addAttribute("profileUser", profileUser);
            // Henter den andens wishlist og tilføjer den til modellen
            model.addAttribute("wishCollection", wishCollectionService.getWishCollectionByUserID(id));
            return "MemberController/public-wishlist";
        } catch (Exception e) {
            return "redirect:/members";
        }
    }

    // 4. Vis en andens Decks (Read-Only)
    @GetMapping("/decks/{id}")
    public String showPublicDecks(@PathVariable int id, Model model, HttpSession session) {
        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/login";
        }
        try {
            User profileUser = userService.getUserByID(id);
            model.addAttribute("profileUser", profileUser);
            // Henter den andens offentlige decks og tilføjer til modellen
            model.addAttribute("decks", deckService.findAllDecksByUserId(id));
            return "MemberController/public-decks";
        } catch (Exception e) {
            return "redirect:/members";
        }
    }

    @GetMapping("/public-deck/{deckId}")
    public String showPublicDeckDetails(@PathVariable int deckId, Model model, HttpSession session) {
        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/login";
        }

        try {
            Deck deck = deckService.getDeck(deckId);
            model.addAttribute("deck", deck);

            // Tæller antal kort i decket for at vise "60 kort"
            int totalCardsInDeck = deck.getDeckItems().stream()
                    .mapToInt(item -> item.getQuantity())
                    .sum();
            model.addAttribute("totalCardsInDeck", totalCardsInDeck);

            return "MemberController/public-deck-details";
        } catch (Exception e) {
            return "redirect:/members";
        }
    }
}












