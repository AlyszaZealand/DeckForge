package feedback.deckforge.Service.Validation;

import feedback.deckforge.Model.Collection;
import org.springframework.stereotype.Component;

@Component
public class TradeCollectionValidation {

    // BEMÆRK: Parameteren hedder nu newTotalQuantity
    public ValidationResult validateAddCardToTradeCollection(int cardID, int newTotalQuantity, Collection userPrivateCollection) {
        ValidationResult result = new ValidationResult();

        if (cardID <= 0) result.addError("Ugyldigt kort.");
        if (newTotalQuantity <= 0) result.addError("Antal skal være mindst 1.");

        if (userPrivateCollection != null) {
            boolean ownsEnough = userPrivateCollection.getCollectionItems().stream()
                    .anyMatch(item -> item.getCard().getCardID() == cardID && item.getQuantity() >= newTotalQuantity);

            if (!ownsEnough) {
                result.addError("Du ejer ikke nok kopier af dette kort til at sætte flere til bytte.");
            }
        } else {
            result.addError("Kunne ikke finde din private samling.");
        }

        return result;
    }
}

