package feedback.deckforge.Exceptions;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InsufficientCardsException.class)
    public String handleInsufficientCards(InsufficientCardsException ex, RedirectAttributes redirectAttributes) {
        // Vi bruger FlashAttributes her, så beskeden overlever jeres redirect
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());

        // Sender brugeren tilbage til tradelisten (eller hvor de kom fra)
        return "redirect:/myTradeList";
    }

    @ExceptionHandler(CardAlreadyInWishListException.class)
    public String handleCardAlreadyInWishList(CardAlreadyInWishListException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/myWishCollection";
    }


    @ExceptionHandler(InvalidCredentialsException.class)
    public String handleInvalidCredentials(InvalidCredentialsException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/login"; // Send tilbage til login ved forkert kode/email
    }

    @ExceptionHandler(EmailAlreadyInUseException.class)
    public String handleEmailAlreadyInUse(EmailAlreadyInUseException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/registerUser"; // Send tilbage til oprettelse
    }

    @ExceptionHandler(UserNotFoundException.class)
    public String handleUserNotFound(UserNotFoundException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/"; // Eller måske en specifik 404 fejlside
    }

    @ExceptionHandler(CardNotOwnedException.class)
    public String handleCardNotOwned(CardNotOwnedException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/myCards"; // Antager at I har et endpoint til brugerens kort
    }
}
