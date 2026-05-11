package feedback.deckforge.Controller;

import feedback.deckforge.Model.*;
import feedback.deckforge.Service.CollectionService;
import feedback.deckforge.Service.DeckService;
import feedback.deckforge.Service.TradeCollectionService;
import feedback.deckforge.Service.Validation.ValidationResult;
import feedback.deckforge.Service.WishCollectionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class CollectionController {

    private CollectionService collectionService;
    private TradeCollectionService tradeCollectionService;
    private WishCollectionService wishCollectionService;
    private DeckService deckService;

    public CollectionController(CollectionService collectionService, TradeCollectionService tradeCollectionService, WishCollectionService wishCollectionService, DeckService deckService) {
        this.collectionService = collectionService;
        this.tradeCollectionService = tradeCollectionService;
        this.wishCollectionService = wishCollectionService;
        this.deckService = deckService;
    }


    @GetMapping("/collectionPage")
    public String showCollection(HttpSession session, Model model){
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        if(loggedInUser == null){
            return "redirect:/login";
        }

        collectionService.findCollectionByUserId(loggedInUser.getUserID());

        return"CollectionController/collection-hub";
    }

    @GetMapping("/myCards")
    public String showMyCards(HttpSession session, Model model){
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if(loggedInUser == null){
            return "redirect:/login";
        }

        Optional<Collection> collectionOptional = collectionService.findCollectionByUserId(loggedInUser.getUserID());
        collectionOptional.ifPresent(collection -> model.addAttribute("collection", collection));
        return "CollectionController/my-cards";
    }

    @PostMapping("/addCardToCollection")
    public String handleAddCardToCollection(@RequestParam int cardID, HttpSession session){
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if(loggedInUser == null){
            return "redirect:/login";
        }

        Optional<Collection> collectionOpt = collectionService.findCollectionByUserId(loggedInUser.getUserID());
        int collectionID = collectionOpt.get().getCollectionId();
        collectionService.addCardToCollection(collectionID,cardID,1);

        return "redirect:/myCards";
    }

    @PostMapping("/removeCardFromCollection")
    public String handleRemoveCardFromCollection(@RequestParam int cardID, HttpSession session){
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if(loggedInUser == null){
            return "redirect:/login";
        }

        Optional<Collection> collectionOpt = collectionService.findCollectionByUserId(loggedInUser.getUserID());
        int collectionID = collectionOpt.get().getCollectionId();
        collectionService.removeCardFromCollection(collectionID,cardID);

        return "redirect:/myCards";
    }

//----------------------------------------------------


    @GetMapping("/myWishCollection")
    public String showWishCollectionPage(HttpSession session, Model model){
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null){
            return "redirect:/login";
        }

        Optional<WishCollection> wishCollectionOptional = wishCollectionService.getWishCollectionByUserID(loggedInUser.getUserID());
        wishCollectionOptional.ifPresent(wishCollection -> model.addAttribute("collection", wishCollection));

        return "CollectionController/my-wishlists";
    }

    @PostMapping("/addCardToWishList")
    public String handleRemoveCardFromWishCollection(@RequestParam int cardID, HttpSession session){
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null){
            return "redirect:/login";
        }

        Optional<WishCollection> wishCollectionOptional = wishCollectionService.getWishCollectionByUserID(loggedInUser.getUserID());
        int wishCollectionID = wishCollectionOptional.get().getWishCollectionId();
        wishCollectionService.addCardToWishCollection(wishCollectionID,cardID);

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


//-----------------------------------------------------------------------------------------


    @GetMapping("/myTradeList")
    public String showTradeCollectionPage(HttpSession session, Model model){
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null){
            return "redirect:/login";
        }

        Optional<TradeCollection> tradeCollectionOptional = tradeCollectionService.getTradeCollectionByUserID(loggedInUser.getUserID());
        tradeCollectionOptional.ifPresent(tradeCollection -> model.addAttribute("tradeCollection", tradeCollection));

        return "CollectionController/my-tradelist";
    }

    @PostMapping("/addCardToTradeList")
    public String handleAddCardToTradeCollection(@RequestParam int cardID, HttpSession session, RedirectAttributes redirectAttributes){
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null){
            return "redirect:/login";
        }

        Optional<TradeCollection> tradeCollectionOptional = tradeCollectionService.getTradeCollectionByUserID(loggedInUser.getUserID());

        int tradeCollectionID = tradeCollectionOptional.get().getTradeCollectionId();

        tradeCollectionService.addCardToTradeCollection(loggedInUser.getUserID(), tradeCollectionID, cardID, 1);

       //Error handling

        return "redirect:/myTradeList";
    }

    @PostMapping("/removeCardFromTradeList")
    public String handleRemoveCardFromTradeCollection(@RequestParam int cardID, HttpSession session){
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null){
            return "redirect:/login";
        }

        Optional<TradeCollection> tradeCollectionOptional = tradeCollectionService.getTradeCollectionByUserID(loggedInUser.getUserID());
        int tradeCollectionID = tradeCollectionOptional.get().getTradeCollectionId();
        tradeCollectionService.removeCardFromTradeCollection(tradeCollectionID, cardID);

        return "redirect:/myTradeList";


    }






}
