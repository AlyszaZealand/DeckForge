package feedback.deckforge.Service.Validation;

import feedback.deckforge.Model.Collection;
import org.springframework.stereotype.Component;

@Component
public class TradeCollectionValidation {

    public ValidationResult validateAddCardToTradeCollection(int cardID, int quantity, Collection userPrivateCollection) {
        ValidationResult result = new ValidationResult(); //

        if (cardID <= 0) {
            result.addError("Ugyldigt kort.");
        }
        if (quantity <= 0) {
            result.addError("Antal skal være mindst 1.");
        }

        // Tjek om brugeren faktisk ejer kortet (og har nok af dem)
        if (userPrivateCollection != null) {
            boolean ownsEnough = userPrivateCollection.getCollectionItems().stream()
                    .anyMatch(item -> item.getCard().getCardId() == cardID && item.getQuantity() >= quantity);

            if (!ownsEnough) {
                result.addError("Du ejer ikke nok kopier af dette kort til at sætte det til bytte."); //
            }
        } else {
            result.addError("Kunne ikke finde din private samling."); //
        }

        return result;
    }
}

