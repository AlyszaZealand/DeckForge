package feedback.deckforge.Exceptions;

public class CardAlreadyInWishListException extends RuntimeException {
    public CardAlreadyInWishListException(String message) {
        super(message);
    }
}
