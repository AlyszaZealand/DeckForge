package feedback.deckforge.Controller;

import feedback.deckforge.Model.Card;
import feedback.deckforge.Model.Enum.CollectionType;
import feedback.deckforge.Model.Enum.UserRole;
import feedback.deckforge.Model.Format;
import feedback.deckforge.Model.User;
import feedback.deckforge.Service.CardService;
import feedback.deckforge.Service.FormatService;
import feedback.deckforge.Service.Validation.ValidationResult;
import jakarta.servlet.http.HttpSession;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class AdminController {

    private final FormatService formatService;
    private final CardService cardService;

    public AdminController(FormatService formatService, CardService cardService) {
        this.formatService = formatService;
        this.cardService = cardService;
    }

    // ==========================================
    // FORMAT ADMINISTRATION
    // ==========================================

    @GetMapping("/admin/formats")
    public String showAdminFormats(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.getUserRole().equals(UserRole.ADMIN)){
            return "redirect:/login";
        }

        // Henter alle formater fra databasen og sender dem til HTML'en
        model.addAttribute("formats", formatService.getAllFormats());
        return "AdminController/admin-formats";
    }

    @PostMapping("/deleteFormat")
    public String deleteFormat(@RequestParam int formatID, HttpSession session, RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.getUserRole().equals(UserRole.ADMIN)){
            return "redirect:/login";
        }

        try {
            formatService.deleteExistingFormat(formatID);
            redirectAttributes.addFlashAttribute("successMessage", "Formatet blev slettet succesfuldt.");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Formatet kan ikke slettes, fordi der findes decks eller events, som bruger det.");
        }

        return "redirect:/admin/formats";
    }

    @GetMapping("/createFormat")
    public String showCreateFormat(HttpSession session, Model model){
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.getUserRole().equals(UserRole.ADMIN)){
            return "redirect:/login";
        }

        model.addAttribute("format", new Format());
        return "AdminController/create-format";
    }

    @PostMapping("/createFormat")
    public String handleFormatForm(@ModelAttribute Format newFormat, Model model, HttpSession session){
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.getUserRole().equals(UserRole.ADMIN)){
            return "redirect:/login";
        }

        ValidationResult result = formatService.createNewFormat(newFormat);

        if(result.hasErrors()){
            model.addAttribute("errorMessage", result.getErrors());
            return "AdminController/create-format";
        }

        return "redirect:/admin/formats";
    }

    @GetMapping("/updateFormat")
    public String showUpdateFormat(@RequestParam int formatID, HttpSession session, Model model){
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.getUserRole().equals(UserRole.ADMIN)){
            return "redirect:/login";
        }

        Format formatToUpdate = formatService.getFormatById(formatID);

        if (formatToUpdate == null){
            return "redirect:/admin/formats";
        }

        model.addAttribute("format", formatToUpdate);
        return "AdminController/update-format";
    }

    @PostMapping("/updateFormat")
    public String handleUpdateFormat(@ModelAttribute("format") Format format, Model model, HttpSession session){
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.getUserRole().equals(UserRole.ADMIN)){
            return "redirect:/login";
        }

        ValidationResult result = formatService.updateExitingFormat(format);

        if(result.hasErrors()){
            model.addAttribute("errorMessage", result.getErrors());
            return "AdminController/update-format";
        }

        return "redirect:/admin/formats";
    }


    // ==========================================
    // KORT ADMINISTRATION
    // ==========================================

    @GetMapping("/admin/cards")
    public String showAdminCards(@RequestParam(required = false) String search,
                                 @RequestParam(required = false) String rarity,
                                 @RequestParam(required = false) String color,
                                 @RequestParam(required = false) String cardType,
                                 HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.getUserRole().equals(UserRole.ADMIN)){
            return "redirect:/login";
        }

        // Bruger jeres eksisterende søgefunktion, men på det globale katalog
        List<Card> searchResults = cardService.searchCards(search, rarity, color, cardType, CollectionType.CATALOG, null);

        model.addAttribute("searchResults", searchResults);
        model.addAttribute("currentSearch", search);
        model.addAttribute("currentRarity", rarity);
        model.addAttribute("currentColor", color);
        model.addAttribute("currentCardType", cardType);

        return "AdminController/admin-cards";
    }

    @GetMapping("/createCard")
    public String showCardCreation(HttpSession session, Model model){
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.getUserRole().equals(UserRole.ADMIN)){
            return "redirect:/login";
        }

        model.addAttribute("card", new Card());
        return "AdminController/create-card";
    }

    @PostMapping("/createCard")
    public String handleCardCreation(@ModelAttribute("card") Card card, Model model, HttpSession session){
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.getUserRole().equals(UserRole.ADMIN)){
            return "redirect:/login";
        }

        cardService.saveCard(card);

        return "redirect:/admin/cards";
    }

    @PostMapping("/admin/deleteCard")
    public String deleteCardFromDatabase(@RequestParam int cardID, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.getUserRole().equals(UserRole.ADMIN)){
            return "redirect:/login";
        }

        cardService.deleteCard(cardID);
        return "redirect:/admin/cards";
    }
}
