package feedback.deckforge.Controller;

import feedback.deckforge.Model.User;
import feedback.deckforge.Service.UserService;
import feedback.deckforge.Service.Validation.UserValidation;
import feedback.deckforge.Service.Validation.ValidationResult;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class AuthController {

    private UserService userService;

    public AuthController(UserService userService, UserValidation userValidation) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String showLoginPage(){
        return "AuthController/login";
    }

    @PostMapping("/login")
    public String handleLogin(@RequestParam String email, @RequestParam String password, HttpSession httpSession, Model model) {
        Optional<User> userOptional = userService.loginValidation(email,password);

        if (userOptional.isPresent()) {
            httpSession.setAttribute("loggedInUser", userOptional.get());
            return "redirect:/";
        }

        else {
            model.addAttribute("errorMessage", "Hov! E-mailen eller kodeordet er forkert.");
            return "AuthController/login";
        }
    }

    @GetMapping("/registerUser")
    public String showRegisterFormel(Model model){
        model.addAttribute("user",new User());
        return "AuthController/register";
    }

    @PostMapping("/registerUser")
    public String handleRegistration(@ModelAttribute User newUser, Model model){

        ValidationResult result = userService.registerNewUser(newUser);

        if (result.hasErrors()) {
            model.addAttribute("errorMessage", result.getErrors());
            return "AuthController/register";
        }
        return "redirect:/login";
    }

    @PostMapping("/logout")
    public String handleLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

}
