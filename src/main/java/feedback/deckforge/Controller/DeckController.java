package feedback.deckforge.Controller;

import feedback.deckforge.Model.Deck;
import feedback.deckforge.Model.User;
import feedback.deckforge.Service.DeckService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DeckController {

    private DeckService deckService;

    public DeckController(DeckService deckService){
        this.deckService = deckService;
    }


    @GetMapping("/myDecks")
    public String showMyDecks(HttpSession session, Model model){
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null){
            return "redirect:/login";
        }

        List<Deck> deckCollections = deckService.findAllDecksByUserId(loggedInUser.getUserID());
        model.addAttribute("deckCollection", deckCollections);
        return "DeckController/my-decks";

    }
}
