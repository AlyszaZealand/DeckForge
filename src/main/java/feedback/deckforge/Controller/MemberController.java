package feedback.deckforge.Controller;

import feedback.deckforge.Model.User;
import feedback.deckforge.Service.CollectionService;
import feedback.deckforge.Service.TradeCollectionService;
import feedback.deckforge.Service.UserService;
import feedback.deckforge.Service.WishCollectionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class MemberController {

    private UserService userService;
    private CollectionService collectionService;
    private TradeCollectionService tradeCollectionService;
    private WishCollectionService wishCollectionService;

    public MemberController(UserService userService, CollectionService collectionService, TradeCollectionService tradeCollectionService
                        , WishCollectionService wishCollectionService) {
        this.userService = userService;
        this.collectionService = collectionService;
        this.tradeCollectionService = tradeCollectionService;
        this.wishCollectionService = wishCollectionService;
    }

    @GetMapping("/profile")
    public String showProfilePage(HttpSession session, Model model){

        User loggedInUser = (User) session.getAttribute("loggedInUser");

        if(loggedInUser == null){
            return "redirect:/login";
        }

        model.addAttribute("loggedInUser",loggedInUser);
        return "UserController/profile";
    }


    /*@PostMapping("/profile")
    public String handleGoToCollectionPage(){
        return "CollectionController/collection-hub";
    }*/








}
