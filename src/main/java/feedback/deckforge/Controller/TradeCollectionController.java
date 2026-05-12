package feedback.deckforge.Controller;

import feedback.deckforge.Model.TradeCollection;
import feedback.deckforge.Model.User;
import feedback.deckforge.Service.TradeCollectionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class TradeCollectionController {

    private TradeCollectionService tradeCollectionService;

    public TradeCollectionController(TradeCollectionService tradeCollectionService) {
        this.tradeCollectionService = tradeCollectionService;
    }


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
