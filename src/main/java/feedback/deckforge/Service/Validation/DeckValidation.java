package feedback.deckforge.Service.Validation;

import feedback.deckforge.Model.Format;
import feedback.deckforge.Model.Deck;
import org.springframework.stereotype.Component;

@Component
public class DeckValidation {

    private ValidationResult validationResult;

    public DeckValidation(ValidationResult validationResult){
        this.validationResult = validationResult;
    }

    public ValidationResult validateDeck(Deck deck, Format format){
        ValidationResult result = new ValidationResult();

        if (deck.getDeckName() == null || deck.getDeckName().trim().isEmpty()){
            result.addError("Deck'et skal have et navn.");
        }

        if (format == null){
            result.addError("Du skal vælge et format til deck'et");
        }

        // 2. Calculate total cards
        int totalCards = 0;
        if (deckItems != null) {
            for (DeckItem item : deckItems) {
                totalCards += item.getQuantity();

                // 3. Max Copies Validation
                boolean isBasicLand = isBasicLand(item.getCard().getCardName());
                if (!isBasicLand && item.getQuantity() > format.getMaxCopiesOfCard()) {
                    result.addError("Du må højst have " + format.getMaxCopiesOfCard() +
                            " kopier af kortet: " + item.getCard().getCardName());
                }
            }
        }

        return result;
    }




}
