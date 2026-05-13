package feedback.deckforge.Exceptions;

public class CardNotOwnedException extends RuntimeException {
    public CardNotOwnedException(String message) {
        super(message);
    }
}
