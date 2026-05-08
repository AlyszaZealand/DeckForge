package feedback.deckforge.Service.Validation;

import feedback.deckforge.Model.Card;
import org.springframework.stereotype.Component;

@Component
public class CardValidation {

    public ValidationResult validateCard(Card card) {
        ValidationResult result = new ValidationResult();

        // 1. Tjek kortets navn
        if (card.getCardName() == null || card.getCardName().trim().isEmpty()) {
            result.addError("Kortet skal have et navn.");
        }

        String colors = card.getColorIdentity();
        if (colors != null && !colors.trim().isEmpty()) {
            if (!colors.matches("^[WUBRGC]+$")) {
                result.addError("Sikkerhedsfejl: Ugyldig farvekode. Kun W, U, B, R, G og C er tilladt.");
            }
        } else {
            // Hvis admin ikke har valgt noget, bliver kortet farveløst
            card.setColorIdentity("C");
        }

        // 3. Tjek at der er valgt korttype og rarity
        if (card.getCardType() == null) {
            result.addError("Du skal vælge en korttype (f.eks. Creature eller Land).");
        }
        if (card.getCardRarity() == null) {
            result.addError("Du skal vælge en rarity (f.eks. Rare eller Common).");
        }

        return result;
    }
}
