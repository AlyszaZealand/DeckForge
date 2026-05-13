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
public String showWishCollection(  @RequestParam(required = false) String name,
                                   @RequestParam(required = false) String rarity,
                                   @RequestParam(required = false) String color,
                                   @RequestParam(required = false, defaultValue = "CATALOG") String searchTarget,
                                   HttpSession session, Model model){
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if(loggedInUser == null) return "redirect:/login";

        Optional<WishCollection> wishCollectionOptional = wishCollectionService.getWishCollectionByUserID(loggedInUser.getUserID());

        List<Card> catalogCards;

        if("COLLECTION".equals(searchTarget)){
            // --- LOGIK FOR SØGNING I EGEN SAMLING ---

            // Vi henter listen over kort i samlingen der matcher filteret
            List<Card> filteredOwnedCards = cardService.searchCards(name, rarity, color, CollectionType.COLLECTION, loggedInUser.getUserID());

            // Vi filtrerer 'collection' objektet, så det kun indeholder de matchende kort
            wishCollectionOptional.ifPresent(coll -> {
                coll.getWishCollectionItems().removeIf(item ->
                        filteredOwnedCards.stream().noneMatch(c -> c.getCardID() == item.getCard().getCardID())
                );
                model.addAttribute("wishCollection", coll);
            });

            // Katalog-baren (højre side) skal bare vise alle kort (eller være tom/uændret)
            catalogCards = cardService.searchCards("", "", "", CollectionType.CATALOG, null);

        } else {
            // --- LOGIK FOR SØGNING I KATALOG (Standard) ---

            // Filtrér kataloget (højre side)
            catalogCards = cardService.searchCards(name, rarity, color, CollectionType.CATALOG, null);

            // Vis den fulde samling (venstre side) uden filtrering
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


        wishCollectionService.addCardToWishCollection(wishCollectionID, cardID);

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
        wishCollectionService.removeCardFromWishCollection(wishCollectionID,cardID);

        return "redirect:/myWishCollection";
    }


}
