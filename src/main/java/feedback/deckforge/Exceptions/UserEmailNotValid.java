package feedback.deckforge.Exceptions;

public class UserEmailNotValid extends RuntimeException {
    public UserEmailNotValid(String message) {
        super(message);
    }
}
