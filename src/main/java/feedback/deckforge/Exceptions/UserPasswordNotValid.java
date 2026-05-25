package feedback.deckforge.Exceptions;

public class UserPasswordNotValid extends RuntimeException {
    public UserPasswordNotValid(String message) {
        super(message);
    }
}
