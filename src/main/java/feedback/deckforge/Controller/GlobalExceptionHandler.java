package feedback.deckforge.Controller;

import feedback.deckforge.Exceptions.CollectionNotFoundException;
import feedback.deckforge.Exceptions.TradeNotFoundException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(TradeNotFoundException.class)
    public String handleTradeNotFound(TradeNotFoundException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error-page"; // Navnet på jeres HTML-fejlside
    }

    @ExceptionHandler(CollectionNotFoundException.class)
    public String handleCollectionNotFound(CollectionNotFoundException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error-page";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex, Model model) {
        model.addAttribute("errorMessage", "Der skete en uventet systemfejl. Prøv igen senere.");
        // Man kan også logge fejlen her: ex.printStackTrace();
        return "error-page";
    }
}
