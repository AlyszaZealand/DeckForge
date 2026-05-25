package feedback.deckforge.Controller;

import feedback.deckforge.Exceptions.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CardAlreadyInWishListException.class)
    public String handleCardAlreadyInWishList(CardAlreadyInWishListException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/myWishCollection";
    }


    @ExceptionHandler(InsufficientCardsException.class)
    public String handleInsufficientCards(InsufficientCardsException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/myTradeList";
    }

    @ExceptionHandler(UnauthorizedException.class)
    public String handleUnauthorizedUser(UnauthorizedException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/respond";
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

    @ExceptionHandler(UserNameNotValid.class)
    public String handleUsernameNotValid(UserNameNotValid ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/registerUser"; // Send tilbage til oprettelse
    }

    @ExceptionHandler(UserEmailNotValid.class)
    public String handleUserEmailNotValid(UserEmailNotValid ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/registerUser"; // Send tilbage til oprettelse
    }

    @ExceptionHandler(UserPasswordNotValid.class)
    public String handleUserPaswordNotValid(UserPasswordNotValid ex, RedirectAttributes redirectAttributes) {
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

    @ExceptionHandler(CardNotFoundException.class)
    public String handleCardNotFound(CardNotFoundException ex, RedirectAttributes redirectAttributes){
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "";//ikke sikker på hvor den skal bruges
    }

    @ExceptionHandler(CollectionNotFoundException.class)
    public String handleCollectionNotFound(CollectionNotFoundException ex, RedirectAttributes redirectAttributes){
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/myCards";
    }

    @ExceptionHandler(DeckNotFoundException.class)
    public String handleDeckNotFound(DeckNotFoundException ex, RedirectAttributes redirectAttributes){
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/myDecks";
    }

    @ExceptionHandler(DeckNameNotValid.class)
    public String handleDeckNameNotValid(DeckNameNotValid ex, RedirectAttributes redirectAttributes){
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/createDeck";
    }

    @ExceptionHandler(EventFullException.class)
    public String handleEventFull(EventFullException ex, RedirectAttributes redirectAttributes){
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/events/{id}";
    }

    @ExceptionHandler(EventNameNotValid.class)
    public String handleEventNameNotValid(EventNameNotValid ex, RedirectAttributes redirectAttributes){
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/registerEvent";
    }

    @ExceptionHandler(EventDateNotValid.class)
    public String handleEventDateNotValid(EventDateNotValid ex, RedirectAttributes redirectAttributes){
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/registerEvent";
    }

    @ExceptionHandler(EventDescriptionNotValid.class)
    public String handleEventDescriptionNotValid(EventDescriptionNotValid ex, RedirectAttributes redirectAttributes){
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/registerEvent";
    }

    @ExceptionHandler(InvalidEventSizeException.class)
    public String handleInvalidEventSize(InvalidEventSizeException ex, RedirectAttributes redirectAttributes){
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/registerEvent";
    }

    @ExceptionHandler(FormatNotFoundException.class)
    public String handleFormatNotEvent(FormatNotFoundException ex, RedirectAttributes redirectAttributes){
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/myDecks";
    }

    @ExceptionHandler(IllegalDeckCompositionException.class)
    public String handleIllegalDeckComposition(IllegalDeckCompositionException ex, RedirectAttributes redirectAttributes){
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/deckBuilder/{deckID}";
    }

    @ExceptionHandler(TradeNotFoundException.class)
    public String handleTradeNotFound(TradeNotFoundException ex, RedirectAttributes redirectAttributes){
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return ""; //INDSÆT
    }


    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException(RuntimeException ex, RedirectAttributes redirectAttributes) {
        // IMPORTANT: You should always log generic runtime exceptions
        // so you can investigate what caused them later!
        System.err.println("Unexpected Runtime Error: " + ex.getMessage());
        // (In a real app, use a logger like SLF4J: log.error("Error", ex))

        // Give the user a friendly, non-technical message
        redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred. Please try again later.");

        // Send them back to the home page (or a dedicated generic error page)
        return "Error/404";
    }


}
