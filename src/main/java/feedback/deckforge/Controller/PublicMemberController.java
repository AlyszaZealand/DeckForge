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

@Controller
public class PublicMemberController {

    private UserService userService;
    private CollectionService collectionService;
    private TradeCollectionService tradeCollectionService;
    private WishCollectionService wishCollectionService;

    public PublicMemberController(UserService userService, CollectionService collectionService, TradeCollectionService tradeCollectionService
                        , WishCollectionService wishCollectionService) {
        this.userService = userService;
        this.collectionService = collectionService;
        this.tradeCollectionService = tradeCollectionService;
        this.wishCollectionService = wishCollectionService;
    }












}
