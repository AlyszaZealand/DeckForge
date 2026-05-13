package feedback.deckforge.Exceptions;

public class InsufficientCardsException extends RuntimeException {
    public InsufficientCardsException(String message) {
        super(message);
    }
}
