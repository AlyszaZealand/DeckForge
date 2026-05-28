package feedback.deckforge.Controller;


import feedback.deckforge.Exceptions.CardAlreadyInWishListException;
import feedback.deckforge.Model.Card;
import feedback.deckforge.Model.Enum.CollectionType;
import feedback.deckforge.Model.User;
import feedback.deckforge.Model.WishCollection;
import feedback.deckforge.Service.CardService;
import feedback.deckforge.Service.WishCollectionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class WishCollectionController {

    private WishCollectionService wishCollectionService;
    private CardService cardService;

    public WishCollectionController(WishCollectionService wishCollectionService,  CardService cardService) {
        this.wishCollectionService = wishCollectionService;
        this.cardService = cardService;
    }

    @GetMapping("/myWishCollection")
    public String showWishCollection(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String rarity,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String type, // <-- NY PARAMETER TILFØJET HER
            @RequestParam(required = false, defaultValue = "CATALOG") String searchTarget,
            HttpSession session, Model model){

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if(loggedInUser == null) return "redirect:/login";

        Optional<WishCollection> wishCollectionOptional = wishCollectionService.getWishCollectionByUserID(loggedInUser.getUserID());

        List<Card> catalogCards;

        if("COLLECTION".equals(searchTarget)){

            List<Card> filteredWishCards = cardService.searchCards(name, rarity, color, type, CollectionType.WISH, loggedInUser.getUserID());

            // Vi filtrerer 'wishCollection' objektet, så det kun indeholder de matchende kort
            wishCollectionOptional.ifPresent(coll -> {
                coll.getWishCollectionItems().removeIf(item ->
                        filteredWishCards.stream().noneMatch(c -> c.getCardID() == item.getCard().getCardID())
                );
                model.addAttribute("wishCollection", coll);
            });

            catalogCards = cardService.searchCards("", "", "", "", CollectionType.CATALOG, null);

        } else {
            // --- LOGIK FOR SØGNING I KATALOG (Standard) ---

            catalogCards = cardService.searchCards(name, rarity, color, type, CollectionType.CATALOG, null);

            wishCollectionOptional.ifPresent(coll -> model.addAttribute("wishCollection", coll));
        }

        model.addAttribute("cardList", catalogCards);
        return "CollectionController/my-wishlists";
    }

    @PostMapping("/addCardToWishList")
    public String handleRemoveCardFromWishCollection(@RequestParam int cardID, HttpSession session, RedirectAttributes redirectAttributes){
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null){
            return "redirect:/login";
        }

        Optional<WishCollection> wishCollectionOptional = wishCollectionService.getWishCollectionByUserID(loggedInUser.getUserID());
        int wishCollectionID = wishCollectionOptional.get().getWishCollectionId();


        wishCollectionService.addCardToWishlist(loggedInUser.getUserID(), cardID);

        return "redirect:/myWishCollection";
    }

    @PostMapping("/removeCardFromWishList")
    public String handleAddCardToWishCollection(@RequestParam int cardID, HttpSession session){
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null){
            return "redirect:/login";
        }

        Optional<WishCollection> wishCollectionOpt = wishCollectionService.getWishCollectionByUserID(loggedInUser.getUserID());
        int wishCollectionID = wishCollectionOpt.get().getWishCollectionId();
        wishCollectionService.removeCardFromWishlist(loggedInUser.getUserID(), cardID);

        return "redirect:/myWishCollection";
    }
}