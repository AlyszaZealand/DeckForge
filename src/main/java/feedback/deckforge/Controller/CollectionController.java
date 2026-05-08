package feedback.deckforge.Controller;

import feedback.deckforge.Model.User;
import feedback.deckforge.Service.CollectionService;
import feedback.deckforge.Service.TradeCollectionService;
import feedback.deckforge.Service.WishCollectionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CollectionController {

    private CollectionService collectionService;
    private TradeCollectionService tradeCollectionService;
    private WishCollectionService wishCollectionService;

    public CollectionController(CollectionService collectionService, TradeCollectionService tradeCollectionService, WishCollectionService wishCollectionService) {
        this.collectionService = collectionService;
        this.tradeCollectionService = tradeCollectionService;
        this.wishCollectionService = wishCollectionService;
    }


    @GetMapping("/collecitonPage")
    public String showCollection(HttpSession session, Model model){
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        if(loggedInUser == null){
            return "redirect:/login";
        }

        collectionService.findCollectionByUserId(loggedInUser.getUserID());

        return"CollectionController/collection-hub";
    }

    @GetMapping("/wishCollection")
    public String showWishCollectionPage(HttpSession session, Model model){
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        if(loggedInUser == null){
            return "redirect:/login";
        }

        wishCollectionService.getWishCollectionByUserID(loggedInUser.getUserID());
        return "CollectionController/my-wishlists";
    }


}
