package feedback.deckforge.Controller;


import feedback.deckforge.Model.User;
import feedback.deckforge.Model.WishCollection;
import feedback.deckforge.Service.WishCollectionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class WishCollectionController {

    private WishCollectionService wishCollectionService;

    public WishCollectionController(WishCollectionService wishCollectionService) {
        this.wishCollectionService = wishCollectionService;
    }


    @GetMapping("/myWishList")
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


}
