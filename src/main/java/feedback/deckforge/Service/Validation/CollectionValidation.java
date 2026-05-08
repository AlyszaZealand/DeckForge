package feedback.deckforge.Service.Validation;

import org.springframework.stereotype.Component;

@Component
public class CollectionValidation {

    public ValidationResult validationResult;

    public CollectionValidation(ValidationResult validationResult) {
        this.validationResult = validationResult;
    }

    public ValidationResult validateAddCard(int cardId, int quantity) {
        ValidationResult result = new ValidationResult(); //

        if (cardId <= 0) {
            result.addError("Der skete en fejl. Ugyldigt kort valgt.");
        }

        if (quantity <= 0) {
            result.addError("Du skal tilføje mindst 1 kopi af kortet.");
        }

        return result;
    }
}


