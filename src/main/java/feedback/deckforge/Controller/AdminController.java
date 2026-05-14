package feedback.deckforge.Controller;


import feedback.deckforge.Model.Enum.UserRole;
import feedback.deckforge.Model.Event;
import feedback.deckforge.Model.Format;
import feedback.deckforge.Model.User;
import feedback.deckforge.Service.CardService;
import feedback.deckforge.Service.FormatService;
import feedback.deckforge.Service.Validation.ValidationResult;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AdminController {

    private FormatService formatService;
    private CardService cardService;

    public AdminController(FormatService formatService, CardService cardService) {
        this.formatService = formatService;
        this.cardService = cardService;
    }

    @GetMapping("/createFormat")
    public String showCreateFormat(HttpSession session, Model model){
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null){
            return "redirect:/login";
        }

        model.addAttribute("format", new Format());

        return "AdminController/create-format";
    }

    @PostMapping("/createFormat")
    public String handleFormatForm(@ModelAttribute Format newFormat, Model model, HttpSession session){
        ValidationResult result = formatService.createNewFormat(newFormat);

        if(result.hasErrors()){
            model.addAttribute("errorMessage", result.getErrors());
            return "AdminController/create-format";
        }

        return "redirect:/";
    }

    @GetMapping("/updateFormat")
    public String showUpdateFormat(@RequestParam int formatID, HttpSession session, Model model){
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null){
            return "redirect:/login";
        }
        if(!loggedInUser.getUserRole().equals(UserRole.ADMIN)){
            return "/";
        }

        Format formatToUpdate = formatService.getFormatById(formatID);

        if (formatToUpdate == null){
            return "redirect:/";
        }

        model.addAttribute("format", formatToUpdate);
        return "AdminController/update-format";
    }

    @PostMapping("/updateFormat")
    public String handleUpdateFormat(@ModelAttribute("format") Format format, Model model, HttpSession session){
        if (session.getAttribute("loggedInUser") == null){
            return "redirect:/login";
        }

        ValidationResult result = formatService.updateExitingFormat(format);

        if(result.hasErrors()){
            model.addAttribute("errorMessage", result.getErrors());
            return "AdminController/update-format";
        }

        return "redirect:/";
    }


}
