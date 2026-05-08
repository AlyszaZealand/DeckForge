package feedback.deckforge.Service.Validation;

import feedback.deckforge.Model.WishCollection;
import org.springframework.stereotype.Component;

@Component
public class WishCollectionValidation {

    public ValidationResult validateAddCardToWishlist(int cardID) {
        ValidationResult result = new ValidationResult(); //

        if (cardID <= 0) {
            result.addError("Ugyldigt kort."); //
        }

        return result;
    }
}